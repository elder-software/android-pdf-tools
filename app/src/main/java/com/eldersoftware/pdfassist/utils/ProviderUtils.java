package com.eldersoftware.pdfassist.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.eldersoftware.pdfassist.DocPagesActivity;
import com.eldersoftware.pdfassist.data.DocInfoContract;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A list of helper methods for creating abstraction between the UI and the content provider
 */
public class ProviderUtils {

    public static final String CREATE_DOC_SUCCESS = "Created new doc: ";
    public static final String CREATE_DOC_ERROR_EXISTS = "Doc already exists with that name";

    public static final String UPDATE_DOC_SUCCESS = "Updated doc: ";

    public static final String CREATE_PAGE_SUCCESS = "Created new page: ";
    private static final String CREATE_PAGE_ERROR = "Error creating new page";

    public static final String UPDATE_PAGE_SUCCESS = "Updated page";
    public static final String UPDATE_PAGE_ERROR = "Error updating page";


    public static final String DELETE_PAGE_SUCCESS = "Deleted page";
    public static final String DELETE_PAGE_ERROR = "Error deleting page";

    //Projection for querying the database and returning columns related to page information
    public static final String[] PAGE_PROJECTION = {
            DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_NAMES,
            DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_IMAGES,
            DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_TEXT
    };

    public static final int PAGE_NAMES_INDEX = 0;
    public static final int PAGE_IMAGES_INDEX = 1;
    public static final int PAGE_TEXT_INDEX = 2;


    //Used by the pages columns, so there is no need to create a new table for each new page
    private static final String PROVIDER_SPLIT = "_split_";
    public static final String PROVIDER_NULL = "null";


    /**
     * Creates a new row in the database with the doc name entered
     * @param docName - doc name
     * @param context - context
     * @return String - status message for success or error
     */
    public static String createDoc(String docName, Context context) {
        docName = docName.trim(); //Removes leading/trailing whitespace

        //Creates URI using the doc name to check if the doc name already exists
        Uri uriDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI
                .buildUpon().appendPath(docName).build();
        Cursor cursor = context.getContentResolver()
                .query(uriDocName, null, null, null, null);

        //If cursor contains data then doc name already exists
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return CREATE_DOC_ERROR_EXISTS;
        } else {
            ContentValues newDocCV = new ContentValues();
            newDocCV.put(DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME, docName);
            context.getContentResolver().insert(DocInfoContract.DocInfoListEntry.DOC_INFO_URI, newDocCV);
            return CREATE_DOC_SUCCESS;
        }
    }


    /**
     * Updates an existing row in the database table
     * @param oldDocName - old doc name to potentially be replaced
     * @param newDocName - new doc name
     * @param context - context
     * @return String - contains success or error message
     */
    public static String updateDoc(String oldDocName, String newDocName, Context context) {
        //Trims leading/trailing whitespace and creates URIs using the old/new doc names
        oldDocName = oldDocName.trim();
        newDocName = newDocName.trim();
        Uri uriOldDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon()
                .appendPath(oldDocName).build();
        Uri uriNewDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon()
                .appendPath(newDocName).build();

        Cursor cursor = context.getContentResolver()
                .query(uriNewDocName, null, null, null, null);

        //If the cursor contains data the new doc name already exists
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return CREATE_DOC_ERROR_EXISTS;
        } else {
            ContentValues newDocCV = new ContentValues();
            newDocCV.put(DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME, newDocName);
            context.getContentResolver().update(uriOldDocName, newDocCV, null, null);
            return CREATE_DOC_SUCCESS;
        }
    }


    /**
     * Removes a row of data in the database
     * @param docName - doc name for the search criteria
     * @param context - context
     * @return boolean if the row was successfully removed or not
     */
    public static boolean deleteDoc(String docName, Context context) {
        Uri uriDocName = DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon()
                .appendPath(docName).build();
        int rowsDeleted = context.getContentResolver().delete(uriDocName,null, null);
        return rowsDeleted > 0; //Returns true if a row was deleted
    }


    /**
     * Creates a new page using the pagename and updates database row with the new data
     * @param docNameUri - doc name uri
     * @param pageName - page name
     * @param context - context
     */
    public static void createPage(Uri docNameUri, String pageName, Context context) {
        //As we are not creating a new table or row we need the existing page data
        Cursor cursor = context.getContentResolver()
                .query(docNameUri, PAGE_PROJECTION, null, null, null);
        ContentValues newPageCV = new ContentValues();

        //Loops through each column and appends new page information
        for (int i = 0; i < PAGE_PROJECTION.length; i++) {
            if (cursor != null && cursor.moveToFirst()) {
                String currentColumnString = cursor.getString(i); //Gets existing data in column

                //If its the page name index page name needs to be entered,
                //else add empty item for other columns
                if (i == PAGE_NAMES_INDEX) {
                    //Adds the PROVIDER_SPLIT constant between page data if there is existing pages
                    if (currentColumnString == null || currentColumnString.isEmpty()) {
                        currentColumnString = pageName;
                    } else {
                        currentColumnString = currentColumnString + PROVIDER_SPLIT + pageName;
                    }
                } else {
                    //Other columns add the PROViDER_NULL constant to ensure there is consistant
                    //page information
                    if (currentColumnString == null || currentColumnString.isEmpty()) {
                        currentColumnString = PROVIDER_NULL;
                    } else {
                        currentColumnString = currentColumnString + PROVIDER_SPLIT + PROVIDER_NULL;
                    }
                }

                //Puts the respective column information into the content values
                newPageCV.put(PAGE_PROJECTION[i], currentColumnString);
            }
        }

        int rowsUpdated = context.getContentResolver().update(docNameUri, newPageCV,
                null, null);
        if (cursor != null) {
            cursor.close();
        }

        //Makes success or error toast
        if (rowsUpdated > 0) {
            Toast.makeText(context, CREATE_PAGE_SUCCESS + pageName, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, CREATE_PAGE_ERROR, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Updates the chosen column with the new data for a specific doc name row,
     * used when changes are made to the page data
     * @param docNameUri - doc name uri
     * @param column - column name, taken from the DocInfoContract
     * @param newData - new data
     * @param context - context
     */
    public static void updatePageColumn(Uri docNameUri,
                                        String column,
                                        String newData,
                                        Context context) {
        ContentValues cv = new ContentValues();
        cv.put(column, newData);

        int rowsUpdated = context.getContentResolver()
                .update(docNameUri, cv, null, null);

        if (rowsUpdated > 0) {
            Toast.makeText(context, UPDATE_PAGE_SUCCESS, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, UPDATE_PAGE_ERROR, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Splits the page data and adds it to an ArrayList of String arrays
     * @param cursor - cursor
     * @return ArrayList<String[]> containing page names, images and text
     */
    public static ArrayList<String[]> splitPageData(Cursor cursor) {
        ArrayList<String[]> pageData = new ArrayList<>();

        //Assumes that the data was queried using the PAGE_PROJECTION
        for (int i = 0; i < PAGE_PROJECTION.length; i++) {
            String[] splitColumnData = cursor.getString(i).split(PROVIDER_SPLIT);
            pageData.add(splitColumnData);
        }

        return pageData;
    }


    /**
     * Changes data for a specified index of page information and joins the array with the
     * PROVIDER_SPLIT delimiter
     * @param pageData - page data array
     * @param newData - new data to replace with
     * @param index - index where the new data will be placed
     * @return String of the data joined
     */
    public static String replaceArrayElementAndJoin(String[] pageData, String newData, int index) {
        pageData[index] = newData;
        return TextUtils.join(PROVIDER_SPLIT, pageData);
    }


    /**
     * Converts the array to an ArrayList, removes the given index and joins the array using
     * the PROVIDER_SPLIT delimiter
     * @param pageData - Array of page data
     * @param index - index to remove
     * @return String with chosen element removed and the array joined
     */
    public static String removeArrayElementAndJoin(String[] pageData, int index) {
        ArrayList<String> pageDataArray = new ArrayList<>(Arrays.asList(pageData));
        pageDataArray.remove(index);

        return TextUtils.join(PROVIDER_SPLIT, pageDataArray);
    }


    /**
     * Removes the given page index
     * @param context - context
     * @param allPageData - all page data
     * @param index - page index
     * @param docNameUri - uri for row update
     */
    public static boolean deletePage(Context context, ArrayList<String[]> allPageData, int index, Uri docNameUri) {
        // Creates the strings for the content provider with the given page index removed
        String newPageNames = removeArrayElementAndJoin(allPageData.get(PAGE_NAMES_INDEX), index);
        String newPageImages = removeArrayElementAndJoin(allPageData.get(PAGE_IMAGES_INDEX), index);
        String newPageText = removeArrayElementAndJoin(allPageData.get(PAGE_TEXT_INDEX), index);

        ContentValues newPagesCV = new ContentValues();
        newPagesCV.put(DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_NAMES, newPageNames);
        newPagesCV.put(DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_IMAGES, newPageImages);
        newPagesCV.put(DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_TEXT, newPageText);

        int rowsUpdated = context.getContentResolver()
                .update(docNameUri, newPagesCV, null, null);

        if (rowsUpdated > 0) {
            Toast.makeText(context, DELETE_PAGE_SUCCESS, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(context, DELETE_PAGE_ERROR, Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
