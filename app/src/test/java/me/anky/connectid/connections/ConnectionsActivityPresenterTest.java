package me.anky.connectid.connections;

public class ConnectionsActivityPresenterTest {

//    @Rule
//    public MockitoRule mockitoRule = MockitoJUnit.rule();
//
//    @Mock
//    ConnectionsDataSource connectionsDataSource;
//
//    @Mock
//    ConnectionsActivityMVP.View view;
//
//    private ConnectionsActivityPresenter presenter;
//
//    @Before
//    public void setUp() throws Exception {
//        presenter = new ConnectionsActivityPresenter(connectionsDataSource);
//        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
//    }
//
//    @Test
//    public void shouldDeliverConnectionsToView() {
//
//        List<ConnectidConnection> connections = Arrays.asList(
//                new ConnectidConnection(),
//                new ConnectidConnection(),
//                new ConnectidConnection());
//        Mockito.when(connectionsDataSource.getConnections()).thenReturn(Single.just(connections));
//
//        presenter.loadConnections();
//
//        Mockito.verify(view).displayConnections(connections);
//    }
//
//    @Test
//    public void shouldHandleNoConnectionsFound() {
//
//        List<ConnectidConnection> connections = Collections.emptyList();
//        Mockito.when(connectionsDataSource.getConnections()).thenReturn(Single.just(connections));
//
//        presenter.loadConnections();
//
//        Mockito.verify(view).displayNoConnections();
//    }
//
//    @Test
//    public void shouldHandleError() {
//
//        Mockito.when(connectionsDataSource.getConnections()).thenReturn(Single.<List<ConnectidConnection>>error(new Throwable("error")));
//
//        presenter.loadConnections();
//
//        Mockito.verify(view).displayError();
//    }
}