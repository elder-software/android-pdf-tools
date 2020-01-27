package com.eldersoftware.pdfassist.dialogs;

import android.app.Dialog;
import android.content.Context;
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
 * Dialog used to create a new doc, or to edit the name of an existing doc.
 * It is a seperate file from the CreateEditTextDialog as this dialog may change,
 * eg: an EditText may be added for author name
 */
public class CreateEditDocDialog extends Dialog {
    private EditText mDocNameET;
    private Button mCloseButton, mSaveButton;
    private Context mContext;
    private String mExistingDocName;
    private TextView mTitle;
    private SuccessCallback mSuccessCallback;

    /**
     * Constructor
     * @param context - context
     * @param existingDocName - used when updating an existing doc
     * @param successCallback - for interfacing with the activity when a doc is successfully
     *                        created/edited
     */
    public CreateEditDocDialog(@NonNull Context context,
                               @Nullable String existingDocName,
                               @NonNull SuccessCallback successCallback) {
        super(context);
        mContext = context;
        mExistingDocName = existingDocName;
        mSuccessCallback = successCallback;
    }

    //For when a doc is successfully created/updated.
    public interface SuccessCallback {
        void success();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //Layout includes title already
        setContentView(R.layout.dialog_create_doc);

        //Sets width and height layout params in onCreate, otherwise the dialog won't
        //use the entire screen width
        Window window = getWindow();
        if (window != null) {
            ViewGroup.LayoutParams params = getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }

        mDocNameET = findViewById(R.id.et_create_doc_name);
        mCloseButton = findViewById(R.id.b_dialog_create_doc_close);
        mSaveButton = findViewById(R.id.b_dialog_create_doc_save);
        mTitle = findViewById(R.id.et_dialog_create_doc_title);

        //Changes title to "Edit Doc" if we are modifying an existing doc and sets the mDocNameET
        //to the existing doc name
        String title = "Create Doc";
        if (mExistingDocName != null && !mExistingDocName.isEmpty()) {
            mDocNameET.setText(mExistingDocName);
            title = "Edit Doc";
        }
        mTitle.setText(title);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String docName = mDocNameET.getText().toString();
                String createDocStatus; //Indicates a success/error for creating/editing a doc name

                //If mExistingDocName exists, then we are updating an existing doc,
                //else we are creating a new doc
                if (mExistingDocName != null && !mExistingDocName.isEmpty()) {
                    createDocStatus = ProviderUtils.updateDoc(mExistingDocName, docName, mContext);
                } else {
                    createDocStatus = ProviderUtils.createDoc(docName, mContext);
                }

                //Dismisses the dialog and shows success toast if successfully created/updated,
                //else, shows toast with the error message
                if (createDocStatus.equals(ProviderUtils.CREATE_DOC_SUCCESS) ||
                        createDocStatus.equals(ProviderUtils.UPDATE_DOC_SUCCESS)) {
                    Toast.makeText(mContext, createDocStatus + docName, Toast.LENGTH_SHORT).show();
                    dismiss();
                    mSuccessCallback.success();
                } else {
                    Toast.makeText(mContext, createDocStatus, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
