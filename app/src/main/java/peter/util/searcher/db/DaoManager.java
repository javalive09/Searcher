package peter.util.searcher.db;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;
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
import peter.util.searcher.tab.SearcherTab;
import peter.util.searcher.tab.Tab;
import peter.util.searcher.tab.TabGroup;
import peter.util.searcher.tab.WebViewTab;

public class DaoManager {

    private final DaoSession mDaoSession;

    private DaoManager() {
        DaoMaster.DevOpenHelper mHelper = new DaoMaster.DevOpenHelper(Searcher.context, "searcher_db", null);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        DaoMaster mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    private static class SingletonInstance {
        private static final DaoManager INSTANCE = new DaoManager();
    }

    public static DaoManager getInstance() {
        return SingletonInstance.INSTANCE;
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
        mDaoSession.getTabDataDao().getSession().runInTx(() -> {
            mDaoSession.getTabDataDao().deleteAll();
            realSaveTabs();
        });
    }

    public void realSaveTabs() {
        List<TabGroup> tabGroupList = TabGroupManager.getInstance().getList();
        final TabGroup currentTabGroup = TabGroupManager.getInstance().getCurrentTabGroup();
        for (int groupIndex = 0, groupSize = tabGroupList.size(); groupIndex < groupSize; groupIndex++) {
            TabGroup tabGroup = tabGroupList.get(groupIndex);
            ArrayList<SearcherTab> tabs = tabGroup.getTabs();
            for (int tabIndex = 0, tabSize = tabs.size(); tabIndex < tabSize; tabIndex++) {
                SearcherTab tab = tabs.get(tabIndex);
                if (!TextUtils.isEmpty(tab.getUrl())) {
                    TabData tabData;
                    Bundle state = new Bundle(ClassLoader.getSystemClassLoader());
                    if (tab instanceof WebViewTab) {
                        WebViewTab webViewTab = (WebViewTab) tab;
                        webViewTab.getView().saveState(state);
                        tabData = webViewTab.getTabData();
                        tabData.setUrl(webViewTab.getUrl());
                        tabData.setTitle(webViewTab.getTitle());
                        Parcel parcel = Parcel.obtain();
                        parcel.writeBundle(state);
                        tabData.setBundle(parcel.marshall());
                        parcel.recycle();
                    } else {
                        tabData = new TabData();
                        tabData.setUrl(Tab.URL_HOME);
                    }
                    tabData.setTabCount(tabSize);
                    tabData.setTabGroupCount(groupSize);
                    tabData.setIsCurrentTab(tabGroup.getCurrentTab() == tab);
                    tabData.setIsCurrentTabGroup(tabGroup == currentTabGroup);
                    tabData.setGroupTabIndex(groupIndex);
                    tabData.setTabIndex(tabIndex);
                    mDaoSession.getTabDataDao().insert(tabData);
                }
            }
        }
    }

    public void restoreTabs() {
        List<TabData> tabDataList = mDaoSession.getTabDataDao().queryBuilder().list();

        if (tabDataList.size() > 0) {
            final TabData firstTabData = tabDataList.get(0);
            final int groupCount = firstTabData.getTabGroupCount();
            int currentGroupIndex = -1;
            int currentTabIndex = -1;

            SparseArray<TabData[]> groupsArray = new SparseArray<>(groupCount);

            for (TabData tabData : tabDataList) {
                int groupTabIndex = tabData.getGroupTabIndex();

                if (tabData.getIsCurrentTabGroup()) {
                    currentGroupIndex = tabData.getGroupTabIndex();
                    if (currentTabIndex == -1 && tabData.getIsCurrentTab()) {
                        currentTabIndex = tabData.getTabIndex();
                    }
                }

                if (groupsArray.indexOfKey(groupTabIndex) > 0) {
                    TabData[] tabDataArray = groupsArray.get(groupTabIndex);
                    tabDataArray[tabData.getTabIndex()] = tabData;
                } else {
                    TabData[] tabDataArray = new TabData[tabData.getTabCount()];
                    tabDataArray[tabData.getTabIndex()] = tabData;
                    groupsArray.put(tabData.getGroupTabIndex(), tabDataArray);
                }
            }

            for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
                TabData[] tabData = groupsArray.get(groupIndex);
                for (int index = 0; index < tabData.length; index++) {
                    if (index == 0) {
                        TabGroupManager.getInstance().load(tabData[index], true);
                    } else {
                        TabGroupManager.getInstance().createTabGroup(tabData[index], false);
                    }
                }
            }

            TabGroupManager.getInstance().restoreTabPos(currentGroupIndex, currentTabIndex);
        }
    }


}