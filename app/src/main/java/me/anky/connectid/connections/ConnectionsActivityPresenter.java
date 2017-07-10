package me.anky.connectid.connections;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsActivityPresenter {

    private ConnectionsActivityView view;
    private ConnectionsDataSource connectionsDataSource;

    // Composite used for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ConnectionsActivityPresenter(ConnectionsActivityView view,
                                        ConnectionsDataSource connectionsDataSource) {
        this.view = view;
        this.connectionsDataSource = connectionsDataSource;
    }

    public void loadConnections() {

        DisposableSingleObserver<List<ConnectidConnection>> disposableSingleObserver =
                connectionsDataSource.getConnections()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ConnectidConnection>>() {
                            @Override
                            public void onSuccess(@NonNull List<ConnectidConnection> connections) {

                                System.out.println("Thread subscribe: " + Thread.currentThread().getId());

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

        // Add this subscription to the RxJava cleanup composite
        compositeDisposable.add(disposableSingleObserver);
    }

    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
