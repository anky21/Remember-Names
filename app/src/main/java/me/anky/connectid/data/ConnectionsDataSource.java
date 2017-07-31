package me.anky.connectid.data;

import java.util.List;

import io.reactivex.Single;

public interface ConnectionsDataSource {

    Single<List<ConnectidConnection>> getConnections();

    Single<ConnectidConnection> getOneConnection(int data_id);

    int insertNewConnection(ConnectidConnection newConnection);

    int deleteConnection(int databaseId);

    int updateConnection(ConnectidConnection connection);
}
