/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.net

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.widget.Toast

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

import peter.util.searcher.R

/**
 * This class is used to pull down the http headers of a given URL so that we
 * can analyse the mimetype and make any correction needed before we give the
 * URL to the download manager. This operation is needed when the user
 * long-clicks on a link or image and we don't know the mimetype. If the user
 * just clicks on the link, we will do the same steps of correcting the mimetype
 * down in android.os.webkit.LoadListener rather than handling it here.
 */
internal class FetchUrlMimeType(private val mContext: Context, private val mRequest: DownloadManager.Request, private val mUri: String,
                                private val mCookies: String?, private val mUserAgent: String) : Thread() {

    override fun run() {
        // User agent is likely to be null, though the AndroidHttpClient
        // seems ok with that.
        var mimeType: String? = null
        var contentDisposition: String? = null
        var connection: HttpURLConnection? = null
        try {
            val url = URL(mUri)
            connection = url.openConnection() as HttpURLConnection
            if (mCookies != null && !mCookies.isEmpty()) {
                connection.addRequestProperty("Cookie", mCookies)
                connection.setRequestProperty("User-Agent", mUserAgent)
            }
            connection.connect()
            // We could get a redirect here, but if we do lets let
            // the download manager take care of it, and thus trust that
            // the server sends the right mimetype
            if (connection.responseCode == 200) {
                val header = connection.getHeaderField("Content-Type")
                if (header != null) {
                    mimeType = header
                    val semicolonIndex = mimeType.indexOf(';')
                    if (semicolonIndex != -1) {
                        mimeType = mimeType.substring(0, semicolonIndex)
                    }
                }
                val contentDispositionHeader = connection.getHeaderField("Content-Disposition")
                if (contentDispositionHeader != null) {
                    contentDisposition = contentDispositionHeader
                }
            }
        } catch (ex: IllegalArgumentException) {
            if (connection != null)
                connection.disconnect()
        } catch (ex: IOException) {
            if (connection != null)
                connection.disconnect()
        } finally {
            if (connection != null)
                connection.disconnect()
        }

        var filename = ""
        if (mimeType != null) {
            if (mimeType.equals("text/plain", ignoreCase = true) || mimeType.equals("application/octet-stream", ignoreCase = true)) {
                val newMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        MimeTypeMap.getFileExtensionFromUrl(mUri))
                if (newMimeType != null) {
                    mRequest.setMimeType(newMimeType)
                }
            }
            filename = URLUtil.guessFileName(mUri, contentDisposition, mimeType)
            mRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
        }

        // Start the download
        val manager = mContext
                .getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(mRequest)
        val handler = Handler(Looper.getMainLooper())
        val file = filename
        handler.post { Toast.makeText(mContext, mContext.getString(R.string.download_pending) + ' ' + file, Toast.LENGTH_LONG).show() }
    }
}
