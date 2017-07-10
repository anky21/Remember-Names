package me.anky.connectid.connections;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
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

        connectionsDataSource.getConnections()
                .subscribeWith(new DisposableSingleObserver<List<ConnectidConnection>>() {
                    @Override
                    public void onSuccess(@NonNull List<ConnectidConnection> connections) {
                        if (connections.isEmpty()) {
                            view.displayNoConnections();
                        } else {
                            view.displayConnections(connections);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        view.displayError();
                    }
                });
    }
}
