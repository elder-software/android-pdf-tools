package com.eldersoftware.pdfassist.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Class to specify the constants that will be used by the content provider
 */
public class DocInfoContract {
    public static final String AUTHORITY = "com.eldersoftware.pdfassist";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    //For accessing this specific table through the Uri
    public static final String PATH_DOC_INFO_NAME = "docInfoTable";

    //Column constants
    public static final class DocInfoListEntry implements BaseColumns {
        public static final Uri DOC_INFO_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DOC_INFO_NAME).build();

        public static final String TABLE_NAME = "docInfoTable";
        public static final String COLUMN_DOC_NAME = "docName";
        public static final String COLUMN_DOC_PAGE_NAMES = "docPageNames";
        public static final String COLUMN_DOC_PAGE_IMAGES = "docPageImages";
        public static final String COLUMN_DOC_PAGE_TEXT = "docPageText";
    }

}
