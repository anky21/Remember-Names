package me.anky.connectid;

import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.CursorLoader;
import android.content.Loader;
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
import me.anky.connectid.view.ConnectidCursorAdapter;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 0;
    private ConnectidCursorAdapter mCursorAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stetho.initializeWithDefaults(this);

        testSchematic();


        mCursorAdapter = new ConnectidCursorAdapter(this, null);
        mRecyclerView = (RecyclerView) findViewById(R.id.connections_list_rv);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mCursorAdapter);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
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
    }

    public void insertTestData() {

        ConnectidConnection[] connections = {
                new ConnectidConnection("Aragorn", "elf lover"),
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
                new ConnectidConnection("Princess Peach", "damsel in distress"),
                new ConnectidConnection("Princess Peach", "damsel in distress"),
                new ConnectidConnection("Princess Peach", "damsel in distress"),
                new ConnectidConnection("Princess Peach", "damsel in distress"),
                new ConnectidConnection("Princess Peach", "damsel in distress"),
                new ConnectidConnection("Princess Zelda", "damsel in distress"),
                new ConnectidConnection("Mario", "jumps a lot")
        };

        ArrayList<ContentProviderOperation> batchOperations =
                new ArrayList<>(connections.length);

        for (ConnectidConnection connection : connections) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                    ConnectidProvider.Connections.CONTENT_URI);
            builder.withValue(ConnectidColumns.NAME, connection.getName());
            builder.withValue(ConnectidColumns.DESCRIPTION, connection.getDescription());
            batchOperations.add(builder.build());
        }

        try {
            getContentResolver().applyBatch(ConnectidProvider.AUTHORITY, batchOperations);

        } catch (RemoteException | OperationApplicationException e) {
            Log.e("DATABASE_TEST", "Error applying batch insert", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        return new CursorLoader(this, ConnectidProvider.Connections.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data){
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mCursorAdapter.swapCursor(null);
    }
}
