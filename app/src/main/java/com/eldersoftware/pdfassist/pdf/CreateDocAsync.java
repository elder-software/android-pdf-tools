package com.eldersoftware.pdfassist.pdf;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.eldersoftware.pdfassist.utils.ProviderUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


/**
 * Asyncronous class used to create a PDF
 */
public class CreateDocAsync extends AsyncTask<Uri, Void, Void> {
    private WeakReference<Context> mContext;
    private CreateDocAsyncCallback mCallback;
    private File mPdfFile;

    /**
     * Constructor
     * @param context - context, weak reference used in case the apps context changes during execution
     * @param callback - CreateDocAsyncCallback
     */
    public CreateDocAsync(Context context, CreateDocAsyncCallback callback) {
        mContext = new WeakReference<>(context);
        mCallback = callback;
    }


    /**
     * Callback containing onComplete and onUpdate for PDF creation.
     */
    public interface CreateDocAsyncCallback {
        void onComplete(File pdfFile);
        void onUpdate();
    }

    @Override
    protected Void doInBackground(Uri... uris) {
        Uri docNameUri = uris[0];

        // Cursor used to gather all the page data from the content provider
        Cursor pageInfoCursor = mContext.get().getContentResolver()
                .query(docNameUri, ProviderUtils.PAGE_PROJECTION,
                null, null ,null);

        // Page info then split into array to be used by the CreateDocLayout class
        ArrayList<String[]> pageInfo = new ArrayList<>();
        if (pageInfoCursor != null && pageInfoCursor.moveToFirst()) {
            pageInfo = ProviderUtils.splitPageData(pageInfoCursor);
        }

        CreateDocLayout docLayout = new CreateDocLayout(mContext.get(), pageInfo,
                docNameUri.getLastPathSegment());
        try {
            mPdfFile = createPdfFile(docNameUri.getLastPathSegment());
            docLayout.saveToFile(mPdfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        mCallback.onComplete(mPdfFile);
    }


    /**
     * Uses the doc name to create a directory and file for saving the PDF into
     * @param docName - doc name
     * @return File with the correct directory and file name
     * @throws IOException thrown when errors occur creating/saving the file
     */
    private static File createPdfFile(String docName) throws IOException {
        // Creates the directory and ensures it exists
        String externalDirectory = Environment.getExternalStorageDirectory().toString() +
                "/Android Pdf Tools/" + docName + "/PDFs";
        File directory = new File(externalDirectory);
        boolean bool = false;
        if (!directory.exists()) {
            bool = directory.mkdirs(); // Creates directory if it doesn't exist
        }

        // Creates file with directory path, adds a suffix, (#), if the file already exists
        String docNameWithExt = docName + ".pdf";
        File pdfFile = new File(directory, docNameWithExt);
        int i = 1;
        while (pdfFile.exists()) {
            docNameWithExt = docName + " (" + i + ").pdf";
            pdfFile = new File(directory, docNameWithExt);
            i++;
        }

        return pdfFile;
    }

}
