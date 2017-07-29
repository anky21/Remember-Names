package me.anky.connectid.edit;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.root.ConnectidApplication;

import static me.anky.connectid.Utilities.resizeBitmap;

public class EditActivity extends AppCompatActivity implements EditActivityMVP.View {
    private final static String TAG = EditActivity.class.getSimpleName();

    private Bitmap mBitmap;
    private String mImageName = "blank_profile.jpg";

    // TODO Allow user clicking outside of EditText to close the soft keyboard
    @BindView(R.id.toolbar_edit)
    Toolbar mToolbar;

    @BindView(R.id.edit_portrait_iv)
    ImageView mPortraitIv;

    @BindView(R.id.first_name_et)
    EditText mFirstNameEt;

    @BindView(R.id.last_name_et)
    EditText mLastNameEt;

    @BindView(R.id.meet_venue_et)
    EditText mMeetVenueEt;

    @BindView(R.id.appearance_et)
    EditText mAppearanceEt;

    @BindView(R.id.feature_et)
    EditText mFeatureEt;

    @BindView(R.id.common_friends_et)
    EditText mCommonFriendsEt;

    @BindView(R.id.description_et)
    EditText mDescriptionEt;

    @Inject
    EditActivityPresenter presenter;

    ConnectidConnection newConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        // Set a Toolbar to act as the ActionBar for this Activity window
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle("Add new connection");
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

    @OnClick(R.id.edit_portrait_iv)
    public void changePortraitPhoto() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(null);
        PickerFragment newFragment = new PickerFragment();
        newFragment.show(fm, "dialog");
        ft.commit();
    }

    public void changePhoto(Object object) {
        try {
            if (object instanceof Uri) {
                Uri imageUri = (Uri) object;
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                mBitmap = BitmapFactory.decodeStream(imageStream);
                mBitmap = resizeBitmap(mBitmap);

                mImageName = Utilities.generateImageName();

                mPortraitIv.setImageBitmap(mBitmap);

            } else {
                mBitmap = (Bitmap) object;
                mBitmap = resizeBitmap(mBitmap);
                mImageName = Utilities.generateImageName();

                mPortraitIv.setImageBitmap(mBitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "error in changing photo");
        }
    }

    @OnClick(R.id.add_connection_button)
    public void handleAddConnectionClicked(View view) {
        Utilities.saveToInternalStorage(this, mBitmap, mImageName);

        newConnection = new ConnectidConnection(
                mFirstNameEt.getText().toString(),
                mLastNameEt.getText().toString(),
                mImageName,
                mMeetVenueEt.getText().toString(),
                mAppearanceEt.getText().toString(),
                mFeatureEt.getText().toString(),
                mCommonFriendsEt.getText().toString(),
                mDescriptionEt.getText().toString());

        Log.i("MVP view", "clicked Add Connection\n" +
                newConnection.getFirstName() + " - " +
                newConnection.getDescription());

        presenter.deliverNewConnection();
    }

    @Override
    public Single<ConnectidConnection> getNewConnection() {
        Log.i("MVP view", "getNewConnection returning " + newConnection.getFirstName());

        return Single.fromCallable(new Callable<ConnectidConnection>() {
            @Override
            public ConnectidConnection call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                return newConnection;
            }
        });
    }

    @Override
    public void displaySuccess() {
        Log.i("MVP view", "displaySuccess called - successfully inserted into database");

        Intent data = new Intent();
        data.putExtra("edit_activity_result", newConnection);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void displayError() {
        Log.i("MVP view", "displayError called - failed to insert into database");
    }
}
