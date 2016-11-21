package peter.util.searcher.tab;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

import peter.util.searcher.activity.MainActivity;

/**
 *
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
        if(currentTab == null) {
            currentTab = newTabByUrl(url);
            tabArrayList.add(currentTab);
            mCurrentTabIndex = tabArrayList.size() - 1;
        }else {
            if(!url.startsWith(LOCAL_SCHEMA)) {//web url
                if(currentTab instanceof WebviewTab) {//web tab
                    currentTab.loadUrl(url, searchWord);
                    return;
                }else {//local tab
                    try {
                        currentTab = newTabByUrl(url);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            int index = mCurrentTabIndex + 1;
            tabArrayList.add(index, currentTab);
            mCurrentTabIndex = index;
        }
        currentTab.loadUrl(url, searchWord);
    }

    private SearcherTab newTabByUrl(String url) {
        if (url.startsWith(LOCAL_SCHEMA)) {
            try {
                return newLocalTab(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return new WebviewTab(mainActivity);
        }
        return null;
    }

    private LocalViewTab newLocalTab(String url) throws Exception {
        Class clazz = mainActivity.getRounterClass(url);
        Constructor localConstructor = clazz.getConstructor(new Class[]{MainActivity.class});
        return (LocalViewTab) localConstructor.newInstance(new Object[]{mainActivity});
    }


    public void setCurrentTab(int index) {
        mCurrentTabIndex = index;
        Tab tab = tabArrayList.get(index);
        tab.loadUrl(tab.getUrl(), tab.getSearchWord());
    }

    public SearcherTab getCurrentTab() {
        if(tabArrayList.size() > 0) {
            return tabArrayList.get(mCurrentTabIndex);
        }else {
            return null;
        }
    }


    public void reload() {
        setCurrentTab(mCurrentTabIndex);
    }

    public boolean canGoBack() {
        Tab currentTab = getCurrentTab();
        if(currentTab instanceof LocalViewTab) {
            return mCurrentTabIndex > 0;
        }else {
            boolean webTabCanGoBack = currentTab.canGoBack();
            if(webTabCanGoBack) {
                return true;
            }else {
                return mCurrentTabIndex > 0;
            }
        }
    }

    public void goBack() {
        Tab currentTab = getCurrentTab();
        if(currentTab instanceof LocalViewTab) {
            setCurrentTab(mCurrentTabIndex - 1);
        }else {
            boolean webTabCanGoBack = currentTab.canGoBack();
            if(webTabCanGoBack) {
                currentTab.goBack();
            }else if(mCurrentTabIndex > 0){
                setCurrentTab(mCurrentTabIndex - 1);
            }
        }
    }

    public boolean canGoForward() {
        Tab currentTab = getCurrentTab();
        if(currentTab instanceof WebviewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if(webTabCanGoForward) {
                return true;
            }else if(mCurrentTabIndex < tabArrayList.size() - 1){
                return true;
            }else {
                return false;
            }
        }else {
            return mCurrentTabIndex < tabArrayList.size() - 1;
        }
    }

    public void goForward() {
        Tab currentTab = getCurrentTab();
        if(currentTab instanceof WebviewTab) {
            boolean webTabCanGoForward = currentTab.canGoForward();
            if(webTabCanGoForward) {
                currentTab.goForward();
            }else if(mCurrentTabIndex < tabArrayList.size() - 1){
                setCurrentTab(mCurrentTabIndex + 1);
            }
        }else {
            setCurrentTab(mCurrentTabIndex + 1);
        }
    }


}
