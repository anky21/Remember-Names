package me.anky.connectid.data.source.local;


import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = ConnectidProvider.AUTHORITY, database = ConnectidDatabase.class)
public class ConnectidProvider {

    public static final String AUTHORITY =
            "me.anky.connectid.data.source.local.ConnectidProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String CONNECTIONS = "connections";
        String TAGS = "tags";
    }

    private static Uri buildUri(String ... paths){
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths){
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = ConnectidDatabase.CONNECTIONS)
    public static class Connections {

        @ContentUri(
                path = Path.CONNECTIONS,
                type = "vnd.android.cursor.dir/connection",
                defaultSort = ConnectidColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.CONNECTIONS);

        @InexactContentUri(
                name = "CONNECTION_ID",
                path = Path.CONNECTIONS + "/#",
                type = "vnd.android.cursor.item/connection",
                whereColumn = ConnectidColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id){
            return buildUri(Path.CONNECTIONS, String.valueOf(id));
        }
    }

    @TableEndpoint(table = ConnectidDatabase.TAGS)
    public static class Tags {

        @ContentUri(
                path = Path.TAGS,
                type = "vnd.android.cursor.dir/tag",
                defaultSort = TagsColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.TAGS);

        @InexactContentUri(
                name = "TAG_ID",
                path = Path.TAGS + "/#",
                type = "vnd.android.cursor.item/tag",
                whereColumn = TagsColumns._ID,
                pathSegment = 1)
        public static Uri withId(long id){
            return buildUri(Path.TAGS, String.valueOf(id));
        }
    }
}
