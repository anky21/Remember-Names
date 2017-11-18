package me.anky.connectid.selectedConnections;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;
import me.anky.connectid.events.TagDeleted;

/**
 * Created by Anky An on 8/11/2017.
 * anky25@gmail.com
 */

public class SelectedConnectionsActivityPresenter implements SelectedConnectionsActivityMVP.Presenter {
    private final static String TAG = "SelectedConnectionsActivityPresenter";

    private SelectedConnectionsActivityMVP.View view;
    private ConnectionsDataSource dataSource;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public SelectedConnectionsActivityPresenter(ConnectionsDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setView(SelectedConnectionsActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void loadTag(int tagId) {
        DisposableSingleObserver<ConnectionTag> singleObserver =
                dataSource.getOneTag(tagId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<ConnectionTag>() {
                            @Override
                            public void onSuccess(ConnectionTag connectionTag) {
                                String ids = connectionTag.getConnection_ids();
                                if (ids == null || ids.length() == 0){
                                    view.displayNoConnections();
                                } else {
                                    List<String> idsList = new ArrayList(Arrays.asList(ids.split(", ")));
                                    loadConnections(idsList);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Utilities.logFirebaseError("error_load_tag", TAG + ".loadTag", e.getMessage());
                            }
                        });
    }

    @Override
    public void loadConnections(final List<String> idsList) {
        // Default menuOption: 1
        DisposableSingleObserver<List<ConnectidConnection>> disposableSingleObserver =
                dataSource.getConnections(1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ConnectidConnection>>() {
                            @Override
                            public void onSuccess(List<ConnectidConnection> connectidConnections) {
                                Iterator<ConnectidConnection> i = connectidConnections.iterator();
                                while (i.hasNext()) {
                                    ConnectidConnection connection = i.next();
                                    if (!idsList.contains(String.valueOf(connection.getDatabaseId()))) {
                                        i.remove();
                                    }
                                }

                                view.displayConnections(connectidConnections);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Utilities.logFirebaseError("error_load_connections", TAG + ".loadConnections", e.getMessage());
                            }
                        });
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void deleteTag(int databaseId, String tag, List<ConnectidConnection> connections) {
        dataSource.deleteTag(databaseId);

        // Remove this tag from connections
        if (connections != null && connections.size() != 0) {
            for (ConnectidConnection connection : connections) {
                String tags = connection.getTags();
                String newTags;
                List<String> tagsList = new ArrayList(Arrays.asList(tags.split(", ")));
                if (tagsList.size() == 1) {
                    newTags = null;
                } else {
                    tagsList.remove(tag);
                    newTags = tagsList.toString();
                    newTags = newTags.substring(1, newTags.length() -1 );
                }
                connection.setTags(newTags);
                dataSource.updateConnectionWithTags(connection);
            }

            EventBus.getDefault().post(new TagDeleted());
        }

    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
