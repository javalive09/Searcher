package peter.util.searcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SqliteHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "search.db";
    private static final String TABLE_HISTORY = "history";
    private static final String TABLE_FAVORITE = "favorite";
    private static final int version = 1;
    private static final int LIMIT = 5;
    private static SqliteHelper helper;

    private SqliteHelper(Context context) {
        super(context, DB_NAME, null, version);
    }

    public static SqliteHelper instance(Context context) {
        synchronized (SqliteHelper.class) {
            if (helper == null) {
                helper = new SqliteHelper(context.getApplicationContext());
            }
        }
        return helper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists " + TABLE_HISTORY + " (historyId integer primary key autoincrement, time integer, name varchar(20), show integer, url varchar(100))");
        db.execSQL("create table if not exists " + TABLE_FAVORITE +" (favId integer primary key autoincrement, time integer, name varchar(20), url varchar(100))");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public synchronized void insertHistory(Bean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        Cursor cursor = db.rawQuery("select * from "+ TABLE_HISTORY +" where name=?", new String[] { bean.name });

        if(cursor != null) {
            if(cursor.getCount() == 0) {//没有记录
                values.put("time", bean.time);
                values.put("name", bean.name);
                values.put("url", bean.url);
                values.put("show", 1);
                db.insert("history", null, values);
            }else {
                values.put("show", 1);
                db.update(TABLE_HISTORY, values, "name=?", new String[]{bean.name});
            }
            cursor.close();
        }
    }

    public List<Bean> queryAllHistory() {
        List<Bean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, null, null, null, null, null, "historyId DESC", null);
        if(cursor != null && cursor.moveToFirst()) {
            int timeColumnIndex = cursor.getColumnIndex("time");
            int nameColumnIndex = cursor.getColumnIndex("name");
            int urlColumnIndex = cursor.getColumnIndex("url");
            do {
                long time = cursor.getLong(timeColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String url = cursor.getString(urlColumnIndex);
                Bean bean = new Bean();
                bean.name = name;
                bean.time = time;
                bean.url = url;
                list.add(bean);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public synchronized boolean insertFav(Bean bean) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        Cursor cursor = db.rawQuery("select * from "+ TABLE_FAVORITE + " where url=?", new String[] { bean.url });
        boolean result = false;
        if(cursor != null) {
            if(cursor.getCount() == 0) {//没有记录
                values.put("time", bean.time);
                values.put("name", bean.name);
                values.put("url", bean.url);
                db.insert(TABLE_FAVORITE, null, values);
                result = true;
            }
            cursor.close();
        }
        return result;
    }

    public List<Bean> queryAllFavorite() {
        List<Bean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITE, null, null, null, null, null, "favId DESC", null);
        if(cursor != null && cursor.moveToFirst()) {
            int timeColumnIndex = cursor.getColumnIndex("time");
            int nameColumnIndex = cursor.getColumnIndex("name");
            int urlColumnIndex = cursor.getColumnIndex("url");
            do {
                long time = cursor.getLong(timeColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String url = cursor.getString(urlColumnIndex);
                Bean bean = new Bean();
                bean.name = name;
                bean.time = time;
                bean.url = url;
                list.add(bean);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<Bean> queryRecentData() {
        List<Bean> list = new ArrayList<>(LIMIT);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_HISTORY, null, null, null, null, null, "searchId DESC", LIMIT + "");

        if (cursor != null && cursor.moveToFirst()) {
            int timeColumnIndex = cursor.getColumnIndex("time");
            int nameColumnIndex = cursor.getColumnIndex("name");
            do {
                long time = cursor.getLong(timeColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                Bean bean = new Bean();
                bean.name = name;
                bean.time = time;
                list.add(bean);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }



}
