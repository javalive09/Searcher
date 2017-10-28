package peter.util.searcher.tab;

import android.view.View;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.TabBean;

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
        return null;
    }

    public String getTitle() {
        return null;
    }

    @Override
    public String getHost() {
        return null;
    }

    public String getUrl() {
        return null;
    }

    @Override
    public TabGroup create(TabBean bean) {
        SearcherTab currentTab = getCurrentTab();
        if (currentTab == null) {//head tab
            currentTab = newTab(bean);
            tabArrayList.add(currentTab);
            mCurrentTabIndex = tabArrayList.size() - 1;
        } else {//body tab
            if (bean.url.startsWith(LOCAL_SCHEMA) || //local url
                    currentTab instanceof LocalViewTab) {//current local tab
                if (currentTab.getUrl().equals(bean.url)) {//same local url
                    return this;
                }
                currentTab = newTab(bean);
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
        return null;
    }

    @Override
    public int getPageNo() {
        return 0;
    }

    public void loadUrl(TabBean bean) {
        getCurrentTab().loadUrl(bean);
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

    private SearcherTab newTab(TabBean bean) {
        if (bean.url.startsWith(LOCAL_SCHEMA)) {
            return newLocalTab(bean).create(bean);
        } else {
            return new WebViewTab(mainActivity).create(bean);
        }
    }

    public void onResume() {
        for (SearcherTab searcherTab : tabArrayList) {
            searcherTab.onResume();
        }
    }

    public void onPause() {
        for (SearcherTab searcherTab : tabArrayList) {
            searcherTab.onPause();
        }
    }


    private LocalViewTab newLocalTab(TabBean bean) {
        Class clazz = mainActivity.getRouterClass(bean.url);
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
        mCurrentTabIndex = index;
        SearcherTab tab = tabArrayList.get(index);
        tab.loadUrl(new TabBean(tab.getSearchWord(), tab.getUrl()));
        checkReloadCurrentTab();
    }

    public void checkReloadCurrentTab() {
        SearcherTab tab = getCurrentTab();
        if (tab instanceof WebViewTab) {
            WebViewTab webViewTab = (WebViewTab) tab;
            if (webViewTab.getView().getProgress() != 100 || webViewTab.getView().getContentHeight() == 0) {
                webViewTab.reload();
            }
        }
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
        peter.util.searcher.tab.Tab currentTab = getCurrentTab();
        if (currentTab instanceof LocalViewTab) {
            return mCurrentTabIndex > 0;
        } else {
            return currentTab.canGoBack() || mCurrentTabIndex > 0;
        }
    }

    public void goBack() {
        peter.util.searcher.tab.Tab currentTab = getCurrentTab();
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
        peter.util.searcher.tab.Tab currentTab = getCurrentTab();
        if (currentTab instanceof WebViewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if (webTabCanGoForward) {
                return true;
            }
        }
        return mCurrentTabIndex < tabArrayList.size() - 1;
    }

    public void goForward() {
        peter.util.searcher.tab.Tab currentTab = getCurrentTab();
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
