package me.anky.connectid.data;

import java.util.List;

import io.reactivex.Single;

public interface ConnectionsDataSource {

    Single<List<ConnectidConnection>> getConnections(int menuOption);

    Single<ConnectidConnection> getOneConnection(int data_id);

    int insertNewConnection(ConnectidConnection newConnection);

    int deleteConnection(int databaseId);

    int updateConnection(ConnectidConnection connection);

    int updateConnection(int id, String tags);

    Single<List<ConnectionTag>> getTags();

    Single<ConnectionTag> getOneTag(int data_id);

    int insertNewTag(ConnectionTag newTag);

    int updateTag(ConnectionTag tag);
}
