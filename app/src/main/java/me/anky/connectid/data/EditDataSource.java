package me.anky.connectid.data;

public interface EditDataSource {

    void insertNewConnection(ConnectidConnection newConnection);

    int getResultCode();
}
