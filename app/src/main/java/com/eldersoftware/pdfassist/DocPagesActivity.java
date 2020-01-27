package com.eldersoftware.pdfassist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.net.Uri;
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
import com.eldersoftware.pdfassist.utils.ProviderUtils;

import java.util.ArrayList;

import static com.eldersoftware.pdfassist.utils.ProviderUtils.PAGE_PROJECTION;

public class DocPagesActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TitleImageTextAdapter.TitleImageTextClickHandler{
    private final String LOG_TAG = getClass().getSimpleName();

    private static final int PAGE_LOADER_ID = 349;

    ArrayList<String[]> mAllPageInfo = new ArrayList<>();
    TitleImageTextAdapter mPagesAdapter;

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
            final CreateEditTextDialog editTextDialog =
                    new CreateEditTextDialog(this, "Create Page", null);
            editTextDialog.setClickListener(new CreateEditTextDialog.CreateEditTextCallback() {
                @Override
                public void onSave(String newText) {
                    ProviderUtils.createPage(mDocNameUri, newText, DocPagesActivity.this);
                    LoaderManager.getInstance(DocPagesActivity.this)
                            .restartLoader(PAGE_LOADER_ID, null, DocPagesActivity.this);
                    editTextDialog.dismiss();
                }
            });
            editTextDialog.show();
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

    @Override
    public void onImageTap(int position) {

    }

    @Override
    public void onTextTap(int position) {

    }

    @Override
    public void onPopUpMenuTap(int menuItemId, String pageName) {

    }
}
