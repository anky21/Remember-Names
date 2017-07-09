package me.anky.connectid.data;

import java.util.List;

public interface ConnectionsRepository {

    List<ConnectidConnection> getConnections();
}
