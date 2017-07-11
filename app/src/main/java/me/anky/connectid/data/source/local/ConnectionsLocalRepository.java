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
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsLocalRepository implements ConnectionsDataSource {

    private final List<ConnectidConnection> connections = new ArrayList<>();

    public ConnectionsLocalRepository(Context context) {

        // Construct a fake database.

        String testString = context.getString(R.string.dagger_test);
        Log.i("DAGGERZ", testString);

        testSchematic(context);

    }

    @Override
    public Single<List<ConnectidConnection>> getConnections() {

        return Single.fromCallable(new Callable<List<ConnectidConnection>>() {
            @Override
            public List<ConnectidConnection> call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                Log.i("MVP", "connections size: " +  connections.size());

                return connections;
            }
        });
    }

    private void testSchematic(Context context) {

        // Clear the database each time the app is launched. Just temporary.
        context.getContentResolver().delete(
                ConnectidProvider.Connections.CONTENT_URI,
                null,
                null);

        Cursor cursor = context.getContentResolver().query(
                ConnectidProvider.Connections.CONTENT_URI,
                null,
                null,
                null,
                null);

        Log.i("MVP", "cursor size: " +  cursor.getCount());

        if (cursor == null || cursor.getCount() == 0) {
            insertTestData(context);
        }
    }

    public void insertTestData(Context context) {

        connections.add(new ConnectidConnection("Aragorn", "you have my sword"));
        connections.add(new ConnectidConnection("Legolas", "and you have my bow"));
        connections.add(new ConnectidConnection("Gimli", "and my axe!"));
        connections.add(new ConnectidConnection("Gandalf", "fly, you fools!"));
        connections.add(new ConnectidConnection("Bilbo", "misses his ring"));
        connections.add(new ConnectidConnection("Frodo", "misses his finger"));
        connections.add(new ConnectidConnection("Boromir", "one does not simply"));
        connections.add(new ConnectidConnection("Saruman", "don't trust him"));


        ArrayList<ContentProviderOperation> batchOperations =
                new ArrayList<>(connections.size());

        for (
                ConnectidConnection connection : connections)

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
            Log.e("DATABASE_TEST", "Error applying batch insert", e);
        }
    }
}

