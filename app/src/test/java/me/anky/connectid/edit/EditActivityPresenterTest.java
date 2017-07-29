package me.anky.connectid.edit;

public class EditActivityPresenterTest {

//    @Rule
//    public MockitoRule mockitoRule = MockitoJUnit.rule();
//
//    @Mock
//    ConnectionsDataSource connectionsDataSource;
//
//    @Mock
//    EditActivityMVP.View view;
//
//    private EditActivityPresenter presenter;
//
//    @Before
//    public void setUp() throws Exception {
//        presenter = new EditActivityPresenter(connectionsDataSource);
//        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
//    }
//
//    @Test
//    public void shouldDeliverConnectionToModel() {
//
//        ConnectidConnection newConnection = new ConnectidConnection();
//        Mockito.when(view.getNewConnection()).thenReturn(Single.just(newConnection));
//
//        presenter.deliverNewConnection();
//
//        Mockito.verify(connectionsDataSource).insertNewConnection(newConnection);
//    }
//
//    @Test
//    public void shouldHandleInsertionError() {
//        ConnectidConnection newConnection = new ConnectidConnection();
//        Mockito.when(view.getNewConnection()).thenReturn(Single.just(newConnection));
//
//        int resultCode = -1;
//        Mockito.when(connectionsDataSource.insertNewConnection(newConnection)).thenReturn(resultCode);
//
//        presenter.deliverNewConnection();
//
//        Mockito.verify(view).displayError();
//    }
//
//    @Test
//    public void shouldHandleInsertionSuccess() {
//        ConnectidConnection newConnection = new ConnectidConnection();
//        Mockito.when(view.getNewConnection()).thenReturn(Single.just(newConnection));
//
//        int resultCode = 1;
//        Mockito.when(connectionsDataSource.insertNewConnection(newConnection)).thenReturn(resultCode);
//
//        presenter.deliverNewConnection();
//
//        Mockito.verify(view).displaySuccess();
//    }
}
