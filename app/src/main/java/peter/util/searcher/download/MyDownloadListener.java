/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.download;

import android.Manifest;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.URLUtil;

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
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        DownloadHandler.onDownloadStart(mActivity, url, userAgent,
                                                contentDisposition, mimetype);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity); // dialog
                        builder.setTitle(fileName)
                                .setMessage(mActivity.getResources().getString(R.string.dialog_download))
                                .setPositiveButton(mActivity.getResources().getString(R.string.action_download),
                                        dialogClickListener)
                                .setNegativeButton(mActivity.getResources().getString(R.string.action_cancel),
                                        dialogClickListener).show();
                        Log.i(TAG, "Downloading" + fileName);
                    }

                    @Override
                    public void onDenied(String permission) {
                        //TODO show message
                    }
                });
    }
}