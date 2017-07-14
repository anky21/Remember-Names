package me.anky.connectid.edit;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.EditDataSource;

public class EditActivityPresenter implements EditContract.Presenter {

    private EditContract.View view;
    private EditDataSource editDataSource;
    private Scheduler mainScheduler;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public EditActivityPresenter(EditContract.View view,
                                 EditDataSource editDataSource,
                                 Scheduler mainScheduler) {

        this.view = view;
        this.editDataSource = editDataSource;
        this.mainScheduler = mainScheduler;
    }

    @Override
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

        // Add this subscription to the RxJava cleanup composite
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
