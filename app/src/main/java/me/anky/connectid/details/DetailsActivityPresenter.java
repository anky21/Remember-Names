package me.anky.connectid.details;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import me.anky.connectid.data.DetailsDataSource;

public class DetailsActivityPresenter implements DetailsContract.Presenter {

    private DetailsContract.View view;
    private DetailsDataSource detailsDataSource;
    private Scheduler mainScheduler;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public DetailsActivityPresenter(DetailsContract.View view,
                                 DetailsDataSource detailsDataSource,
                                 Scheduler mainScheduler) {

        this.view = view;
        this.detailsDataSource = detailsDataSource;
        this.mainScheduler = mainScheduler;
    }

    @Override
    public void deliverDatabaseIdtoDelete() {
        int databaseId = view.getConnectionToDelete();

        int resultCode = detailsDataSource.deleteConnection(databaseId);

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
