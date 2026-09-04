package me.anky.connectid.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsLocalRepository implements ConnectionsDataSource {

    private final List<ConnectionTag> tags = new ArrayList<>();
    private ConnectionTag connectionTag;

    private Context context;

    public ConnectionsLocalRepository(Context context) {
        this.context = context;
    }

    @Override
    public Single<List<ConnectidConnection>> getConnections(int menuOption) {
        return Single.fromCallable(() -> loadConnections(menuOption));
    }

    @Override
    public Single<ConnectidConnection> getOneConnection(int data_id) {
        return Single.fromCallable(() -> {
            Uri uri = ConnectidProvider.Connections.withId(data_id);
            try (Cursor cursor = context.getContentResolver().query(
                    uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return readConnection(cursor);
                }
                throw new IllegalStateException("Connection not found: " + data_id);
            }
        });
    }

    private List<ConnectidConnection> loadConnections(int menuOption) {
        List<ConnectidConnection> result = new ArrayList<>();
        try (Cursor cursor = getAllEntries(menuOption)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    result.add(readConnection(cursor));
                }
            }
        }
        return result;
    }

    private ConnectidConnection readConnection(Cursor cursor) {
        ConnectidConnection result = new ConnectidConnection(
                cursor.getInt(cursor.getColumnIndexOrThrow(ConnectidColumns._ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.FIRST_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.LAST_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.IMAGE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.MEET_WHERE)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.APPEARANCE)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.FEATURE)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.COMMON_FRIENDS)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.DESCRIPTION)),
                cursor.getString(cursor.getColumnIndexOrThrow(ConnectidColumns.TAGS)));
        result.setFlashcardBox(cursor.getInt(
                cursor.getColumnIndexOrThrow(ConnectidColumns.FLASHCARD_BOX)));
        result.setFlashcardLastReviewed(cursor.getLong(
                cursor.getColumnIndexOrThrow(ConnectidColumns.FLASHCARD_LAST_REVIEWED)));
        result.setFlashcardNextReview(cursor.getLong(
                cursor.getColumnIndexOrThrow(ConnectidColumns.FLASHCARD_NEXT_REVIEW)));
        result.setFlashcardAttempts(cursor.getInt(
                cursor.getColumnIndexOrThrow(ConnectidColumns.FLASHCARD_ATTEMPTS)));
        result.setFlashcardCorrect(cursor.getInt(
                cursor.getColumnIndexOrThrow(ConnectidColumns.FLASHCARD_CORRECT)));
        return result;
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
        String imageName = loadImageName(uri);
        int deletedRows = context.getContentResolver().delete(uri, null, null);
        if (deletedRows > 0) {
            try {
                removeConnectionFromTags(databaseId);
            } catch (RuntimeException error) {
                Utilities.logFirebaseError("error_cleanup_deleted_tags",
                        "ConnectionsLocalRepository.deleteConnection");
            }
            try {
                deleteUnusedImages(imageName);
            } catch (RuntimeException error) {
                Utilities.logFirebaseError("error_cleanup_deleted_photo",
                        "ConnectionsLocalRepository.deleteConnection");
            }
        }
        return deletedRows;
    }

    private String loadImageName(Uri connectionUri) {
        String[] columns = {ConnectidColumns.IMAGE_NAME};
        try (Cursor cursor = context.getContentResolver().query(
                connectionUri, columns, null, null, null)) {
            if (cursor != null && cursor.moveToFirst() && !cursor.isNull(0)) {
                return cursor.getString(0);
            }
        }
        return null;
    }

    private void removeConnectionFromTags(int databaseId) {
        List<ConnectionTag> changedTags = new ArrayList<>();
        try (Cursor cursor = context.getContentResolver().query(
                ConnectidProvider.Tags.CONTENT_URI, null, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int tagId = cursor.getInt(cursor.getColumnIndexOrThrow(TagsColumns._ID));
                    String tag = cursor.getString(cursor.getColumnIndexOrThrow(TagsColumns.TAG));
                    String oldIds = cursor.getString(
                            cursor.getColumnIndexOrThrow(TagsColumns.CONNECTION_IDS));
                    String newIds = ConnectionIdList.remove(oldIds, databaseId);
                    if (!sameValue(oldIds, newIds)) {
                        changedTags.add(new ConnectionTag(tagId, tag, newIds));
                    }
                }
            }
        }
        for (ConnectionTag tag : changedTags) {
            updateTag(tag);
        }
    }

    private void deleteUnusedImages(String imageName) {
        if (imageName == null || imageName.trim().isEmpty()
                || "blank_profile.jpg".equals(imageName)
                || !new File(imageName).getName().equals(imageName)) {
            return;
        }
        String[] columns = {ConnectidColumns._ID};
        try (Cursor cursor = context.getContentResolver().query(
                ConnectidProvider.Connections.CONTENT_URI,
                columns,
                ConnectidColumns.IMAGE_NAME + " = ?",
                new String[]{imageName},
                null)) {
            if (cursor != null && cursor.getCount() > 0) {
                return;
            }
        }

        File image = Utilities.getContactImageFile(context, imageName);
        File preview = Utilities.getContactPreviewFile(context, imageName);
        if (image.exists() && !image.delete()) {
            Utilities.logFirebaseError("error_delete_photo",
                    "ConnectionsLocalRepository.deleteUnusedImages");
        }
        if (preview.exists() && !preview.delete()) {
            Utilities.logFirebaseError("error_delete_preview",
                    "ConnectionsLocalRepository.deleteUnusedImages");
        }
    }

    private static boolean sameValue(String first, String second) {
        return first == null ? second == null : first.equals(second);
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
    public int updateFlashcardProgress(ConnectidConnection connection) {
        Uri uri = ConnectidProvider.Connections.withId(connection.getDatabaseId());
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConnectidColumns.FLASHCARD_BOX, connection.getFlashcardBox());
        contentValues.put(ConnectidColumns.FLASHCARD_LAST_REVIEWED,
                connection.getFlashcardLastReviewed());
        contentValues.put(ConnectidColumns.FLASHCARD_NEXT_REVIEW,
                connection.getFlashcardNextReview());
        contentValues.put(ConnectidColumns.FLASHCARD_ATTEMPTS,
                connection.getFlashcardAttempts());
        contentValues.put(ConnectidColumns.FLASHCARD_CORRECT,
                connection.getFlashcardCorrect());
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
                int databaseId = cursor.getInt(cursor.getColumnIndexOrThrow(TagsColumns._ID));
                String tag = cursor.getString(cursor.getColumnIndexOrThrow(TagsColumns.TAG));
                String connectionIds = cursor.getString(cursor.getColumnIndexOrThrow(TagsColumns.CONNECTION_IDS));

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
                String tag = cursor.getString(cursor.getColumnIndexOrThrow(TagsColumns.TAG));
                String connectionIds = cursor.getString(cursor.getColumnIndexOrThrow(TagsColumns.CONNECTION_IDS));

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
}
