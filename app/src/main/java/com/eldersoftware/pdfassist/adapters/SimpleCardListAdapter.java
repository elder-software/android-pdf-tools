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

public class SimpleCardListAdapter extends RecyclerView.Adapter<SimpleCardListAdapter.SimpleCardViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    private SimpleCardListClickHandler mClickHandler;

    public interface SimpleCardListClickHandler {
        void onSimpleCardListTap(String docName, View view);
        void onSimpleCardListPopUpTap(int menuItemId, String docName);
    }

    public SimpleCardListAdapter(@NonNull Context context, SimpleCardListClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
    }

    @NonNull
    @Override
    public SimpleCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.adapter_simple_card_list;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutIdForListItem, parent, false);

        return new SimpleCardViewHolder(view);

    }

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



    //Viewholder used by the adapter for holding the job information and the
    public class SimpleCardViewHolder extends RecyclerView.ViewHolder {
        TextView mDocNameTextView;
        TextView mDocPopUpMenu;

        public SimpleCardViewHolder(final View viewParent) {
            super(viewParent);
            mDocNameTextView = viewParent.findViewById(R.id.tv_adapter_simple_card_text);
            mDocPopUpMenu = viewParent.findViewById(R.id.tv_adapter_simple_card_popup_menu);

            mDocPopUpMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, mDocPopUpMenu);
                    //inflating menu from xml resource
                    popup.inflate(R.menu.menu_simple_card_popup);
                    //adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            mClickHandler.onSimpleCardListPopUpTap(item.getItemId(),
                                    mDocNameTextView.getText().toString());
                            return true;
                        }
                    });
                    //displaying the popup
                    popup.show();
                }
            });


            mDocNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickHandler.onSimpleCardListTap(mDocNameTextView.getText().toString(), viewParent);
                }
            });
        }
    }

}
