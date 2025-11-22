package me.anky.connectid.edit;

import android.app.FragmentManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.editTag.EditTagActivity;
import me.anky.connectid.events.SetToUpdateTagTable;
import me.anky.connectid.root.ConnectidApplication;

import static me.anky.connectid.Utilities.resizeBitmap;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import android.content.SharedPreferences;

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
    private boolean isExistingConnection = false;
    private int mDatabaseId = -1;
    //Boolean flag that keeps track of whether the connection has been edited (true) or not (false)
    private boolean mConnectionHasChanged = false;
    private static final int EDIT_TAG_ACTIVITY_REQUEST = 317;

    private List<ConnectionTag> mAllTags;
    private List<String> mTagsList;

//    @BindView(R.id.toolbar_edit)
//    Toolbar mToolbar;

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

    private InterstitialAd mInterstitialAd;
    private int adActionCount;
    private static final String PREFS_NAME = "AdActionPrefs";
    private static final String AD_ACTION_COUNT_KEY = "adActionCount";
    private SharedPreferences sharedPreferences;

    void showInterstitialAd(){
        if(mInterstitialAd!=null){
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(TAG, "Ad was clicked.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d(TAG, "Ad dismissed fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    Log.e(TAG, "Ad failed to show fullscreen content.");
                    mInterstitialAd = null;
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(TAG, "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d(TAG, "Ad showed fullscreen content.");
                }
            });
            mInterstitialAd.show(this);
        }else{
            Log.d("TAG", "The interstitial ad wasn't loaded.");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        Intent intent = getIntent();
        if (intent.hasExtra("DETAILS")) {
            isExistingConnection = true;
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

            RequestOptions myOptions = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.blank_profile_round)
                    .error(R.drawable.blank_profile_round);

            // For existing connections with no stored image (or only the placeholder name),
            // show the bundled default portrait instead of trying to load from internal storage.
            if (mImageName == null || mImageName.equals("") || mImageName.equals("blank_profile.jpg")) {
                Glide.with(this)
                        .applyDefaultRequestOptions(myOptions)
                        .load(R.drawable.blank_profile_round)
                        .into(mPortraitIv);
            } else {
                ContextWrapper cw = new ContextWrapper(getApplicationContext());
                // path to /data/data/yourapp/app_data/imageDir
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                String path = directory.getAbsolutePath() + "/" + mImageName;
                File imageFile = new File(path);

                Glide.with(this)
                        .applyDefaultRequestOptions(myOptions)
                        .load(Uri.fromFile(imageFile))
                        .into(mPortraitIv);
            }
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
            EditActivity.this.setTitle(getString(R.string.title_edit_connection));
        } else {
            EditActivity.this.setTitle(getString(R.string.title_add_new_connection));
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
                if (firstName.equals("")) {
                    Toast.makeText(this, R.string.first_name_required, Toast.LENGTH_SHORT).show();
                    return true;
                }
                // Save connection to database
                saveConnection();
                if (isExistingConnection){
                    finish();
                }
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
        String[] tagsArray = mTags.split(", ");
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
        Utilities.logFirebaseEventWithNoParams("Change portrait photo");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        PickerFragment newFragment = new PickerFragment();
        newFragment.show(ft, "dialog");

    }

    @OnClick(R.id.tags_linear_layout)
    public void launchEditTagActivity(View view) {
        Utilities.logFirebaseEventWithNoParams("Launch edit tag act");

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
//            Log.e(TAG, "error in changing photo");
            Utilities.logFirebaseError("error_change_photo", TAG + ".changePhoto", e.getMessage());
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
        if (firstName.equals("")) {
            Toast.makeText(this, R.string.first_name_required, Toast.LENGTH_SHORT).show();
            return;
        }
        String lastName = mLastNameEt.getText().toString().trim();
        String meetVenue = mMeetVenueEt.getText().toString().trim();
        String appearance = mAppearanceEt.getText().toString().trim();
        String feature = mFeatureEt.getText().toString().trim();
        String commonFriends = mCommonFriendsEt.getText().toString().trim();
        String description = mDescriptionEt.getText().toString().trim();

        if (isExistingConnection) {
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
        //
        //init interstitial ad and show every 5 times
        //
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        adActionCount = sharedPreferences.getInt(AD_ACTION_COUNT_KEY, 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(AD_ACTION_COUNT_KEY, adActionCount+1);
        editor.apply();

        if(adActionCount != 0 && adActionCount % 5 == 0){
            AdRequest adRequest = new AdRequest.Builder().build();
            //ca-app-pub-9341383437171371/7023517119
            InterstitialAd.load(this,"ca-app-pub-5943597081127232/2388295197", adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            // The mInterstitialAd reference will be null until
                            // an ad is loaded.
                            mInterstitialAd = interstitialAd;
                            Log.i(TAG, "onAdLoaded");
                            showInterstitialAd();
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            Log.d(TAG, loadAdError.toString());
                            mInterstitialAd = null;
                        }
                    });
        }else{
            Log.i(TAG, String.format("skip to show ad - count = %d",adActionCount));
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

//                System.out.println("Thread db: " + Thread.currentThread().getId());

                return updatedConnection;
            }
        });
    }

    @Override
    public void displaySuccess(int id) {
        if(!isExistingConnection){
            mDatabaseId = id;
        }
        Intent data = new Intent();
        setResult(RESULT_OK, data);
    }

    @Override
    public void handleAllTags(List<ConnectionTag> connectionTags) {
        mAllTags = connectionTags;
    }

    @Override
    public void displayError() {
//        Log.i("MVP view", "displayError called - failed to insert into database");
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
