package peter.util.searcher.tab;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import peter.util.searcher.activity.MainActivity;

/**
 * Created by peter on 2016/11/20.
 */

public class TabGroup extends SearcherTab {

    private static final int MAX_TAB = 50;
    private ArrayList<SearcherTab> tabArrayList = new ArrayList<>(MAX_TAB);
    private int mCurrentTabIndex;

    public TabGroup(MainActivity activity) {
        super(activity);
    }

    public String getTitle() {
        return null;
    }

    public String getUrl() {
        return null;
    }

    public void loadUrl(String url, String searchWord) {
        SearcherTab currentTab = getCurrentTab();
        if (currentTab == null) {//head tab
            currentTab = newTabByUrl(url);
            tabArrayList.add(currentTab);
            mCurrentTabIndex = tabArrayList.size() - 1;
        } else {//body tab
            if (url.startsWith(LOCAL_SCHEMA) || //local url
                    currentTab instanceof LocalViewTab) {//current local tab
                currentTab = newLocalTab(url);
                int index = mCurrentTabIndex + 1;
                tabArrayList.add(index, currentTab);
                removeTabFromIndeoToEnd(index + 1);
            }
        }
        currentTab.loadUrl(url, searchWord);
        mainActivity.refreshBottomBar();
    }

    private void removeTabFromIndeoToEnd(int index) {
        for (int i = index, size = tabArrayList.size(); i < size; i++) {
            tabArrayList.remove(index);
        }
    }

    private SearcherTab newTabByUrl(String url) {
        if (url.startsWith(LOCAL_SCHEMA)) {
            return newLocalTab(url);
        } else {
            return new WebviewTab(mainActivity);
        }
    }

    private LocalViewTab newLocalTab(String url) {
        Class clazz = mainActivity.getRounterClass(url);
        LocalViewTab tab = null;
        try {
            Constructor localConstructor = clazz.getConstructor(new Class[]{MainActivity.class});
            tab = (LocalViewTab) localConstructor.newInstance(new Object[]{mainActivity});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tab;
    }


    public void setCurrentTab(int index) {
        mCurrentTabIndex = index;
        Tab tab = tabArrayList.get(index);
        tab.loadUrl(tab.getUrl(), tab.getSearchWord());
    }

    public SearcherTab getCurrentTab() {
        if (tabArrayList.size() > 0) {
            return tabArrayList.get(mCurrentTabIndex);
        } else {
            return null;
        }
    }


    public void reload() {
        setCurrentTab(mCurrentTabIndex);
    }

    public boolean canGoBack() {
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof LocalViewTab) {
            return mCurrentTabIndex > 0;
        } else {
            boolean webTabCanGoBack = currentTab.canGoBack();
            if (webTabCanGoBack) {
                return true;
            } else {
                return mCurrentTabIndex > 0;
            }
        }
    }

    public void goBack() {
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof LocalViewTab) {
            setCurrentTab(mCurrentTabIndex - 1);
        } else {
            boolean webTabCanGoBack = currentTab.canGoBack();
            if (webTabCanGoBack) {
                currentTab.goBack();
            } else if (mCurrentTabIndex > 0) {
                setCurrentTab(mCurrentTabIndex - 1);
            }
        }
        mainActivity.refreshBottomBar();
    }

    public boolean canGoForward() {
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof WebviewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if (webTabCanGoForward) {
                return true;
            } else if (mCurrentTabIndex < tabArrayList.size() - 1) {
                return true;
            } else {
                return false;
            }
        } else {
            return mCurrentTabIndex < tabArrayList.size() - 1;
        }
    }

    public void goForward() {
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof WebviewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if (webTabCanGoForward) {
                currentTab.goForward();
            } else if (mCurrentTabIndex < tabArrayList.size() - 1) {
                setCurrentTab(mCurrentTabIndex + 1);
            }
        } else {
            setCurrentTab(mCurrentTabIndex + 1);
        }
        mainActivity.refreshBottomBar();
    }


}
