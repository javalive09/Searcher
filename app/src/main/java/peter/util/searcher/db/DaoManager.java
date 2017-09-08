package peter.util.searcher.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import peter.util.searcher.bean.Bean;
import peter.util.searcher.db.dao.DaoMaster;
import peter.util.searcher.db.dao.DaoSession;
import peter.util.searcher.db.dao.FavoriteSearch;
import peter.util.searcher.db.dao.HistorySearch;

public class DaoManager {

    private DaoMaster.DevOpenHelper mHelper;
    private SQLiteDatabase db;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;

    private DaoManager() {
    }

    private static class SingletonInstance {
        private static final DaoManager INSTANCE = new DaoManager();
    }

    public static DaoManager getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void init(Context context) {
        mHelper = new DaoMaster.DevOpenHelper(context, "searcher_db", null);
        db = mHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(db);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getSession() {
        return mDaoSession;
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

    public List<Bean> queryAllHistory() {
        List<Bean> list = new ArrayList<>();
        List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().loadAll();
        for (HistorySearch historySearch : historySearchList) {
            Bean bean = new Bean();
            bean.name = historySearch.getSearchWord();
            bean.time = historySearch.getTime();
            bean.url = historySearch.getUrl();
            bean.pageNo = historySearch.getPageNo();
            list.add(bean);
        }
        return list;
    }

    public List<Bean> queryAllFavorite() {
        List<Bean> list = new ArrayList<>();
        List<FavoriteSearch> favoriteSearchList = mDaoSession.getFavoriteSearchDao().loadAll();
        for (FavoriteSearch favoriteSearch : favoriteSearchList) {
            Bean bean = new Bean();
            bean.name = favoriteSearch.getSearchWord();
            bean.time = favoriteSearch.getTime();
            bean.url = favoriteSearch.getUrl();
            bean.pageNo = favoriteSearch.getPageNo();
            list.add(bean);
        }

        return list;
    }

    public List<Bean> queryRecentData(int count) {
        List<Bean> list = new ArrayList<>();
        List<HistorySearch> historySearchList = mDaoSession.getHistorySearchDao().queryBuilder().limit(count).list();
        for (HistorySearch historySearch : historySearchList) {
            Bean bean = new Bean();
            bean.name = historySearch.getSearchWord();
            bean.time = historySearch.getTime();
            bean.url = historySearch.getUrl();
            bean.pageNo = historySearch.getPageNo();
            list.add(bean);
        }
        return list;
    }

}