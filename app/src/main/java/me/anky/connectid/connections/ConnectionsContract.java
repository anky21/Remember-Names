package me.anky.connectid.connections;

import android.support.annotation.NonNull;

import me.anky.connectid.BasePresenter;
import me.anky.connectid.data.ConnectidConnection;

public interface ConnectionsContract {

    interface Presenter extends BasePresenter {

        void result(int requestCode, int resultCode);

        void loadConnections(boolean forceUpdate);

        void addNewConnection();

        void openConnectionDetails(@NonNull ConnectidConnection connection);

        void clearCompletedTasks();
    }
}
