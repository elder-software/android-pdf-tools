package com.eldersoftware.pdfassist.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DocInfoProvider extends ContentProvider {
    public static final int DOC_INFO = 100;
    public static final int DOC_INFO_WITH_NAME = 101;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private DocInfoDbHelper mOpenHelper;


    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        //For general access to the doc info table
        uriMatcher.addURI(DocInfoContract.AUTHORITY,
                DocInfoContract.PATH_DOC_INFO_NAME,
                DOC_INFO);

        //For accessing the jobinfo table. Only the jobname is needed to access a single row
        //Will have the format content://authority/jobname
        uriMatcher.addURI(DocInfoContract.AUTHORITY,
                DocInfoContract.PATH_DOC_INFO_NAME + "/*",
                DOC_INFO_WITH_NAME);

        return uriMatcher;
    }


    /**
     * In onCreate the content provider is initialised on startup
     * @return true if the provider was successfully loaded
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new DocInfoDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case DOC_INFO: {
                //For general queries to the JobInfo Db
                cursor = mOpenHelper.getReadableDatabase().query(
                        DocInfoContract.DocInfoListEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }

            case DOC_INFO_WITH_NAME: {
                //The last path segment has been confirmed as a string type, and assumed to be
                //the doc name, returning the row matching with the job name

                String[] jobNameSelectionArg = {uri.getLastPathSegment()};

                cursor = mOpenHelper.getReadableDatabase().query(
                        DocInfoContract.DocInfoListEntry.TABLE_NAME,
                        projection,
                        DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME + " = ?",
                        jobNameSelectionArg,
                        null,
                        null,
                        sortOrder);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case DOC_INFO: {
                //Inserts an entirely new row into the Job Info table
                long insertId = mOpenHelper.getWritableDatabase().insert(
                        DocInfoContract.DocInfoListEntry.TABLE_NAME,
                        null,
                        values);
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsDeleted = 0;

        switch (sUriMatcher.match(uri)) {
            case DOC_INFO: {
                //Inserts an entirely new row into the Job Info table
                rowsDeleted = mOpenHelper.getWritableDatabase().delete(
                        DocInfoContract.DocInfoListEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }

            case DOC_INFO_WITH_NAME: {
                //The last path segment has been confirmed as a string type, and assumed to be
                //the jobs name. This can be used to return a cursor that contains the row
                //from the table containing job info. Will be useful for EditJobActivity
                String[] docNameSelectionArg = {uri.getLastPathSegment()};

                rowsDeleted = mOpenHelper.getReadableDatabase().delete(
                        DocInfoContract.DocInfoListEntry.TABLE_NAME,
                        DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME + " = ?",
                        docNameSelectionArg);

                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int rowsUpdated = 0;

        switch (sUriMatcher.match(uri)) {
            case DOC_INFO: {
                //Inserts an entirely new row into the Job Info table
                rowsUpdated = mOpenHelper.getWritableDatabase().update(
                        DocInfoContract.DocInfoListEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }

            case DOC_INFO_WITH_NAME: {
                //The last path segment has been confirmed as a string type, and assumed to be
                //the jobs name. This can be used to return a cursor that contains the row
                //from the table containing job info. Will be useful for EditJobActivity
                String[] docNameSelectionArg = {uri.getLastPathSegment()};

                rowsUpdated = mOpenHelper.getReadableDatabase().update(
                        DocInfoContract.DocInfoListEntry.TABLE_NAME,
                        values,
                        DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME + " = ?",
                        docNameSelectionArg);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return rowsUpdated;

    }

}
