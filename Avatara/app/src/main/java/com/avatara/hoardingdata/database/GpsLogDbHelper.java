package com.avatara.hoardingdata.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Created by shobhit on 22/6/16.
 */

public class GpsLogDbHelper extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "aaho_db";
    // table name
    private static final String LOG_TABLE_NAME = "gps_log";
    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED = "created";
    private static final String KEY_DATA = "data";

    // Queries
    private static final String QUERY_CREATE_LOG_TABLE = "CREATE TABLE " + LOG_TABLE_NAME + "("
            + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
            + KEY_CREATED + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + KEY_DATA + " TEXT" + ")";

    private static final String QUERY_DROP_LOG_TABLE = "DROP TABLE IF EXISTS " + LOG_TABLE_NAME;
    private static final String QUERY_BULK_ADD_LOG = "INSERT INTO " + LOG_TABLE_NAME + "(" + KEY_DATA + ") VALUES (?);";
    private static final String QUERY_SELECT_LOG = "SELECT * FROM " + LOG_TABLE_NAME;

    public GpsLogDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(QUERY_CREATE_LOG_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(QUERY_DROP_LOG_TABLE);
        // Creating tables again
        onCreate(db);
    }

    // Adding new log entry
    public void addGpsLog(GpsLog log) {
        addGpsLog(log.getData());
    }

    public void addGpsLog(String data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DATA, data);

        // Inserting Row
        long status = db.insert(LOG_TABLE_NAME, null, values);
        if (status == -1) {
            throw new AssertionError("insert query failed");
        }
        db.close(); // Closing database connection
    }

    // Bulk Adding new log entry
    private void addGpsLog(List<GpsLog> logs) {
        SQLiteDatabase db = this.getWritableDatabase();
        SQLiteStatement statement = db.compileStatement(QUERY_BULK_ADD_LOG);
        db.beginTransaction();
        for (GpsLog log : logs) {
            statement.clearBindings();
            statement.bindString(1, log.getData());
            statement.execute();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close(); // Closing database connection
    }

    // Getting one log
    public GpsLog getGpsLog(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(LOG_TABLE_NAME, new String[]{KEY_ID, KEY_CREATED, KEY_DATA}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        GpsLog log = new GpsLog(Integer.parseInt(cursor.getString(0)), parseDate(cursor.getString(1)), cursor.getString(2));
        cursor.close();
        db.close();
        return log;
    }

    // Getting All Shops
    public List<GpsLog> getAllGpsLogs() {
        List<GpsLog> logList = new ArrayList<GpsLog>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(QUERY_SELECT_LOG, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                logList.add(logFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return logList;
    }

    private GpsLog logFromCursor(Cursor cursor) {
        GpsLog log = new GpsLog();
        log.setId(Integer.parseInt(cursor.getString(0)));
        log.setCreated(parseDate(cursor.getString(1)));
        log.setData(cursor.getString(2));
        return log;
    }

    public Date parseDate(String dateStr) {
        DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return iso8601Format.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Getting log Count
    public int getGpsLogCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(QUERY_SELECT_LOG, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    // Deleting
    public void deleteGpsLog(GpsLog log) {
        deleteGpsLog(log.getId());
    }

    public void deleteGpsLog(int id) {
        deleteGpsLog(new int[] { id });
    }

    public void deleteGpsLog(int[] ids) {
        String[] values = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            values[i] = String.valueOf(ids[i]);
        }
        String args = TextUtils.join(", ", values);
        String query = "DELETE FROM " + LOG_TABLE_NAME + " WHERE " + KEY_ID + " IN (%s);";

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(String.format(query, args));
        db.close();
    }
}
