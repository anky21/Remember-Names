package me.anky.connectid.edit;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

public class EditActivityPresenter implements EditActivityMVP.Presenter {

    private EditActivityMVP.View view;
    private ConnectionsDataSource connectionsDataSource;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public EditActivityPresenter(ConnectionsDataSource connectionsDataSource) {

        this.connectionsDataSource = connectionsDataSource;
    }

    @Override
    public void setView(EditActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void deliverNewConnection() {

        DisposableSingleObserver<ConnectidConnection> disposableSingleObserver =
                view.getNewConnection()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<ConnectidConnection>() {

                            @Override
                            public void onSuccess(@NonNull ConnectidConnection connection) {
                                System.out.println("Thread subscribe: " + Thread.currentThread().getId());

                                int resultCode = connectionsDataSource.insertNewConnection(connection);
//                                ConnectionTag newTag = new ConnectionTag("cool", "1");
//                                connectionsDataSource.insertNewTag(newTag);
//                                ConnectionTag newTag1 = new ConnectionTag("white", "3,5,7");
//                                connectionsDataSource.insertNewTag(newTag1);
//                                ConnectionTag newTag2 = new ConnectionTag("US", "1,5,8");
//                                connectionsDataSource.insertNewTag(newTag2);
                                ConnectionTag newTag3 = new ConnectionTag("hello tags", "2,4,9");
                                connectionsDataSource.insertNewTag(newTag3);


                                System.out.println("MVP presenter - " + "delivered new connection, resultCode " + resultCode);

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
    public void updateConnection() {
        DisposableSingleObserver<ConnectidConnection> disposableSingleObserver =
                view.getUpdatedConnection()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<ConnectidConnection>() {

                            @Override
                            public void onSuccess(@NonNull ConnectidConnection connection) {
                                System.out.println("Thread subscribe: " + Thread.currentThread().getId());

                                int resultCode = connectionsDataSource.updateConnection(connection);

                                System.out.println("MVP presenter - " + "delivered new connection, resultCode " + resultCode);

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
