package me.anky.connectid.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsLocalRepository implements ConnectionsDataSource {

    private final List<ConnectidConnection> connections = new ArrayList<>();
    private ConnectidConnection connection;

    private final List<ConnectionTag> tags = new ArrayList<>();
    private ConnectionTag connectionTag;

    private Context context;

    public ConnectionsLocalRepository(Context context) {
        this.context = context;
    }

    @Override
    public Single<List<ConnectidConnection>> getConnections(int menOption) {
        prepareConnectionsList(menOption);

        return Single.fromCallable(new Callable<List<ConnectidConnection>>() {
            @Override
            public List<ConnectidConnection> call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                return connections;
            }
        });
    }

    @Override
    public Single<ConnectidConnection> getOneConnection(int data_id) {
        Uri uri = ConnectidProvider.Connections.withId(data_id);

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.getColumnCount() != 0) {
            if (cursor.moveToFirst()) {
                String firstName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.LAST_NAME));
                String imageName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.IMAGE_NAME));
                String meetVenue = cursor.getString(cursor.getColumnIndex(ConnectidColumns.MEET_WHERE));
                String appearance = cursor.getString(cursor.getColumnIndex(ConnectidColumns.APPEARANCE));
                String feature = cursor.getString(cursor.getColumnIndex(ConnectidColumns.FEATURE));
                String commonFriends = cursor.getString(cursor.getColumnIndex(ConnectidColumns.COMMON_FRIENDS));
                String description = cursor.getString(cursor.getColumnIndex(ConnectidColumns.DESCRIPTION));
                String tags = cursor.getString(cursor.getColumnIndex(ConnectidColumns.TAGS));

                connection = new ConnectidConnection(data_id, firstName, lastName, imageName,
                        meetVenue, appearance, feature, commonFriends, description, tags);
            }
        }

        return Single.fromCallable(new Callable<ConnectidConnection>() {
            @Override
            public ConnectidConnection call() throws Exception {
                return connection;
            }
        });
    }

    private void prepareConnectionsList(int menOption) {
        connections.clear();

        Cursor cursor = getAllEntries(menOption);
        if (cursor != null && cursor.getCount() != 0) {

            while (cursor.moveToNext()) {
                int databaseId = cursor.getInt(cursor.getColumnIndex(ConnectidColumns._ID));
                String firstName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.FIRST_NAME));
                String lastName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.LAST_NAME));
                String imageName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.IMAGE_NAME));
                String meetVenue = cursor.getString(cursor.getColumnIndex(ConnectidColumns.MEET_WHERE));
                String appearance = cursor.getString(cursor.getColumnIndex(ConnectidColumns.APPEARANCE));
                String feature = cursor.getString(cursor.getColumnIndex(ConnectidColumns.FEATURE));
                String commonFriends = cursor.getString(cursor.getColumnIndex(ConnectidColumns.COMMON_FRIENDS));
                String description = cursor.getString(cursor.getColumnIndex(ConnectidColumns.DESCRIPTION));
                String tags = cursor.getString(cursor.getColumnIndex(ConnectidColumns.TAGS));

                connections.add(new ConnectidConnection(databaseId, firstName, lastName, imageName,
                        meetVenue, appearance, feature, commonFriends, description, tags));
            }
        }
    }

    private Cursor getAllEntries(int menOption) {
        return context.getContentResolver().query(
                ConnectidProvider.Connections.CONTENT_URI,
                null,
                null,
                null,
                Utilities.SORT_ORDER_OPTIONS[menOption]);
    }

    private void deleteAllEntries() {
        context.getContentResolver().delete(
                ConnectidProvider.Connections.CONTENT_URI,
                null,
                null);
    }

    @Override
    public int insertNewConnection(ConnectidConnection newConnection) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ConnectidColumns.FIRST_NAME, newConnection.getFirstName());
        contentValues.put(ConnectidColumns.LAST_NAME, newConnection.getLastName());
        contentValues.put(ConnectidColumns.IMAGE_NAME, newConnection.getImageName());
        contentValues.put(ConnectidColumns.MEET_WHERE, newConnection.getMeetVenue());
        contentValues.put(ConnectidColumns.APPEARANCE, newConnection.getAppearance());
        contentValues.put(ConnectidColumns.FEATURE, newConnection.getFeature());
        contentValues.put(ConnectidColumns.COMMON_FRIENDS, newConnection.getCommonFriends());
        contentValues.put(ConnectidColumns.DESCRIPTION, newConnection.getDescription());
        contentValues.put(ConnectidColumns.TAGS, newConnection.getTags());

        Uri uri = context.getContentResolver().insert(ConnectidProvider.Connections.CONTENT_URI, contentValues);

        return generateResultCode(uri);
    }

    @Override
    public int deleteConnection(int databaseId) {
        Uri uri = ConnectidProvider.Connections.withId(databaseId);

        return context.getContentResolver().delete(uri, null, null);
    }

    @Override
    public int updateConnection(ConnectidConnection connection) {
        Uri uri = ConnectidProvider.Connections.withId(connection.getDatabaseId());

        ContentValues contentValues = new ContentValues();
        contentValues.put(ConnectidColumns.FIRST_NAME, connection.getFirstName());
        contentValues.put(ConnectidColumns.LAST_NAME, connection.getLastName());
        contentValues.put(ConnectidColumns.IMAGE_NAME, connection.getImageName());
        contentValues.put(ConnectidColumns.MEET_WHERE, connection.getMeetVenue());
        contentValues.put(ConnectidColumns.APPEARANCE, connection.getAppearance());
        contentValues.put(ConnectidColumns.FEATURE, connection.getFeature());
        contentValues.put(ConnectidColumns.COMMON_FRIENDS, connection.getCommonFriends());
        contentValues.put(ConnectidColumns.DESCRIPTION, connection.getDescription());
        contentValues.put(ConnectidColumns.TAGS, connection.getTags());

        return context.getContentResolver().update(uri, contentValues, null, null);
    }


    @Override
    public Single<List<ConnectionTag>> getTags() {
        prepareTagsList();

        return Single.fromCallable(new Callable<List<ConnectionTag>>() {
            @Override
            public List<ConnectionTag> call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                return tags;
            }
        });
    }

    private void prepareTagsList() {
        tags.clear();

        Cursor cursor = context.getContentResolver().query(
                ConnectidProvider.Tags.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (cursor != null && cursor.getCount() != 0) {

            while (cursor.moveToNext()) {
                int databaseId = cursor.getInt(cursor.getColumnIndex(TagsColumns._ID));
                String tag = cursor.getString(cursor.getColumnIndex(TagsColumns.TAG));
                String connectionIds = cursor.getString(cursor.getColumnIndex(TagsColumns.CONNECTION_IDS));

                tags.add(new ConnectionTag(databaseId, tag, connectionIds));
            }
        }
    }

    @Override
    public Single<ConnectionTag> getOneTag(int data_id) {
        Uri uri = ConnectidProvider.Tags.withId(data_id);

        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null && cursor.getColumnCount() != 0) {
            if (cursor.moveToFirst()) {
                String tag = cursor.getString(cursor.getColumnIndex(TagsColumns.TAG));
                String connectionIds = cursor.getString(cursor.getColumnIndex(TagsColumns.CONNECTION_IDS));

                connectionTag = new ConnectionTag(data_id, tag, connectionIds);
            }
        }

        return Single.fromCallable(new Callable<ConnectionTag>() {
            @Override
            public ConnectionTag call() throws Exception {
                return connectionTag;
            }
        });
    }

    @Override
    public int insertNewTag(ConnectionTag newTag) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TagsColumns.TAG, newTag.getTag());
        contentValues.put(TagsColumns.CONNECTION_IDS, newTag.getConnection_ids());

        Uri uri = context.getContentResolver().insert(ConnectidProvider.Tags.CONTENT_URI, contentValues);

        return generateResultCode(uri);
    }

    @Override
    public int updateTag(ConnectionTag tag) {
        Uri uri = ConnectidProvider.Tags.withId(tag.getDatabaseId());

        ContentValues contentValues = new ContentValues();
        contentValues.put(TagsColumns.TAG, tag.getTag());
        contentValues.put(TagsColumns.CONNECTION_IDS, tag.getConnection_ids());

        return context.getContentResolver().update(uri, contentValues, null, null);
    }

    private int generateResultCode(Uri uri) {

        int lastPathSegment = Integer.parseInt(uri.getLastPathSegment());
        if (lastPathSegment == -1) {
            return -1;
        } else {
            return 1;
        }
    }
}
