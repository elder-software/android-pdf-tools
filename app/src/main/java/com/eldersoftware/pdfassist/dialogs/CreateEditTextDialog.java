package com.eldersoftware.pdfassist.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.eldersoftware.pdfassist.R;
import com.eldersoftware.pdfassist.utils.ProviderUtils;

/**
 * Dialog for creating/editing a single text field. Used for the page names and page text
 */
public class CreateEditTextDialog extends Dialog {
    private EditText mEditText;
    private Button mCloseButton, mSaveButton;
    private String mExistingText;
    private String mTitleText;
    private TextView mTitle;
    private CreateEditTextCallback mSuccessCallback;
    private Uri mDocUri;

    /**
     * Constructor
     * @param context - context
     * @param title - dialogs title
     * @param existingText - used when modifying an existing field
     */
    public CreateEditTextDialog(@NonNull Context context,
                                @NonNull String title,
                                @Nullable String existingText) {
        super(context);
        mExistingText = existingText;
        mTitleText = title;
    }

    //Called when save button is tapped. Dismiss callback not needed
    public interface CreateEditTextCallback {
        void onSave(String newText);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //Layout includes title already
        setContentView(R.layout.dialog_create_page);

        //Sets width and height layout params in onCreate, otherwise the dialog won't
        //use the entire screen width
        Window window = getWindow();
        if (window != null) {
            ViewGroup.LayoutParams params = getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }

        mEditText = findViewById(R.id.et_create_page_name);
        mCloseButton = findViewById(R.id.b_dialog_create_page_close);
        mSaveButton = findViewById(R.id.b_dialog_create_page_save);
        mTitle = findViewById(R.id.et_dialog_create_page_title);

        mTitle.setText(mTitleText);

        //Sets the existing text in the EditText view (if not null)
        if (mExistingText != null && !mExistingText.isEmpty()) {
            mEditText.setText(mExistingText);
        }

        //Dismisses the dialog if clicked
        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        //Invokes the callback if clicked
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSuccessCallback.onSave(mEditText.getText().toString());
            }
        });
    }

    /**
     * Sets the callback method
     * @param callback - editTextCallback
     */
    public void setClickListener(CreateEditTextCallback callback) {
        mSuccessCallback = callback;
    }
}
