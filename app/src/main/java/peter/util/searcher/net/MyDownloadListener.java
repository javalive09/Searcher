/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.net;

import android.Manifest;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.anthonycr.grant.PermissionsManager;
import com.anthonycr.grant.PermissionsResultAction;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.R;


public class MyDownloadListener implements android.webkit.DownloadListener {

    private MainActivity mActivity;
    private static final String TAG = MyDownloadListener.class.getSimpleName();

    public MyDownloadListener(MainActivity mainActivity) {
        this.mActivity = mainActivity;
    }

    @Override
    public void onDownloadStart(final String url, final String userAgent,
                                final String contentDisposition, final String mimetype, long contentLength) {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mActivity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new PermissionsResultAction() {
                    @Override
                    public void onGranted() {
                        String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity); // dialog
                        builder.setTitle(fileName)
                                .setMessage(mActivity.getResources().getString(R.string.dialog_download))
                                .setPositiveButton(mActivity.getResources().getString(R.string.action_download),
                                        (dialog, which) -> DownloadHandler.onDownloadStart(mActivity, url, userAgent,
                                                contentDisposition, mimetype))
                                .setNegativeButton(mActivity.getResources().getString(R.string.action_cancel), (dialog, which) -> dialog.cancel()).show();
                        Log.i(TAG, "Downloading" + fileName);
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(mActivity, R.string.wr_sdcard_permission, Toast.LENGTH_LONG).show();
                    }
                });
    }
}