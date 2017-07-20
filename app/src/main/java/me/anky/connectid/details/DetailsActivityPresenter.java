package me.anky.connectid.details;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectionsDataSource;

public class DetailsActivityPresenter implements DetailsActivityMVP.Presenter {

    private DetailsActivityMVP.View view;
    private ConnectionsDataSource connectionsDataSource;
    private Scheduler mainScheduler;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public DetailsActivityPresenter(DetailsActivityMVP.View view,
                                    ConnectionsDataSource connectionsDataSource,
                                    Scheduler mainScheduler) {

        this.view = view;
        this.connectionsDataSource = connectionsDataSource;
        this.mainScheduler = mainScheduler;
    }

    @Override
    public void deliverDatabaseIdtoDelete() {

        DisposableSingleObserver<Integer> disposableSingleObserver =
                view.getConnectionToDelete()
                        .subscribeOn(Schedulers.io())
                        .observeOn(mainScheduler)
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
