package com.eldersoftware.pdfassist.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DocInfoDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "docinfo.db";

    private static final int DATABASE_VERSION = 1;


    public DocInfoDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_DOCLIST_TABLE = "CREATE TABLE " + DocInfoContract.DocInfoListEntry.TABLE_NAME + " (" +
                DocInfoContract.DocInfoListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME + " TEXT NOT NULL, " +
                DocInfoContract.DocInfoListEntry.COLUMN_DOC_IMAGES + " TEXT, " +
                DocInfoContract.DocInfoListEntry.COLUMN_DOC_TEXT + " TEXT" +
                ");";

        db.execSQL(SQL_CREATE_DOCLIST_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
