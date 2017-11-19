package me.anky.connectid;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import me.anky.connectid.data.source.local.ConnectidColumns;
import me.anky.connectid.root.ConnectidApplication;

/**
 * Created by Anky An on 28/07/2017.
 * anky25@gmail.com
 */

public class Utilities {
    private static final Random random = new Random();
    private static final String CHARS = "abcdefghijkmnopqrstuvwxyz";
    public static final String SORTBY = "sortby";
    public static final int TAG_BASE_NUMBER = 1000;
    private static ConnectidApplication application;
    private static FirebaseAnalytics mFirebaseAnalytics;


    // Load image from internal storage
    public static Bitmap loadImageFromStorage(String imageName, String path) {
        Bitmap bitmap = null;
        try {
            File f = new File(path, imageName);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // Generate a 6 character token
    public static String generateImageName() {
        int length = 6;
        StringBuilder token = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            token.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return token.toString() + ".jpg";
    }

    // Save Bitmap to internal storage
    public static void saveToInternalStorage(Context context, Bitmap bitmapImage, String imageName) {
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File file = new File(directory, imageName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap resizeBitmap(Bitmap bitmap) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        final int desiredSize = 600;
        int maximumSize = Math.max(originalHeight, originalWidth);

        if (maximumSize > desiredSize) {
            float ratio = (float) desiredSize / maximumSize;
            int newWidth = Math.round(originalWidth * ratio);
            int newHeight = Math.round(originalHeight * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return bitmap;
    }

    // Sort by options in MainActivity
    public static final String[] SORT_ORDER_OPTIONS = {
            ConnectidColumns._ID + " DESC",
            ConnectidColumns._ID + " DESC",
            ConnectidColumns._ID + " ASC",
            ConnectidColumns.FIRST_NAME + " COLLATE NOCASE ASC",
            ConnectidColumns.FIRST_NAME + " COLLATE NOCASE DESC",
            ConnectidColumns.LAST_NAME + " COLLATE NOCASE ASC",
            ConnectidColumns.LAST_NAME + " COLLATE NOCASE DESC"
    };

    public static void hideKeyboard(Activity activity, View view) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void showKeyboard(Activity activity, View view) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);

            if (inputManager != null) {
                inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
        }
    }

    public static String createStringFromList(List<String> ids) {
        if (ids == null || ids.size() == 0){
            return null;
        } else {
            String idString = ids.toString();
            idString = idString.substring(1, idString.length() - 1);

            return idString;
        }
    }

    // Display tags in DetailActivity && EditActivity
    public static void displayTags(Context context, List<String> tags, RelativeLayout layout) {
        layout.removeAllViews();
        int containerWidth = layout.getMeasuredWidth() - 16;
        int i = 0;

        int count = 0;
        int currentWidth = 0;
        boolean isNewLine;
        boolean isFirstLine = true;

        for (String tag : tags) {
            TextView tagTv = new TextView(context);
            tagTv.setId(TAG_BASE_NUMBER + i);
            tagTv.setText(tag);
            tagTv.setTextSize(14);
            tagTv.setEllipsize(TextUtils.TruncateAt.END);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 4, 8, 4);
            tagTv.setMaxLines(1);
            tagTv.setLayoutParams(params);

            tagTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            tagTv.setBackgroundResource(R.drawable.round_bg_blue);

            tagTv.measure(0, 0);

            int width = tagTv.getMeasuredWidth();

            if (currentWidth + width < containerWidth) {
                currentWidth += width + 16;
                isNewLine = false;
                count++;
            } else {
                currentWidth = width + 16;
                isNewLine = true;
                isFirstLine = false;
                count = 1;
            }

            // Add TextView to the screen
            if (i == 0) {
                params.addRule(RelativeLayout.ALIGN_START);
                tagTv.setLayoutParams(params);
                layout.addView(tagTv);
            } else if (isNewLine) {
                params.addRule(RelativeLayout.ALIGN_LEFT);
                params.addRule(RelativeLayout.BELOW, TAG_BASE_NUMBER - 1 + i);
                tagTv.setLayoutParams(params);
                layout.addView(tagTv);
            } else if (isFirstLine) {
                params.addRule(RelativeLayout.RIGHT_OF, TAG_BASE_NUMBER - 1 + i);
                tagTv.setLayoutParams(params);
                layout.addView(tagTv);
            } else {
                params.addRule(RelativeLayout.RIGHT_OF, TAG_BASE_NUMBER - 1 + i);
                params.addRule(RelativeLayout.BELOW, TAG_BASE_NUMBER - count + i);
                tagTv.setLayoutParams(params);
                layout.addView(tagTv);
            }

            i++;
        }
    }

    /**
     *
     * @param event Content of the events
     * @param origin Activity + class name
     */
    public static void logFirebaseEvents(String event, String origin) {
        application = ConnectidApplication.getAppInstance();
        mFirebaseAnalytics = application.getAnalyticsInstance();

        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ORIGIN, origin);
        mFirebaseAnalytics.logEvent(event, params);
    }

    /**
     *
     * @param event Type of errors
     * @param origin Activity + class name
     * @param msg Error message
     */
    public static void logFirebaseError(String event, String origin, String msg) {
        application = ConnectidApplication.getAppInstance();
        mFirebaseAnalytics = application.getAnalyticsInstance();

        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ORIGIN, origin);
        params.putString(FirebaseAnalytics.Param.ITEM_NAME, msg);
        mFirebaseAnalytics.logEvent(event, params);
    }
}
