package peter.util.searcher.activity;

import android.view.View;

import java.util.ArrayList;

import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public class TabManager {

    private static final int MAX_TAB = 9;
    private ArrayList<TabGroup> tabGroupArrayList = new ArrayList<>(MAX_TAB);
    private MainActivity mainActivity;
    private int mCurrentTabIndex;

    public TabManager(MainActivity activity) {
        mainActivity = activity;
    }

    public void loadUrl(String url, boolean newTab) {
        loadUrl(url, "", newTab);
    }

    public void loadUrl(String url, String searchWord, boolean newTab) {
        if(tabGroupArrayList.size() == MAX_TAB) {
            newTab = false;
        }
        if (newTab) {
            TabGroup tab = new TabGroup(mainActivity);
            tabGroupArrayList.add(tab);
            mCurrentTabIndex = tabGroupArrayList.size() - 1;
        }
        TabGroup tabGroup = getCurrentTabGroup();
        tabGroup.loadUrl(url, searchWord);
    }

    public TabGroup getCurrentTabGroup() {
        return tabGroupArrayList.size() > 0 ? tabGroupArrayList.get(mCurrentTabIndex) : null;
    }

    public void switchTabGroup(TabGroup tabGroup) {
        mCurrentTabIndex = tabGroupArrayList.indexOf(tabGroup);
        View currentTabGroupView = tabGroup.getView();
        pauseTabGroupExclude(tabGroup);
        mainActivity.setCurrentView(currentTabGroupView);
        getCurrentTabGroup().onResume();
        mainActivity.refreshBottomBar();
    }

    public TabGroup getTabGroup(SearcherTab topTab) {
        for(TabGroup tabGroup : tabGroupArrayList) {
            if(tabGroup.containsTab(topTab)) {
                return  tabGroup;
            }
        }
        return null;
    }

    public void removeTabGroup(TabGroup tabGroup) {
        if(tabGroupArrayList.size() > 0) {
            tabGroup.onDestory();
            tabGroupArrayList.remove(tabGroup);
            if(tabGroupArrayList.size() > 0) {
                int indexNext = tabGroupArrayList.size() - 1;
                TabGroup tabGroupNext = tabGroupArrayList.get(indexNext);
                switchTabGroup(tabGroupNext);
            }
        }
    }

    public void removeIndex(TabGroup tabGroup) {
        if(tabGroupArrayList.size() > 0) {
            tabGroupArrayList.remove(tabGroup);
        }
    }

    public void resumeTabGroupExclude(TabGroup exTabGroup) {
        for(TabGroup tabGroup : tabGroupArrayList) {
            if(tabGroup != exTabGroup) {
                tabGroup.onResume();
            }
        }
    }

    public void pauseTabGroupExclude(TabGroup exTabGroup) {
        for(TabGroup tabGroup : tabGroupArrayList) {
            if(tabGroup != exTabGroup) {
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

//    public boolean canGoBack() {
//        TabGroup currentTabGroup = getCurrentTabGroup();
//        if(currentTabGroup.canGoBack()) {
//            return true;
//        }else {
//            return mCurrentTabIndex > 0;
//        }
//    }

//    public void goBack() {
//        TabGroup currentTabGroup = getCurrentTabGroup();
//        if(currentTabGroup.canGoBack()) {
//            currentTabGroup.goBack();
//        }else {
//            if(mCurrentTabIndex > 0) {
//                removeTabGroup(getCurrentTabGroup());
//            }
//        }
//    }

}
