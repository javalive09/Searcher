/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.net

import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.support.v7.app.AlertDialog
import android.support.v7.appcompat.BuildConfig
import android.text.TextUtils
import android.util.Log
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast

import java.io.File
import java.io.IOException

import peter.util.searcher.activity.MainActivity
import peter.util.searcher.R


/**
 * Handle download requests
 */
object DownloadHandler {

    val FILE = "file://"
    private val TAG = DownloadHandler::class.java.simpleName
    private val COOKIE_REQUEST_HEADER = "Cookie"

    /**
     * Notify the host application a download should be done, or that the data
     * should be streamed if a streaming viewer is available.
     *
     * @param context            The context in which the download was requested.
     * @param url                The full url to the content that should be downloaded
     * @param userAgent          User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype           The mimetype of the content reported by the server
     */
    fun onDownloadStart(context: Context, url: String, userAgent: String,
                        contentDisposition: String?, mimetype: String) {
        // if we're dealing wih A/V content that's not explicitly marked
        // for download, check if it's streamable.
        if (contentDisposition == null || !contentDisposition.regionMatches(0, "attachment", 0, 10, ignoreCase = true)) {
            // query the package manager to see if there's a registered handler
            // that matches.
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(url), mimetype)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.component = null
            intent.selector = null
            val info = context.packageManager.resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY)
            if (info != null) {
                // If we resolved to ourselves, we don't want to attempt to
                // load the url only to try and download it again.
                if (BuildConfig.APPLICATION_ID == info.activityInfo.packageName || MainActivity::class.java.name == info.activityInfo.name) {
                    // someone (other than us) knows how to handle this mime
                    // type with this scheme, don't download.
                    try {
                        context.startActivity(intent)
                        return
                    } catch (ex: ActivityNotFoundException) {
                        // Best behavior is to fall back to a download in this
                        // case
                    }

                }
            }
        }
        onDownloadStartNoStream(context, url, userAgent, contentDisposition, mimetype)
    }

    // This is to work around the fact that java.net.URI throws Exceptions
    // instead of just encoding URL's properly
    // Helper method for onDownloadStartNoStream
    private fun encodePath(path: String): String {
        val chars = path.toCharArray()

        val needed = chars.any { it == '[' || it == ']' || it == '|' }
        if (!needed) {
            return path
        }

        val sb = StringBuilder("")
        for (c in chars) {
            if (c == '[' || c == ']' || c == '|') {
                sb.append('%')
                sb.append(Integer.toHexString(c.toInt()))
            } else {
                sb.append(c)
            }
        }

        return sb.toString()
    }

    /**
     * Notify the host application a download should be done, even if there is a
     * streaming viewer available for thise type.
     *
     * @param context            The context in which the download is requested.
     * @param url                The full url to the content that should be downloaded
     * @param userAgent          User agent of the downloading application.
     * @param contentDisposition Content-disposition http header, if present.
     * @param mimetype           The mimetype of the content reported by the server
     */
    /* package */
    private fun onDownloadStartNoStream(context: Context,
                                        url: String, userAgent: String,
                                        contentDisposition: String?, mimetype: String?) {
        //        final Bus eventBus = BrowserApp.getBus(context);
        val filename = URLUtil.guessFileName(url, contentDisposition, mimetype)

        // Check to see if we have an SDCard
        val status = Environment.getExternalStorageState()
        if (status != Environment.MEDIA_MOUNTED) {
            val title: Int
            val msg: String

            // Check to see if the SDCard is busy, same as the music app
            if (status == Environment.MEDIA_SHARED) {
                msg = context.getString(R.string.download_sdcard_busy_dlg_msg)
                title = R.string.download_sdcard_busy_dlg_title
            } else {
                msg = context.getString(R.string.download_no_sdcard_dlg_msg)
                title = R.string.download_no_sdcard_dlg_title
            }

            AlertDialog.Builder(context).setTitle(title)
                    .setIcon(android.R.drawable.ic_dialog_alert).setMessage(msg)
                    .setPositiveButton(R.string.action_ok, null).show()
            return
        }

        // java.net.URI is a lot stricter than KURL so we have to encode some
        // extra characters. Fix for b 2538060 and b 1634719
        val webAddress: WebAddress
        try {
            webAddress = WebAddress(url)
            webAddress.path = encodePath(webAddress.path)
        } catch (e: Exception) {
            // This only happens for very bad urls, we want to catch the
            // exception here
            Log.e(TAG, "Exception while trying to parse url '$url'", e)
            Toast.makeText(context, context.getString(R.string.problem_download), Toast.LENGTH_SHORT).show()
            return
        }

        val addressString = webAddress.toString()
        val uri = Uri.parse(addressString)
        val request: DownloadManager.Request
        try {
            request = DownloadManager.Request(uri)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(context, context.getString(R.string.cannot_download), Toast.LENGTH_SHORT).show()
            return
        }

        request.setMimeType(mimetype)
        // set downloaded file destination to /sdcard/Download.
        // or, should it be set to one of several Environment.DIRECTORY* dirs
        // depending on mimetype?

        var location = ""
        val file = context.getExternalFilesDir(null)
        if (file != null) {
            location = file.toString()
        }

        val downloadFolder: Uri
        location = addNecessarySlashes(location)
        downloadFolder = Uri.parse(location)

        val dir = File(downloadFolder.path)
        if (!dir.isDirectory && !dir.mkdirs()) {
            // Cannot make the directory
            Toast.makeText(context, context.getString(R.string.problem_location_download), Toast.LENGTH_LONG).show()
            return
        }

        if (!isWriteAccessAvailable(downloadFolder)) {
            Toast.makeText(context, context.getString(R.string.problem_location_download), Toast.LENGTH_LONG).show()

            return
        }
        //
        val customUri = Uri.parse(FILE + location + filename)
        request.setDestinationUri(customUri)//如没有自定义uri，则通过getUriForDownloadedFile可获取默认uri
        // let this downloaded file be scanned by MediaScanner - so that it can
        // show up in Gallery app, for example.
        request.setVisibleInDownloadsUi(true)
        request.allowScanningByMediaScanner()
        request.setDescription(webAddress.host)
        // XXX: Have to use the old url since the cookies were stored using the
        // old percent-encoded url.
        val cookies = CookieManager.getInstance().getCookie(url)
        request.addRequestHeader(COOKIE_REQUEST_HEADER, cookies)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        if (mimetype == null) {
            Log.d(TAG, "Mimetype is null")
            if (TextUtils.isEmpty(addressString)) {
                return
            }
            // We must have long pressed on a link or image to download it. We
            // are not sure of the mimetype in this case, so do a head request
            FetchUrlMimeType(context, request, addressString, cookies, userAgent).start()
        } else {
            Log.d(TAG, "Valid mimetype, attempting to download")
            val manager = context
                    .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            try {
                val id = manager.enqueue(request)
                context.registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(ctxt: Context, intent: Intent) {
                        val currentId = intent.extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)
                        if (currentId == id) {
                            openFile(customUri, ctxt)
                            context.unregisterReceiver(this)
                        }
                    }
                }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

            } catch (e: IllegalArgumentException) {
                // Probably got a bad URL or something
                Log.e(TAG, "Unable to enqueue request", e)
                Toast.makeText(context, context.getString(R.string.cannot_download), Toast.LENGTH_LONG).show()
            } catch (e: SecurityException) {
                // because the system can only handle Environment.getExternal... as a path
                Toast.makeText(context, context.getString(R.string.problem_location_download), Toast.LENGTH_LONG).show()

            }

            Toast.makeText(context, context.getString(R.string.download_pending) + ' ' + filename, Toast.LENGTH_LONG).show()
        }

    }

    fun openFile(uri: Uri, context: Context) {
        openFile(uri, context, getMimeType(uri.path))
    }

    private fun openFile(uri: Uri, context: Context, mineType: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mineType)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun getMimeType(url: String): String {
        var extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (TextUtils.isEmpty(extension)) {
            extension = url.substring(url.lastIndexOf(".") + 1, url.length)
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    private fun isWriteAccessAvailable(fileUri: Uri): Boolean {
        val file = File(fileUri.path)
        return try {
            if (file.createNewFile()) {
                file.delete()
            }
            true
        } catch (ignored: IOException) {
            false
        }

    }

    fun addNecessarySlashes(mOriginalPath: String?): String {
        var originalPath = mOriginalPath
        if (originalPath == null || originalPath.isEmpty()) {
            return "/"
        }
        if (originalPath[originalPath.length - 1] != '/') {
            originalPath += '/'
        }
        if (originalPath[0] != '/') {
            originalPath = '/' + originalPath
        }
        return originalPath
    }

}
