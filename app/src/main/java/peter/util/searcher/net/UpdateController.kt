package peter.util.searcher.net

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast

import java.io.File
import java.lang.ref.WeakReference

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import peter.util.searcher.R
import peter.util.searcher.activity.BaseActivity
import peter.util.searcher.bean.VersionInfo

/**
 * 版本更新控制类
 * Created by peter on 16/5/4.
 */
class UpdateController private constructor() {

    private object HOLDER {
        val INSTANCE = UpdateController()
    }

    companion object {
        val instance: UpdateController by lazy { HOLDER.INSTANCE }
    }

    private fun showUpdateDialog(act: BaseActivity, versionInfo: VersionInfo) {
        val dialog = AlertDialog.Builder(act)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(act.getString(R.string.update_dialog_title_one))
                .setMessage(versionInfo.message)
                .setPositiveButton(R.string.update_dialog_ok) { _, _ -> downloadApk(act.applicationContext, versionInfo.url) }
                .setNegativeButton(R.string.update_dialog_cancel) { dialog1, _ -> dialog1.cancel() }.create()
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }

    fun checkVersion(act: BaseActivity, showToast: Boolean) {
        act.getSharedPreferences("updateTime", Context.MODE_PRIVATE).edit().putLong("lastTime", System.currentTimeMillis()).apply()
        if (showToast) {
            Toast.makeText(act, R.string.update_toast_start, Toast.LENGTH_SHORT).show()
        }
        val iVersionService = CommonRetrofit.instance.getRetrofit()!!.create(IVersionService::class.java)
        iVersionService.getUrl(IVersionService.URL).retry(5).subscribeOn(Schedulers.io()).flatMap { urlInfo -> iVersionService.getInfo(urlInfo.url) }.retry(5).observeOn(AndroidSchedulers.mainThread()).subscribe({ versionInfo ->
            if (act.getVersionCode() < versionInfo.code) {
                showUpdateDialog(act, versionInfo)
            } else {
                if (showToast) {
                    Toast.makeText(act, R.string.update_toast_nonew, Toast.LENGTH_SHORT).show()
                }
            }
        }) { throwable ->
            throwable.printStackTrace()
            Log.i("error", throwable.toString())
        }
    }

    private fun downloadApk(context: Context, url: String?) {
        val uri = Uri.parse(url)
        val req = DownloadManager.Request(uri)
        req.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        req.setDescription(context.getString(R.string.update_toast_des))
        val mineType = DownloadHandler.getMimeType(url!!)
        req.setMimeType(mineType)
        val filename = URLUtil.guessFileName(url, "", mineType)
        req.setTitle(filename)

        val file = context.getExternalFilesDir(null)
        if (file != null) {
            val location = DownloadHandler.addNecessarySlashes(file.toString())
            val customUri = Uri.parse(DownloadHandler.FILE + location + filename)
            req.setDestinationUri(customUri)

            // Ok go!
            val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = dm.enqueue(req)
            context.registerReceiver(object : BroadcastReceiver() {
                override fun onReceive(ctx: Context, intent: Intent) {
                    val currentId = intent.extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID)
                    if (currentId == downloadId) {
                        DownloadHandler.openFile(customUri, context)
                    }
                }
            }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }
    }

    fun autoCheckVersion(act: BaseActivity) {
        val time = act.getSharedPreferences("updateTime", Context.MODE_PRIVATE).getLong("lastTime", 0)
        if (time == 0L) {
            checkVersion(act, false)
        } else {
            val delta = System.currentTimeMillis() - time
            if (delta > 1000 * 60 * 60 * 24) {//24h
                checkVersion(act, false)
            }
        }
    }

}
