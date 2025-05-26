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
    public Single<List<ConnectidConnection>> getConnections(int menuOption) {
        prepareConnectionsList(menuOption);

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

        if (cursor != null) {
            cursor.close();
        }

        return Single.fromCallable(new Callable<ConnectidConnection>() {
            @Override
            public ConnectidConnection call() throws Exception {
                return connection;
            }
        });
    }

    private void prepareConnectionsList(int menuOption) {
        connections.clear();

        Cursor cursor = getAllEntries(menuOption);
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
        int databaseId = Integer.parseInt(uri.getLastPathSegment());
        return databaseId;
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

        return context.getContentResolver().update(uri, contentValues, null, null);
    }

    @Override
    public int updateConnection(int id, String tags) {
        Uri uri = ConnectidProvider.Connections.withId(id);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConnectidColumns.TAGS, tags);

        return context.getContentResolver().update(uri, contentValues, null, null);
    }

    @Override
    public int updateConnectionWithTags(ConnectidConnection connection) {
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

        if (cursor != null) {
            cursor.close();
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

        if (cursor != null) {
            cursor.close();
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

    @Override
    public int deleteTag(int databaseId) {
        Uri uri = ConnectidProvider.Tags.withId(databaseId);

        return context.getContentResolver().delete(uri, null, null);
    }

    @Override
    public void insertBulkTags(List<String> connectionTags, int databaseId) {
        ContentValues[] contentValuesArray = new ContentValues[connectionTags.size()];
        for (int i=0; i<connectionTags.size();i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TagsColumns.TAG, connectionTags.get(i));
            contentValues.put(TagsColumns.CONNECTION_IDS, databaseId);
            contentValuesArray[i] = contentValues;
        }

        context.getContentResolver().bulkInsert(ConnectidProvider.Tags.CONTENT_URI, contentValuesArray);
    }

    @Override
    public void insertBulkNewTags(List<String> connectionTags) {
        ContentValues[] contentValuesArray = new ContentValues[connectionTags.size()];
        for (int i=0; i<connectionTags.size();i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(TagsColumns.TAG, connectionTags.get(i));
            contentValuesArray[i] = contentValues;
        }

        context.getContentResolver().bulkInsert(ConnectidProvider.Tags.CONTENT_URI, contentValuesArray);
    }

    private int generateResultCode(Uri uri) {

        int lastPathSegment = Integer.parseInt(uri.getLastPathSegment());
        if (lastPathSegment == -1) {
            return -1;
        } else {
            return 1;
        }
    }

    // Helper method to update a connection's TAGS string
    private boolean updateConnectionTags(int connectionId, String newTags) {
        Uri uri = ConnectidProvider.Connections.withId(connectionId);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConnectidColumns.TAGS, newTags);
        int updatedRows = context.getContentResolver().update(uri, contentValues, null, null);
        return updatedRows > 0;
    }

    // Helper method to get a connection's current TAGS string
    private String getConnectionTags(int connectionId) {
        Uri uri = ConnectidProvider.Connections.withId(connectionId);
        Cursor cursor = context.getContentResolver().query(uri, new String[]{ConnectidColumns.TAGS}, null, null, null);
        String tags = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                tags = cursor.getString(cursor.getColumnIndex(ConnectidColumns.TAGS));
            }
            cursor.close();
        }
        return tags == null ? "" : tags;
    }

    // Helper method to get Tag ID and its associated connection IDs
    private ConnectionTag getTagDataByName(String tagName) {
        Cursor cursor = context.getContentResolver().query(
                ConnectidProvider.Tags.CONTENT_URI,
                null,
                TagsColumns.TAG + " = ?",
                new String[]{tagName},
                null);

        ConnectionTag tagData = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int id = cursor.getInt(cursor.getColumnIndex(TagsColumns._ID));
                String currentConnectionIds = cursor.getString(cursor.getColumnIndex(TagsColumns.CONNECTION_IDS));
                tagData = new ConnectionTag(id, tagName, currentConnectionIds);
            }
            cursor.close();
        }
        return tagData;
    }

    // Helper method to insert a new tag and return its ID (or URI's last segment)
    private int insertNewTagByName(String tagName, String initialConnectionId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TagsColumns.TAG, tagName);
        contentValues.put(TagsColumns.CONNECTION_IDS, String.valueOf(initialConnectionId)); // Start with the first connection ID
        Uri uri = context.getContentResolver().insert(ConnectidProvider.Tags.CONTENT_URI, contentValues);
        if (uri != null) {
            return Integer.parseInt(uri.getLastPathSegment());
        }
        return -1; // Error
    }

    // Helper method to update a tag's CONNECTION_IDS string
    private boolean updateTagConnectionIds(int tagId, String newConnectionIds) {
        Uri uri = ConnectidProvider.Tags.withId(tagId);
        ContentValues contentValues = new ContentValues();
        contentValues.put(TagsColumns.CONNECTION_IDS, newConnectionIds);
        int updatedRows = context.getContentResolver().update(uri, contentValues, null, null);
        return updatedRows > 0;
    }


    @Override
    public boolean batchAddTagToConnections(List<Integer> connectionIds, String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        // For ContentProvider, true atomicity across operations requires applyBatch.
        // Here, we'll do our best by updating records one by one.
        // This is not truly atomic if one of the later updates fails.

        ConnectionTag existingTag = getTagDataByName(tagName);
        int tagId;
        String currentTagConnectionIds = "";

        if (existingTag == null) {
            // If tag doesn't exist, create it. For simplicity, we'll add the first connectionId.
            // A more robust way would be to insert the tag first, then update its connection_ids.
            // However, insertNewTagByName is designed to take an initial ID.
            // Let's assume connectionIds is not empty.
            if (connectionIds.isEmpty()) return false; // Cannot create a tag without any connection.

            // To avoid issues with insertNewTagByName needing an initial ID, and then having to merge,
            // let's handle it differently: insert tag with empty connections, then update.
            ContentValues newTagValues = new ContentValues();
            newTagValues.put(TagsColumns.TAG, tagName);
            newTagValues.put(TagsColumns.CONNECTION_IDS, ""); // Initially empty
            Uri newTagUri = context.getContentResolver().insert(ConnectidProvider.Tags.CONTENT_URI, newTagValues);
            if (newTagUri == null) return false; // Failed to insert new tag
            tagId = Integer.parseInt(newTagUri.getLastPathSegment());
            // existingTag = new ConnectionTag(tagId, tagName, ""); // re-assign
        } else {
            tagId = existingTag.getDatabaseId();
            currentTagConnectionIds = existingTag.getConnection_ids() == null ? "" : existingTag.getConnection_ids();
        }
        
        List<String> tagConnectionIdList = Utilities.getListFromCommaSeparatedString(currentTagConnectionIds);

        boolean overallSuccess = true;

        for (int connectionId : connectionIds) {
            // Update Connections table: Append tag to TAGS column
            String connectionTagsStr = getConnectionTags(connectionId);
            List<String> connectionTagsList = Utilities.getListFromCommaSeparatedString(connectionTagsStr);
            if (!connectionTagsList.contains(tagName)) {
                connectionTagsList.add(tagName);
                String newConnectionTagsStr = Utilities.getCommaSeparatedStringFromList(connectionTagsList);
                if (!updateConnectionTags(connectionId, newConnectionTagsStr)) {
                    overallSuccess = false; // Log this error or handle more gracefully
                }
            }

            // Update Tags table: Add connectionId to CONNECTION_IDS for this tag
            String connIdStr = String.valueOf(connectionId);
            if (!tagConnectionIdList.contains(connIdStr)) {
                tagConnectionIdList.add(connIdStr);
            }
        }
        
        // Update the tag's CONNECTION_IDS string once after processing all connections
        String finalTagConnectionIds = Utilities.getCommaSeparatedStringFromList(tagConnectionIdList);
        if (existingTag != null && existingTag.getConnection_ids() != null && existingTag.getConnection_ids().equals(finalTagConnectionIds)) {
            // No change in connection IDs for the tag, no need to update
        } else {
             if (!updateTagConnectionIds(tagId, finalTagConnectionIds)) {
                overallSuccess = false;
            }
        }

        return overallSuccess;
    }

    @Override
    public boolean batchRemoveTagFromConnections(List<Integer> connectionIds, String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }

        ConnectionTag tagToRemove = getTagDataByName(tagName);
        if (tagToRemove == null) {
            return true; // Tag doesn't exist, so effectively removed from all.
        }

        boolean overallSuccess = true;
        List<String> currentTagConnectionIdList = Utilities.getListFromCommaSeparatedString(tagToRemove.getConnection_ids());

        for (int connectionId : connectionIds) {
            // Update Connections table: Remove tag from TAGS column
            String connectionTagsStr = getConnectionTags(connectionId);
            List<String> connectionTagsList = Utilities.getListFromCommaSeparatedString(connectionTagsStr);
            if (connectionTagsList.contains(tagName)) {
                connectionTagsList.remove(tagName);
                String newConnectionTagsStr = Utilities.getCommaSeparatedStringFromList(connectionTagsList);
                if (!updateConnectionTags(connectionId, newConnectionTagsStr)) {
                    overallSuccess = false; // Log or handle
                }
            }

            // Remove from the list for the tag's CONNECTION_IDS
            currentTagConnectionIdList.remove(String.valueOf(connectionId));
        }

        // Update the tag's CONNECTION_IDS string
        String finalTagConnectionIds = Utilities.getCommaSeparatedStringFromList(currentTagConnectionIdList);
        if (!updateTagConnectionIds(tagToRemove.getDatabaseId(), finalTagConnectionIds)) {
            overallSuccess = false;
        }
        
        // Optional: If finalTagConnectionIds is empty, consider deleting the tag itself if it's an orphaned tag.
        // For now, we keep the tag.

        return overallSuccess;
    }
}
