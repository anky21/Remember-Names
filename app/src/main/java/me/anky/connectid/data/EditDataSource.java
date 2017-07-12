package me.anky.connectid.data;

public interface EditDataSource {

    void putNewConnection(ConnectidConnection newConnection);

    int getResultCode();
}
