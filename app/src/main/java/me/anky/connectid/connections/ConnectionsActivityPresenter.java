package me.anky.connectid.connections;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsActivityPresenter implements ConnectionsActivityMVP.Presenter {
    private final static String TAG = "ConnectionsActivityPresenter";
    private ConnectionsActivityMVP.View view;
    private ConnectionsDataSource connectionsDataSource;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public ConnectionsActivityPresenter(ConnectionsDataSource connectionsDataSource) {
        this.connectionsDataSource = connectionsDataSource;
    }

    @Override
    public void setView(ConnectionsActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void loadConnections(Integer integer) {
//        Log.v("testing", "load connections");
        DisposableSingleObserver<List<ConnectidConnection>> disposableSingleObserver =
                connectionsDataSource.getConnections(integer)
                        .subscribeOn(Schedulers.io())
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
                                Utilities.logFirebaseError("error_load_connections", TAG + ".loadConnections", e.getMessage());
                            }
                        });

        // Add this subscription to the RxJava cleanup composite
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void handleSortByOptionChange() {
        int option = view.getSortByOption();
        loadConnections(option);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }

    @Override
    public void addTagToSelectedConnections(List<Integer> connectionIds, String tagName) {
        if (view == null) return; // View is not available

        DisposableSingleObserver<Boolean> disposableSingleObserver =
                Single.fromCallable(() -> connectionsDataSource.batchAddTagToConnections(connectionIds, tagName))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Boolean>() {
                            @Override
                            public void onSuccess(@NonNull Boolean success) {
                                if (view != null) {
                                    String message = success ? "Tag '" + tagName + "' added to selected connections."
                                            : "Failed to add tag.";
                                    view.onBatchTagOperationCompleted(message, success);
                                    if (success) {
                                        // Optionally, reload connections if tags affect display or sorting
                                        // For now, just a message. DetailsActivity will refresh on its own if opened.
                                        // loadConnections(view.getSortByOption()); // Example if refresh is needed
                                    }
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                if (view != null) {
                                    view.onBatchTagOperationCompleted("Error adding tag: " + e.getMessage(), false);
                                }
                                Utilities.logFirebaseError("error_batch_add_tag", TAG + ".addTagToSelectedConnections", e.getMessage());
                            }
                        });
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void removeTagFromSelectedConnections(List<Integer> connectionIds, String tagName) {
        if (view == null) return; // View is not available

        DisposableSingleObserver<Boolean> disposableSingleObserver =
                Single.fromCallable(() -> connectionsDataSource.batchRemoveTagFromConnections(connectionIds, tagName))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<Boolean>() {
                            @Override
                            public void onSuccess(@NonNull Boolean success) {
                                if (view != null) {
                                    String message = success ? "Tag '" + tagName + "' removed from selected connections."
                                            : "Failed to remove tag.";
                                    view.onBatchTagOperationCompleted(message, success);
                                    if (success) {
                                        // Optionally, reload connections
                                        // loadConnections(view.getSortByOption());
                                    }
                                }
                            }

                            @Override
                            public void onError(@NonNull Throwable e) {
                                if (view != null) {
                                    view.onBatchTagOperationCompleted("Error removing tag: " + e.getMessage(), false);
                                }
                                Utilities.logFirebaseError("error_batch_remove_tag", TAG + ".removeTagFromSelectedConnections", e.getMessage());
                            }
                        });
        compositeDisposable.add(disposableSingleObserver);
    }
}
