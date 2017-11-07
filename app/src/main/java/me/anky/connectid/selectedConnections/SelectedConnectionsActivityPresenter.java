package me.anky.connectid.selectedConnections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

/**
 * Created by Anky An on 8/11/2017.
 * anky25@gmail.com
 */

public class SelectedConnectionsActivityPresenter implements SelectedConnectionsActivityMVP.Presenter {
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

                            }
                        });
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
