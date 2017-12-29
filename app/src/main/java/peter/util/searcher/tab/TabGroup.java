package peter.util.searcher.tab;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import peter.util.searcher.TabGroupManager;
import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.db.dao.TabData;

/**
 * 标签组
 * Created by peter on 2016/11/20.
 */

public class TabGroup extends SearcherTab {

    private final ArrayList<SearcherTab> tabArrayList = new ArrayList<>(2);
    private int mCurrentTabIndex;
    private TabGroup parent;
    private final Class[] classes;
    private final Object[] activityObject;

    public TabGroup(MainActivity activity) {
        super(activity);
        classes = new Class[]{MainActivity.class};
        activityObject = new Object[]{mainActivity};
    }

    @Override
    public View getView() {
        return getCurrentTab().getView();
    }

    public String getTitle() {
        return getCurrentTab().getTitle();
    }

    @Override
    public String getHost() {
        return getCurrentTab().getHost();
    }

    public String getUrl() {
        return getCurrentTab().getUrl();
    }

    @Override
    public TabGroup create(TabData tabData) {
        SearcherTab currentTab = getCurrentTab();
        if (currentTab == null) {//head tab
            currentTab = newTab(tabData);
            tabArrayList.add(currentTab);
            mCurrentTabIndex = tabArrayList.size() - 1;
        } else {//body tab
            if (tabData.getUrl().startsWith(LOCAL_SCHEMA) || //local url
                    currentTab instanceof LocalViewTab) {//current local tab
                if (currentTab.getUrl().equals(tabData.getUrl())) {//same local url
                    return this;
                }
                currentTab = newTab(tabData);
                int index = mCurrentTabIndex + 1;
                tabArrayList.add(index, currentTab);
                removeTabFromIndexToEnd(index + 1);
                mCurrentTabIndex = tabArrayList.size() - 1;
            }
        }
        return this;
    }

    public TabGroup getParent() {
        return parent;
    }

    public void setParent(TabGroup parent) {
        this.parent = parent;
    }

    @Override
    public String getSearchWord() {
        return getCurrentTab().getSearchWord();
    }

    @Override
    public int getPageNo() {
        return getCurrentTab().getPageNo();
    }

    public void loadUrl(TabData tabData) {
        getCurrentTab().loadUrl(tabData);
        mainActivity.refreshTitle();
    }

    public ArrayList<SearcherTab> getTabs() {
        return tabArrayList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (SearcherTab searcherTab : tabArrayList) {
            searcherTab.onDestroy();
        }
    }

    private void removeTabFromIndexToEnd(int index) {
        for (int i = index, size = tabArrayList.size(); i < size; i++) {
            SearcherTab tab = tabArrayList.remove(index);
            tab.onDestroy();
        }
    }

    public boolean containsTab(SearcherTab tab) {
        return tabArrayList.contains(tab);
    }

    private SearcherTab newTab(TabData tabData) {
        if (tabData.getUrl().startsWith(LOCAL_SCHEMA)) {
            if(TextUtils.equals(tabData.getUrl(), URL_HOME)) {
                if(TabGroupManager.getInstance().getHomeTab() == null) {
                    TabGroupManager.getInstance().setHomeTab(newLocalTab(tabData).create(tabData));
                }
                return TabGroupManager.getInstance().getHomeTab();
            }
            return newLocalTab(tabData).create(tabData);
        } else {
            return new WebViewTab(mainActivity).create(tabData);
        }
    }

    public void onResume() {
        getCurrentTab().onResume();
    }

    public void onPause() {
        getCurrentTab().onPause();
    }

    private LocalViewTab newLocalTab(TabData bean) {
        Class clazz = mainActivity.getRouterClass(bean.getUrl());
        LocalViewTab tab = null;
        try {
            @SuppressWarnings("unchecked")
            Constructor localConstructor = clazz.getConstructor(classes);
            tab = (LocalViewTab) localConstructor.newInstance(activityObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tab;
    }

    public void setCurrentTab(int index) {
        Log.e("peter", "setCurrentTab index=" + index);
        Log.e("peter", "setCurrentTab mCurrentTabIndex=" + mCurrentTabIndex);

        setCurrentTab(index, false);
    }

    public void setCurrentTab(int index, boolean reload) {
        mCurrentTabIndex = index;
        SearcherTab tab = tabArrayList.get(index);
        tab.loadUrl(tab.getTabData());
        if(reload) {
            reload();
        }
        mainActivity.refreshTitle();
    }

    public SearcherTab getCurrentTab() {
        if (tabArrayList.size() > 0) {
            return tabArrayList.get(mCurrentTabIndex);
        } else {
            return null;
        }
    }

    public void reload() {
        final SearcherTab tab = getCurrentTab();
        if (tab instanceof WebViewTab) {
            WebViewTab webViewTab = (WebViewTab) tab;
            if (webViewTab.getView().getProgress() != 100 || webViewTab.getView().getContentHeight() == 0) {
                webViewTab.reload();
            }
        }
    }

    public boolean canGoBack() {
        final SearcherTab currentTab = getCurrentTab();
        if (currentTab instanceof LocalViewTab) {
            return mCurrentTabIndex > 0;
        } else {
            return currentTab.canGoBack() || mCurrentTabIndex > 0;
        }
    }

    public void goBack() {
        final SearcherTab currentTab = getCurrentTab();
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
        mainActivity.refreshTitle();
    }

    public boolean canGoForward() {
        final SearcherTab currentTab = getCurrentTab();
        if (currentTab instanceof WebViewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if (webTabCanGoForward) {
                return true;
            }
        }
        return mCurrentTabIndex < tabArrayList.size() - 1;
    }

    public void goForward() {
        final SearcherTab currentTab = getCurrentTab();
        if (currentTab instanceof WebViewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if (webTabCanGoForward) {
                currentTab.goForward();
            } else if (mCurrentTabIndex < tabArrayList.size() - 1) {
                setCurrentTab(mCurrentTabIndex + 1);
            }
        } else {
            setCurrentTab(mCurrentTabIndex + 1);
        }
        mainActivity.refreshTitle();
    }

}
