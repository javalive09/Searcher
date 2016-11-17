package peter.util.searcher.activity;

import java.util.ArrayList;

/**
 *
 * Created by peter on 2016/11/17.
 *
 */

public class TabManager {

    private static final int MAX_TAB = 15;
    private ArrayList<Tab> tabArrayList = new ArrayList<>(MAX_TAB);
    private MainActivity mainActivity;
    private int mCurrentTabIndex;

    public TabManager(MainActivity activity) {
        mainActivity = activity;
    }

    public void loadUrl(String url, boolean newTab) {
        loadUrl(url, "", newTab);
    }

    public void loadUrl(String url, String searchWord, boolean newTab) {
        if(tabArrayList.size() == 15) {
            newTab = false;
        }
        if (newTab) {
            Tab tab = new Tab(mainActivity);
            tabArrayList.add(tab);
            setCurrentTab(tabArrayList.size() - 1);
        }
        Tab tab = getCurrentTab();
        tab.loadUrl(url, searchWord, newTab);
    }

    public void setCurrentTab(int index) {
        mCurrentTabIndex = index;
    }

    public Tab getCurrentTab() {
        return tabArrayList.get(mCurrentTabIndex);
    }

}
