package com.eldersoftware.pdfassist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.eldersoftware.pdfassist.R;
import com.eldersoftware.pdfassist.utils.ProviderUtils;

import java.util.ArrayList;

/**
 * Adapter that contains items with a page number, title, image, supporting text and popup menu
 */
public class TitleImageTextAdapter extends
        RecyclerView.Adapter<TitleImageTextAdapter.TitleImageTextViewHolder> {
    Context mContext;
    TitleImageTextClickHandler mClickHandler;
    String[] mPageNameData;
    String[] mPageImageData;
    String[] mPageTextData;


    //Interface click handler used for tapping on either the title, image, text or popup menu
    public interface TitleImageTextClickHandler {
        void onTitleTap(int position);
        void onImageTap(int position);
        void onTextTap(int position);
        void onPopUpMenuTap(int menuItemId, String pageName);
    }


    /**
     * Constructor to store member variables
     * @param context - context
     * @param pageData - page data
     * @param clickHandler - click handler
     */
    public TitleImageTextAdapter(@NonNull Context context,
                                 ArrayList<String[]> pageData,
                                 TitleImageTextClickHandler clickHandler) {
        this.mContext = context;
        this.mClickHandler = clickHandler;
        if (pageData.size() > 0) {
            this.mPageNameData = pageData.get(ProviderUtils.PAGE_NAMES_INDEX);
            this.mPageImageData = pageData.get(ProviderUtils.PAGE_IMAGES_INDEX);
            this.mPageTextData = pageData.get(ProviderUtils.PAGE_TEXT_INDEX);
        }
    }


    /**
     * Inflates the row using the layout file adapter_title_image_text
     * @param parent - parent recycler view
     * @param viewType - unused
     * @return view holder
     */
    @NonNull
    @Override
    public TitleImageTextAdapter.TitleImageTextViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.adapter_title_image_text, parent, false);

        return new TitleImageTextAdapter.TitleImageTextViewHolder(view);
    }

    /**
     * Assigns values/images to the textviews and imageview contained in the view holder
     * @param holder - title image text adapter view holder used from the onCreateViewHolder method
     * @param position - index of row
     */
    @Override
    public void onBindViewHolder(@NonNull TitleImageTextAdapter.TitleImageTextViewHolder holder,
                                 int position) {
        holder.mPageNum.setText(String.valueOf(position + 1));
        holder.mPageNameTextView.setText(mPageNameData[position]);
//        holder.mPageNameTextView.setText(mPageNameData[position]);
//        holder.mPageNameTextView.setText(mPageNameData[position]);
    }

    /**
     * @return integer with the number of rows
     */
    @Override
    public int getItemCount() {
        if (mPageNameData != null) {
            return mPageNameData.length;
        }

        return 0;
    }

    /**
     * Used to exchange new adapter data, then refreshes the data set
     * @param newPageData - contains page titles, images and text
     */
    public void swapData(ArrayList<String[]> newPageData) {
        if (newPageData.size() > 0) {
            this.mPageNameData = newPageData.get(ProviderUtils.PAGE_NAMES_INDEX);
            this.mPageImageData = newPageData.get(ProviderUtils.PAGE_IMAGES_INDEX);
            this.mPageTextData = newPageData.get(ProviderUtils.PAGE_TEXT_INDEX);
        }
        notifyDataSetChanged();
    }


    /**
     * View holder class that will be used by the rows, taken from the adapter_title_image_text
     * layout file
     */
    public class TitleImageTextViewHolder extends RecyclerView.ViewHolder {
        TextView mPageNum;
        TextView mPageNameTextView;
        ImageView mPageImage;
        TextView mPageText;
        TextView mPagePopUpMenu;


        public TitleImageTextViewHolder(final View viewParent) {
            super(viewParent);
            mPageNum = viewParent.findViewById(R.id.tv_adapter_num);
            mPageNameTextView = viewParent.findViewById(R.id.tv_adapter_title);
            mPageImage = viewParent.findViewById(R.id.iv_adapter_image);
            mPageText = viewParent.findViewById(R.id.tv_adapter_info);
            mPagePopUpMenu = viewParent.findViewById(R.id.tv_adapter_item_popup);

            mPagePopUpMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Creating a popup menu
                    PopupMenu popup = new PopupMenu(mContext, mPagePopUpMenu);

                    //Inflating menu from xml resource
                    popup.inflate(R.menu.menu_simple_card_popup);

                    //Adding click listener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            mClickHandler.onPopUpMenuTap(item.getItemId(),
                                    mPageNameTextView.getText().toString());
                            return true;
                        }
                    });

                    //Displaying the popup
                    popup.show();
                }
            });

            //OnClick for changing a pages title
            mPageNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickHandler.onTitleTap(getAdapterPosition());
                }
            });

            //OnClick for changing a pages image
            mPageImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickHandler.onImageTap(getAdapterPosition());
                }
            });

            //OnClick for changing a pages text
            mPageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickHandler.onTextTap(getAdapterPosition());
                }
            });
        }
    }
}
