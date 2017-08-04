package me.anky.connectid;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import me.anky.connectid.data.source.local.ConnectidColumns;

/**
 * Created by Anky An on 28/07/2017.
 * anky25@gmail.com
 */

public class Utilities {
    private static final Random random = new Random();
    private static final String CHARS = "abcdefghijkmnopqrstuvwxyz";
    public static final String SORTBY = "sortby";

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

        final int desiredSize = 800;
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
            null,
            ConnectidColumns._ID + " DESC",
            ConnectidColumns._ID + " ASC",
            ConnectidColumns.FIRST_NAME + " COLLATE NOCASE ASC",
            ConnectidColumns.FIRST_NAME + " COLLATE NOCASE DESC",
            ConnectidColumns.LAST_NAME + " COLLATE NOCASE ASC",
            ConnectidColumns.LAST_NAME + " COLLATE NOCASE DESC"
    };
}
