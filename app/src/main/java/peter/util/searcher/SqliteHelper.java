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
    private static final int version = 1;
    private static SqliteHelper helper;
    private static final int LIMIT = 5;

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
        db.execSQL("create table if not exists search (searchId integer primary key autoincrement, time integer, name varchar(20), show integer)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public synchronized void insert(Search search) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("time", search.time);
        values.put("name", search.name);
        values.put("show", 1);
        db.insert("search", null, values);
    }

    public synchronized void trimData() {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query("search", null, null, null, null, null, "searchId DESC", LIMIT + "");
        int count = cursor.getCount();
        if (count == LIMIT) {
            cursor.moveToLast();
            int searchIdColumnIndex = cursor.getColumnIndex("searchId");
            int id = cursor.getInt(searchIdColumnIndex);
            db.execSQL("delete from search where searchId < " + id);
        }
        cursor.close();
    }

    public synchronized void deleteAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("delete from search where searchId != 0");
    }

    public List<Search> queryData() {
        List<Search> list = new ArrayList<>(LIMIT);
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("search", null, null, null, null, null, "searchId DESC", LIMIT + "");

        if (cursor != null && cursor.moveToFirst()) {
            int timeColumnIndex = cursor.getColumnIndex("time");
            int nameColumnIndex = cursor.getColumnIndex("name");
            do {
                long time = cursor.getLong(timeColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                Search search = new Search();
                search.name = name;
                search.time = time;
                list.add(search);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }
}
