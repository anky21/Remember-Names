package me.anky.connectid.connections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionsActivityPresenterTest {

    @Mock
    ConnectionsDataSource connectionsDataSource;

    @Mock
    ConnectionsActivityView view;

    @Test
    public void shouldPassConnectionsToView() {

        // given
        List<ConnectidConnection> connections = Arrays.asList(
                new ConnectidConnection(),
                new ConnectidConnection(),
                new ConnectidConnection());
        Mockito.when(connectionsDataSource.getConnections()).
                thenReturn(connections);

        // when
        ConnectionsActivityPresenter presenter =
                new ConnectionsActivityPresenter(view, connectionsDataSource);
        presenter.loadConnections();

        // then
        Mockito.verify(view).displayConnections(connections);
    }

    @Test
    public void shouldHandleNoConnectionsFound() {

        // given
        Mockito.when(connectionsDataSource.getConnections()).
                thenReturn(Collections.<ConnectidConnection>emptyList());

        // when
        ConnectionsActivityPresenter presenter =
                new ConnectionsActivityPresenter(view, connectionsDataSource);
        presenter.loadConnections();

        // then
        Mockito.verify(view).displayNoConnections();
    }
}