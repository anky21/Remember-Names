package me.anky.connectid.edit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class EditActivityPresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    ConnectionsDataSource connectionsDataSource;

    @Mock
    EditContract.View view;

    private EditActivityPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new EditActivityPresenter(
                view, connectionsDataSource, Schedulers.trampoline());
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Test
    public void shouldDeliverConnectionToModel() {

        ConnectidConnection newConnection = new ConnectidConnection();
        Mockito.when(view.getNewConnection()).thenReturn(Single.just(newConnection));

        presenter.deliverNewConnection();

        Mockito.verify(connectionsDataSource).insertNewConnection(newConnection);
    }

    @Test
    public void shouldHandleInsertionError() {
        ConnectidConnection newConnection = new ConnectidConnection();
        Mockito.when(view.getNewConnection()).thenReturn(Single.just(newConnection));

        int resultCode = -1;
        Mockito.when(connectionsDataSource.insertNewConnection(newConnection)).thenReturn(resultCode);

        presenter.deliverNewConnection();

        Mockito.verify(view).displayError();
    }

    @Test
    public void shouldHandleInsertionSuccess() {
        ConnectidConnection newConnection = new ConnectidConnection();
        Mockito.when(view.getNewConnection()).thenReturn(Single.just(newConnection));

        int resultCode = 1;
        Mockito.when(connectionsDataSource.insertNewConnection(newConnection)).thenReturn(resultCode);

        presenter.deliverNewConnection();

        Mockito.verify(view).displaySuccess();
    }
}
