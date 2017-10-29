package me.anky.connectid.details;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class DetailsActivityPresenter implements DetailsActivityMVP.Presenter {

    private DetailsActivityMVP.View view;
    private ConnectionsDataSource connectionsDataSource;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public DetailsActivityPresenter(ConnectionsDataSource connectionsDataSource) {
        this.connectionsDataSource = connectionsDataSource;
    }

    @Override
    public void setView(DetailsActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void loadConnection(int data_id) {
        DisposableSingleObserver<ConnectidConnection> disposableConnectionSingleObserver =
                connectionsDataSource.getOneConnection(data_id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<ConnectidConnection>() {
                    @Override
                    public void onSuccess(@NonNull ConnectidConnection connection) {
                        view.displayConnection(connection);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
        compositeDisposable.add(disposableConnectionSingleObserver);
    }

    @Override
    public void deliverDatabaseIdtoDelete() {

        DisposableSingleObserver<Integer> disposableSingleObserver =
                view.getConnectionToDelete()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Integer>() {
                            @Override
                            public void onSuccess(@NonNull Integer databaseId) {

                                int resultCode = connectionsDataSource.deleteConnection(databaseId);

                                if (resultCode == -1) {
                                    view.displayError();
                                } else {
                                    view.displaySuccess();
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                System.out.println("MVP presenter - " + "something went seriously wrong");
                            }
                        });

        // Add this subscription to the RxJava cleanup composite
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
