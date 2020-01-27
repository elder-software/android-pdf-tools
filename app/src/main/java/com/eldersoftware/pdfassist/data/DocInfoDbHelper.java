package com.eldersoftware.pdfassist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * Helper class used to instantiate a SQLite table that can be accessed by the content provider
 */
public class DocInfoDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "docinfo.db";
    private static final int DATABASE_VERSION = 1;


    public DocInfoDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * Executed SQLite code to instatiate a new SQLite database
     * @param db database
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //SQLite code executed to create the table
        final String SQL_CREATE_DOCLIST_TABLE = "CREATE TABLE " +
                DocInfoContract.DocInfoListEntry.TABLE_NAME + " (" +
                DocInfoContract.DocInfoListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME + " TEXT NOT NULL, " +
                DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_NAMES + " TEXT, " +
                DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_IMAGES + " TEXT, " +
                DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_TEXT + " TEXT" +
                ");";

        db.execSQL(SQL_CREATE_DOCLIST_TABLE);
    }


    /**
     * Called when the DATABASE_VERSION is incremented
     * @param db - database
     * @param oldVersion - oldVersion
     * @param newVersion - newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
