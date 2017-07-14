package me.anky.connectid.details;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.DetailsDataSource;

public class DetailsActivityPresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    DetailsDataSource detailsDataSource;

    @Mock
    DetailsContract.View view;

    private DetailsActivityPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new DetailsActivityPresenter(
                view, detailsDataSource, Schedulers.trampoline());
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Test
    public void shouldDeliverDatabaseIdToDeleteToModel() {
        int databaseIdFromUri = 1;
        Mockito.when(view.getConnectionToDelete()).thenReturn(databaseIdFromUri);

        presenter.deliverDatabaseIdtoDelete();

        Mockito.verify(detailsDataSource).deleteConnection(databaseIdFromUri);
    }

    @Test
    public void shouldHandleDeletionError() {
        int databaseIdFromUri = 1;
        Mockito.when(view.getConnectionToDelete()).thenReturn(databaseIdFromUri);

        int resultCode = -1;
        Mockito.when(detailsDataSource.deleteConnection(databaseIdFromUri)).thenReturn(resultCode);

        presenter.deliverDatabaseIdtoDelete();

        Mockito.verify(view).displayError();
    }

    @Test
    public void shouldHandleDeletionSuccess() {
        int databaseIdFromUri = 1;
        Mockito.when(view.getConnectionToDelete()).thenReturn(databaseIdFromUri);

        int resultCode = 1;
        Mockito.when(detailsDataSource.deleteConnection(databaseIdFromUri)).thenReturn(resultCode);

        presenter.deliverDatabaseIdtoDelete();

        Mockito.verify(view).displaySuccess();
    }
}
