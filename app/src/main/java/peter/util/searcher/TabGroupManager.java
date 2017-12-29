package peter.util.searcher;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.db.DaoManager;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.TabGroup;

/**
 * 标签管理类
 * Created by peter on 2016/11/17.
 */

public class TabGroupManager {

    public static final int MAX_TAB = 99;
    private ArrayList<TabGroup> tabGroupArrayList = new ArrayList<>();
    private MainActivity mainActivity;
    private int mCurrentTabGroupIndex;
    private SearcherTab homeTab;
    private static volatile TabGroupManager singleton;

    public static TabGroupManager getInstance() {
        if (singleton == null) {
            synchronized (DaoManager.class) {
                if (singleton == null) {
                    singleton = new TabGroupManager();
                }
            }
        }
        return singleton;
    }

    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void clear() {
        singleton = null;
    }

    public void reset() {
        tabGroupArrayList = new ArrayList<>();
        mCurrentTabGroupIndex = -1;
    }

    private TabGroupManager() {
    }

    public SearcherTab getHomeTab() {
        return homeTab;
    }

    public TabGroupManager setHomeTab(SearcherTab homeTab) {
        this.homeTab = homeTab;
        return this;
    }

    public boolean load(TabData tabData, boolean newGroup) {
        boolean suc = createTabGroup(tabData, newGroup);
        if (suc) {
            getCurrentTabGroup().loadUrl(tabData);
        }
        return suc;
    }

    public boolean createTabGroup(TabData tabData, boolean newGroup) {
        if (tabGroupArrayList.size() == MAX_TAB && newGroup) {
            Toast.makeText(Searcher.context, R.string.tabs_max_txt, Toast.LENGTH_LONG).show();
            return false;
        }
        if (newGroup) {
            TabGroup tab = new TabGroup(mainActivity);
            tabGroupArrayList.add(tab);
            mCurrentTabGroupIndex = tabGroupArrayList.size() - 1;
        }
        TabGroup tabGroup = getCurrentTabGroup();
        tabGroup.create(tabData);
        return true;
    }

    public int getCurrentTabIndex() {
        return mCurrentTabGroupIndex;
    }

    public TabGroup getCurrentTabGroup() {
        if (tabGroupArrayList.size() > 0) {
            if (mCurrentTabGroupIndex > -1 && mCurrentTabGroupIndex < tabGroupArrayList.size()) {
                return tabGroupArrayList.get(mCurrentTabGroupIndex);
            }
        }
        return null;
    }

    public void switchTabGroup(TabGroup tabGroup) {
        switchTabGroup(tabGroup, true);
    }

    private void switchTabGroup(TabGroup tabGroup, boolean reload) {
        int index = tabGroupArrayList.indexOf(tabGroup);
        Log.e("peter", "index=" + index);
        Log.e("peter", "mCurrentTabGroupIndex=" + mCurrentTabGroupIndex);
        if (mCurrentTabGroupIndex != index) {
            TabGroup oldTabGroup = getCurrentTabGroup();
            if (oldTabGroup != null) {
                oldTabGroup.onPause();
            }
            mCurrentTabGroupIndex = index;
            getCurrentTabGroup().onResume();
            mainActivity.refreshTitle();
            if (reload) {
                getCurrentTabGroup().reload();
            }
            //switch view
            final View currentTabGroupView = tabGroup.getCurrentTab().getView();
            mainActivity.setCurrentView(currentTabGroupView);
        }
    }

    public void restoreTabPos(int groupIndex, int tabIndex) {
        Log.e("peter", "restoreTabPos");
        TabGroup tabGroup = tabGroupArrayList.get(groupIndex);
        switchTabGroup(tabGroup, false);
        getCurrentTabGroup().setCurrentTab(tabIndex, false);
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
            int removeIndex = tabGroupArrayList.indexOf(tabGroup);
            int nextIndex = 0;
            if (removeIndex < mCurrentTabGroupIndex) {
                nextIndex = mCurrentTabGroupIndex - 1;
            } else if (removeIndex > mCurrentTabGroupIndex) {
                nextIndex = mCurrentTabGroupIndex;
            } else if (removeIndex == mCurrentTabGroupIndex) {
                if (removeIndex == tabGroupArrayList.size() - 1) {//end
                    nextIndex = tabGroupArrayList.size() - 2;
                } else {
                    nextIndex = mCurrentTabGroupIndex;
                }
            }
            tabGroupArrayList.remove(tabGroup);
            if (tabGroupArrayList.size() > 0) {
                TabGroup tabGroupNext = tabGroupArrayList.get(nextIndex);
                switchTabGroup(tabGroupNext);
            }
        }
    }

    public void removeIndex(TabGroup tabGroup) {
        if (tabGroupArrayList.size() > 0) {
            tabGroupArrayList.remove(tabGroup);
        }
    }

    public ArrayList<TabGroup> getList() {
        return tabGroupArrayList;
    }

    public int getTabGroupCount() {
        return tabGroupArrayList.size();
    }

}
