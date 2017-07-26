package me.anky.connectid.details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.root.ConnectidApplication;

public class DetailsActivity extends AppCompatActivity implements DetailsActivityMVP.View {
    private final static String TAG = DetailsActivity.class.getSimpleName();
    private String mImageName = "profile.jpg";

    @BindView(R.id.toolbar_1)
    Toolbar mToolbar;

    @BindView(R.id.portrait_iv)
    ImageView mPortraitIv;

    @BindView(R.id.description_tv)
    TextView mDescriptionTv;

    @Inject
    DetailsActivityPresenter presenter;

    int databaseId;
    ConnectidConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        // Set a Toolbar to act as the ActionBar for this Activity window
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        Intent intent = getIntent();
        connection = intent.getParcelableExtra("DETAILS");
        databaseId = connection.getDatabaseId();
        String feature = connection.getFeature();
        String firstName = connection.getFirstName();
        String lastName = connection.getLastName();
        getSupportActionBar().setTitle(firstName + " " + lastName);

        mDescriptionTv.setText("Database item id: " + databaseId + " " + feature);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    public void handleDeleteConnectionClicked(View view) {
        presenter.deliverDatabaseIdtoDelete();
    }

    @Override
    public Single<Integer> getConnectionToDelete() {

        Log.i("MVP view", "getConnectionToDelete returning " + databaseId);

        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                return databaseId;
            }
        });
    }

    @Override
    public void displayError() {
        Log.i("MVP view", "delete failed");
    }

    @Override
    public void displaySuccess() {

        Log.i("MVP view", "delete succeeded");

        Toast.makeText(this, databaseId + " deleted!", Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }
}
