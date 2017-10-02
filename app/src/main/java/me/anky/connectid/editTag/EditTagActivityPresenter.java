package me.anky.connectid.editTag;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import me.anky.connectid.data.ConnectionsDataSource;

/**
 * Created by anky on 2/10/17.
 */

public class EditTagActivityPresenter implements EditTagActivityMVP.Presenter {

    private EditTagActivityMVP.View view;
    private ConnectionsDataSource dataSource;

    // Create a composite for RxJava subscriber cleanup
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Inject
    public EditTagActivityPresenter(ConnectionsDataSource dataSource){
        this.dataSource = dataSource;
    }

    public void setView(EditTagActivityMVP.View view) {
        this.view = view;
    }

    @Override
    public void loadTags() {

    }

    @Override
    public void unsubscribe() {

    }
}
