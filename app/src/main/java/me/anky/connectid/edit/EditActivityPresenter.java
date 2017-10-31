package me.anky.connectid.edit;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import events.SetToUpdateTagTable;
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

                                int databaseId = connectionsDataSource.insertNewConnection(connection);
                                Log.v("testing", "new databaseId is " + databaseId);

                                EventBus.getDefault().post(new SetToUpdateTagTable(databaseId));
                                System.out.println("MVP presenter - " + "delivered new connection, resultCode " + databaseId);

                                if (databaseId == -1) {
                                    view.displayError();
                                } else {
                                    view.displaySuccess(databaseId);
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
                                    view.displaySuccess(resultCode);
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
    public void loadTags() {
        DisposableSingleObserver<List<ConnectionTag>> disposableSingleTagsObserver =
                connectionsDataSource.getTags().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ConnectionTag>>() {
                            @Override
                            public void onSuccess(List<ConnectionTag> connectionTags) {
                                view.handleAllTags(connectionTags);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
        compositeDisposable.add(disposableSingleTagsObserver);
    }

    @Override
    public void updateTagTable(List<ConnectionTag> allTags, List<String> connectionTags, int databaseId) {
        Log.v("testing", "update Tag Table in EditActivityPresenter");

        if (connectionTags != null && connectionTags.size() != 0){
            for (ConnectionTag tag : allTags) {
                Log.v("testing", "connectionTags is " + connectionTags.toString());
                if (connectionTags.contains(tag.getTag())){
                    Log.v("testing", "tag.getTag is " + tag.getTag());
                    String ids = tag.getConnection_ids();
                    if (ids == null || ids.length() == 0){
                        ids = String.valueOf(databaseId);
                    } else {
                        ids = ids + "," + String.valueOf(databaseId);
                    }
                    tag.setConnection_ids(ids);
                    connectionsDataSource.updateTag(tag);
                }
            }
        }
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
