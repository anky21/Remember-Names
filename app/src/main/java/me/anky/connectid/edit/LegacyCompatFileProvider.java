package me.anky.connectid.edit;

import android.database.Cursor;
import android.net.Uri;
import androidx.core.content.FileProvider;

import com.commonsware.cwac.provider.LegacyCompatCursorWrapper;

/**
 * Created by Anky An on 3/08/2017.
 * anky25@gmail.com
 */

public class LegacyCompatFileProvider extends FileProvider {
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return(new LegacyCompatCursorWrapper(super.query(uri, projection, selection, selectionArgs, sortOrder)));
    }
}
