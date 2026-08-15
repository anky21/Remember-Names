package me.anky.connectid;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
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
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import me.anky.connectid.data.source.local.ConnectidColumns;
import me.anky.connectid.root.ConnectidApplication;

/**
 * Created by Anky An on 28/07/2017.
 * anky25@gmail.com
 */

public class Utilities {
    private static final int MAX_STORED_IMAGE_SIZE_PX = 2048;
    private static final String IMAGE_DIRECTORY = "imageDir";
    private static final String PREVIEW_SUFFIX = "_preview.jpg";
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

    // Generate a collision-resistant image name.
    public static String generateImageName() {
        return UUID.randomUUID().toString() + ".jpg";
    }

    // Save Bitmap to internal storage
    public static boolean saveToInternalStorage(Context context, Bitmap bitmapImage, String imageName) {
        if (bitmapImage == null || imageName == null || imageName.length() == 0) {
            return false;
        }

        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE);
        // Create imageDir
        File file = new File(directory, imageName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            return bitmapImage.compress(Bitmap.CompressFormat.JPEG, 95, fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        final int desiredSize = MAX_STORED_IMAGE_SIZE_PX;
        int maximumSize = Math.max(originalHeight, originalWidth);

        if (maximumSize > desiredSize) {
            float ratio = (float) desiredSize / maximumSize;
            int newWidth = Math.round(originalWidth * ratio);
            int newHeight = Math.round(originalHeight * ratio);
            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        return bitmap;
    }

    public static Bitmap decodeContactPhoto(Context context, Uri imageUri) throws IOException {
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream exifStream = context.getContentResolver().openInputStream(imageUri)) {
            if (exifStream != null) {
                orientation = new ExifInterface(exifStream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (IOException ignored) {
            // Some providers do not expose EXIF metadata; the image can still be decoded.
        }

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        decodeStream(context, imageUri, bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw new IOException("Unable to read selected image dimensions");
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        int maximumDimension = Math.max(bounds.outWidth, bounds.outHeight);
        options.inSampleSize = Math.max(1,
                (int) Math.ceil(maximumDimension / (MAX_STORED_IMAGE_SIZE_PX * 1.5d)));

        Bitmap decoded = decodeStream(context, imageUri, options);
        if (decoded == null) {
            throw new IOException("Unable to decode selected image");
        }

        Bitmap oriented = applyExifOrientation(decoded, orientation);
        if (oriented != decoded) {
            decoded.recycle();
        }

        Bitmap resized = resizeBitmap(oriented);
        if (resized != oriented) {
            oriented.recycle();
        }
        return resized;
    }

    public static File getContactImageFile(Context context, String imageName) {
        ContextWrapper cw = new ContextWrapper(context.getApplicationContext());
        return new File(cw.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE), imageName);
    }

    public static File getContactPreviewFile(Context context, String imageName) {
        return new File(getContactImageFile(context, imageName).getParentFile(),
                getContactPreviewImageName(imageName));
    }

    public static File getBestContactPreviewFile(Context context, String imageName) {
        File previewFile = getContactPreviewFile(context, imageName);
        return previewFile.exists() ? previewFile : getContactImageFile(context, imageName);
    }

    public static String getContactPreviewImageName(String imageName) {
        int extensionStart = imageName.lastIndexOf('.');
        String baseName = extensionStart > 0 ? imageName.substring(0, extensionStart) : imageName;
        return baseName + PREVIEW_SUFFIX;
    }

    private static Bitmap decodeStream(Context context, Uri imageUri,
                                       BitmapFactory.Options options) throws IOException {
        try (InputStream imageStream = context.getContentResolver().openInputStream(imageUri)) {
            if (imageStream == null) {
                throw new IOException("Unable to open selected image");
            }
            return BitmapFactory.decodeStream(imageStream, null, options);
        }
    }

    private static Bitmap applyExifOrientation(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180f);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90f);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90f);
                matrix.postScale(-1f, 1f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90f);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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
     * @param event Type of errors
     * @param origin Activity + class name
     */
    public static void logFirebaseError(String event, String origin) {
        application = ConnectidApplication.getAppInstance();
        mFirebaseAnalytics = application.getAnalyticsInstance();

        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ORIGIN, origin);
        mFirebaseAnalytics.logEvent(event, params);
    }

    /**
     *
     * @param event Content of the events
     */
    public static void logFirebaseEventWithNoParams(String event) {
        application = ConnectidApplication.getAppInstance();
        mFirebaseAnalytics = application.getAnalyticsInstance();

        mFirebaseAnalytics.logEvent(event, null);
    }
}
