package peter.util.searcher.tab

import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.support.v4.util.ArrayMap
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView

import java.net.URI

import peter.util.searcher.R
import peter.util.searcher.SettingsManager
import peter.util.searcher.activity.MainActivity
import peter.util.searcher.bean.Bean
import peter.util.searcher.db.DaoManager
import peter.util.searcher.net.MyDownloadListener
import peter.util.searcher.net.MyWebChromeClient
import peter.util.searcher.net.MyWebClient
import peter.util.searcher.utils.Constants

/**
 * webView类型的标签
 * Created by peter on 2016/11/17.
 */

class WebViewTab internal constructor(activity: MainActivity) : SearcherTab(activity) {

    override fun getView(): WebView? = webView

    override fun getUrl(): String? = webView?.url

    override fun getSearchWord(): String? = bean?.name

    override fun getPageNo(): Int? = bean?.pageNo ?: 0

    override fun getTitle(): String? = webView?.title

    override fun getHost(): String? {
        return if (TextUtils.isEmpty(getUrl())) {
            null
        } else {
            val domain = URI(getUrl()).host
            if (domain.startsWith("www.")) domain.substring(4) else domain
        }
    }

    private var webView: WebView? = null

    private var bean: Bean? = null

    private var currentUA: String? = null

    private var myWebChromeClient: MyWebChromeClient? = null

    val mRequestHeaders = ArrayMap<String, String>()

    val isDeskTopUA: Boolean = Constants.DESKTOP_USER_AGENT == currentUA

    override fun onCreate() {
        initmWebView()
        initializeSettings()
        mainActivity.registerForContextMenu(webView)
    }

    override fun onDestroy() {
        if (webView != null) {
            webView!!.clearHistory()
            webView!!.clearCache(true)
            webView!!.loadUrl("about:blank")
            webView!!.pauseTimers()
            webView = null
        }
    }

    override fun onResume() {
        webView!!.resumeTimers()
        webView!!.onResume()
    }

    override fun onPause() {
        webView!!.pauseTimers()
        webView!!.onPause()
    }

    fun setUA(ua: String) {
        currentUA = ua
        webView!!.settings.userAgentString = ua
    }

    override fun onCreateViewResId(): Int = R.layout.tab_webview

    override fun loadUrl(bean: Bean) {
        if (!TextUtils.isEmpty(bean.url)) {
            this.bean = bean
            Log.i("peter", "url=" + bean.url!!)
            if (webView == null) {
                val resId = onCreateViewResId()
                webView = mainActivity.setCurrentView(resId) as WebView
                onCreate()
                if (Tab.ACTION_NEW_WINDOW != bean.url) {
                    webView!!.loadUrl(bean.url, mRequestHeaders)
                }

            } else {
                if (getUrl() != bean.url) {
                    webView!!.loadUrl(bean.url)
                }
                mainActivity.setCurrentView(webView!!)
            }
            if (!TextUtils.isEmpty(bean.name)) {
                saveData(bean)
            }
        }
    }

    override fun reload() = webView!!.reload()

    override fun canGoBack(): Boolean = webView!!.canGoBack() || myWebChromeClient!!.isCustomViewShow

    override fun goBack() {
        if (myWebChromeClient!!.isCustomViewShow) {
            myWebChromeClient!!.hideCustomView()
        } else {
            webView!!.goBack()
        }
    }

    override fun canGoForward(): Boolean = webView!!.canGoForward()

    override fun goForward() = webView!!.goForward()

    private fun saveData(bean: Bean) {
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void): Void? {
                bean.time = System.currentTimeMillis()
                DaoManager.getInstance().insertHistory(bean)
                return null
            }
        }.execute()
    }

    @Suppress("DEPRECATION")
    private fun initmWebView() {
        webView!!.drawingCacheBackgroundColor = Color.WHITE
        webView!!.isFocusableInTouchMode = true
        webView!!.isFocusable = true
        webView!!.isDrawingCacheEnabled = false
        webView!!.setWillNotCacheDrawing(true)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            webView!!.isAnimationCacheEnabled = false
            webView!!.isAlwaysDrawnWithCacheEnabled = false
        }
        webView!!.setBackgroundColor(Color.WHITE)
        webView!!.isScrollbarFadingEnabled = true
        webView!!.isSaveEnabled = true
        webView!!.setNetworkAvailable(true)
        myWebChromeClient = MyWebChromeClient(this@WebViewTab)
        webView!!.setWebChromeClient(myWebChromeClient)
        webView!!.setWebViewClient(MyWebClient(this@WebViewTab))
        webView!!.setDownloadListener(MyDownloadListener(mainActivity))
        setUA(WebSettings.getDefaultUserAgent(webView!!.context))
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        if (SettingsManager.instance.isNoTrack) {
            mRequestHeaders.put("DNT", "1")
        } else {
            mRequestHeaders.remove("DNT")
        }

    }

    @Suppress("DEPRECATION")
    private fun initializeSettings() {
        val settings = webView!!.settings
        settings.mediaPlaybackRequiresUserGesture = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        settings.domStorageEnabled = true
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.databaseEnabled = true

        settings.javaScriptEnabled = true
        settings.javaScriptCanOpenWindowsAutomatically = true

        settings.setGeolocationEnabled(true)

        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.allowContentAccess = true
        settings.allowFileAccess = true

        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false

        settings.savePassword = true
        settings.saveFormData = true
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
        settings.blockNetworkImage = false
        settings.setSupportMultipleWindows(true)

        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true

        //需要加上否则播放不了一些视频如今日头条的视频
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

    }

}
