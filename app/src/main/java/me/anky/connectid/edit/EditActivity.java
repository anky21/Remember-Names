package me.anky.connectid.edit;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
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
    private ConnectidConnection connection;
    private ConnectidConnection newConnection;
    private ConnectidConnection updatedConnection;
    private boolean intentHasExtra = false;
    private int mDatabaseId;

    // TODO Allow user clicking outside of EditText to close the soft keyboard
    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

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

        Intent intent = getIntent();
        if (intent.hasExtra("DETAILS")) {
            intentHasExtra = true;
            connection = intent.getParcelableExtra("DETAILS");
            mDatabaseId = connection.getDatabaseId();
            String firstName = connection.getFirstName();
            String lastName = connection.getLastName();
            mImageName = connection.getImageName();
            String meetVenue = connection.getMeetVenue();
            String appearance = connection.getAppearance();
            String feature = connection.getFeature();
            String commonFriends = connection.getCommonFriends();
            String description = connection.getDescription();

            mFirstNameEt.setText(firstName);
            mLastNameEt.setText(lastName);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            String path = directory.getAbsolutePath() + "/" + mImageName;
            Glide.with(this)
                    .load(Uri.fromFile(new File(path)))
                    .into(mPortraitIv);
            mMeetVenueEt.setText(meetVenue);
            mAppearanceEt.setText(appearance);
            mFeatureEt.setText(feature);
            mCommonFriendsEt.setText(commonFriends);
            mDescriptionEt.setText(description);

            mCollapsingToolbarLayout.setTitle(getString(R.string.title_edit_connection));
        } else {
            mCollapsingToolbarLayout.setTitle(getString(R.string.title_add_new_connection));
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveConnection();
                // Exit activity
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    private void saveConnection() {
        if (!mImageName.equals("blank_profile.jpg")) {
            Utilities.saveToInternalStorage(this, mBitmap, mImageName);
        }
        String firstName = mFirstNameEt.getText().toString().trim();
        String lastName = mLastNameEt.getText().toString().trim();
        String meetVenue = mMeetVenueEt.getText().toString().trim();
        String appearance = mAppearanceEt.getText().toString().trim();
        String feature = mFeatureEt.getText().toString().trim();
        String commonFriends = mCommonFriendsEt.getText().toString().trim();
        String description = mDescriptionEt.getText().toString().trim();

        if (intentHasExtra) {
            updatedConnection = new ConnectidConnection(
                    mDatabaseId,
                    firstName,
                    lastName,
                    mImageName,
                    meetVenue,
                    appearance,
                    feature,
                    commonFriends,
                    description);
            presenter.updateConnection();
        } else {
            newConnection = new ConnectidConnection(
                    firstName,
                    lastName,
                    mImageName,
                    meetVenue,
                    appearance,
                    feature,
                    commonFriends,
                    description);

            Log.i("MVP view", "clicked Add Connection");

            presenter.deliverNewConnection();
        }
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
    public Single<ConnectidConnection> getUpdatedConnection() {
        return Single.fromCallable(new Callable<ConnectidConnection>() {
            @Override
            public ConnectidConnection call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                return updatedConnection;
            }
        });
    }

    @Override
    public void displaySuccess() {
        Log.i("MVP view", "displaySuccess called - successfully inserted into database");

        Intent data = new Intent();
        if (intentHasExtra) {
            data.putExtra("edit_activity_result", updatedConnection);
        } else {
            data.putExtra("edit_activity_result", newConnection);
        }
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void displayError() {
        Log.i("MVP view", "displayError called - failed to insert into database");
    }
}
