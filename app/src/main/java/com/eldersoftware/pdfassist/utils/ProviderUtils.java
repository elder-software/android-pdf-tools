package com.eldersoftware.pdfassist.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.eldersoftware.pdfassist.data.DocInfoContract;

public class ProviderUtils {

    public static final String CREATE_DOC_SUCCESS = "Created new doc: ";
    public static final String CREATE_DOC_ERROR_EXISTS = "Doc already exists with that name";

    public static final String UPDATE_DOC_SUCCESS = "Updated new doc: ";

    public static String createDoc(String docName, Context context) {
        Uri uriDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon().appendPath(docName).build();

        Cursor cursor = context.getContentResolver()
                .query(uriDocName, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            return CREATE_DOC_ERROR_EXISTS;
        } else {
            ContentValues newDocCV = new ContentValues();
            newDocCV.put(DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME, docName.trim());
            context.getContentResolver().insert(DocInfoContract.DocInfoListEntry.DOC_INFO_URI, newDocCV);
            return CREATE_DOC_SUCCESS;
        }
    }


    public static String updateDoc(String oldDocName, String newDocName, Context context) {
        Uri uriOldDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon().appendPath(oldDocName).build();
        Uri uriNewDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon().appendPath(newDocName).build();

        Cursor cursor = context.getContentResolver()
                .query(uriNewDocName, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            return CREATE_DOC_ERROR_EXISTS;
        } else {
            ContentValues newDocCV = new ContentValues();
            newDocCV.put(DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME, newDocName.trim());
            context.getContentResolver().update(uriOldDocName, newDocCV, null, null);
            return CREATE_DOC_SUCCESS;
        }

    }


    public static boolean deleteDoc(String docName, Context context) {
        Uri uriDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon().appendPath(docName).build();
        int rowsDeleted = context.getContentResolver().delete(uriDocName,null, null);
        return rowsDeleted > 0;
    }


}
