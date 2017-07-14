package me.anky.connectid.connections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsActivityPresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    ConnectionsDataSource connectionsDataSource;

    @Mock
    ConnectionsContract.View view;

    private ConnectionsActivityPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new ConnectionsActivityPresenter(
                view, connectionsDataSource, Schedulers.trampoline());
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Test
    public void shouldDeliverConnectionsToView() {

        List<ConnectidConnection> connections = Arrays.asList(
                new ConnectidConnection(),
                new ConnectidConnection(),
                new ConnectidConnection());
        Mockito.when(connectionsDataSource.getConnections()).thenReturn(Single.just(connections));

        presenter.loadConnections();

        Mockito.verify(view).displayConnections(connections);
    }

    @Test
    public void shouldHandleNoConnectionsFound() {

        List<ConnectidConnection> connections = Collections.emptyList();
        Mockito.when(connectionsDataSource.getConnections()).thenReturn(Single.just(connections));

        presenter.loadConnections();

        Mockito.verify(view).displayNoConnections();
    }

    @Test
    public void shouldHandleError() {

        Mockito.when(connectionsDataSource.getConnections()).thenReturn(Single.<List<ConnectidConnection>>error(new Throwable("error")));

        presenter.loadConnections();

        Mockito.verify(view).displayError();
    }
}