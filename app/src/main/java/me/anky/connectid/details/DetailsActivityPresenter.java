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
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

public class DetailsActivityPresenter implements DetailsActivityMVP.Presenter {
    private final static String TAG = "DetailsActivityPresenter";

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
                                Utilities.logFirebaseError("error_load_connection", TAG + ".loadConnection", e.getMessage());
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
//                                System.out.println("MVP presenter - " + "something went seriously wrong");
                                Utilities.logFirebaseError("error_delete_connection", TAG + ".deliverDatabaseIdtoDelete", e.getMessage());

                            }
                        });

        // Add this subscription to the RxJava cleanup composite
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void loadAndUpdateTagTable(final String databaseId, final String tags) {
        DisposableSingleObserver<List<ConnectionTag>> disposableSingleTagsObserver =
                connectionsDataSource.getTags().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ConnectionTag>>() {
                            @Override
                            public void onSuccess(List<ConnectionTag> connectionTags) {
                                if (connectionTags != null && connectionTags.size() != 0) {
                                    deleteIdsFromTag(databaseId, tags, connectionTags);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Utilities.logFirebaseError("error_update_tag_table", TAG + ".loadAndUpdateTagTable", e.getMessage());
                            }
                        });
        compositeDisposable.add(disposableSingleTagsObserver);
    }

    @Override
    public void deleteIdsFromTag(String databaseId, String tags, List<ConnectionTag> allTags) {
        List<String> tagsList = new ArrayList(Arrays.asList(tags.split(", ")));
        for (ConnectionTag connectionTag : allTags) {
            if (tagsList.contains(connectionTag.getTag())) {
                String ids = connectionTag.getConnection_ids();
                List<String> idsList = new ArrayList(Arrays.asList(ids.split(", ")));
                idsList.remove(databaseId);
                String newIdsString;
                if (idsList == null || idsList.size() == 0) {
                    newIdsString = null;
                } else {
                    newIdsString = idsList.toString();
                    newIdsString = newIdsString.substring(1, newIdsString.length() - 1);
                }
                connectionTag.setConnection_ids(newIdsString);
                connectionsDataSource.updateTag(connectionTag);
            }

        }
    }
}
