package me.anky.connectid.data.source.local;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

import static android.content.ContentProviderOperation.newInsert;

public class ConnectionsLocalRepository implements ConnectionsDataSource {

    private final List<ConnectidConnection> connections = new ArrayList<>();

    private Context context;

    public ConnectionsLocalRepository(Context context) {
        this.context = context;

        // FOR DEBUG: clear the database when app launches
        // deleteAllEntries();

        // FOR DEBUG: populate the database with data if it is empty
        initDatabase();


    }

    @Override
    public Single<List<ConnectidConnection>> getConnections() {
        prepareConnectionsList();

        return Single.fromCallable(new Callable<List<ConnectidConnection>>() {
            @Override
            public List<ConnectidConnection> call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                Log.i("MVP model", "getConnections returned " + connections.size() + " connections");

                return connections;
            }
        });
    }

    private void initDatabase() {

        Cursor cursor = getAllEntries();
        if (cursor == null || cursor.getCount() == 0) {
            insertDummyData();
            Log.i("MVP model", "initialized database");
        }
    }

    private void prepareConnectionsList() {
        connections.clear();

        int databaseId;
        String firstName;
        String lastName;
        String imageName;
        String meetVenue;
        String appearance;
        String feature;
        String commonFriends;
        String description;

        Cursor cursor = getAllEntries();
        if (cursor != null && cursor.getCount() != 0) {

            while (cursor.moveToNext()) {
                databaseId = cursor.getInt(cursor.getColumnIndex(ConnectidColumns._ID));
                firstName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.FIRST_NAME));
                lastName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.LAST_NAME));
                imageName = cursor.getString(cursor.getColumnIndex(ConnectidColumns.IMAGE_NAME));
                meetVenue = cursor.getString(cursor.getColumnIndex(ConnectidColumns.MEET_WHERE));
                appearance = cursor.getString(cursor.getColumnIndex(ConnectidColumns.APPEARANCE));
                feature = cursor.getString(cursor.getColumnIndex(ConnectidColumns.FEATURE));
                commonFriends = cursor.getString(cursor.getColumnIndex(ConnectidColumns.COMMON_FRIENDS));
                description = cursor.getString(cursor.getColumnIndex(ConnectidColumns.DESCRIPTION));

                connections.add(new ConnectidConnection(databaseId, firstName, lastName, imageName, meetVenue, appearance, feature, commonFriends, description));
            }
        }
    }

    private Cursor getAllEntries() {
        return context.getContentResolver().query(
                ConnectidProvider.Connections.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    private void deleteAllEntries() {
        context.getContentResolver().delete(
                ConnectidProvider.Connections.CONTENT_URI,
                null,
                null);
    }

    private void insertDummyData() {

        List<ConnectidConnection> dummyConnections = new ArrayList<>();

        dummyConnections.add(new ConnectidConnection("Yoda", "M", "blank_profile.jpg", "met in swamp", "good looking", "good person", "no common friends", "total stranger"));
        dummyConnections.add(new ConnectidConnection("Donkey", "Who", "blank_profile.jpg", "met in swamp", "looks like a donkey", "grumpy", "Shrek", "Cute"));
        dummyConnections.add(new ConnectidConnection("Snow", "White", "blank_profile.jpg", "met in swamp", "looks like a princess", "easy going", "7 dwarfs", "she's pretty"));

        ArrayList<ContentProviderOperation> batchOperations =
                new ArrayList<>(dummyConnections.size());

        for (ConnectidConnection connection : dummyConnections) {

            ContentProviderOperation.Builder builder = newInsert(
                    ConnectidProvider.Connections.CONTENT_URI);
            builder.withValue(ConnectidColumns.FIRST_NAME, connection.getFirstName());
            builder.withValue(ConnectidColumns.LAST_NAME, connection.getLastName());
            builder.withValue(ConnectidColumns.IMAGE_NAME, connection.getImageName());
            builder.withValue(ConnectidColumns.MEET_WHERE, connection.getMeetVenue());
            builder.withValue(ConnectidColumns.APPEARANCE, connection.getAppearance());
            builder.withValue(ConnectidColumns.FEATURE, connection.getFeature());
            builder.withValue(ConnectidColumns.COMMON_FRIENDS, connection.getCommonFriends());
            builder.withValue(ConnectidColumns.DESCRIPTION, connection.getDescription());
            batchOperations.add(builder.build());
        }

        try {
            context.getContentResolver().applyBatch(ConnectidProvider.AUTHORITY, batchOperations);
        } catch (RemoteException | OperationApplicationException e) {

            // TODO Add some sort of Analytics for reporting.
            Log.e("DATABASE_TEST", "Error applying batch insert", e);
        }
    }

    @Override
    public int insertNewConnection(ConnectidConnection newConnection) {

        Log.i("MVP model", "insertNewConnection inserting " + newConnection.getFirstName());

        ContentValues contentValues = new ContentValues();
        contentValues.put(ConnectidColumns.FIRST_NAME, newConnection.getFirstName());
        contentValues.put(ConnectidColumns.LAST_NAME, newConnection.getLastName());
        contentValues.put(ConnectidColumns.IMAGE_NAME, newConnection.getImageName());
        contentValues.put(ConnectidColumns.MEET_WHERE, newConnection.getMeetVenue());
        contentValues.put(ConnectidColumns.APPEARANCE, newConnection.getAppearance());
        contentValues.put(ConnectidColumns.FEATURE, newConnection.getFeature());
        contentValues.put(ConnectidColumns.COMMON_FRIENDS, newConnection.getCommonFriends());
        contentValues.put(ConnectidColumns.DESCRIPTION, newConnection.getDescription());

        Uri uri = context.getContentResolver().insert(ConnectidProvider.Connections.CONTENT_URI, contentValues);

        Log.i("MVP model", "insertNewConnection inserted uri " + uri.toString());

        return generateResultCode(uri);
    }

    @Override
    public int deleteConnection(int databaseId) {

        Uri uri = ConnectidProvider.Connections.withId(databaseId);

        Log.i("MVP model", "deleteConnection deleted " + uri.toString());

        return context.getContentResolver().delete(uri, null, null);
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

