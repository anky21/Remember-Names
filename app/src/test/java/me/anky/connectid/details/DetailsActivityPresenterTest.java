package me.anky.connectid.details;

public class DetailsActivityPresenterTest {

//    @Rule
//    public MockitoRule mockitoRule = MockitoJUnit.rule();
//
//    @Mock
//    ConnectionsDataSource connectionsDataSource;
//
//    @Mock
//    DetailsActivityMVP.View view;
//
//    private DetailsActivityPresenter presenter;
//
//    @Before
//    public void setUp() throws Exception {
//        presenter = new DetailsActivityPresenter(connectionsDataSource);
//        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
//    }
//
//    @Test
//    public void shouldDeliverDatabaseIdToDeleteToModel() {
//        int databaseIdFromUri = 1;
//        Mockito.when(view.getConnectionToDelete()).thenReturn(Single.just(databaseIdFromUri));
//
//        presenter.deliverDatabaseIdtoDelete();
//
//        Mockito.verify(connectionsDataSource).deleteConnection(databaseIdFromUri);
//    }
//
//    @Test
//    public void shouldHandleDeletionError() {
//        int databaseIdFromUri = 1;
//        Mockito.when(view.getConnectionToDelete()).thenReturn(Single.just(databaseIdFromUri));
//
//        int resultCode = -1;
//        Mockito.when(connectionsDataSource.deleteConnection(databaseIdFromUri)).thenReturn(resultCode);
//
//        presenter.deliverDatabaseIdtoDelete();
//
//        Mockito.verify(view).displayError();
//    }
//
//    @Test
//    public void shouldHandleDeletionSuccess() {
//        int databaseIdFromUri = 1;
//        Mockito.when(view.getConnectionToDelete()).thenReturn(Single.just(databaseIdFromUri));
//
//        int resultCode = 1;
//        Mockito.when(connectionsDataSource.deleteConnection(databaseIdFromUri)).thenReturn(resultCode);
//
//        presenter.deliverDatabaseIdtoDelete();
//
//        Mockito.verify(view).displaySuccess();
//    }
}
