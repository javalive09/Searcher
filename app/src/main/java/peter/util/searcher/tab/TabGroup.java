package peter.util.searcher.tab;

import android.view.View;
import android.webkit.WebView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;

/**
 * 标签组
 * Created by peter on 2016/11/20.
 */

public class TabGroup extends SearcherTab {

    private static final int MAX_TAB = 50;
    private ArrayList<SearcherTab> tabArrayList = new ArrayList<>(MAX_TAB);
    private int mCurrentTabIndex;
    private TabGroup parent;

    public TabGroup(MainActivity activity) {
        super(activity);
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
        return null;
    }

    public String getUrl() {
        return getCurrentTab().getUrl();
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

    public void loadUrl(Bean bean) {
        SearcherTab currentTab = getCurrentTab();
        if (currentTab == null) {//head tab
            currentTab = newTabByUrl(bean.url);
            tabArrayList.add(currentTab);
            mCurrentTabIndex = tabArrayList.size() - 1;
        } else {//body tab
            if (bean.url.startsWith(LOCAL_SCHEMA) || //local url
                    currentTab instanceof LocalViewTab) {//current local tab
                if (currentTab.getUrl().equals(bean.url)) {//same local url
                    return;
                }
                currentTab = newTabByUrl(bean.url);
                int index = mCurrentTabIndex + 1;
                tabArrayList.add(index, currentTab);
                removeTabFromIndeoToEnd(index + 1);
                mCurrentTabIndex = tabArrayList.size() - 1;
            }
        }
        currentTab.loadUrl(bean);
        mainActivity.refreshTitle();
    }

    public ArrayList<SearcherTab> getTabs() {
        return tabArrayList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (SearcherTab tab : tabArrayList) {
            tab.onDestroy();
        }
    }

    private void removeTabFromIndeoToEnd(int index) {
        for (int i = index, size = tabArrayList.size(); i < size; i++) {
            SearcherTab tab = tabArrayList.remove(index);
            tab.onDestroy();
        }
    }

    public boolean containsTab(SearcherTab tab) {
        return tabArrayList.contains(tab);
    }

    private SearcherTab newTabByUrl(String url) {
        if (url.startsWith(LOCAL_SCHEMA)) {
            return newLocalTab(url);
        } else {
            return new WebViewTab(mainActivity);
        }
    }

    public void onResume() {
        for (SearcherTab tab : tabArrayList) {
            tab.onResume();
        }
    }

    public void onPause() {
        for (SearcherTab tab : tabArrayList) {
            tab.onPause();
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
        SearcherTab tab = tabArrayList.get(index);
        tab.loadUrl(new Bean(tab.getSearchWord(), tab.getUrl()));
    }

    public void checkReloadCurrentTab() {
        SearcherTab tab = getCurrentTab();
        if(tab.getView() instanceof WebView) {
            WebView webView = (WebView) tab.getView();
            if(webView.getProgress() != 100 || webView.getContentHeight() == 0) {
                tab.reload();
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
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof LocalViewTab) {
            return mCurrentTabIndex > 0;
        } else {
            return currentTab.canGoBack() || mCurrentTabIndex > 0;
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
        mainActivity.refreshTitle();
    }

    public ArrayList<SearcherTab> getTabArrayList() {
        return new ArrayList<>(tabArrayList);
    }

    public boolean canGoForward() {
        Tab currentTab = getCurrentTab();
        if (currentTab instanceof WebViewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if (webTabCanGoForward) {
                return true;
            }
            if (mCurrentTabIndex < tabArrayList.size() - 1) {
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
