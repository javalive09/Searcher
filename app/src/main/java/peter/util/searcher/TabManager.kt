package peter.util.searcher

import android.widget.Toast

import java.util.ArrayList

import peter.util.searcher.activity.MainActivity
import peter.util.searcher.bean.Bean
import peter.util.searcher.tab.SearcherTab
import peter.util.searcher.tab.TabGroup

/**
 * 标签管理类
 * Created by peter on 2016/11/17.
 */

class TabManager(private val mainActivity: MainActivity) {
    private val maxTabs = 50
    private var mCurrentTabIndex: Int = 0
    val list = ArrayList<TabGroup>(maxTabs)

    fun currentTabGroup(): TabGroup? = when {
        !list.isEmpty() -> list[mCurrentTabIndex]
        else -> null
    }

    fun loadUrl(bean: Bean, mNewTab: Boolean) {
        var newTab = mNewTab
        if (list.size == maxTabs) {
            newTab = false
            Toast.makeText(Searcher.context, R.string.tabs_max_txt, Toast.LENGTH_LONG).show()
        }
        if (newTab) {
            val tab = TabGroup(mainActivity)
            if (list.size == 0 || mCurrentTabIndex == list.size - 1) {//有0个tab 或 currentTab在最后
                mCurrentTabIndex = list.size
                list.add(tab)
            } else {//currentTab不在最后
                mCurrentTabIndex += 1
                list.add(mCurrentTabIndex, tab)
            }
        }
        val tabGroup = currentTabGroup()
        tabGroup?.loadUrl(bean)
    }

    fun switchTabGroup(tabGroup: TabGroup) {
        switchTabGroup(tabGroup, true)
    }

    private fun switchTabGroup(tabGroup: TabGroup, reload: Boolean) {
        mCurrentTabIndex = list.indexOf(tabGroup)
        val currentTabGroupView = tabGroup.currentTab()!!.getView()
        pauseTabGroupExclude(tabGroup)
        mainActivity.setCurrentView(currentTabGroupView!!)
        currentTabGroup()!!.onResume()
        mainActivity.refreshTitle()
        if (reload) {
            currentTabGroup()!!.checkReloadCurrentTab()
        }
    }

    fun restoreTabPos(groupIndex: Int, tabIndex: Int) {
        val tabGroup = list[groupIndex]
        switchTabGroup(tabGroup, false)
        currentTabGroup()!!.setCurrentTab(tabIndex)
        currentTabGroup()!!.checkReloadCurrentTab()
    }

    fun getTabGroup(topTab: SearcherTab): TabGroup? = list.firstOrNull { it.containsTab(topTab) }

    fun removeTabGroup(tabGroup: TabGroup) {
        if (list.size > 0) {
            tabGroup.onDestroy()
            list.remove(tabGroup)
            if (list.size > 0) {
                val indexNext = list.size - 1
                val tabGroupNext = list[indexNext]
                switchTabGroup(tabGroupNext)
            }
        }
    }

    fun removeIndex(tabGroup: TabGroup) {
        if (list.size > 0) {
            list.remove(tabGroup)
        }
    }

    fun resumeTabGroupExclude(exTabGroup: TabGroup?) {
        list.filter { it !== exTabGroup }.forEach { it.onResume() }
    }

    fun pauseTabGroupExclude(exTabGroup: TabGroup?) {
        list.filter { it !== exTabGroup }.forEach { it.onPause() }
    }

}
