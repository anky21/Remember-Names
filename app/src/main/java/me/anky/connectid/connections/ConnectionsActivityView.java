package me.anky.connectid.connections;

import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

public interface ConnectionsActivityView {

    void displayConnections(List<ConnectidConnection> connections);

    void displayNoConnections();

    void displayError();
}
