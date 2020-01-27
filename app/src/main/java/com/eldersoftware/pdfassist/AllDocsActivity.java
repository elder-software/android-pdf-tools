package com.eldersoftware.pdfassist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.eldersoftware.pdfassist.adapters.SimpleCardListAdapter;
import com.eldersoftware.pdfassist.data.DocInfoContract;
import com.eldersoftware.pdfassist.dialogs.CreateEditDocDialog;
import com.eldersoftware.pdfassist.dialogs.YesNoDialog;
import com.eldersoftware.pdfassist.utils.ProviderUtils;

public class AllDocsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCardListAdapter.SimpleCardListClickHandler {
    private RecyclerView mDocsRecyclerView;
    private SimpleCardListAdapter mAllDocsAdapter;

    private static final int DOC_LOADER_ID = 118;
    public static final String[] DOC_LOADER_PROJECTION = {
            DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME,
    };
    public static final int INDEX_DOC_NAME = 0;


    private final String LOG_TAG = getClass().getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_docs);

        mDocsRecyclerView = findViewById(R.id.rv_all_docs);

        //LinearLayoutManager to be used by the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setReverseLayout(false);

        mDocsRecyclerView.setLayoutManager(layoutManager);
        mDocsRecyclerView.setHasFixedSize(true);

        mAllDocsAdapter = new SimpleCardListAdapter(this, this);
        mDocsRecyclerView.setAdapter(mAllDocsAdapter);

        setTitle("All Docs");

        //Initialises the loader for retrieving the doc information
        LoaderManager.getInstance(this).initLoader(DOC_LOADER_ID, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflates the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_all_docs, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create_doc) {
            new CreateEditDocDialog(this, null,
                    new CreateEditDocDialog.SuccessCallback() {
                @Override
                public void success() {
                    //Restarts loader if there is a new doc created
                    LoaderManager.getInstance(AllDocsActivity.this)
                            .restartLoader(DOC_LOADER_ID, null, AllDocsActivity.this);
                }
            }).show();
        }

        return super.onOptionsItemSelected(item);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case DOC_LOADER_ID:
                Uri jobListQueryUri = DocInfoContract.DocInfoListEntry.DOC_INFO_URI;

                //Orders the results by job name
                String sortOrder = DocInfoContract.DocInfoListEntry.COLUMN_DOC_NAME + " ASC";

                return new CursorLoader(this,
                        jobListQueryUri,
                        DOC_LOADER_PROJECTION,
                        null,
                        null,
                        sortOrder);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAllDocsAdapter.swapCursor(data);
    }


    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }


    /**
     * ClickHandler from the SimpleCardListAdapter,
     * called when the row is tapped (not the rows popup menu)
     * @param docName - doc name
     * @param view - view
     */
    @Override
    public void onSimpleCardListTap(String docName, View view) {
        //Starts the doc pages intent and passes the URI that will be used to retrieve page information
        Intent docPagesIntent = new Intent(this, DocPagesActivity.class);
        docPagesIntent.setData(
                DocInfoContract.DocInfoListEntry.DOC_INFO_URI.buildUpon()
                        .appendPath(docName).build());
        startActivity(docPagesIntent);
    }


    /**
     * ClickHandler from the SimpleCardListAdapter,
     * called when a rows popup menu is tapped
     * @param menuItemId - menu item id, will either be edit or delete for a doc name
     * @param docName - job name
     */
    @Override
    public void onSimpleCardListPopUpTap(int menuItemId, final String docName) {
        switch (menuItemId) {
            case R.id.action_simple_card_list_edit:
                //Dialog called to edit a doc name
                new CreateEditDocDialog(this, docName,
                        new CreateEditDocDialog.SuccessCallback() {
                    @Override
                    public void success() {
                        LoaderManager.getInstance(AllDocsActivity.this)
                                .restartLoader(DOC_LOADER_ID, null,
                                        AllDocsActivity.this);
                    }
                }).show();
                break;
            case R.id.action_simple_card_list_delete:
                YesNoDialog.showYesNoDialog(this,
                        "Delete Doc",
                        "Are you sure you want to delete: " + docName,
                        new YesNoDialog.YesNoDialogCallback() {
                            @Override
                            public void yesNoCallback(boolean yesSelected) {
                                //Attempts to remove the respective row from the database
                                //Shows a toast message for a success/error
                                if (ProviderUtils.deleteDoc(docName, AllDocsActivity.this)) {
                                    Toast.makeText(AllDocsActivity.this,
                                            "Deleted: " + docName, Toast.LENGTH_LONG).show();
                                    LoaderManager.getInstance(AllDocsActivity.this)
                                            .restartLoader(DOC_LOADER_ID, null,
                                                    AllDocsActivity.this);
                                } else {
                                    Toast.makeText(AllDocsActivity.this,
                                            "Failed to delete: " + docName, Toast.LENGTH_LONG)
                                            .show();
                                }
                            }
                        });
        }
    }
}
