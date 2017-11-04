package me.anky.connectid.tags;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import me.anky.connectid.data.ConnectionsDataSource;

/**
 * Created by Anky An on 5/11/2017.
 * anky25@gmail.com
 */

public class TagsActivityPresenter implements TagsActivityMVP.Presenter {
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

    }

    @Override
    public void unsubscribe() {

    }
}
