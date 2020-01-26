package com.eldersoftware.pdfassist.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class DocInfoContract {
    public static final String AUTHORITY = "com.eldersoftware.pdfassist";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    //For accessing this specific table through the Uri
    public static final String PATH_DOC_INFO_NAME = "docInfoTable";

    public static final class DocInfoListEntry implements BaseColumns {
        public static final Uri DOC_INFO_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DOC_INFO_NAME).build();

        public static final String TABLE_NAME = "docInfoTable";
        public static final String COLUMN_DOC_NAME = "docName";
        public static final String COLUMN_DOC_IMAGES = "docImages";
        public static final String COLUMN_DOC_TEXT = "docText";
    }

}
