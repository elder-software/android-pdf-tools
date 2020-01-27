package com.eldersoftware.pdfassist.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eldersoftware.pdfassist.AllDocsActivity;
import com.eldersoftware.pdfassist.R;


/**
 * A basic adapter that uses a cursors data to inflate views that contain simply text and a
 * popup menu to support CRUD operations.
 */
public class SimpleCardListAdapter extends
        RecyclerView.Adapter<SimpleCardListAdapter.SimpleCardViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    private SimpleCardListClickHandler mClickHandler;

    //Interface is a click handler for when the popup menu or row is tapped
    public interface SimpleCardListClickHandler {
        void onSimpleCardListTap(String docName, View view);
        void onSimpleCardListPopUpTap(int menuItemId, String docName);
    }

    /**
     * Default constructer
     * @param context - apps current context
     * @param clickHandler - click handler
     */
    public SimpleCardListAdapter(@NonNull Context context, SimpleCardListClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
    }

    /**
     * Inflates the row using the layout file adapter_simple_card_list
     * @param parent -  parent recycler view
     * @param viewType - unused
     * @return view holder
     */
    @NonNull
    @Override
    public SimpleCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.adapter_simple_card_list, parent, false);

        return new SimpleCardViewHolder(view);
    }


    /**
     * Uses the Cursor member variable to get the docs name and sets the the textview
     * @param holder - simple card adapter view holder used from the onCreateViewHolder method
     * @param position - index of row
     */
    @Override
    public void onBindViewHolder(@NonNull SimpleCardViewHolder holder, int position) {
        mCursor.moveToPosition(position); // get to the right location in the cursor

        String description = mCursor.getString(AllDocsActivity.INDEX_DOC_NAME);
        holder.mDocNameTextView.setText(description);
    }

    /**
     * Total number of items in the data set held by the adapter.
     * @return total amount of items for adapter
     */
    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }


    /**
     * Used to exchange new cursor data
     * @param newCursor - new cursor
     * @return the cursor used for the data set
     */
    public Cursor swapCursor(Cursor newCursor) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == newCursor) {
            return null; //as nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = newCursor; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (newCursor != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }


    /**
     * View holder class that will be used by the rows, taken from the adapter_simple_card_list
     * layout file
     */
    public class SimpleCardViewHolder extends RecyclerView.ViewHolder {
        TextView mDocNameTextView;
        TextView mDocPopUpMenu;

        public SimpleCardViewHolder(final View viewParent) {
            super(viewParent);
            mDocNameTextView = viewParent.findViewById(R.id.tv_adapter_simple_card_text);
            mDocPopUpMenu = viewParent.findViewById(R.id.tv_adapter_simple_card_popup_menu);

            //OnClick method for the popup menu attached to each row
            mDocPopUpMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, mDocPopUpMenu);

                    //Inflating menu from xml resource
                    popup.inflate(R.menu.menu_simple_card_popup);

                    //Adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            mClickHandler.onSimpleCardListPopUpTap(item.getItemId(),
                                    mDocNameTextView.getText().toString());
                            return true;
                        }
                    });

                    //Displaying the popup
                    popup.show();
                }
            });


            // OnClick listener used by the row itself
            mDocNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickHandler.onSimpleCardListTap(mDocNameTextView.getText().toString(), viewParent);
                }
            });
        }
    }

}
