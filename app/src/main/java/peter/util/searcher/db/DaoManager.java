package peter.util.searcher.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import peter.util.searcher.Searcher;
import peter.util.searcher.TabGroupManager;
import peter.util.searcher.db.dao.DaoMaster;
import peter.util.searcher.db.dao.DaoSession;
import peter.util.searcher.db.dao.FavoriteSearch;
import peter.util.searcher.db.dao.FavoriteSearchDao;
import peter.util.searcher.db.dao.HistorySearch;
import peter.util.searcher.db.dao.HistorySearchDao;
import peter.util.searcher.db.dao.TabData;
import peter.util.searcher.tab.HomeTab;
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.tab.WebViewTab;
import peter.util.searcher.utils.UrlUtils;

public class DaoManager {

    private final DaoSession mDaoSession;
    private static volatile DaoManager singleton;

    private DaoManager() {
        DaoMaster.DevOpenHelper mHelper = new DaoMaster.DevOpenHelper(Searcher.context, "searcher_db", null);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public static DaoManager getInstance() {
        if (singleton == null) {
            synchronized (DaoManager.class) {
                if (singleton == null) {
                    singleton = new DaoManager();
                }
            }
        }
        return singleton;
    }

    public long insertHistory(TabData bean) {
        HistorySearch historySearch = new HistorySearch(bean.getTitle(), bean.getTime(), bean.getUrl(), bean.getPageNo());
        return mDaoSession.getHistorySearchDao().insertOrReplace(historySearch);
    }

    public long insertFavorite(TabData bean) {
        FavoriteSearch favoriteSearch = new FavoriteSearch(bean.getTitle(), bean.getTime(), bean.getUrl(), bean.getPageNo());
        return mDaoSession.getFavoriteSearchDao().insertOrReplace(favoriteSearch);
    }

    public void deleteHistory(TabData bean) {
        HistorySearch historySearch = new HistorySearch(bean.getTitle(), bean.getTime(), bean.getUrl(), bean.getPageNo());
        mDaoSession.getHistorySearchDao().delete(historySearch);
    }

    public void deleteFav(TabData bean) {
        FavoriteSearch favoriteSearch = new FavoriteSearch(bean.getTitle(), bean.getTime(), bean.getUrl(), bean.getPageNo());
        mDaoSession.getFavoriteSearchDao().delete(favoriteSearch);
    }

    public Observable<List<TabData>> queryAllHistory() {
        return Observable.create(subscriber -> {
            List<TabData> list = new ArrayList<>();
            List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().queryBuilder().orderDesc(HistorySearchDao.Properties.Time).list();
            for (HistorySearch historySearch : historySearchList) {
                TabData bean = new TabData();
                bean.setTitle(historySearch.getSearchWord());
                bean.setTime(historySearch.getTime());
                bean.setUrl(historySearch.getUrl());
                bean.setPageNo(historySearch.getPageNo());
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public TabData queryBean(String word, String url) {
        List<HistorySearch> list = mDaoSession.getHistorySearchDao().queryBuilder().where(HistorySearchDao.Properties.SearchWord.eq(word)).list();
        TabData bean = new TabData();
        if (list.size() > 0) {
            HistorySearch historySearch = list.get(0);
            bean.setPageNo(historySearch.getPageNo());
            bean.setUrl(historySearch.getUrl());
            bean.setTime(historySearch.getTime());
            bean.setTitle(historySearch.getSearchWord());
        } else {
            bean.setUrl(url);
            bean.setTitle(word);
        }
        return bean;
    }

    public Observable<List<TabData>> queryHistoryLike(String word) {
        return Observable.create(subscriber -> {
            List<TabData> list = new ArrayList<>();
            List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().queryBuilder().
                    orderDesc(HistorySearchDao.Properties.Time).
                    where(HistorySearchDao.Properties.SearchWord.like("%" + word + "%")).list();
            for (HistorySearch historySearch : historySearchList) {
                TabData bean = new TabData();
                bean.setTitle(historySearch.getSearchWord());
                bean.setTime(historySearch.getTime());
                bean.setUrl(historySearch.getUrl());
                bean.setPageNo(historySearch.getPageNo());
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public Observable<List<TabData>> queryFavoriteLike(String word) {
        return Observable.create(subscriber -> {
            List<TabData> list = new ArrayList<>();
            List<FavoriteSearch> favoriteSearchList = mDaoSession.getFavoriteSearchDao().queryBuilder().
                    orderDesc(FavoriteSearchDao.Properties.Time).
                    where(FavoriteSearchDao.Properties.SearchWord.like("%" + word + "%")).list();
            for (FavoriteSearch favoriteSearch : favoriteSearchList) {
                TabData bean = new TabData();
                bean.setTitle(favoriteSearch.getSearchWord());
                bean.setTime(favoriteSearch.getTime());
                bean.setUrl(favoriteSearch.getUrl());
                bean.setPageNo(favoriteSearch.getPageNo());
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }


    public Observable<List<TabData>> queryAllFavorite() {
        return Observable.create(subscriber -> {
            List<TabData> list = new ArrayList<>();
            List<FavoriteSearch> favoriteSearchList = mDaoSession.getFavoriteSearchDao().queryBuilder().orderDesc(FavoriteSearchDao.Properties.Time).list();
            for (FavoriteSearch favoriteSearch : favoriteSearchList) {
                TabData bean = new TabData();
                bean.setTitle(favoriteSearch.getSearchWord());
                bean.setTime(favoriteSearch.getTime());
                bean.setUrl(favoriteSearch.getUrl());
                bean.setPageNo(favoriteSearch.getPageNo());
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public Observable<List<TabData>> queryRecentData(int count) {
        return Observable.create(subscriber -> {
            List<TabData> list = new ArrayList<>();
            List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().queryBuilder().orderDesc(HistorySearchDao.Properties.Time).limit(count).list();
            for (HistorySearch historySearch : historySearchList) {
                TabData bean = new TabData();
                bean.setTitle(historySearch.getSearchWord());
                bean.setTime(historySearch.getTime());
                bean.setUrl(historySearch.getUrl());
                bean.setPageNo(historySearch.getPageNo());
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public void deleteTabData(TabData tabData) {
        Observable.just(tabData).subscribeOn(Schedulers.io()).subscribe(tabData1 -> mDaoSession.getTabDataDao().delete(tabData));
    }

    public void saveTabs() {
        if (cacheGroupCount > 0) {//party load
            List<TabGroup> tabGroupList = TabGroupManager.getInstance().getList();
            for (int groupIndex = 0, groupSize = tabGroupList.size(); groupIndex < groupSize; groupIndex++) {
                TabGroup tabGroup = tabGroupList.get(groupIndex);
                ArrayList<SearcherTab> tabs = tabGroup.getTabs();
                for (int tabIndex = 0, tabSize = tabs.size(); tabIndex < tabSize; tabIndex++) {
                    SearcherTab tab = tabs.get(tabIndex);
                    if (!TextUtils.isEmpty(tab.getUrl())) {
                        TabData tabData = tab.getTabData();
                        Bundle state = new Bundle(ClassLoader.getSystemClassLoader());
                        if (tab instanceof WebViewTab) {
                            WebViewTab webViewTab = (WebViewTab) tab;
                            webViewTab.getView().saveState(state);
                            if (state.size() > 0) {
                                Parcel parcel = Parcel.obtain();
                                parcel.writeBundle(state);
                                tabData.setBundle(parcel.marshall());
                                parcel.recycle();
                            }
                        }
                        tabData.setUrl(tab.getUrl());
                        tabData.setTitle(tab.getTitle());
                        tabData.setTabCount(tabSize);
                        tabData.setTabGroupCount(cacheGroupCount);//size
                        tabData.setIsCurrentTab(tabGroup.getCurrentTab() == tab);
                        tabData.setIsCurrentTabGroup(true);
                        tabData.setGroupTabIndex(currentGroupIndex);
                        tabData.setTabIndex(tabIndex);
                        mDaoSession.getTabDataDao().insertOrReplace(tabData);
                    }
                }
            }
        } else {//all load
            mDaoSession.getTabDataDao().getSession().runInTx(() -> {
//                mDaoSession.getTabDataDao().deleteAll();
                List<TabGroup> tabGroupList = TabGroupManager.getInstance().getList();
                final TabGroup currentTabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
                for (int groupIndex = 0, groupSize = tabGroupList.size(); groupIndex < groupSize; groupIndex++) {
                    TabGroup tabGroup = tabGroupList.get(groupIndex);
                    ArrayList<SearcherTab> tabs = tabGroup.getTabs();
                    for (int tabIndex = 0, tabSize = tabs.size(); tabIndex < tabSize; tabIndex++) {
                        SearcherTab tab = tabs.get(tabIndex);
                        if (!TextUtils.isEmpty(tab.getUrl())) {
                            TabData tabData = tab.getTabData();
                            Bundle state = new Bundle(ClassLoader.getSystemClassLoader());
                            if (tab instanceof WebViewTab) {
                                WebViewTab webViewTab = (WebViewTab) tab;
                                webViewTab.getView().saveState(state);
                                if (state.size() > 0) {
                                    Parcel parcel = Parcel.obtain();
                                    parcel.writeBundle(state);
                                    tabData.setBundle(parcel.marshall());
                                    parcel.recycle();
                                }
                            }else if(tab instanceof HomeTab) {
                                tabData.setId(null);
                            }
                            tabData.setUrl(tab.getUrl());
                            tabData.setTitle(tab.getTitle());
                            tabData.setTabCount(tabSize);
                            tabData.setTabGroupCount(groupSize);
                            tabData.setIsCurrentTab(tabGroup.getCurrentTab() == tab);
                            tabData.setIsCurrentTabGroup(tabGroup == currentTabGroup);
                            tabData.setGroupTabIndex(groupIndex);
                            tabData.setTabIndex(tabIndex);
                            Log.e("peter", "id =" + tabData.getId());
                            mDaoSession.getTabDataDao().insertOrReplace(tabData);
                        }
                    }
                }
            });
        }
    }

    private SparseArray<TabData[]> groupsArray = null;
    private int cacheGroupCount = -1;
    private int currentGroupIndex = -1;
    private int currentTabIndex = -1;

    public void restoreCurrentTabs() {
        List<TabData> tabDataList = mDaoSession.getTabDataDao().queryBuilder().list();
        if (tabDataList.size() > 0) {
            final TabData firstTabData = tabDataList.get(0);
            cacheGroupCount = firstTabData.getTabGroupCount();
            groupsArray = new SparseArray<>(cacheGroupCount);
            for (TabData tabData : tabDataList) {
                int groupTabIndex = tabData.getGroupTabIndex();

                if (tabData.getIsCurrentTabGroup()) {
                    currentGroupIndex = tabData.getGroupTabIndex();
                    if (currentTabIndex == -1 && tabData.getIsCurrentTab()) {
                        currentTabIndex = tabData.getTabIndex();
                    }
                }

                if (groupsArray.indexOfKey(groupTabIndex) < 0) {//no key
                    TabData[] tabDataArray = new TabData[tabData.getTabCount()];
                    tabDataArray[tabData.getTabIndex()] = tabData;
                    groupsArray.put(tabData.getGroupTabIndex(), tabDataArray);
                } else {
                    TabData[] tabDataArray = groupsArray.get(groupTabIndex);
                    tabDataArray[tabData.getTabIndex()] = tabData;
                }
            }

            //load current group
            TabData[] tabData = groupsArray.get(currentGroupIndex);
            for (int index = 0; index < tabData.length; index++) {
                TabGroupManager.getInstance().createTabGroup(tabData[index], index == 0);
            }

            TabGroupManager.getInstance().restoreTabPos(0, currentTabIndex);
        }
    }

    public void restoreAllTabs() {
        if(cacheGroupCount > 0) {
            TabGroupManager.getInstance().reset();
            for (int groupIndex = 0; groupIndex < cacheGroupCount; groupIndex++) {
                TabData[] tabData = groupsArray.get(groupIndex);
                for (int index = 0; index < tabData.length; index++) {
                    TabGroupManager.getInstance().createTabGroup(tabData[index], index == 0);
                }
            }
            TabGroupManager.getInstance().restoreTabPos(currentGroupIndex, currentTabIndex);
            groupsArray = null;
            currentGroupIndex = -1;
            currentTabIndex = -1;
            cacheGroupCount = -1;
        }
    }

    public void realRestoreAllTabs() {
        List<TabData> tabDataList = mDaoSession.getTabDataDao().queryBuilder().list();
        if (tabDataList.size() > 0) {
            final TabData firstTabData = tabDataList.get(0);
            int cacheGroupCount = firstTabData.getTabGroupCount();
            groupsArray = new SparseArray<>(cacheGroupCount);
            int currentGroupIndex = -1;
            int currentTabIndex = -1;
            for (TabData tabData : tabDataList) {
                int groupTabIndex = tabData.getGroupTabIndex();

                if (tabData.getIsCurrentTabGroup()) {
                    currentGroupIndex = tabData.getGroupTabIndex();
                    if (currentTabIndex == -1 && tabData.getIsCurrentTab()) {
                        currentTabIndex = tabData.getTabIndex();
                    }
                }

                if (groupsArray.indexOfKey(groupTabIndex) < 0) {//no key
                    TabData[] tabDataArray = new TabData[tabData.getTabCount()];
                    tabDataArray[tabData.getTabIndex()] = tabData;
                    groupsArray.put(tabData.getGroupTabIndex(), tabDataArray);
                } else {
                    TabData[] tabDataArray = groupsArray.get(groupTabIndex);
                    tabDataArray[tabData.getTabIndex()] = tabData;
                }
            }

            TabGroupManager.getInstance().reset();
            for (int groupIndex = 0; groupIndex < cacheGroupCount; groupIndex++) {
                TabData[] tabData = groupsArray.get(groupIndex);
                for (int index = 0; index < tabData.length; index++) {
                    TabGroupManager.getInstance().createTabGroup(tabData[index], index == 0);
                }
            }
            TabGroupManager.getInstance().restoreTabPos(currentGroupIndex, currentTabIndex);
        }
    }

    public int getCacheGroupCount() {
        return cacheGroupCount;
    }

    public void clear() {
        singleton = null;
    }


}