package com.eldersoftware.pdfassist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.eldersoftware.pdfassist.adapters.SimpleCardListAdapter;
import com.eldersoftware.pdfassist.adapters.TitleImageTextAdapter;
import com.eldersoftware.pdfassist.data.DocInfoContract;
import com.eldersoftware.pdfassist.dialogs.CreateEditTextDialog;
import com.eldersoftware.pdfassist.dialogs.LoadingDialog;
import com.eldersoftware.pdfassist.dialogs.YesNoDialog;
import com.eldersoftware.pdfassist.pdf.CreateDocAsync;
import com.eldersoftware.pdfassist.utils.CameraUtils;
import com.eldersoftware.pdfassist.utils.ProviderUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import static com.eldersoftware.pdfassist.utils.CameraUtils.REQUEST_CAMERA_AND_STORAGE_PERMISSIONS;
import static com.eldersoftware.pdfassist.utils.ProviderUtils.PAGE_NAMES_INDEX;
import static com.eldersoftware.pdfassist.utils.ProviderUtils.PAGE_PROJECTION;

public class DocPagesActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TitleImageTextAdapter.TitleImageTextClickHandler {

    private final String LOG_TAG = getClass().getSimpleName();

    private static final int ACTIVITY_START_CAMERA_APP = 100;
    private static final int PAGE_LOADER_ID = 349;

    ArrayList<String[]> mAllPageInfo = new ArrayList<>();
    TitleImageTextAdapter mPagesAdapter;

    String mImageLocation;
    int mImageIndex;

    RecyclerView mPagesRecyclerView;

    Uri mDocNameUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_pages);

        mPagesRecyclerView = findViewById(R.id.rv_doc_pages);

        //LinearLayoutManager to be used by the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);

        mPagesRecyclerView.setLayoutManager(layoutManager);
        mPagesRecyclerView.setHasFixedSize(true);

        mPagesAdapter = new TitleImageTextAdapter(this, mAllPageInfo, this);
        mPagesRecyclerView.setAdapter(mPagesAdapter);

        mDocNameUri = getIntent().getData();

        if (mDocNameUri != null) {
            setTitle(mDocNameUri.getLastPathSegment());
        }

        //Initialises loader for retrieving page information
        LoaderManager.getInstance(this).initLoader(PAGE_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_doc_pages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create_page) {
            // Inits the dialog used to create a new page with the page heading as the required input
            final CreateEditTextDialog editTextDialog =
                    new CreateEditTextDialog(this, "Create Page", null);
            editTextDialog.setClickListener(new CreateEditTextDialog.CreateEditTextCallback() {
                @Override
                public void onSave(String newText) {
                    // Saves the page to the content provider and restarts the page info loader
                    ProviderUtils.createPage(mDocNameUri, newText, DocPagesActivity.this);
                    LoaderManager.getInstance(DocPagesActivity.this)
                            .restartLoader(PAGE_LOADER_ID, null, DocPagesActivity.this);
                    editTextDialog.dismiss();
                }
            });
            editTextDialog.show();
        } else if (item.getItemId() == R.id.action_create_pdf) {
            // Shows the loading dialog and creates the pdf
            final LoadingDialog loadingDialog = new LoadingDialog(this, "Creating PDF");
            loadingDialog.show();
            new CreateDocAsync(this, new CreateDocAsync.CreateDocAsyncCallback() {
                @Override
                public void onComplete(File pdfFile) {
                    loadingDialog.dismiss();
                    launchPdfIntent(pdfFile);
                }

                @Override
                public void onUpdate() {

                }
            }).execute(mDocNameUri);
        }

        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case PAGE_LOADER_ID:
                return new CursorLoader(this,
                        mDocNameUri,
                        PAGE_PROJECTION,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mAllPageInfo = ProviderUtils.splitPageData(data);
            mPagesAdapter.swapData(mAllPageInfo);
        }
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) { }


    /**
     * ClickHandler from the TitleImageTextAdaper, used when the pages title is tapped to edit the
     * page name
     * @param position - page number
     */
    @Override
    public void onTitleTap(final int position) {
        final CreateEditTextDialog editTextDialog =
                new CreateEditTextDialog(
                        this,
                        "Update Page",
                        mAllPageInfo.get(ProviderUtils.PAGE_NAMES_INDEX)[position]);
        editTextDialog.setClickListener(new CreateEditTextDialog.CreateEditTextCallback() {
            @Override
            public void onSave(String newText) {
                //Joins the array with the new data inserted
                String newData = ProviderUtils.replaceArrayElementAndJoin(
                        mAllPageInfo.get(ProviderUtils.PAGE_NAMES_INDEX), newText, position);

                //Updates the row with new page information
                ProviderUtils.updatePageColumn(
                        mDocNameUri,
                        DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_NAMES,
                        newData,
                        DocPagesActivity.this);

                //Restarts the loader to show the new page data in the recyclerview
                LoaderManager.getInstance(DocPagesActivity.this)
                        .restartLoader(PAGE_LOADER_ID, null, DocPagesActivity.this);
                editTextDialog.dismiss();
            }
        });
        editTextDialog.show();
    }


    /**
     * ClickHandler from the TitleImageTextAdaper, used when the pages image is tapped
     * to capture an image
     * @param position - page number
     */
    @Override
    public void onImageTap(int position) {
        // Checks whether camera and storage permissions have been enabled
        if (CameraUtils.checkCameraPermissions(this, this)) {
            File imageFile = null;

            // Creates the image file using the doc name and a time/date stamp
            try {
                imageFile = CameraUtils.createImageFile(mDocNameUri.getLastPathSegment());
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageFile != null && !imageFile.toString().isEmpty()) {
                // Stored to member variables to save image to content provider
                mImageLocation = imageFile.toString();
                mImageIndex = position;

                // Creates a URI for the given image file and opens the native android camera app
                Uri uri = CameraUtils.makeUriUsingSdkVersion(imageFile, this);
                CameraUtils.callCameraApp(this, uri, ACTIVITY_START_CAMERA_APP);
            }
        }
    }


    /**
     * ClickHandler from the TitleImageTextAdaper, used when the pages image is tapped
     * to capture an image
     * @param position - page number
     */
    @Override
    public void onTextTap(final int position) {
        final CreateEditTextDialog editTextDialog =
                new CreateEditTextDialog(
                        this,
                        "Update Page Text",
                        mAllPageInfo.get(ProviderUtils.PAGE_TEXT_INDEX)[position]);
        editTextDialog.setClickListener(new CreateEditTextDialog.CreateEditTextCallback() {
            @Override
            public void onSave(String newText) {
                //Joins the array with the new data inserted
                String newData = ProviderUtils.replaceArrayElementAndJoin(
                        mAllPageInfo.get(ProviderUtils.PAGE_TEXT_INDEX), newText, position);

                //Updates the row with new page information
                ProviderUtils.updatePageColumn(
                        mDocNameUri,
                        DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_TEXT,
                        newData,
                        DocPagesActivity.this);

                //Restarts the loader to show the new page data in the recyclerview
                LoaderManager.getInstance(DocPagesActivity.this)
                        .restartLoader(PAGE_LOADER_ID, null, DocPagesActivity.this);
                editTextDialog.dismiss();
            }
        });
        editTextDialog.show();

    }

    @Override
    public void onPopUpMenuTap(int menuItemId, String pageName) {

    }

    @Override
    public void onDeleteTap(final int position) {
        YesNoDialog.showYesNoDialog(this,
                "Delete Page",
                "Are you sure you want to delete: " +
                        mAllPageInfo.get(PAGE_NAMES_INDEX)[position],
                new YesNoDialog.YesNoDialogCallback() {
                    @Override
                    public void yesNoCallback(boolean yesSelected) {
                        //Attempts to remove the respective page from the database
                        //Shows a toast message for a success/error
                        if (ProviderUtils.deletePage(DocPagesActivity.this, mAllPageInfo, position, mDocNameUri)) {
                            LoaderManager.getInstance(DocPagesActivity.this)
                                    .restartLoader(PAGE_LOADER_ID, null,
                                            DocPagesActivity.this);
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
            //Joins the array with the new data inserted
            String newData = ProviderUtils.replaceArrayElementAndJoin(
                    mAllPageInfo.get(ProviderUtils.PAGE_IMAGES_INDEX), mImageLocation, mImageIndex);

            //Updates the row with new page information
            ProviderUtils.updatePageColumn(
                    mDocNameUri,
                    DocInfoContract.DocInfoListEntry.COLUMN_DOC_PAGE_IMAGES,
                    newData,
                    this);

            //Restarts the loader to show the new page data in the recyclerview
            LoaderManager.getInstance(this).restartLoader(PAGE_LOADER_ID, null, this);

            Toast.makeText(this, "Image successfully saved.", Toast.LENGTH_LONG).show();
            mImageLocation = "";
            mImageIndex = 0;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSIONS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this,
                        "Permissions granted, you can now take photos and save images",
                        Toast.LENGTH_LONG).show();
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this,
                        "External write permission has not been granted, cannot save images",
                        Toast.LENGTH_LONG).show();
            } else if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this,
                        "Camera permission has not been granted, cannot save images",
                        Toast.LENGTH_LONG).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void launchPdfIntent(File file) {
        Intent target = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(this,
                BuildConfig.APPLICATION_ID + ".fileprovider", file);
        target.setDataAndType(uri,"application/pdf");
        target.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent intent = Intent.createChooser(target, "Open File");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Instruct the user to install a PDF reader here, or something
            Toast.makeText(this, "No PDF reader found", Toast.LENGTH_LONG).show();
        }
    }

}
