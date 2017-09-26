package peter.util.searcher;

import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.TabGroup;

/**
 * 标签管理类
 * Created by peter on 2016/11/17.
 */

public class TabManager {

    private static final int MAX_TAB = 50;
    private ArrayList<TabGroup> tabGroupArrayList = new ArrayList<>(MAX_TAB);
    private MainActivity mainActivity;
    private int mCurrentTabIndex;

    public TabManager(MainActivity activity) {
        mainActivity = activity;
    }

    public void loadUrl(Bean bean, boolean newTab) {
        if (tabGroupArrayList.size() == MAX_TAB) {
            newTab = false;
            Toast.makeText(Searcher.context, R.string.tabs_max_txt, Toast.LENGTH_LONG).show();
        }
        if (newTab) {
            TabGroup tab = new TabGroup(mainActivity);
            tabGroupArrayList.add(tab);
            mCurrentTabIndex = tabGroupArrayList.size() - 1;
        }
        TabGroup tabGroup = getCurrentTabGroup();
        tabGroup.loadUrl(bean);
    }

    public TabGroup getCurrentTabGroup() {
        return tabGroupArrayList.size() > 0 ? tabGroupArrayList.get(mCurrentTabIndex) : null;
    }

    public void switchTabGroup(TabGroup tabGroup) {
        switchTabGroup(tabGroup, true);
    }

    private void switchTabGroup(TabGroup tabGroup, boolean reload) {
        mCurrentTabIndex = tabGroupArrayList.indexOf(tabGroup);
        View currentTabGroupView = tabGroup.getCurrentTab().getView();
        pauseTabGroupExclude(tabGroup);
        mainActivity.setCurrentView(currentTabGroupView);
        getCurrentTabGroup().onResume();
        mainActivity.refreshTitle();
        if (reload) {
            getCurrentTabGroup().checkReloadCurrentTab();
        }
    }

    public void restoreTabPos(int groupIndex, int tabIndex) {
        TabGroup tabGroup = tabGroupArrayList.get(groupIndex);
        switchTabGroup(tabGroup, false);
        getCurrentTabGroup().setCurrentTab(tabIndex);
        getCurrentTabGroup().checkReloadCurrentTab();
    }

    public TabGroup getTabGroup(SearcherTab topTab) {
        for (TabGroup tabGroup : tabGroupArrayList) {
            if (tabGroup.containsTab(topTab)) {
                return tabGroup;
            }
        }
        return null;
    }

    public void removeTabGroup(TabGroup tabGroup) {
        if (tabGroupArrayList.size() > 0) {
            tabGroup.onDestroy();
            tabGroupArrayList.remove(tabGroup);
            if (tabGroupArrayList.size() > 0) {
                int indexNext = tabGroupArrayList.size() - 1;
                TabGroup tabGroupNext = tabGroupArrayList.get(indexNext);
                switchTabGroup(tabGroupNext);
            }
        }
    }

    public void removeIndex(TabGroup tabGroup) {
        if (tabGroupArrayList.size() > 0) {
            tabGroupArrayList.remove(tabGroup);
        }
    }

    public void resumeTabGroupExclude(TabGroup exTabGroup) {
        for (TabGroup tabGroup : tabGroupArrayList) {
            if (tabGroup != exTabGroup) {
                tabGroup.onResume();
            }
        }
    }

    public void pauseTabGroupExclude(TabGroup exTabGroup) {
        for (TabGroup tabGroup : tabGroupArrayList) {
            if (tabGroup != exTabGroup) {
                tabGroup.onPause();
            }
        }
    }

    public ArrayList<TabGroup> getList() {
        return tabGroupArrayList;
    }

    public int getTabGroupCount() {
        return tabGroupArrayList.size();
    }

}
