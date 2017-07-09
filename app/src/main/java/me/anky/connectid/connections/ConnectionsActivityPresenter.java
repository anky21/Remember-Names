package me.anky.connectid.connections;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsRepository;

public class ConnectionsActivityPresenter {

    private ConnectionsActivityView view;
    private ConnectionsRepository connectionsRepository;

    public ConnectionsActivityPresenter(ConnectionsActivityView view,
                                        ConnectionsRepository connectionsRepository) {
        this.view = view;
        this.connectionsRepository = connectionsRepository;
    }

    public void loadConnections() {
        List<ConnectidConnection> connections = connectionsRepository.getConnections();

        if (connections.isEmpty()) {
            view.displayNoConnections();
        } else {
            view.displayConnections(connections);
        }
    }
}
