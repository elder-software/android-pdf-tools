package com.eldersoftware.pdfassist.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraUtils {
    public static final int REQUEST_CAMERA_AND_STORAGE_PERMISSIONS = 703;
    private static final String AUTHORITY_FORMAT =  "%s.fileprovider";

    /**
     * Checks to ensure that the user has given permission for the app to use both the
     * camera and external storage to capture and save images
     * @param context - context
     * @param activity - activity
     * @return Boolean if permissions were granted/are already granted
     */
    public static boolean checkCameraPermissions(Context context, Activity activity) {
        // Checking permissions changed after android version M
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // If permissions had been granted prior to attempting to capture/save an image
                return true;
            } else {
                // Request permissions if not already granted
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(activity, "External storage permission required to save images",
                            Toast.LENGTH_LONG).show();
                } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    Toast.makeText(activity, "Camera permission required to save images",
                            Toast.LENGTH_LONG).show();
                }
                activity.requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA}, REQUEST_CAMERA_AND_STORAGE_PERMISSIONS);
                return false;
            }
        } else {
            return true;
        }
    }


    /**
     * Calls the built in android app for capturing images
     * @param activity - activity
     * @param imageUri - uri where the image will be saved
     * @param activityResultCode - to be used by the onActivityResult in the activity the app was called from
     */
    public static void callCameraApp(Activity activity, Uri imageUri, int activityResultCode) {
        Intent callCameraAppIntent = new Intent();
        callCameraAppIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        callCameraAppIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        callCameraAppIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        activity.startActivityForResult(callCameraAppIntent, activityResultCode);
    }


    /**
     * Uses the doc name and a timestamp to create a directory and file for saving the image into
     * @param docName - doc name
     * @return - File with the image directory/file
     * @throws IOException
     */
    public static File createImageFile(String docName) throws IOException {
        // Time stampe and directory created
        String timeStamp = new SimpleDateFormat("ddMMyyyy_hhmmss", Locale.ENGLISH)
                .format(new Date());
        String externalDirectory = Environment.getExternalStorageDirectory().toString() +
                "/Android Pdf Tools/" + docName + "/Images";

        File directory = new File(externalDirectory);

        // Checks if the directory exists before creating file
        boolean bool = false;
        if (!directory.exists()) {
            bool = directory.mkdirs();
        }

        return new File(directory, timeStamp + ".jpg");
    }


    /**
     * The app needs a URI for the image to save
     * @param file - image file name
     * @param context - context
     * @return Uri for image
     */
    public static Uri makeUriUsingSdkVersion(File file, Context context) {
        Uri uri;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            String packageName = context.getPackageName();
            String authority = String.format(Locale.getDefault(), AUTHORITY_FORMAT, packageName);
            uri = FileProvider.getUriForFile(context, authority, file);
        }

        return uri;
    }

}
