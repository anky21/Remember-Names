package me.anky.connectid.edit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.EditDataSource;

public class EditActivityPresenterTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    EditDataSource editDataSource;

    @Mock
    EditActivityView view;

    private EditActivityPresenter presenter;

    @Before
    public void setUp() throws Exception {
        presenter = new EditActivityPresenter(
                view, editDataSource, Schedulers.trampoline());
        RxJavaPlugins.setIoSchedulerHandler(scheduler -> Schedulers.trampoline());
    }

    @Test
    public void shouldDeliverConnectionToModel() {

        ConnectidConnection newConnection = new ConnectidConnection();
        Mockito.when(view.getNewConnection()).thenReturn(newConnection);

        presenter.deliverNewConnection();

        Mockito.verify(editDataSource).putNewConnection(newConnection);
    }

    @Test
    public void shouldHandleInsertionError() {

        int resultCode = 1;
        Mockito.when(editDataSource.getResultCode()).thenReturn(resultCode);

        presenter.deliverError();

        Mockito.verify(view).displayError();
    }
}
