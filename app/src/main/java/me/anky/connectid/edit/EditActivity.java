package me.anky.connectid.edit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.EditDataSource;
import me.anky.connectid.root.ConnectidApplication;

public class EditActivity extends AppCompatActivity implements EditActivityView {

    // TODO Allow user clicking outside of EditText to close the soft keyboard

    @BindView(R.id.name_et)
    EditText nameEt;

    @BindView(R.id.description_et)
    EditText descriptionEt;

    EditActivityPresenter presenter;

    @Inject
    EditDataSource editDataSource;

    ConnectidConnection newConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter = new EditActivityPresenter(
                this, editDataSource, AndroidSchedulers.mainThread());
    }

    @Override
    protected void onStop() {
        super.onStop();

        presenter.unsubscribe();
    }

    public void handleAddConnectionClicked(View view) {

        newConnection = new ConnectidConnection(
                nameEt.getText().toString(),
                descriptionEt.getText().toString());

        Log.i("MVP view", "clicked Add Connection\n" +
                newConnection.getName() + " - " +
                newConnection.getDescription());

        presenter.deliverNewConnection();

    }

    @Override
    public ConnectidConnection getNewConnection() {
        Log.i("MVP view", "getNewConnection returning " + newConnection.getName());

        return newConnection;
    }

    @Override
    public void displayError() {
        Log.i("MVP view", "failed to add new connection to database");
    }
}
