package peter.util.searcher.activity

import android.animation.ObjectAnimator
import android.app.DownloadManager
import android.app.SearchManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.widget.DrawerLayout
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.ContextMenu
import android.view.Gravity
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.widget.PopupMenu
import android.widget.Toast

import com.umeng.analytics.MobclickAgent

import java.util.HashMap

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import peter.util.searcher.SettingsManager
import peter.util.searcher.TabManager
import peter.util.searcher.adapter.MultiWindowAdapter
import peter.util.searcher.bean.Bean
import peter.util.searcher.R
import peter.util.searcher.net.MyDownloadListener
import peter.util.searcher.net.UpdateController
import peter.util.searcher.tab.HomeTab
import peter.util.searcher.tab.LocalViewTab
import peter.util.searcher.tab.Tab
import peter.util.searcher.tab.TabGroup
import peter.util.searcher.tab.WebViewTab
import peter.util.searcher.utils.Constants
import peter.util.searcher.utils.FileUtils
import peter.util.searcher.utils.UrlUtils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_find_control.*
import kotlinx.android.synthetic.main.layout_tabs.*
import peter.util.searcher.db.DaoManager
import peter.util.searcher.net.DownloadHandler
import peter.util.searcher.view.SearchWebView
import peter.util.searcher.view.TextDrawable

/**
 * 主页activity
 * Created by peter on 16/5/9.
 */
class MainActivity : BaseActivity(), View.OnClickListener {

    private var popup: PopupMenu? = null
    private var multiWindowDrawable: TextDrawable? = null
    var tabManager: TabManager? = null
    private val router = HashMap<String, Class<*>>()
    private var multiWindowAdapter: MultiWindowAdapter? = null
    private var realBack = false
    private val mHandler = Handler(Looper.getMainLooper())

    private val contextMenuListener = object : SearchWebView.OnMenuItemClickListener() {

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val info = info
            when (item.itemId) {
                R.id.open_pic_new_tab -> {
                    val bean = Bean()
                    bean.url = info?.extra
                    bean.time = System.currentTimeMillis()
                    val parentTabGroup = tabManager!!.currentTabGroup()
                    tabManager!!.loadUrl(bean, true)
                    tabManager!!.currentTabGroup()!!.parent = parentTabGroup
                }
                R.id.copy_pic_link -> {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.primaryClip = ClipData.newPlainText(info?.extra, info?.extra)
                    Toast.makeText(this@MainActivity, R.string.copy_link_txt, Toast.LENGTH_SHORT).show()
                }
                R.id.save_pic -> {
                    var url = info?.extra
                    var mimeType = "image/jpeg"
                    if (info?.extra != null) {
                        mimeType = DownloadHandler.getMimeType(info.extra)
                    }
                    if (!url.isNullOrEmpty()) {
                        MyDownloadListener(this@MainActivity).onDownloadStart(url!!, "", "", mimeType, 0)
                    }
                }
                R.id.shard_pic -> {
                    val sendIntent = Intent()
                    sendIntent.action = Intent.ACTION_SEND
                    sendIntent.putExtra(Intent.EXTRA_TEXT, info?.extra)
                    sendIntent.type = "text/plain"
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)))
                }
                R.id.open_url_new_tab -> {
                    val bean = Bean()
                    bean.url = info?.extra
                    bean.time = System.currentTimeMillis()
                    val parentTabGroup = tabManager!!.currentTabGroup()
                    tabManager!!.loadUrl(bean, true)
                    tabManager!!.currentTabGroup()!!.parent = parentTabGroup
                }
                R.id.copy_txt_link_free -> {
                }
            }
            return false
        }
    }

    private fun isTopBarHide(): Boolean = top_bar!!.translationY == (-Constants.getActionBarH(this)).toFloat()

    private fun isTopBarShow(): Boolean = top_bar!!.translationY == 0f

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    private fun init() {
        tabManager = TabManager(this@MainActivity)
        installLocalTabRounter()
        initTopBar()
        initTabs()
        checkIntentData(intent)
        UpdateController.instance.autoCheckVersion(this@MainActivity)
    }

    private fun initTopBar() {
        setSupportActionBar(toolbar)
        multiWindowDrawable = TextDrawable(this@MainActivity)
        toolbar!!.navigationIcon = multiWindowDrawable
        toolbar!!.setNavigationContentDescription(R.string.app_name)
        toolbar!!.setNavigationOnClickListener { _ -> drawer_layout!!.openDrawer(Gravity.START) }
        top_txt!!.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                touchSearch()
            }
            false
        }
    }

    private fun initTabs() {
        multiWindowAdapter = MultiWindowAdapter()
        tabs!!.adapter = multiWindowAdapter
        drawer_layout!!.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                updateTabs()
            }

            override fun onDrawerClosed(drawerView: View) {
                updateTabs()
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
        updateTabs()
        restoreLostTabs()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo) {
        val info = menuInfo as SearchWebView.ContextMenuInfo
        popupContextMenu(info)
    }

    private fun popupContextMenu(info: SearchWebView.ContextMenuInfo) {
        menu_anchor!!.x = info.x.toFloat()
        menu_anchor!!.y = info.y.toFloat()
        popup?.dismiss()
        popup = PopupMenu(this@MainActivity, menu_anchor)
        popup!!.menuInflater.inflate(R.menu.context, popup!!.menu)
        if (info.result.extra != null) {
            when (info.result.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE // 带有链接的图片类型
                    , WebView.HitTestResult.IMAGE_TYPE -> {
                    popup!!.menu.setGroupVisible(R.id.picture, true)
                    popup!!.menu.getItem(0).menuInfo
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> popup!!.menu.setGroupVisible(R.id.txt_link, true)
            }
            contextMenuListener.info = info.result
            contextMenuListener.searchWebView = info.searchWebView
            popup!!.setOnMenuItemClickListener(contextMenuListener)
            popup!!.show()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (tabManager!!.currentTabGroup()?.currentTab() is LocalViewTab) {
            menu.setGroupVisible(R.id.web_sites, false)
        } else {
            menu.setGroupVisible(R.id.web_sites, true)
            menu.findItem(R.id.action_auto_fullscreen).isChecked = SettingsManager.instance.isAutoFullScreen
            val webViewTab = tabManager!!.currentTabGroup()?.currentTab() as WebViewTab
            menu.findItem(R.id.action_desktop).isChecked = webViewTab.isDeskTopUA
        }
        menu.findItem(R.id.action_goforward).isVisible = tabManager!!.currentTabGroup()!!.canGoForward()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share -> {
                var url = tabManager!!.currentTabGroup()!!.currentTab()!!.getUrl()
                val title = tabManager!!.currentTabGroup()!!.currentTab()!!.getTitle()
                val sendIntent = Intent()
                if (!TextUtils.isEmpty(title)) {
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, title)
                    url = title + "\n" + url
                }
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, url)
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_link_title)))
            }
            R.id.action_favorite -> {
                val url : String? = tabManager?.currentTabGroup()?.currentTab()?.getUrl()
                if (!url!!.startsWith(Tab.LOCAL_SCHEMA)) {
                    val bean = Bean()
                    bean.name = tabManager!!.currentTabGroup()!!.currentTab()!!.getTitle()
                    if (TextUtils.isEmpty(bean.name)) {
                        bean.name = tabManager!!.currentTabGroup()!!.currentTab()!!.getUrl()
                    }
                    bean.url = url
                    bean.time = System.currentTimeMillis()

                    Observable.create<Boolean> { s ->
                        s.onNext(DaoManager.getInstance().insertFavorite(bean) != 0L)
                        s.onComplete()
                    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).
                            subscribe { s ->
                                if (s) Toast.makeText(this@MainActivity, R.string.favorite_txt, Toast.LENGTH_SHORT).show()
                            }
                }
            }
            R.id.action_copy_link -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(tabManager!!.currentTabGroup()!!.currentTab()!!.getTitle(), tabManager!!.currentTabGroup()!!.currentTab()!!.getUrl())
                clipboard.primaryClip = clip
                Toast.makeText(this@MainActivity, R.string.copy_link_txt, Toast.LENGTH_SHORT).show()
            }
            R.id.action_download -> {
                val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }
            R.id.action_setting -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            R.id.action_exit -> exit()
            R.id.action_bookmark -> startActivity(Intent(this@MainActivity, BookMarkActivity::class.java))
            R.id.action_goforward -> tabManager!!.currentTabGroup()!!.goForward()
            R.id.action_refresh -> tabManager!!.currentTabGroup()!!.currentTab()!!.reload()
            R.id.action_auto_fullscreen -> {
                SettingsManager.instance.saveAutoFullScreenSp(!item.isChecked)
                tabManager!!.currentTabGroup()!!.currentTab()!!.getView()!!.requestLayout()
            }
            R.id.action_desktop -> {
                val tab = tabManager!!.currentTabGroup()!!.currentTab() as WebViewTab
                if (tab.isDeskTopUA) {
                    tab.setUA(Constants.MOBILE_USER_AGENT)
                } else {
                    tab.setUA(Constants.DESKTOP_USER_AGENT)
                }
                tab.reload()
            }
            R.id.action_find -> showFindControlView(true)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showFindControlView(show: Boolean) {
        if (show) {
            val searcherTab = tabManager!!.currentTabGroup()!!.currentTab()
            if (searcherTab is WebViewTab) {
                searcherTab.getView()!!.setFindListener({ activeMatchOrdinal, numberOfMatches, isDoneCounting ->
                    if (isDoneCounting) {
                        if (numberOfMatches > 0) {
                            count_find!!.text = "${activeMatchOrdinal + 1}) / $numberOfMatches"
                        } else {
                            count_find!!.text = ""
                        }
                    } else {
                        if (count_find != null) {
                            count_find!!.text = "..."
                        }
                    }
                })
                find_content_txt!!.addTextChangedListener(object : TextWatcher {

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable) {
                        if (!TextUtils.isEmpty(s)) {
                            searcherTab.getView()!!.findAllAsync(s.toString())
                        } else {
                            count_find!!.text = ""
                        }
                    }
                })
            }
            find_content_txt!!.setText("")
            count_find!!.text = ""
            find_content_txt!!.requestFocus()
            find_control!!.visibility = View.VISIBLE
            openIME()
        } else {
            find_control!!.visibility = View.GONE
            closeIME()
        }
    }

    private fun touchSearch() {
        val content = tabManager!!.currentTabGroup()!!.currentTab()!!.getSearchWord()
        val pageNo = tabManager!!.currentTabGroup()!!.currentTab()!!.getPageNo()
        val intent = Intent(this@MainActivity, SearchActivity::class.java)
        val bean = Bean(content)
        bean.pageNo = pageNo!!
        intent.putExtra(BaseActivity.Companion.NAME_BEAN, bean)
        startActivity(intent)
    }

    fun updateTabs() {
        if (multiWindowAdapter != null) {
            multiWindowAdapter!!.update(this@MainActivity)
        }
    }

    private fun installLocalTabRounter() {
        router.put(Tab.URL_HOME, HomeTab::class.java)
    }

    fun getRouterClass(url: String): Class<*>? = router[url]

    fun setCurrentView(view: View) {
        webview_container!!.setCurrentView(view)
        progress!!.visibility = View.INVISIBLE
        showFindControlView(false)
        showTopBar()
    }

    fun setCurrentView(viewId: Int): View {
        val view = webview_container!!.setCurrentView(viewId)
        progress!!.visibility = View.INVISIBLE
        showFindControlView(false)
        showTopBar()
        return view
    }

    fun showTopBar() {
        if (isTopBarHide() && SettingsManager.instance.isAutoFullScreen) {
            ObjectAnimator.ofFloat(top_bar, "translationY", -Constants.getActionBarH(this).toFloat(), 0f).setDuration(300).start()
            ObjectAnimator.ofFloat(webview_container, "translationY", -Constants.getActionBarH(this).toFloat(), 0f).setDuration(300).start()
        }
    }

    fun hideTopBar() {
        if (isTopBarShow() && SettingsManager.instance.isAutoFullScreen) {
            ObjectAnimator.ofFloat(top_bar, "translationY", 0f, -Constants.getActionBarH(this).toFloat()).setDuration(300).start()
            ObjectAnimator.ofFloat(webview_container, "translationY", 0f, -Constants.getActionBarH(this).toFloat()).setDuration(300).start()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntentData(intent)
    }

    private fun checkIntentData(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (BaseActivity.Companion.ACTION_INNER_BROWSE == action) { // inner invoke
                val bean = intent.getParcelableExtra<Bean>(BaseActivity.Companion.NAME_BEAN)
                if (!TextUtils.isEmpty(bean.url)) {
                    tabManager!!.loadUrl(bean, false)
                }
            } else if (Intent.ACTION_VIEW == action) { // outside invoke
                if (!TextUtils.isEmpty(intent.dataString)) {
                    tabManager!!.loadUrl(Bean("", intent.dataString), true)
                }
            } else if (Intent.ACTION_WEB_SEARCH == action) {
                val searchWord = intent.getStringExtra(SearchManager.QUERY)
                val engineUrl = getString(R.string.default_engine_url)
                val url = UrlUtils.smartUrlFilter(searchWord, true, engineUrl)
                tabManager!!.loadUrl(Bean(searchWord, url), true)
            } else if (Intent.ACTION_MAIN == action) {
                if (tabManager!!.list.size == 0) {
                    loadHome(true)
                }
            } else if (Intent.ACTION_ASSIST == action) {
                if (tabManager!!.list.size == 0) {
                    loadHome(true)
                } else {
                    val searcherTab = tabManager!!.currentTabGroup()!!.currentTab()
                    if (searcherTab is WebViewTab) {//webView
                        loadHome(true)
                    }
                }
                touchSearch()
            }
        }
    }

    private fun loadHome(newTab: Boolean) {
        tabManager!!.loadUrl(Bean("", Tab.URL_HOME), newTab)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawer_layout!!.isDrawerOpen(Gravity.START)) {
                drawer_layout!!.closeDrawers()
                return true
            }

            if (find_control!!.visibility == View.VISIBLE) {
                showFindControlView(false)
                return true
            }

            val tabGroup = tabManager!!.currentTabGroup()
            if (tabGroup!!.canGoBack()) {
                tabGroup.goBack()
                return true
            } else {
                if (tabGroup.parent != null) {
                    tabManager!!.removeIndex(tabGroup)
                    tabManager!!.switchTabGroup(tabGroup.parent!!)
                    return true
                }
            }

            if (!realBack) {
                realBack = true
                Toast.makeText(this@MainActivity, R.string.exit_hint, Toast.LENGTH_SHORT).show()
                mHandler.postDelayed({ realBack = false }, 1000)
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onResume() {
        super.onResume()
        tabManager!!.resumeTabGroupExclude(null)
        updateTabs()
        MobclickAgent.onResume(this)
    }

    override fun onPause() {
        super.onPause()
        tabManager!!.pauseTabGroupExclude(null)
        MobclickAgent.onPause(this)
        saveTabs()
    }

    fun refreshTitle() {
        refreshTopText(tabManager!!.currentTabGroup()!!.currentTab()!!.getHost())
        multiWindowDrawable!!.setText(tabManager!!.list.size)
    }

    private fun saveTabs() {
        val outState = Bundle(ClassLoader.getSystemClassLoader())
        val tabGroupList = tabManager!!.list
        outState.putString(BUNDLE_KEY_GROUP_SIZE, tabGroupList.size.toString() + "")

        outState.putInt(BUNDLE_KEY_CURRENT_GROUP, tabManager!!.list.indexOf(tabManager!!.currentTabGroup()))
        outState.putInt(BUNDLE_KEY_CURRENT_TAB, tabManager!!.currentTabGroup()!!.tabs.indexOf(tabManager!!.currentTabGroup()!!.currentTab()))

        for (g in 0 until tabGroupList.size) {
            outState.putInt(BUNDLE_KEY_TAB_SIZE + g, tabGroupList[g].tabs.size)
            for (t in tabGroupList[g].tabs.indices) {
                val tab = tabGroupList[g].tabs[t]
                if (!TextUtils.isEmpty(tab.getUrl())) {
                    val state = Bundle(ClassLoader.getSystemClassLoader())
                    val key = g.toString() + BUNDLE_KEY_SIGN + t
                    state.putString(URL_KEY, tab.getUrl())
                    if (tab is WebViewTab) {
                        (tab.getView() as WebView).saveState(state)
                        outState.putBundle(key, state)
                        state.putString(BUNDLE_KEY_SEARCH_WORD, tab.getSearchWord())
                    } else {
                        outState.putBundle(key, state)
                    }
                }
            }
        }
        FileUtils.writeBundleToStorage(application, outState, BUNDLE_STORAGE)
    }

    private fun restoreLostTabs() {
        val savedState = FileUtils.readBundleFromStorage(application, BUNDLE_STORAGE)
        if (savedState != null) {
            val groupSize = Integer.valueOf(savedState.getString(BUNDLE_KEY_GROUP_SIZE))!!
            val currentGroupIndex = savedState.getInt(BUNDLE_KEY_CURRENT_GROUP)
            val currentTabIndex = savedState.getInt(BUNDLE_KEY_CURRENT_TAB)
            for (g in 0 until groupSize) {
                val tabSize = savedState.getInt(BUNDLE_KEY_TAB_SIZE + g)
                for (t in 0 until tabSize) {
                    val key = g.toString() + BUNDLE_KEY_SIGN + t
                    val state = savedState.getBundle(key)
                    if (state != null) {
                        val url = state.getString(URL_KEY)
                        if (t == 0) {//first tab
                            Log.i("url ", url)
                            tabManager!!.loadUrl(Bean("", url), true)
                        } else {// webView
                            val searchWord = state.getString(BUNDLE_KEY_SEARCH_WORD)
                            val bean = DaoManager.getInstance().queryBean(searchWord, url)
                            tabManager!!.loadUrl(bean, false)
                            Log.i("state ", state.toString())

                            val searcherTab = tabManager!!.currentTabGroup()!!.currentTab()
                            if (searcherTab is WebViewTab) {
                                Log.i("webView ", searcherTab.getView().toString())
                                searcherTab.getView()!!.restoreState(state)
                                searcherTab.getView()!!.stopLoading()
                            }
                        }
                    }
                }
            }
            tabManager!!.restoreTabPos(currentGroupIndex, currentTabIndex)
        }
        FileUtils.deleteBundleInStorage(application, BUNDLE_STORAGE)
    }

    private fun refreshTopText(text: String?) {
        if (TextUtils.isEmpty(text)) {
            top_txt!!.setText("")
            top_txt!!.setHint(R.string.search_hint)
        } else {
            top_txt!!.setText(text)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_tab -> {
                loadHome(true)
                drawer_layout!!.closeDrawers()
            }
            R.id.close_tab -> if (tabManager!!.list.size == 1) {
                exit()
            } else {
                val tabGroup = v.tag as TabGroup
                tabManager!!.removeTabGroup(tabGroup)
                updateTabs()
            }
            R.id.multi_window_item -> {
                val tabGroup = v.getTag(R.id.multi_window_item_tag) as TabGroup
                tabManager!!.switchTabGroup(tabGroup)
                drawer_layout!!.closeDrawers()
            }
            R.id.up_find -> {
                val searcherTab = tabManager!!.currentTabGroup()!!.currentTab()
                if (searcherTab is WebViewTab) {
                    searcherTab.getView()!!.findNext(false)
                }
            }
            R.id.down_find -> {
                val searcherTab = tabManager!!.currentTabGroup()!!.currentTab()
                if (searcherTab is WebViewTab) {
                    val webViewTab = searcherTab as WebViewTab?
                    webViewTab!!.getView()!!.findNext(true)
                }
            }
            R.id.close_find -> {
                showFindControlView(false)
                val searcherTab = tabManager!!.currentTabGroup()!!.currentTab()
                if (searcherTab is WebViewTab) {
                    val webViewTab = searcherTab as WebViewTab?
                    webViewTab!!.getView()!!.clearMatches()
                }
            }
            else -> {
            }
        }
    }

    /**
     * @param enabled   status bar
     * @param immersive total fullscreen
     */
    fun setFullscreen(enabled: Boolean, immersive: Boolean) {
        val window = window
        val decor = window.decorView
        if (enabled) {
            if (immersive) {
                decor.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            } else {
                decor.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    fun refreshProgress(webViewTab: WebViewTab, progressValue: Int) {
        if (tabManager!!.currentTabGroup()!!.currentTab() === webViewTab) {
            if (progressValue == 100) {
                progress!!.post {
                    progress!!.progress = progressValue
                    progress!!.visibility = View.INVISIBLE }
            } else {
                progress!!.post {
                    progress.visibility = View.VISIBLE
                    progress!!.progress = progressValue
                }
            }
        }
    }

    companion object {
        private val BUNDLE_KEY_SIGN = "&"
        private val BUNDLE_KEY_TAB_SIZE = "KEY_TAB_SIZE"
        private val BUNDLE_KEY_SEARCH_WORD = "KEY_SEARCH_WORD"
        private val BUNDLE_KEY_GROUP_SIZE = "KEY_GROUP_SIZE"
        private val BUNDLE_KEY_CURRENT_GROUP = "KEY_CURRENT_GROUP"
        private val BUNDLE_KEY_CURRENT_TAB = "KEY_CURRENT_TAB"
        private val URL_KEY = "URL_KEY"
        private val BUNDLE_STORAGE = "SAVED_TABS.parcel"
    }

}
