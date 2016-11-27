package peter.util.searcher.activity;

import java.util.ArrayList;

import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public class TabManager {

    private static final int MAX_TAB = 20;
    private ArrayList<TabGroup> tabArrayList = new ArrayList<>(MAX_TAB);
    private MainActivity mainActivity;
    private int mCurrentTabIndex;

    public TabManager(MainActivity activity) {
        mainActivity = activity;
    }

    public void loadUrl(String url, boolean newTab) {
        loadUrl(url, "", newTab);
    }

    public void loadUrl(String url, String searchWord, boolean newTab) {
        if(tabArrayList.size() == MAX_TAB) {
            newTab = false;
        }
        if (newTab) {
            TabGroup tab = new TabGroup(mainActivity);
            tabArrayList.add(tab);
            setCurrentTabGroup(tabArrayList.size() - 1);
        }
        Tab tab = getCurrentTabGroup();
        tab.loadUrl(url, searchWord);
    }

    public void setCurrentTabGroup(int index) {
        mCurrentTabIndex = index;
    }

    public TabGroup getCurrentTabGroup() {
        return tabArrayList.get(mCurrentTabIndex);
    }

    public ArrayList<TabGroup> getList() {
        return tabArrayList;
    }

    public int getTabGroupCount() {
        return tabArrayList.size();
    }

}
