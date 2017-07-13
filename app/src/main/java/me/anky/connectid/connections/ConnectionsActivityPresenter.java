package me.anky.connectid.connections;

import java.util.List;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsActivityPresenter implements ConnectionsActivityMVP.Presenter {

    private ConnectionsActivityMVP.View view;
    private ConnectionsDataSource connectionsDataSource;
    private Scheduler mainScheduler;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public ConnectionsActivityPresenter(
            ConnectionsDataSource connectionsDataSource,
            Scheduler mainScheduler) {
        this.connectionsDataSource = connectionsDataSource;
        this.mainScheduler = mainScheduler;
    }

    @Override
    public void setView(ConnectionsActivityMVP.View view) {
        this.view = view;
    }

    public void loadConnections() {

        DisposableSingleObserver<List<ConnectidConnection>> disposableSingleObserver =
                connectionsDataSource.getConnections()
                        .subscribeOn(Schedulers.io())
                        .observeOn(mainScheduler)
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
