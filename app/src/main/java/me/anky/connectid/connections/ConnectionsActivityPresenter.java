package me.anky.connectid.connections;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsActivityPresenter {

    private ConnectionsActivityView view;
    private ConnectionsDataSource connectionsDataSource;

    public ConnectionsActivityPresenter(ConnectionsActivityView view,
                                        ConnectionsDataSource connectionsDataSource) {
        this.view = view;
        this.connectionsDataSource = connectionsDataSource;
    }

    public void loadConnections() {
        List<ConnectidConnection> connections = connectionsDataSource.getConnections();

        if (connections.isEmpty()) {
            view.displayNoConnections();
        } else {
            view.displayConnections(connections);
        }
    }
}
