package me.anky.connectid.data;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsRepository implements ConnectionsDataSource {

    @Override
    public List<ConnectidConnection> getConnections() {

        // Retrieve data from a real database and return it here.
        // Because I am having trouble wrapping my head around Loaders
        // and ContentProviders in the Model, I am just returning some fake
        // data in the List format expected by the Presenter.
        // There is something called Room which may do this job better.
        // Room deals with something it calls DAOs (database access objects)


        List<ConnectidConnection> connections = new ArrayList<>();
        connections.add(new ConnectidConnection("Aragorn", "you have my sword"));
        connections.add(new ConnectidConnection("Legolas", "and you have my bow"));
        connections.add(new ConnectidConnection("Gimli", "and my axe!"));
        connections.add(new ConnectidConnection("Gandalf", "fly, you fools!"));
        connections.add(new ConnectidConnection("Bilbo", "misses his ring"));
        connections.add(new ConnectidConnection("Frodo", "misses his finger"));
        connections.add(new ConnectidConnection("Boromir", "one does not simply"));

        return connections;
    }
}

