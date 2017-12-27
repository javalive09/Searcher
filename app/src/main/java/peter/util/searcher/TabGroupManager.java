package peter.util.searcher;

import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.TabGroup;

/**
 * 标签管理类
 * Created by peter on 2016/11/17.
 */

public class TabGroupManager {

    public static final int MAX_TAB = 100;
    private final ArrayList<TabGroup> tabGroupArrayList = new ArrayList<>();
    private MainActivity mainActivity;
    private int mCurrentTabGroupIndex;

    private static class SingletonInstance {
        private static final TabGroupManager INSTANCE = new TabGroupManager();
    }

    public static TabGroupManager getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void init(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private TabGroupManager() {}

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
        return tabGroupArrayList.size() > 0 ? tabGroupArrayList.get(mCurrentTabGroupIndex) : null;
    }

    public void switchTabGroup(TabGroup tabGroup) {
        switchTabGroup(tabGroup, true);
    }

    private void switchTabGroup(TabGroup tabGroup, boolean reload) {
        mCurrentTabGroupIndex = tabGroupArrayList.indexOf(tabGroup);
        View currentTabGroupView = tabGroup.getCurrentTab().getView();
        pauseTabGroupExclude(tabGroup);
        mainActivity.setCurrentView(currentTabGroupView);
        getCurrentTabGroup().onResume();
        mainActivity.refreshTitle();
        if (reload) {
            getCurrentTabGroup().reload();
        }
    }

    public void restoreTabPos(int groupIndex, int tabIndex) {
        TabGroup tabGroup = tabGroupArrayList.get(groupIndex);
        switchTabGroup(tabGroup, false);
        getCurrentTabGroup().setCurrentTab(tabIndex);
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

//    public void revertGroup(TabGroup tabGroup, int position) {
//        tabGroupArrayList.add(position, tabGroup);
//        if(position > mCurrentTabGroupIndex) {
//
//        }else if(position < mCurrentTabGroupIndex) {
//            mCurrentTabGroupIndex = mCurrentTabGroupIndex + 1;
//        } else if(mCurrentTabGroupIndex == position) {
//            mCurrentTabGroupIndex = position;
//        }
//    }


    public void removeIndex(TabGroup tabGroup) {
        if (tabGroupArrayList.size() > 0) {
            tabGroupArrayList.remove(tabGroup);
        }
    }

    public void resumeTabGroupExclude(TabGroup exTabGroup) {
        ArrayList<TabGroup> list = new ArrayList<>(tabGroupArrayList);
        for (TabGroup tabGroup : list) {
            if (tabGroup != exTabGroup) {
                tabGroup.onResume();
            }
        }
    }

    public void pauseTabGroupExclude(TabGroup exTabGroup) {
        ArrayList<TabGroup> list = new ArrayList<>(tabGroupArrayList);
        for (TabGroup tabGroup : list) {
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
