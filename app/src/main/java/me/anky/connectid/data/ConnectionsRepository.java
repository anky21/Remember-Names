package me.anky.connectid.data;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import me.anky.connectid.R;

public class ConnectionsRepository implements ConnectionsDataSource {

    private final List<ConnectidConnection> connections = new ArrayList<>();

    public ConnectionsRepository(Context context) {

        // Construct a fake database.

        String testString = context.getString(R.string.dagger_test);
        Log.i("DAGGERZ", testString);

        connections.add(new ConnectidConnection("Aragorn", "you have my sword"));
        connections.add(new ConnectidConnection("Legolas", "and you have my bow"));
        connections.add(new ConnectidConnection("Gimli", "and my axe!"));
        connections.add(new ConnectidConnection("Gandalf", "fly, you fools!"));
        connections.add(new ConnectidConnection("Bilbo", "misses his ring"));
        connections.add(new ConnectidConnection("Frodo", "misses his finger"));
        connections.add(new ConnectidConnection("Boromir", "one does not simply"));
        connections.add(new ConnectidConnection("Saruman", "don't trust him"));
    }

    @Override
    public Single<List<ConnectidConnection>> getConnections() {

        // TODO Retrieve data from a real database and return it here.
        // Loaders do not play well with MVP, not sure about ContentProviders either
        // Room library with DAOs (database access objects) may be a replacement

        // For now, return this fake database data.

        return Single.fromCallable(new Callable<List<ConnectidConnection>>() {
            @Override
            public List<ConnectidConnection> call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                return connections;
            }
        });
    }
}

