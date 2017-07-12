package me.anky.connectid.edit;

import android.util.Log;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
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
        ConnectidConnection newConnection = view.getNewConnection();

        // TODO Handle threading and callbacks here if we care

        Log.i("MVP presenter", "delivering " + view.getNewConnection().getName());

        editDataSource.putNewConnection(newConnection);

        Log.i("MVP presenter", "delivered " + newConnection.getName());
    }

//    public void deliverSuccess() {
//        if (editDataSource.getResultCode() == 0) {
//
//        }
//    }

    public void deliverError() {
        if (editDataSource.getResultCode() == 1) {
            view.displayError();
        }
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
