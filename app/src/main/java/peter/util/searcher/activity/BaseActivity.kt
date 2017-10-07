package peter.util.searcher.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import java.io.File
import java.util.ArrayList
import java.util.Date
import peter.util.searcher.R
import peter.util.searcher.bean.Bean

/**
 * 基础activity 提供一些公用方法
 * Created by peter on 16/5/9.
 */
open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LIST.add(this)
    }

    open fun getSearchWord() : String? = null

    open fun setSearchWord(word : String) {}

    fun getVersionName() : String = packageManager.getPackageInfo(packageName, 0).versionName

    fun getVersionCode() : Int = packageManager.getPackageInfo(packageName, 0).versionCode

    override fun onDestroy() {
        super.onDestroy()
        LIST.remove(this)
    }

    fun exit() {
        for (activity in LIST) {
            activity.finish()
        }
    }

    fun closeIME() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    fun openIME() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
    }

    fun startBrowser(bean: Bean) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.action = ACTION_INNER_BROWSE
        intent.putExtra(NAME_BEAN, bean)
        startActivity(intent)
    }

    fun clearCacheFolder(dir: File?, numDays: Int): Int {
        var deletedFiles = 0
        if (dir != null && dir.isDirectory) {
            try {
                for (child in dir.listFiles()) {

                    //first delete subdirectories recursively
                    if (child.isDirectory) {
                        deletedFiles += clearCacheFolder(child, numDays)
                    }

                    //then delete the files and subdirectories in this dir
                    //only empty directories can be deleted, so subdirs have been done first
                    if (child.lastModified() < Date().time - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("peter", "Failed to clean the cache, error ${e.message}")
            }
        }
        return deletedFiles
    }

    fun clearCookies() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
    }

    fun sendMailByIntent() {
        val data = Intent(Intent.ACTION_SENDTO)
        data.data = Uri.parse(getString(R.string.setting_feedback_address))
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_feedback))
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_feedback_body))
        startActivity(data)
    }

    fun showAlertDialog(titleRes: Int, contentRes: Int) {
        val dialog = AlertDialog.Builder(this@BaseActivity).create()
        dialog.setCanceledOnTouchOutside(true)
        dialog.setTitle(getString(titleRes))
        dialog.setMessage(getString(contentRes))
        dialog.show()
    }

    companion object {
        val ACTION_INNER_BROWSE = "peter.util.searcher.inner"
        val NAME_BEAN = "peter.util.searcher.bean"
        val NAME_WORD = "peter.util.searcher.word"
        private val LIST = ArrayList<Activity>()
    }

}
