package me.anky.connectid.tags;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.Constant;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.data.ConnectionsDataSource;

/**
 * Created by Anky An on 5/11/2017.
 * anky25@gmail.com
 */

public class TagsActivityPresenter implements TagsActivityMVP.Presenter {
    private final static String TAG = "TagsActivityPresenter";

    private  TagsActivityMVP.View view;
    private ConnectionsDataSource dataSource;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public TagsActivityPresenter(ConnectionsDataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Override
    public void setView(TagsActivityMVP.View view) {
        this.view = view;
    }

    public void loadTags() {
        DisposableSingleObserver<List<ConnectionTag>> disposableSingleObserver =
                dataSource.getTags().subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeWith(new DisposableSingleObserver<List<ConnectionTag>>() {
                            @Override
                            public void onSuccess(List<ConnectionTag> connectionTags) {
                                if (connectionTags.isEmpty()) {
                                    view.displayNoTags();
                                } else {
                                    view.displayTags(connectionTags);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_CRITICAL_ERROR, "loadTags onError " + e.getMessage());
                            }
                        });
        compositeDisposable.add(disposableSingleObserver);
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
