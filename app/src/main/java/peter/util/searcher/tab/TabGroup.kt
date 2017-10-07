package peter.util.searcher.tab

import java.util.ArrayList

import peter.util.searcher.activity.MainActivity
import peter.util.searcher.bean.Bean

/**
 * 标签组
 * Created by peter on 2016/11/20.
 */

class TabGroup(private val mainActivity: MainActivity) : Tab {

    override fun getUrl(): String? = ""

    override fun getSearchWord(): String? = ""

    override fun getPageNo(): Int? = 0

    override fun getTitle(): String? = ""

    override fun getHost(): String? = ""

    val tabs = ArrayList<SearcherTab>(MAX_TAB)
    var parent: TabGroup? = null
    private var mCurrentTabIndex: Int = 0
    private val classes: Array<Class<*>> = arrayOf(MainActivity::class.java)
    private val activityObject: Array<Any> = arrayOf(mainActivity)

    override fun onCreate() {}

    fun currentTab(): SearcherTab? = when {
        !tabs.isEmpty() -> tabs[mCurrentTabIndex]
        else -> null
    }

    override fun loadUrl(bean: Bean) {
        var currentTab = currentTab()
        if (currentTab == null) {//head tab
            currentTab = newTabByUrl(bean.url)!!
            tabs.add(currentTab)
            mCurrentTabIndex = tabs.size - 1
        } else {//body tab
            if (bean.url!!.startsWith(Tab.Companion.LOCAL_SCHEMA) || //local url
                    currentTab is LocalViewTab) {//current local tab
                if (currentTab.getUrl() == bean.url) {//same local url
                    return
                }
                currentTab = newTabByUrl(bean.url)
                val index = mCurrentTabIndex + 1
                tabs.add(index, currentTab!!)
                removeTabFromIndeoToEnd(index + 1)
                mCurrentTabIndex = tabs.size - 1
            }
        }
        currentTab.loadUrl(bean)
        mainActivity.refreshTitle()
    }

    override fun onDestroy() {
        for (tab in tabs) {
            tab.onDestroy()
        }
    }

    private fun removeTabFromIndeoToEnd(index: Int) {
        var i = index
        val size = tabs.size
        while (i < size) {
            val tab = tabs.removeAt(index)
            tab.onDestroy()
            i++
        }
    }

    fun containsTab(tab: SearcherTab): Boolean = tabs.contains(tab)

    private fun newTabByUrl(url: String?): SearcherTab? {
        return if (url!!.startsWith(Tab.Companion.LOCAL_SCHEMA)) {
            newLocalTab(url)
        } else {
            WebViewTab(mainActivity)
        }
    }

    override fun onResume() {
        for (tab in tabs) {
            tab.onResume()
        }
    }

    override fun onPause() {
        for (tab in tabs) {
            tab.onPause()
        }
    }


    private fun newLocalTab(url: String): LocalViewTab? {
        val clazz = mainActivity.getRouterClass(url)
        var tab: LocalViewTab? = null
        try {
            val localConstructor = clazz!!.getConstructor(*classes)
            tab = localConstructor.newInstance(*activityObject) as LocalViewTab
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tab
    }

    fun setCurrentTab(index: Int) {
        mCurrentTabIndex = index
        val tab = tabs[index]
        tab.loadUrl(Bean(tab.getSearchWord(), tab.getUrl()!!))
        checkReloadCurrentTab()
    }

    fun checkReloadCurrentTab() {
        val tab = currentTab()
        if (tab is WebViewTab) {
            val webViewTab = tab as WebViewTab?
            if (webViewTab!!.getView()!!.progress != 100 || webViewTab.getView()!!.contentHeight == 0) {
                webViewTab.reload()
            }
        }
    }

    override fun reload() {
        setCurrentTab(mCurrentTabIndex)
    }

    override fun canGoBack(): Boolean {
        val currentTab = currentTab()
        return if (currentTab is LocalViewTab) {
            mCurrentTabIndex > 0
        } else {
            currentTab!!.canGoBack() || mCurrentTabIndex > 0
        }
    }

    override fun goBack() {
        val currentTab = currentTab()
        if (currentTab is LocalViewTab) {
            setCurrentTab(mCurrentTabIndex - 1)
        } else {
            val webTabCanGoBack = currentTab!!.canGoBack()
            if (webTabCanGoBack) {
                currentTab.goBack()
            } else if (mCurrentTabIndex > 0) {
                setCurrentTab(mCurrentTabIndex - 1)
            }
        }
        mainActivity.refreshTitle()
    }

    override fun canGoForward(): Boolean {
        val currentTab = currentTab()
        if (currentTab is WebViewTab) {
            val webTabCanGoForward = currentTab.canGoForward()
            if (webTabCanGoForward) {
                return true
            }
        }
        return mCurrentTabIndex < tabs.size - 1
    }

    override fun goForward() {
        val currentTab = currentTab()
        if (currentTab is WebViewTab) {
            val webTabCanGoForward = currentTab.canGoForward()
            if (webTabCanGoForward) {
                currentTab.goForward()
            } else if (mCurrentTabIndex < tabs.size - 1) {
                setCurrentTab(mCurrentTabIndex + 1)
            }
        } else {
            setCurrentTab(mCurrentTabIndex + 1)
        }
        mainActivity.refreshTitle()
    }

    companion object {
        private val MAX_TAB = 50
    }


}
