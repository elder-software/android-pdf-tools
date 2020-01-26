package com.eldersoftware.pdfassist.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class YesNoDialog {
    public interface YesNoDialogCallback {
        void yesNoCallback(boolean yesSelected);
    }

    public static void showYesNoDialog(final Context context, String title, String message,
                                       final YesNoDialogCallback callback) {

        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        callback.yesNoCallback(true);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        callback.yesNoCallback(false);
                        break;
                }
            }
        };

        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();

    }
}
