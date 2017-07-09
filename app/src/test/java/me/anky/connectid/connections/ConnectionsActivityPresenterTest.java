package me.anky.connectid.connections;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;

public class ConnectionsActivityPresenterTest {

    @Test
    public void shouldPassConnectionsToView() {

        // given
        ConnectionsActivityView view = new MockView();
        ConnectionsDataSource connectionsDataSource = new MockConnectionsDataSource(true);

        // when
        ConnectionsActivityPresenter presenter =
                new ConnectionsActivityPresenter(view, connectionsDataSource);
        presenter.loadConnections();

        // then
        Assert.assertEquals(true, ((MockView) view).displayConnectionsWithConnectionsCalled);
    }

    @Test
    public void shouldHandleNoConnectionsFound() {

        // given
        ConnectionsActivityView view = new MockView();
        ConnectionsDataSource connectionsDataSource = new MockConnectionsDataSource(false);

        // when
        ConnectionsActivityPresenter presenter =
                new ConnectionsActivityPresenter(view, connectionsDataSource);
        presenter.loadConnections();

        // then
        Assert.assertEquals(true, ((MockView) view).displayConnectionsWithNoConnectionsCalled);
    }

    private class MockView implements ConnectionsActivityView {

        boolean displayConnectionsWithConnectionsCalled;
        boolean displayConnectionsWithNoConnectionsCalled;

        @Override
        public void displayConnections(List<ConnectidConnection> connections) {

            if (connections.size() == 3) {
                displayConnectionsWithConnectionsCalled = true;
            }
        }

        @Override
        public void displayNoConnections() {
            displayConnectionsWithNoConnectionsCalled = true;
        }
    }

    private class MockConnectionsDataSource implements ConnectionsDataSource {

        private boolean returnSomeConnections;

        public MockConnectionsDataSource(boolean returnSomeConnections) {
            this.returnSomeConnections = returnSomeConnections;
        }

        @Override
        public List<ConnectidConnection> getConnections() {

            if (returnSomeConnections) {
                return Arrays.asList(
                        new ConnectidConnection(),
                        new ConnectidConnection(),
                        new ConnectidConnection()
                );
            }
            else {
                return Collections.emptyList();
            }
        }
    }
}