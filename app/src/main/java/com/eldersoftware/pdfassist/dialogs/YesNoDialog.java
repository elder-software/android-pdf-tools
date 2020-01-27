package com.eldersoftware.pdfassist.dialogs;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

/**
 * Simple class to invoke the native android alert dialog, used in this case for a yes and no
 */
public class YesNoDialog {

    //Callback invoked when yes or no clicked
    public interface YesNoDialogCallback {
        void yesNoCallback(boolean yesSelected);
    }

    /**
     * Shows the dialog and sets a click listener for it
     * @param context - context
     * @param title - dialogs title
     * @param message - dialogs message
     * @param callback - callback funtion
     */
    public static void showYesNoDialog(final Context context,
                                       String title,
                                       String message,
                                       final YesNoDialogCallback callback) {

        //Click listener for the callback
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

        //Shows the dialog using the current context
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }
}
