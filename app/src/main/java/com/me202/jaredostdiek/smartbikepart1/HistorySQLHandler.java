package com.me202.jaredostdiek.smartbikepart1;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import java.util.ArrayList;

/**
 * Created by jaredostdiek on 4/17/16.
 * Adapted from http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 *
 */
public class HistorySQLHandler extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "historySQLManager";

    // Contacts table name
    private static final String LIST_HISTORY = "historySQL";

    // Contacts Table Columns names
    private static final String KEY_ID = "id"; //row number for each item
    private static final String KEY_ICON_ID = "iconID";
    private static final String KEY_LOC = "location";
    private static final String KEY_DATE = "date";
    private static final String KEY_FIRE_ID = "fireID";

    public HistorySQLHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HISTORY_ITEM = "CREATE TABLE " + LIST_HISTORY + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_ICON_ID + " INTEGER,"
                + KEY_LOC + " TEXT," + KEY_DATE + " TEXT," + KEY_FIRE_ID + " TEXT" + ")";
        db.execSQL(CREATE_HISTORY_ITEM);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + LIST_HISTORY);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addHistory(HistoryListItem history) {
        //add object toasd

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ICON_ID, history.getIconID()); //icon id for history item
        values.put(KEY_LOC, history.getLocation()); //location of bike ride
        values.put(KEY_DATE, history.getDate()); //location of date of ride
        values.put(KEY_FIRE_ID, history.getFireID());

        // Inserting Row
        db.insert(LIST_HISTORY, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    public HistoryListItem getHistory(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(LIST_HISTORY, new String[] { KEY_ID,
                        KEY_ICON_ID, KEY_LOC, KEY_DATE, KEY_FIRE_ID }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        HistoryListItem history = new HistoryListItem(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        // return contact
        return history;
    }

    // Updating single ride
    public int updateContact(HistoryListItem history) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ICON_ID, history.getIconID()); //icon id for history item
        values.put(KEY_LOC, history.getLocation()); //location of bike ride
        values.put(KEY_DATE, history.getDate()); //location of date of ride
        values.put(KEY_FIRE_ID, history.getDate()); //location of date of ride

        // updating row
        return db.update(LIST_HISTORY, values, KEY_ID+ " = ?",
                new String[] { String.valueOf(history.getFireID()) });
    }

    // Getting All rides
    public ArrayList<HistoryListItem> getAllItems() {
        ArrayList<HistoryListItem> histList = new ArrayList<HistoryListItem>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + LIST_HISTORY;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list; newest will be displayed on top
        if (cursor.moveToLast()) {
            do {
                HistoryListItem histItem = new HistoryListItem();
                histItem.setID(Integer.parseInt(cursor.getString(0)));
                histItem.setIconID(Integer.parseInt(cursor.getString(1)));
                histItem.setLocation(cursor.getString(2));
                histItem.setDate(cursor.getString(3));
                histItem.setFireID(cursor.getString(4));
                // Adding contact to list
                histList.add(histItem);
            } while (cursor.moveToPrevious());
        }

        // return ride list
        return histList;
    }

    // Deleting single ride
    public void deleteHistory(HistoryListItem history) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(LIST_HISTORY, KEY_ID + " = ?",
                new String[]{String.valueOf(history.getID())});
        db.close();
    }

    // Deleting all rides
    public void deleteAllHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(LIST_HISTORY, null, null);
        db.close();
    }
}