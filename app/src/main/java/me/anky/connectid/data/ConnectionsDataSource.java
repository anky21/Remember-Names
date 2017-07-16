package me.anky.connectid.data;

import java.util.List;

import io.reactivex.Single;

public interface ConnectionsDataSource {

    Single<List<ConnectidConnection>> getConnections();

    int insertNewConnection(ConnectidConnection newConnection);

    int deleteConnection(int databaseId);
}
