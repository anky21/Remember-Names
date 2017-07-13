package me.anky.connectid.details;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import me.anky.connectid.data.DetailsDataSource;

public class DetailsActivityPresenter {

    private DetailsActivityView view;
    private DetailsDataSource detailsDataSource;
    private Scheduler mainScheduler;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    public DetailsActivityPresenter(DetailsActivityView view,
                                 DetailsDataSource detailsDataSource,
                                 Scheduler mainScheduler) {

        this.view = view;
        this.detailsDataSource = detailsDataSource;
        this.mainScheduler = mainScheduler;
    }

    public void deliveDatabaseIdtoDelete() {
        int databaseId = view.getConnectionToDelete();

        int resultCode = detailsDataSource.deleteConnection(databaseId);

    }
}
