package me.anky.connectid.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.OnUpgrade;
import net.simonvt.schematic.annotation.Table;

@Database(version = ConnectidDatabase.VERSION)
public final class ConnectidDatabase {
    private final static String COLUMN_ID = "_id";
    private final static String COLUMN_TAG = "tag";
    private final static String COLUMN_CONNECTION_IDS = "connection_ids";

    private ConnectidDatabase(){}

    public static final int VERSION = 2;

    @Table(ConnectidColumns.class)
    public static final String CONNECTIONS = "connections";

    @Table(TagsColumns.class)
    public static final String TAGS = "tags";

    private static final String DATABASE_CREATE_TAGS = "create table "
            + TAGS + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TAG + " string, "
            + COLUMN_CONNECTION_IDS + " string);";

    private static final String DATABASE_ALTER_CONNECTIONS = "ALTER TABLE "
            + CONNECTIONS + " ADD COLUMN " + TAGS + " string;";

    @OnUpgrade
    public static void onUpgrade(Context context, SQLiteDatabase db, int oldVersion,
                                 int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(DATABASE_ALTER_CONNECTIONS);
            db.execSQL(DATABASE_CREATE_TAGS);
        }
    }
}
