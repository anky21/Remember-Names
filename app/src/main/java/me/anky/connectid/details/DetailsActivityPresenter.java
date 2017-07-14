package me.anky.connectid.details;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import me.anky.connectid.data.ConnectionsDataSource;

public class DetailsActivityPresenter implements DetailsContract.Presenter {

    private DetailsContract.View view;
    private ConnectionsDataSource connectionsDataSource;
    private Scheduler mainScheduler;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public DetailsActivityPresenter(DetailsContract.View view,
                                 ConnectionsDataSource connectionsDataSource,
                                 Scheduler mainScheduler) {

        this.view = view;
        this.connectionsDataSource = connectionsDataSource;
        this.mainScheduler = mainScheduler;
    }

    @Override
    public void deliverDatabaseIdtoDelete() {
        int databaseId = view.getConnectionToDelete();

        int resultCode = connectionsDataSource.deleteConnection(databaseId);

        if (resultCode == -1) {
            view.displayError();
        } else {
            view.displaySuccess();
        }
    }

    @Override
    public void unsubscribe() {
        compositeDisposable.clear();
    }
}
