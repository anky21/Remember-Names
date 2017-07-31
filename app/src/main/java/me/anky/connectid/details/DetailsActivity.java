package me.anky.connectid.details;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;

public class DetailsActivity extends AppCompatActivity implements DetailsActivityMVP.View {
    private final static String TAG = DetailsActivity.class.getSimpleName();
    private static final int EDIT_CONNECTION_REQUEST = 300;
    int databaseId;
    ConnectidConnection connection;
    private Intent intent;

    @BindView(R.id.toolbar_1)
    Toolbar mToolbar;

    @BindView(R.id.portrait_iv)
    ImageView mPortraitIv;

    @BindView(R.id.meet_venue_tv)
    TextView mMeetVenueTv;

    @BindView(R.id.appearance_tv)
    TextView mAppearanceTv;

    @BindView(R.id.feature_tv)
    TextView mFeatureTv;

    @BindView(R.id.common_friends_tv)
    TextView mCommonFriendsTv;

    @BindView(R.id.description_tv)
    TextView mDescriptionTv;

    @Inject
    DetailsActivityPresenter presenter;

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

        intent = getIntent();
        ConnectidConnection intentConnection = intent.getParcelableExtra("DETAILS");
        databaseId = intentConnection.getDatabaseId();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
        presenter.loadConnection(databaseId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    @Override
    public void displayConnection(ConnectidConnection connection) {
        this.connection = connection;
        String firstName = connection.getFirstName();
        String lastName = connection.getLastName();

        getSupportActionBar().setTitle(firstName + " " + lastName);
        String imageName = connection.getImageName();
        String meetVenue = connection.getMeetVenue();
        String appearance = connection.getAppearance();
        String feature = connection.getFeature();
        String commonFriends = connection.getCommonFriends();
        String description = connection.getDescription();

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String path = directory.getAbsolutePath() + "/" + imageName;
        Glide.with(this)
                .load(Uri.fromFile(new File(path)))
                .into(mPortraitIv);
        mMeetVenueTv.setText(meetVenue);
        mAppearanceTv.setText(appearance);
        mFeatureTv.setText(feature);
        mCommonFriendsTv.setText(commonFriends);
        mDescriptionTv.setText(description);
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

    @OnClick(R.id.edit_fab)
    public void launchEditActivity(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("DETAILS", connection);
        startActivityForResult(intent, EDIT_CONNECTION_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_CONNECTION_REQUEST) {
            if (resultCode == RESULT_OK) {

            }
        }
    }
}
