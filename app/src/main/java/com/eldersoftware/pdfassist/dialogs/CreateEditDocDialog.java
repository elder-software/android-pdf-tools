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

public class CreateEditDocDialog extends Dialog {
    EditText mDocNameET;
    Button mCloseButton, mSaveButton;
    Context mContext;
    String mExistingJobName;
    TextView mTitle;
    SuccessCallback mSuccessCallback;


    public interface SuccessCallback {
        void success();
    }

    public CreateEditDocDialog(@NonNull Context context, @Nullable String existingJobName, @NonNull SuccessCallback successCallback) {
        super(context);
        mContext = context;
        mExistingJobName = existingJobName;
        mSuccessCallback = successCallback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_doc);
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

        String title = "Create Doc";
        if (mExistingJobName != null && !mExistingJobName.isEmpty()) {
            mDocNameET.setText(mExistingJobName);
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
                String createDocStatus;

                if (mExistingJobName != null && !mExistingJobName.isEmpty()) {
                    createDocStatus = ProviderUtils.updateDoc(mExistingJobName, docName, mContext);
                } else {
                    createDocStatus = ProviderUtils.createDoc(docName, mContext);
                }

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
