package me.anky.connectid.edit;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import events.SetToUpdateTagTable;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.editTag.EditTagActivity;
import me.anky.connectid.root.ConnectidApplication;

import static me.anky.connectid.Utilities.resizeBitmap;

public class EditActivity extends AppCompatActivity implements EditActivityMVP.View {
    private final static String TAG = EditActivity.class.getSimpleName();

    private Bitmap mBitmap;
    private String mOldImageName;
    private String mImageName = "blank_profile.jpg";
    private String mFirstName = "";
    private String mLastName = "";
    private String mMeetVenue = "";
    private String mAppearance = "";
    private String mFeature = "";
    private String mCommonFriends = "";
    private String mDescription = "";
    private String mTags;
    private ConnectidConnection connection;
    private ConnectidConnection newConnection;
    private ConnectidConnection updatedConnection;
    private boolean intentHasExtra = false;
    private int mDatabaseId = -1;
    //Boolean flag that keeps track of whether the connection has been edited (true) or not (false)
    private boolean mConnectionHasChanged = false;
    private static final int EDIT_TAG_ACTIVITY_REQUEST = 317;

    private List<ConnectionTag> mAllTags;
    private List<String> mTagsList;

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

    @BindView(R.id.tags_container)
    RelativeLayout mTagsContainer;

    @BindView(R.id.empty_tags_tv)
    TextView mEmptyTagsTv;

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
            mFirstName = connection.getFirstName();
            mLastName = connection.getLastName();
            mOldImageName = connection.getImageName();
            mImageName = connection.getImageName();
            mMeetVenue = connection.getMeetVenue();
            mAppearance = connection.getAppearance();
            mFeature = connection.getFeature();
            mCommonFriends = connection.getCommonFriends();
            mDescription = connection.getDescription();
            mTags = connection.getTags();

            mFirstNameEt.setText(mFirstName);
            mLastNameEt.setText(mLastName);
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            // path to /data/data/yourapp/app_data/imageDir
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            String path = directory.getAbsolutePath() + "/" + mImageName;
            RequestOptions myOptions = new RequestOptions()
                    .centerCrop();

            Glide.with(this)
                    .applyDefaultRequestOptions(myOptions)
                    .load(Uri.fromFile(new File(path)))
                    .into(mPortraitIv);
            mMeetVenueEt.setText(mMeetVenue);
            mAppearanceEt.setText(mAppearance);
            mFeatureEt.setText(mFeature);
            mCommonFriendsEt.setText(mCommonFriends);
            mDescriptionEt.setText(mDescription);

            ViewTreeObserver vto = mTagsContainer.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int viewWidth = mTagsContainer.getMeasuredWidth();
                        // handle viewWidth here...
                        displayTags(mTags);

                        if (viewWidth > 0) {
                            mTagsContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }

            getSupportActionBar().setTitle(getString(R.string.title_edit_connection));
        } else {
            getSupportActionBar().setTitle(getString(R.string.title_add_new_connection));
        }
    }

    private void displayTags(String tags) {
        if (tags == null) {
            mEmptyTagsTv.setVisibility(View.VISIBLE);
            mTagsContainer.setVisibility(View.GONE);
        } else {
            String[] tagsArray = tags.split(", ");
            List<String> tagsList = new ArrayList(Arrays.asList(tagsArray));
            mEmptyTagsTv.setVisibility(View.GONE);
            mTagsContainer.setVisibility(View.VISIBLE);
            Utilities.displayTags(this, tagsList, mTagsContainer);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
        presenter.loadTags();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
                if (mTags != null) {
                    // Convert tags string to string list
                    convertTagsStringToList();
                }
                // Check if first name is provided
                String firstName = mFirstNameEt.getText().toString().trim();
                if (firstName == null || firstName.equals("")) {
                    Toast.makeText(this, R.string.first_name_required, Toast.LENGTH_SHORT).show();
                    return true;
                }
                // Save pet to database
                saveConnection();
//                presenter.updateTagTable(mAllTags, mTagsList, mDatabaseId);
//                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                return true;
            case android.R.id.home:
                checkIfNameChanged();
                // If the connection hasn't changed, continue with navigating up to parent activity
                if (!mConnectionHasChanged) {
//                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    super.onBackPressed();
                    overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                    return true;
                }

                // Otherwise, warn the user with a dialog
                showUnsavedChangesDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setToUpdateTagTable(SetToUpdateTagTable event) {
        presenter.updateTagTable(mAllTags, mTagsList, event.getDatabaseId());
        finish();
    }

    private List<String> convertTagsStringToList() {
        String[] tagsArray = mTags.split(",");
        mTagsList = new ArrayList(Arrays.asList(tagsArray));
        return mTagsList;
    }

    @Override
    public void onBackPressed() {
        checkIfNameChanged();
        // Continue with handling back button press when there is no change
        if (!mConnectionHasChanged) {
            super.onBackPressed();
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
            return;
        }

        // Otherwise, warn the user about the unsaved changes
        showUnsavedChangesDialog();
    }

    @OnClick(R.id.edit_portrait_iv)
    public void changePortraitPhoto() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        PickerFragment newFragment = new PickerFragment();
        newFragment.show(fm, "dialog");
        ft.commit();
    }

    @OnClick(R.id.tags_linear_layout)
    public void launchEditTagActivity(View view) {
        Intent intent = new Intent(this, EditTagActivity.class);
        if (mDatabaseId != -1) {
            intent.putExtra("data_id", mDatabaseId);
        }
        intent.putExtra("tags", mTags);
        startActivityForResult(intent, EDIT_TAG_ACTIVITY_REQUEST);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
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
            // Connection is modified when image name is changed
            mConnectionHasChanged = true;
        } catch (Exception e) {
            Log.e(TAG, "error in changing photo");
        }
    }

    private void saveConnection() {

        if (mOldImageName != null && !mOldImageName.equals("blank_profile.jpg")) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            File oldImage = new File(directory, mOldImageName);
            oldImage.delete();
        }
        if (!mImageName.equals("blank_profile.jpg")) {
            Utilities.saveToInternalStorage(this, mBitmap, mImageName);
        }
        // Check if first name is provided
        String firstName = mFirstNameEt.getText().toString().trim();
        if (firstName == null || firstName.equals("")) {
            Toast.makeText(this, R.string.first_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        String lastName = mLastNameEt.getText().toString().trim();
        String meetVenue = mMeetVenueEt.getText().toString().trim();
        String appearance = mAppearanceEt.getText().toString().trim();
        String feature = mFeatureEt.getText().toString().trim();
        String commonFriends = mCommonFriendsEt.getText().toString().trim();
        String description = mDescriptionEt.getText().toString().trim();
//        String tags = "hello tags,cool,US,black";

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
                    description,
                    mTags);

            presenter.deliverNewConnection();
        }
    }

    @Override
    public Single<ConnectidConnection> getNewConnection() {
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
    public void displaySuccess(int id) {
        if(!intentHasExtra){
            mDatabaseId = id;
        }
        Intent data = new Intent();
        setResult(RESULT_OK, data);
//        finish();
    }

    @Override
    public void handleAllTags(List<ConnectionTag> connectionTags) {
        mAllTags = connectionTags;
    }

    @Override
    public void displayError() {
        Log.i("MVP view", "displayError called - failed to insert into database");
    }

    private void showUnsavedChangesDialog() {
        // Create an AlertDialog.Builder and set the message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveConnection();
            }
        });

        builder.setNeutralButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Check if any text input field has been changed
    private void checkIfNameChanged() {
        if (!mConnectionHasChanged) {
            if (!mFirstNameEt.getText().toString().trim().equals(mFirstName) ||
                    !mLastNameEt.getText().toString().trim().equals(mLastName) ||
                    !mMeetVenueEt.getText().toString().trim().equals(mMeetVenue) ||
                    !mAppearanceEt.getText().toString().trim().equals(mAppearance) ||
                    !mFeatureEt.getText().toString().trim().equals(mFeature) ||
                    !mCommonFriendsEt.getText().toString().trim().equals(mCommonFriends) ||
                    !mDescriptionEt.getText().toString().trim().equals(mDescription)) {
                mConnectionHasChanged = true;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_TAG_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
            mTags = data.getStringExtra("connectionTags");
            if (mTags != null){
                mTags = mTags.substring(1, mTags.length() - 1);
            }
            displayTags(mTags);
        }
    }
}
