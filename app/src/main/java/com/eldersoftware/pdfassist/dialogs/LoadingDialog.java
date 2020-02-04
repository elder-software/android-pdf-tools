package com.eldersoftware.pdfassist.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eldersoftware.pdfassist.R;

/**
 * Dialog used to show a progress spinner with a message
 */
public class LoadingDialog extends Dialog {
    private TextView mLoadingMessageTV;
    private Button mCloseButton, mSaveButton;
    private Context mContext;
    private String mLoadingMessage;
    private TextView mTitle;


    /**
     * Constructor
     * @param context - context
     * @param loadingMessage - String, shown under the progress spinner
     */
    public LoadingDialog(@NonNull Context context, @Nullable String loadingMessage) {
        super(context);
        mContext = context;
        mLoadingMessage = loadingMessage;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //Layout includes title already
        setContentView(R.layout.dialog_loading);

        //Sets width and height layout params in onCreate, otherwise the dialog won't
        //use the entire screen width
        Window window = getWindow();
        if (window != null) {
            ViewGroup.LayoutParams params = getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }

        mLoadingMessageTV = findViewById(R.id.tv_loading_dialog_message);

        //Sets the loading message in the textview if it exists
        if (mLoadingMessage != null && !mLoadingMessage.isEmpty()) {
            mLoadingMessageTV.setText(mLoadingMessage);
        }
    }


    /**
     * Changes the loading message
     * @param loadingMessage - loading message
     */
    public void updateLoadingMessage(String loadingMessage) {
        mLoadingMessageTV.setText(loadingMessage);
    }
}
