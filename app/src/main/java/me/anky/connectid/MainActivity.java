package me.anky.connectid;

import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.facebook.stetho.Stetho;

import java.util.ArrayList;

import me.anky.connectid.data.ConnectidColumns;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectidProvider;
import me.anky.connectid.view.RecyclerViewAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stetho.initializeWithDefaults(this);

        testSchematic();
    }

    private void testSchematic() {

        // Clear database
//        getContentResolver().delete(
//                ConnectidProvider.Connections.CONTENT_URI,
//                null,
//                null);

        Cursor cursor = getContentResolver().query(
                ConnectidProvider.Connections.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (cursor == null || cursor.getCount() == 0) {
            insertTestData();
        }

        if (cursor != null) {
            Log.d("DATABASE_TEST", "cursor count: " + cursor.getCount());

            StringBuilder builder = new StringBuilder();
            ArrayList<ConnectidConnection> connections = new ArrayList<>();

            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i ++) {
                String name = cursor.getString(cursor.getColumnIndex(ConnectidColumns.NAME));
                String description = cursor.getString(cursor.getColumnIndex(ConnectidColumns.DESCRIPTION));
                builder.append(name);
                builder.append(" - ");
                builder.append(description);
                builder.append("\n");

                connections.add(new ConnectidConnection(name, description));
                cursor.moveToNext();
            }

//            TextView debugTv = (TextView) findViewById(R.id.debug_tv);
//            debugTv.setText(builder.toString());

            RecyclerViewAdapter adapter = new RecyclerViewAdapter(this);
            RecyclerView connectionsListRv = (RecyclerView) findViewById(R.id.connections_list_rv);
            connectionsListRv.setHasFixedSize(true);
            connectionsListRv.setLayoutManager(new LinearLayoutManager(this));
            connectionsListRv.setAdapter(adapter);
            adapter.setConnections(connections);
            Log.d("DATABASE_TEST", "adapter size " + adapter.getItemCount());


            cursor.close();
        }
    }

    public void insertTestData() {

        ConnectidConnection[] connections = {
                new ConnectidConnection("Aragorn", "elf lover"),
                new ConnectidConnection("Gandalf", "great with fireworks"),
                new ConnectidConnection("Bilbo", "misses his ring"),
                new ConnectidConnection("Frodo", "misses his finger"),
                new ConnectidConnection("Ezio", "skilled assassin"),
                new ConnectidConnection("Tony Stark", "he is Iron Man"),
                new ConnectidConnection("Captain America", "boring"),
                new ConnectidConnection("Luke Skywalker", "in denial"),
                new ConnectidConnection("Darth Vader", "loving father"),
                new ConnectidConnection("Xenomorph", "saliva is acid"),
                new ConnectidConnection("GLaDOS", "not even angry"),
                new ConnectidConnection("Yoda", "met in swamp"),
                new ConnectidConnection("Shrek", "met in swamp"),
                new ConnectidConnection("Donkey", "very talkative"),
                new ConnectidConnection("Bowser", "has evil laugh"),
                new ConnectidConnection("Mario", "jumps a lot")
        };

        Log.d("DATABASE_TEST", "inserting Data");

        ArrayList<ContentProviderOperation> batchOperations =
                new ArrayList<>(connections.length);

        for (ConnectidConnection connection : connections) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                    ConnectidProvider.Connections.CONTENT_URI);
            builder.withValue(ConnectidColumns.NAME, connection.getName());
            builder.withValue(ConnectidColumns.DESCRIPTION, connection.getDescription());
            batchOperations.add(builder.build());
        }

        Log.d("DATABASE_TEST", "batchOperations to apply: " + batchOperations.size());

        try {
            getContentResolver().applyBatch(ConnectidProvider.AUTHORITY, batchOperations);

            Log.d("DATABASE_TEST", "applied batch");

        } catch (RemoteException | OperationApplicationException e) {
            Log.e("DATABASE_TEST", "Error applying batch insert", e);
        }
    }
}
