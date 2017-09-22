package peter.util.searcher;

import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

import peter.util.searcher.activity.MainActivity;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.TabGroup;

/**
 * 标签管理类
 * Created by peter on 2016/11/17.
 */

public class TabManager {

    private static final int MAX_TAB = 50;
    private ArrayList<TabGroup> tabGroupArrayList = new ArrayList<>(MAX_TAB);
    private MainActivity mainActivity;
    private int mCurrentTabIndex;

    public TabManager(MainActivity activity) {
        mainActivity = activity;
    }

    public void loadUrl(Bean bean, boolean newTab) {
        if (tabGroupArrayList.size() == MAX_TAB) {
            newTab = false;
        }
        if (newTab) {
            TabGroup tab = new TabGroup(mainActivity);
            tabGroupArrayList.add(tab);
            mCurrentTabIndex = tabGroupArrayList.size() - 1;
        }
        TabGroup tabGroup = getCurrentTabGroup();
        tabGroup.loadUrl(bean);
    }

    public TabGroup getCurrentTabGroup() {
        return tabGroupArrayList.size() > 0 ? tabGroupArrayList.get(mCurrentTabIndex) : null;
    }

    public void switchTabGroup(int index) {
        TabGroup tabGroup = tabGroupArrayList.get(index);
        switchTabGroup(tabGroup);
    }

    public void switchTabGroup(TabGroup tabGroup) {
        mCurrentTabIndex = tabGroupArrayList.indexOf(tabGroup);
        View currentTabGroupView = tabGroup.getView();
        pauseTabGroupExclude(tabGroup);
        mainActivity.setCurrentView(currentTabGroupView);
        getCurrentTabGroup().onResume();
        mainActivity.refreshTitle();
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
            tabGroupArrayList.remove(tabGroup);
            if (tabGroupArrayList.size() > 0) {
                int indexNext = tabGroupArrayList.size() - 1;
                TabGroup tabGroupNext = tabGroupArrayList.get(indexNext);
                switchTabGroup(tabGroupNext);
            }
        }
    }

    public void removeIndex(TabGroup tabGroup) {
        if (tabGroupArrayList.size() > 0) {
            tabGroupArrayList.remove(tabGroup);
        }
    }

    public void resumeTabGroupExclude(TabGroup exTabGroup) {
        for (TabGroup tabGroup : tabGroupArrayList) {
            if (tabGroup != exTabGroup) {
                tabGroup.onResume();
            }
        }
    }

    public void pauseTabGroupExclude(TabGroup exTabGroup) {
        for (TabGroup tabGroup : tabGroupArrayList) {
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

    public String getSaveState() {
        JsonArray tabGroups = new JsonArray();
        TabGroup currentTabGroup = getCurrentTabGroup();
        for (TabGroup tabGroup : tabGroupArrayList) {
            JsonObject tabGroupJson = new JsonObject();
            if (tabGroup == currentTabGroup) {
                tabGroupJson.addProperty("currentTabGroup", true);
            } else {
                tabGroupJson.addProperty("currentTabGroup", false);
            }
            ArrayList<SearcherTab> groupTabs = tabGroup.getTabArrayList();
            JsonArray tabs = new JsonArray();
            SearcherTab currentTab = tabGroup.getCurrentTab();
            for (SearcherTab tab : groupTabs) {
                JsonObject tabJson = new JsonObject();
                if (tab == currentTab) {
                    tabJson.addProperty("currentTab", true);
                } else {
                    tabJson.addProperty("currentTab", false);
                }
                tabJson.addProperty("url", tab.getUrl());
                tabJson.addProperty("searchWord", tab.getSearchWord());
                tabs.add(tabJson);
            }
            tabGroupJson.add("tabs", tabs);
            tabGroups.add(tabGroupJson);
        }
        return tabGroups.toString();
    }

    public void restoreState(String str) {
        JsonParser parser = new JsonParser();
        JsonElement tabGroupsJsonElement = parser.parse(str);
        JsonArray tabGroups = tabGroupsJsonElement.getAsJsonArray();

        TabGroup currentTabGroup = null;
        for (int i = 0, size = tabGroups.size(); i < size; i++) {
            JsonObject tabGroupJson = tabGroups.get(i).getAsJsonObject();
            boolean isCurrentTabGroup = tabGroupJson.getAsJsonPrimitive("currentTabGroup").getAsBoolean();
            JsonArray tabs = tabGroupJson.getAsJsonArray("tabs");
            int currentIndex = -1;
            for (int j = 0, len = tabs.size(); j < len; j++) {
                JsonObject tabJson = tabs.get(j).getAsJsonObject();
                String url = tabJson.get("url").getAsString();
                String searchWord = tabJson.get("searchWord").getAsString();
                boolean isCurrentTab = tabJson.get("currentTab").getAsBoolean();
                if (isCurrentTab) {
                    currentIndex = j;
                }
                if (j == 0) {
                    loadUrl(new Bean(url, searchWord), true);
                } else {
                    loadUrl(new Bean(url, searchWord), false);
                }
                if (j == len - 1) {
                    getCurrentTabGroup().setCurrentTab(currentIndex);
                }
            }
            if (isCurrentTabGroup) {
                currentTabGroup = getCurrentTabGroup();
            }
        }
        if (currentTabGroup != null) {
            switchTabGroup(currentTabGroup);
        }
    }

}
