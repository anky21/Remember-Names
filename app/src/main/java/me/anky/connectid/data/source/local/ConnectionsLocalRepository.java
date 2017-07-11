package me.anky.connectid.data.source.local;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsLocalRepository implements ConnectionsDataSource {

    private final List<ConnectidConnection> connections = new ArrayList<>();
    private Context context;

    public ConnectionsLocalRepository(Context context) {
        this.context = context;

        // Temporarily clear the database when app launches
        deleteAllEntries();

        // Temporarily populate the database with data if it is empty
        initDatabase();

        // Prepare connections when the repository is constructed
        // This is the data the user will see when the app first launches
        prepareConnectionsList();
    }

    @Override
    public Single<List<ConnectidConnection>> getConnections() {

        return Single.fromCallable(new Callable<List<ConnectidConnection>>() {
            @Override
            public List<ConnectidConnection> call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                Log.i("MVP model", "getConnections returned " +  connections.size() + " connections");

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

        String name;
        String description;

        Cursor cursor = getAllEntries();
        if (cursor != null && cursor.getCount() != 0) {

            while (cursor.moveToNext()) {
                name = cursor.getString(cursor.getColumnIndex(ConnectidColumns.NAME));
                description = cursor.getString(cursor.getColumnIndex(ConnectidColumns.DESCRIPTION));
                connections.add(new ConnectidConnection(name, description));
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

        dummyConnections.add(new ConnectidConnection("Aragorn", "you have my sword"));
        dummyConnections.add(new ConnectidConnection("Legolas", "and you have my bow"));
        dummyConnections.add(new ConnectidConnection("Gimli", "and my axe!"));
        dummyConnections.add(new ConnectidConnection("Gandalf", "fly, you fools!"));
        dummyConnections.add(new ConnectidConnection("Bilbo", "misses his ring"));
        dummyConnections.add(new ConnectidConnection("Frodo", "misses his finger"));
        dummyConnections.add(new ConnectidConnection("Sam", "ringbearerbearer"));
        dummyConnections.add(new ConnectidConnection("Boromir", "one does not simply"));
        dummyConnections.add(new ConnectidConnection("Saruman", "don't trust him"));
        dummyConnections.add(new ConnectidConnection("Gollum", "naughty"));
        dummyConnections.add(new ConnectidConnection("Smeagol", "nice"));
        dummyConnections.add(new ConnectidConnection("Elrond", "Agent Smith"));
        dummyConnections.add(new ConnectidConnection("Arwen", "Agent Smith's daughter"));

        // TODO allow attempted duplicate entry to retrieve existing data and merge new data
        dummyConnections.add(new ConnectidConnection("Legolas", "one legolas, two legoli"));

        ArrayList<ContentProviderOperation> batchOperations =
                new ArrayList<>(dummyConnections.size());

        for (
                ConnectidConnection connection : dummyConnections)

        {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                    ConnectidProvider.Connections.CONTENT_URI);
            builder.withValue(ConnectidColumns.NAME, connection.getName());
            builder.withValue(ConnectidColumns.DESCRIPTION, connection.getDescription());
            batchOperations.add(builder.build());
        }

        try

        {
            context.getContentResolver().applyBatch(ConnectidProvider.AUTHORITY, batchOperations);

        } catch (RemoteException |
                OperationApplicationException e)

        {
            // TODO Add some sort of Analytics for reporting.
            Log.e("DATABASE_TEST", "Error applying batch insert", e);
        }
    }
}

