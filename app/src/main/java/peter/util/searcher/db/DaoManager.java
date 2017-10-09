package peter.util.searcher.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import peter.util.searcher.Searcher;
import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.dao.DaoMaster;
import peter.util.searcher.db.dao.DaoSession;
import peter.util.searcher.db.dao.FavoriteSearch;
import peter.util.searcher.db.dao.FavoriteSearchDao;
import peter.util.searcher.db.dao.HistorySearch;
import peter.util.searcher.db.dao.HistorySearchDao;

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

    public long insertHistory(Bean bean) {
        HistorySearch historySearch = new HistorySearch(bean.name, bean.time, bean.url, bean.pageNo);
        return mDaoSession.getHistorySearchDao().insertOrReplace(historySearch);
    }

    public long insertFavorite(Bean bean) {
        FavoriteSearch favoriteSearch = new FavoriteSearch(bean.name, bean.time, bean.url, bean.pageNo);
        return mDaoSession.getFavoriteSearchDao().insertOrReplace(favoriteSearch);
    }

    public void deleteHistory(Bean bean) {
        HistorySearch historySearch = new HistorySearch(bean.name, bean.time, bean.url, bean.pageNo);
        mDaoSession.getHistorySearchDao().delete(historySearch);
    }

    public void deleteFav(Bean bean) {
        FavoriteSearch favoriteSearch = new FavoriteSearch(bean.name, bean.time, bean.url, bean.pageNo);
        mDaoSession.getFavoriteSearchDao().delete(favoriteSearch);
    }

    public Observable<List<Bean>> queryAllHistory() {
        return Observable.create(subscriber -> {
            List<Bean> list = new ArrayList<>();
            List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().queryBuilder().orderDesc(HistorySearchDao.Properties.Time).list();
            for (HistorySearch historySearch : historySearchList) {
                Bean bean = new Bean();
                bean.name = historySearch.getSearchWord();
                bean.time = historySearch.getTime();
                bean.url = historySearch.getUrl();
                bean.pageNo = historySearch.getPageNo();
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public Bean queryBean(String word, String url) {
        List<HistorySearch> list = mDaoSession.getHistorySearchDao().queryBuilder().where(HistorySearchDao.Properties.SearchWord.eq(word)).list();
        Bean bean = new Bean();
        if (list.size() > 0) {
            HistorySearch historySearch = list.get(0);
            bean.pageNo = historySearch.getPageNo();
            bean.url = historySearch.getUrl();
            bean.time = historySearch.getTime();
            bean.name = historySearch.getSearchWord();
        } else {
            bean.url = url;
            bean.name = word;
        }
        return bean;
    }

    public Observable<List<Bean>> queryHistoryLike(String word) {
        return Observable.create(subscriber -> {
            List<Bean> list = new ArrayList<>();
            List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().queryBuilder().
                    orderDesc(HistorySearchDao.Properties.Time).
                    where(HistorySearchDao.Properties.SearchWord.like("%" + word + "%")).list();
            for (HistorySearch historySearch : historySearchList) {
                Bean bean = new Bean();
                bean.name = historySearch.getSearchWord();
                bean.time = historySearch.getTime();
                bean.url = historySearch.getUrl();
                bean.pageNo = historySearch.getPageNo();
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public Observable<List<Bean>> queryFavoriteLike(String word) {
        return Observable.create(subscriber -> {
            List<Bean> list = new ArrayList<>();
            List<FavoriteSearch> favoriteSearchList = mDaoSession.getFavoriteSearchDao().queryBuilder().
                    orderDesc(FavoriteSearchDao.Properties.Time).
                    where(FavoriteSearchDao.Properties.SearchWord.like("%" + word + "%")).list();
            for (FavoriteSearch favoriteSearch : favoriteSearchList) {
                Bean bean = new Bean();
                bean.name = favoriteSearch.getSearchWord();
                bean.time = favoriteSearch.getTime();
                bean.url = favoriteSearch.getUrl();
                bean.pageNo = favoriteSearch.getPageNo();
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }


    public Observable<List<Bean>> queryAllFavorite() {
        return Observable.create(subscriber -> {
            List<Bean> list = new ArrayList<>();
            List<FavoriteSearch> favoriteSearchList = mDaoSession.getFavoriteSearchDao().queryBuilder().orderDesc(FavoriteSearchDao.Properties.Time).list();
            for (FavoriteSearch favoriteSearch : favoriteSearchList) {
                Bean bean = new Bean();
                bean.name = favoriteSearch.getSearchWord();
                bean.time = favoriteSearch.getTime();
                bean.url = favoriteSearch.getUrl();
                bean.pageNo = favoriteSearch.getPageNo();
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

    public Observable<List<Bean>> queryRecentData(int count) {
        return Observable.create(subscriber -> {
            List<Bean> list = new ArrayList<>();
            List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().queryBuilder().orderDesc(HistorySearchDao.Properties.Time).limit(count).list();
            for (HistorySearch historySearch : historySearchList) {
                Bean bean = new Bean();
                bean.name = historySearch.getSearchWord();
                bean.time = historySearch.getTime();
                bean.url = historySearch.getUrl();
                bean.pageNo = historySearch.getPageNo();
                list.add(bean);
            }
            subscriber.onNext(list);
            subscriber.onComplete();
        });
    }

}