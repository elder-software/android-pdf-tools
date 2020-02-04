package com.eldersoftware.pdfassist.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.eldersoftware.pdfassist.utils.ProviderUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Class containing code executed to create a pdf and save to a file.
 */
public class CreateDocLayout {
    String[] mPageTitles, mPageImages, mPageText;
    Context mContext;
    String mTitleText;

    private PdfDocument mDocument = new PdfDocument();

    // Margin and dimension constants
    private static final int PAGE_WIDTH = 595;
    private static final int PAGE_HEIGHT = 842;

    private static final int MARGIN_LEFT = 60;
    private static final int MARGIN_RIGHT = PAGE_WIDTH - 60;
    private static final int MARGIN_HALF = PAGE_WIDTH / 2;
    private static final int MARGIN_QUARTER = PAGE_WIDTH / 4;
    private static final int MARGIN_THREE_QUARTER = MARGIN_HALF + MARGIN_QUARTER;

    private static final int MARGIN_TOP = 60;

    private static final int IMAGE_HEIGHT = 300;
    private static final int IMAGE_PADDING = 40;


    private Paint mWhitePaint, mBlackPaint;
    private TextPaint mTitleTextPaint, mStandardTextPaint;


    /**
     * Constructor for the Doc Layout. Also initialises the various Paints and TextPaints that
     * are to be used by the doc
     * @param context - context
     * @param allPageData - all page data
     * @param titleText - title text
     */
    public CreateDocLayout(Context context, ArrayList<String[]> allPageData, String titleText) {
        mPageTitles = allPageData.get(ProviderUtils.PAGE_NAMES_INDEX);
        mPageImages = allPageData.get(ProviderUtils.PAGE_IMAGES_INDEX);
        mPageText = allPageData.get(ProviderUtils.PAGE_TEXT_INDEX);
        mContext = context;
        mTitleText = titleText;

        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.parseColor("#FFFFFF"));
        mBlackPaint = new Paint();
        mBlackPaint.setColor(Color.BLACK);
        mBlackPaint.setStyle(Paint.Style.STROKE);

        mTitleTextPaint = new TextPaint(mBlackPaint);
        mTitleTextPaint.setTextSize(30);
        mTitleTextPaint.setTextAlign(TextPaint.Align.LEFT);
        mTitleTextPaint.setStyle(Paint.Style.FILL);

        mStandardTextPaint = new TextPaint(mBlackPaint);
        mStandardTextPaint.setStyle(Paint.Style.FILL);
        mStandardTextPaint.setTextSize(12);
        mStandardTextPaint.setTextAlign(TextPaint.Align.LEFT);
    }


    /**
     * Executes the functions that create the pages and creates a FileOutputStream
     * to save the file to the given destination
     * @param file File path for the PDF
     */
    public void saveToFile(File file) {
        createTitlePage();
        createContentPages();

        try {
            mDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates the Title page with the doc name on it
     */
    private void createTitlePage() {
        // Start page and get page canvas
        PdfDocument.PageInfo titlePageInfo =
                new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page titlePage = mDocument.startPage(titlePageInfo);
        Canvas canvas = titlePage.getCanvas();
        canvas.drawPaint(mWhitePaint);

        StaticLayout title = createMultilineTextStaticLayout(mTitleText, mTitleTextPaint,
                MARGIN_LEFT, MARGIN_RIGHT, Layout.Alignment.ALIGN_CENTER);
        drawMultilineText(title, MARGIN_LEFT, MARGIN_TOP, canvas);

        mDocument.finishPage(titlePage);
    }


    /**
     * Loops through the page data and creates the respective pages
     */
    private void createContentPages() {
        int currentPageYPos = 0; // Stores where the bottom of each page info finishes
        for (int i = 0; i < mPageTitles.length; i++) {
            // Start page and get page canvas
            PdfDocument.PageInfo imageTextPageInfo =
                    new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, i + 2)
                            .create();
            PdfDocument.Page contentPage = mDocument.startPage(imageTextPageInfo);
            Canvas canvas = contentPage.getCanvas();

            // Draws the title text and moves the currentPageYPos to the bottom of it
            StaticLayout pageTitle = createMultilineTextStaticLayout(mPageTitles[i], mTitleTextPaint,
                    MARGIN_LEFT, MARGIN_RIGHT, Layout.Alignment.ALIGN_CENTER);
            drawMultilineText(pageTitle, MARGIN_LEFT, MARGIN_TOP, canvas);
            currentPageYPos = MARGIN_TOP + pageTitle.getHeight() + IMAGE_PADDING;

            // Checks if there is an image to draw on the page and draws it if exists
            if (!mPageImages[i].equals(ProviderUtils.PROVIDER_NULL)) {
                RectF imageBoundsRect = new RectF(MARGIN_LEFT, currentPageYPos, MARGIN_RIGHT,
                        currentPageYPos + IMAGE_HEIGHT);
                Bitmap pageImage = retrieveAndScaleImageFromFile(mPageImages[i]);
                RectF imageRect = createBitmapContainer(imageBoundsRect, pageImage);
                canvas.drawBitmap(pageImage, null, imageRect, null);
                currentPageYPos += IMAGE_HEIGHT + IMAGE_PADDING;
            }

            // Checks if there is text to draw on the page and draws it if exists
            if (!mPageText[i].equals(ProviderUtils.PROVIDER_NULL)) {
                StaticLayout pageText = createMultilineTextStaticLayout(mPageText[i], mStandardTextPaint,
                        MARGIN_LEFT, MARGIN_RIGHT, Layout.Alignment.ALIGN_NORMAL);
                drawMultilineText(pageText, MARGIN_LEFT, currentPageYPos, canvas);
            }

            mDocument.finishPage(contentPage);
        }
    }


    /**
     * Uses a RectF as a container for the inputBitmap to create a rectangle that fits into
     * the container rectangle and doesn't stretch or distort the image
     * @param container - container for bitmap bounds
     * @param inputBitmap - bitmap to be contained
     * @return RectF with an aspect ratio that matches the inputBitmap without being larger than
     * the rectangle container
     */
    private RectF createBitmapContainer(RectF container, Bitmap inputBitmap) {
        // Retrieve the bitmap and container dimensions
        float bWidth = inputBitmap.getWidth();
        float bHeight = inputBitmap.getHeight();
        float containerRectWidth = container.width();
        float containerRectHeight = container.height();

        // Ratio of the container against the bitmap width
        float widthRatio = containerRectWidth / bWidth;

        // Get the final image container size
        float imageContainerX = bWidth * widthRatio;
        float imageContainerY = bHeight * (widthRatio);

        // In case the image is in a portrait orientation that is larger the container
        if (imageContainerY > containerRectHeight) {
            float heightRatio = containerRectHeight / imageContainerY;

            imageContainerX = imageContainerX * heightRatio;
            imageContainerY = imageContainerY * heightRatio;
        }

        // Creates the parameters used by the RectF constructor
        float left = container.centerX() - (imageContainerX / 2);
        float right = left + imageContainerX;
        float top = container.centerY() - (imageContainerY / 2);
        float bottom = top + imageContainerY;

        return new RectF(left, top, right, bottom);
    }


    public static int getCameraPhotoOrientation(Context context, String imageUri) {
        Uri uri; // the URI you've received from the other app

        try {
            ExifInterface exifInterface = new ExifInterface(imageUri);
            // Extract Exif tag assuming the image is a JPEG or supported raw format
            String orientString = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

            return rotationAngle;

        } catch (IOException e) {
            // Handle any errors
        }

        return -1;
    }


    /**
     * Uses image file to store the image as a Bitmap
     * @param file image file
     * @return Bitmap of the image to be used
     */
    private Bitmap retrieveAndScaleImageFromFile(String file) {
        try {
            int rotation = getCameraPhotoOrientation(mContext, file); // Gets Exif orientation

            // Bitmap options used for decoding file
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inSampleSize = 1;
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inDither = false;
            bmOptions.inScaled = false;
            bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap outputBitmap = BitmapFactory.decodeFile(file, bmOptions);

            // Scales bitmap to ensure that there is enough memory to avoid the app crashing
            int width = outputBitmap.getWidth();
            int height = outputBitmap.getHeight();
            float scale = 1f;
            scale = 400f / width;

            Matrix matrix = new Matrix();
            matrix.setScale(scale, scale);
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(outputBitmap, 0, 0, outputBitmap.getWidth(),
                    outputBitmap.getHeight(), matrix, false);
        } catch (NullPointerException e) {
            // Returns empty white Bitmap if no file is found
            return Bitmap.createBitmap(100, 100, Bitmap.Config.ALPHA_8);
        }
    }


    /**
     * Static layouts are the best way to create a multiline text on the page canvas. This
     * function returns a static layout with the text
     * @param text - text
     * @param textPaint - text colour/size/font etc
     * @param left - left bound for static layout
     * @param right - right bound for static layout
     * @param alignment - text alignment
     * @return - StaticLayout with the multiline text
     */
    private StaticLayout createMultilineTextStaticLayout(String text,
                                                         TextPaint textPaint,
                                                         int left, int right, Layout.Alignment alignment) {
        int width = right - left;
        float spacingMultiplier = 1;
        float spacingAddition = 0;
        boolean includePadding = false;

        return new StaticLayout(text, textPaint, width, alignment, spacingMultiplier,
                spacingAddition, includePadding);
    }


    /**
     * Uses canvas to draw a static layout for text
     * @param text - text
     * @param left - left start point for static layout
     * @param top - top start point for static layout
     * @param canvas - page canvas
     */
    private void drawMultilineText(StaticLayout text, int left, int top, Canvas canvas) {
        canvas.save();
        canvas.translate(left, top);
        text.draw(canvas);
        canvas.restore();
    }
}
