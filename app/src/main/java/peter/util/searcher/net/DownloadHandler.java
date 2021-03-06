/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.net;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.IOException;

import peter.util.searcher.BuildConfig;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.R;


/**
 * Handle download requests
 */
public class DownloadHandler {

    public static final String FILE = "file://";
    private static final String TAG = DownloadHandler.class.getSimpleName();
    private static final String COOKIE_REQUEST_HEADER = "Cookie";

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
     static void onDownloadStart(@NonNull Context context, String url, String userAgent,
                                 @Nullable String contentDisposition, String mimetype) {
        // if we're dealing wih A/V content that's not explicitly marked
        // for download, check if it's streamable.
        if (contentDisposition == null
                || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10)) {
            // query the package manager to see if there's a registered handler
            // that matches.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(url), mimetype);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            intent.setSelector(null);
            ResolveInfo info = context.getPackageManager().resolveActivity(intent,
                    PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null) {
                // If we resolved to ourselves, we don't want to attempt to
                // load the url only to try and download it again.
                if (BuildConfig.APPLICATION_ID.equals(info.activityInfo.packageName)
                        || MainActivity.class.getName().equals(info.activityInfo.name)) {
                    // someone (other than us) knows how to handle this mime
                    // type with this scheme, don't download.
                    try {
                        context.startActivity(intent);
                        return;
                    } catch (ActivityNotFoundException ex) {
                        // Best behavior is to fall back to a download in this
                        // case
                    }
                }
            }
        }
        onDownloadStartNoStream(context, url, userAgent, contentDisposition, mimetype);
    }

    // This is to work around the fact that java.net.URI throws Exceptions
    // instead of just encoding URL's properly
    // Helper method for onDownloadStartNoStream
    @NonNull
    private static String encodePath(@NonNull String path) {
        char[] chars = path.toCharArray();

        boolean needed = false;
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                needed = true;
                break;
            }
        }
        if (!needed) {
            return path;
        }

        StringBuilder sb = new StringBuilder("");
        for (char c : chars) {
            if (c == '[' || c == ']' || c == '|') {
                sb.append('%');
                sb.append(Integer.toHexString(c));
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
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
    private static void onDownloadStartNoStream(@NonNull final Context context,
                                                String url, String userAgent,
                                                String contentDisposition, @Nullable String mimetype) {
//        final Bus eventBus = BrowserApp.getBus(context);
        final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);

        // Check to see if we have an SDCard
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            int title;
            String msg;

            // Check to see if the SDCard is busy, same as the music app
            if (status.equals(Environment.MEDIA_SHARED)) {
                msg = context.getString(R.string.download_sdcard_busy_dlg_msg);
                title = R.string.download_sdcard_busy_dlg_title;
            } else {
                msg = context.getString(R.string.download_no_sdcard_dlg_msg);
                title = R.string.download_no_sdcard_dlg_title;
            }

            new AlertDialog.Builder(context).setTitle(title)
                    .setIcon(android.R.drawable.ic_dialog_alert).setMessage(msg)
                    .setPositiveButton(R.string.action_ok, null).show();
            return;
        }

        // java.net.URI is a lot stricter than KURL so we have to encode some
        // extra characters. Fix for b 2538060 and b 1634719
        WebAddress webAddress;
        try {
            webAddress = new WebAddress(url);
            webAddress.setPath(encodePath(webAddress.getPath()));
        } catch (Exception e) {
            // This only happens for very bad urls, we want to catch the
            // exception here
            Log.e(TAG, "Exception while trying to parse url '" + url + '\'', e);
            Toast.makeText(context, context.getString(R.string.problem_download), Toast.LENGTH_SHORT).show();
            return;
        }

        String addressString = webAddress.toString();
        Uri uri = Uri.parse(addressString);
        final DownloadManager.Request request;
        try {
            request = new DownloadManager.Request(uri);
        } catch (IllegalArgumentException e) {
            Toast.makeText(context, context.getString(R.string.cannot_download), Toast.LENGTH_SHORT).show();
            return;
        }
        request.setMimeType(mimetype);
        // set downloaded file destination to /sdcard/Download.
        // or, should it be set to one of several Environment.DIRECTORY* dirs
        // depending on mimetype?

        String location = "";
        File file = context.getExternalFilesDir(null);
        if(file != null) {
            location = file.toString();
        }

        Uri downloadFolder;
        location = addNecessarySlashes(location);
        downloadFolder = Uri.parse(location);

        File dir = new File(downloadFolder.getPath());
        if (!dir.isDirectory() && !dir.mkdirs()) {
            // Cannot make the directory
            Toast.makeText(context, context.getString(R.string.problem_location_download), Toast.LENGTH_LONG).show();
            return;
        }

        if (!isWriteAccessAvailable(downloadFolder)) {
            Toast.makeText(context, context.getString(R.string.problem_location_download), Toast.LENGTH_LONG).show();

            return;
        }
        //
        final Uri customUri = Uri.parse(FILE + location + filename);
        request.setDestinationUri(customUri);//如没有自定义uri，则通过getUriForDownloadedFile可获取默认uri
        // let this downloaded file be scanned by MediaScanner - so that it can
        // show up in Gallery app, for example.
        request.setVisibleInDownloadsUi(true);
        request.allowScanningByMediaScanner();
        request.setDescription(webAddress.getHost());
        // XXX: Have to use the old url since the cookies were stored using the
        // old percent-encoded url.
        String cookies = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader(COOKIE_REQUEST_HEADER, cookies);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        if (mimetype == null) {
            Log.d(TAG, "Mimetype is null");
            if (TextUtils.isEmpty(addressString)) {
                return;
            }
            // We must have long pressed on a link or image to download it. We
            // are not sure of the mimetype in this case, so do a head request
            new FetchUrlMimeType(context, request, addressString, cookies, userAgent).start();
        } else {
            Log.d(TAG, "Valid mimetype, attempting to download");
            final DownloadManager manager = (DownloadManager) context
                    .getSystemService(Context.DOWNLOAD_SERVICE);
            try {
                final long id = manager.enqueue(request);
                context.registerReceiver(new BroadcastReceiver() {
                    public void onReceive(Context ctxt, Intent intent) {
                        long currentId = intent.getExtras().getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
                        if (currentId == id) {
                            openFile(customUri, ctxt);
                            context.unregisterReceiver(this);
                        }
                    }
                }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            } catch (IllegalArgumentException e) {
                // Probably got a bad URL or something
                Log.e(TAG, "Unable to enqueue request", e);
                Toast.makeText(context, context.getString(R.string.cannot_download), Toast.LENGTH_LONG).show();
            } catch (SecurityException e) {
                // TODO write a download utility that downloads files rather than rely on the system
                // because the system can only handle Environment.getExternal... as a path
                Toast.makeText(context, context.getString(R.string.problem_location_download), Toast.LENGTH_LONG).show();

            }
            Toast.makeText(context, context.getString(R.string.download_pending) + ' ' + filename, Toast.LENGTH_LONG).show();
        }

    }

    static void openFile(Uri uri, Context context) {
        openFile(uri, context, getMimeType(uri.getPath()));
    }

    private static void openFile(Uri uri, Context context, String mineType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mineType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static String getMimeType(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (TextUtils.isEmpty(extension)) {
            extension = url.substring(url.lastIndexOf(".") + 1, url.length());
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private static boolean isWriteAccessAvailable(@NonNull Uri fileUri) {
        File file = new File(fileUri.getPath());
        try {
            if (file.createNewFile()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    @NonNull
    static String addNecessarySlashes(@Nullable String originalPath) {
        if (originalPath == null || originalPath.length() == 0) {
            return "/";
        }
        if (originalPath.charAt(originalPath.length() - 1) != '/') {
            originalPath = originalPath + '/';
        }
        if (originalPath.charAt(0) != '/') {
            originalPath = '/' + originalPath;
        }
        return originalPath;
    }

}
