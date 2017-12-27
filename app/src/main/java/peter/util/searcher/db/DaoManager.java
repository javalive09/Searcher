package peter.util.searcher.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import peter.util.searcher.Searcher;
import peter.util.searcher.db.dao.DaoMaster;
import peter.util.searcher.db.dao.DaoSession;
import peter.util.searcher.db.dao.FavoriteSearch;
import peter.util.searcher.db.dao.FavoriteSearchDao;
import peter.util.searcher.db.dao.HistorySearch;
import peter.util.searcher.db.dao.HistorySearchDao;
import peter.util.searcher.db.dao.TabData;

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

    public Observable<List<TabData>> queryAllTabData() {
        return Observable.create(subscriber -> {
            final List<TabData> list = mDaoSession.getTabDataDao().queryBuilder().orderDesc(HistorySearchDao.Properties.Time).list();
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public Observable<Integer> queryTabDataGroupCount() {
        return Observable.create(subscriber -> {
            final List<TabData> list = mDaoSession.getTabDataDao().queryBuilder().limit(1).list();
            int count = 0;
            if(list != null) {
                TabData tabData = list.get(0);
                if(tabData != null) {
                    count = tabData.getTabGroupCount();
                }
            }
            subscriber.onNext(count);
            subscriber.onComplete();
        });
    }

}