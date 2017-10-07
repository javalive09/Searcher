/*
 * Copyright 2014 A.C.R. Development
 */
package peter.util.searcher.net

import android.Manifest
import android.support.v7.app.AlertDialog
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast

import com.anthonycr.grant.PermissionsManager
import com.anthonycr.grant.PermissionsResultAction

import peter.util.searcher.activity.MainActivity
import peter.util.searcher.R


class MyDownloadListener(private val mActivity: MainActivity) : android.webkit.DownloadListener {

    override fun onDownloadStart(url: String, userAgent: String,
                                 contentDisposition: String, mimetype: String, contentLength: Long) {
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(mActivity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                object : PermissionsResultAction() {
                    override fun onGranted() {
                        val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
                        val builder = AlertDialog.Builder(mActivity) // dialog
                        builder.setTitle(fileName)
                                .setMessage(mActivity.resources.getString(R.string.dialog_download))
                                .setPositiveButton(mActivity.resources.getString(R.string.action_download)
                                ) { _, _ ->
                                    DownloadHandler.onDownloadStart(mActivity, url, userAgent,
                                            contentDisposition, mimetype)
                                }
                                .setNegativeButton(mActivity.resources.getString(R.string.action_cancel)) { dialog, _ -> dialog.cancel() }.show()
                        Log.i("MyDownloadListener", "Downloading" + fileName)
                    }

                    override fun onDenied(permission: String) {
                        Toast.makeText(mActivity, R.string.wr_sdcard_permission, Toast.LENGTH_LONG).show()
                    }
                })
    }

}