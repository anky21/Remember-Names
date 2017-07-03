package me.anky.connectid.data;


import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

@ContentProvider(authority = ConnectidProvider.AUTHORITY, database = ConnectidDatabase.class)
public class ConnectidProvider {

    public static final String AUTHORITY =
            "me.anky.connectid.data.ConnectidProvider";
    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String CONNECTIONS = "connections";
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

        // TODO: MIME type may become "vnd.android.cursor.dir/contact"
        @ContentUri(
                path = Path.CONNECTIONS,
                type = "vnd.android.cursor.dir/connection",
                defaultSort = ConnectidColumns._ID + " ASC")
        public static final Uri CONTENT_URI = buildUri(Path.CONNECTIONS);

        // TODO: MIME type may become "vnd.android.cursor.dir/contact"
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
}
