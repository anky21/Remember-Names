package me.anky.connectid.edit;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.EditDataSource;

public class EditActivityPresenter {

    private EditActivityView view;
    private EditDataSource editDataSource;
    private Scheduler mainScheduler;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public EditActivityPresenter(EditActivityView view,
                                 EditDataSource editDataSource,
                                 Scheduler mainScheduler) {

        this.view = view;
        this.editDataSource = editDataSource;
        this.mainScheduler = mainScheduler;
    }

    public void deliverNewConnection() {

        DisposableSingleObserver<ConnectidConnection> disposableSingleObserver =
                view.getNewConnection()
                        .subscribeOn(Schedulers.io())
                        .observeOn(mainScheduler)
                        .subscribeWith(new DisposableSingleObserver<ConnectidConnection>() {

                    @Override
                    public void onSuccess(@NonNull ConnectidConnection connection) {
                        System.out.println("Thread subscribe: " + Thread.currentThread().getId());

                        int resultCode = editDataSource.insertNewConnection(connection);

                        System.out.println("MVP presenter - " + "delivered new connection, resultCode " + resultCode);

                        if (resultCode == -1) {
                            view.displayError();
                        } else {
                            view.displaySuccess();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        // TODO Add Analytics. This error should never be thrown.
                        System.out.println("MVP presenter - " + "something went seriously wrong");
                    }
                });




//        ConnectidConnection newConnection = view.getNewConnection();
//
//        // TODO Handle threading and callbacks here if we care
//        int resultCode = editDataSource.insertNewConnection(newConnection);
//
//        System.out.println("MVP presenter - " + "delivered new connection, resultCode " + resultCode);
//
//        if (resultCode == -1) {
//            view.displayError();
//        } else {
//            view.displaySuccess();
//        }
    }

//    public void loadConnections() {
//
//        DisposableSingleObserver<List<ConnectidConnection>> disposableSingleObserver =
//                connectionsDataSource.getConnections()
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(mainScheduler)
//                        .subscribeWith(new DisposableSingleObserver<List<ConnectidConnection>>() {
//                            @Override
//                            public void onSuccess(@NonNull List<ConnectidConnection> connections) {
//
//                                System.out.println("Thread subscribe: " + Thread.currentThread().getId());
//
//                                if (connections.isEmpty()) {
//                                    view.displayNoConnections();
//                                } else {
//                                    view.displayConnections(connections);
//                                }
//                            }
//
//                            @Override
//                            public void onError(@NonNull Throwable e) {
//                                view.displayError();
//                            }
//                        });
//
//        // Add this subscription to the RxJava cleanup composite
//        compositeDisposable.add(disposableSingleObserver);
//    }
//
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
