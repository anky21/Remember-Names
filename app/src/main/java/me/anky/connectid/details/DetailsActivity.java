package me.anky.connectid.details;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.OnBackPressedCallback;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;
import io.getstream.photoview.PhotoView;

public class DetailsActivity extends AppCompatActivity implements DetailsActivityMVP.View {
    private final static String TAG = "DetailsActivity";

    int databaseId;
    ConnectidConnection connection;
    private Intent intent;
    private String mDatabaseId;
    private String mFirstName;
    private String mLastName;
    private String mMeetVenue;
    private String mAppearance;
    private String mFeature;
    private String mCommonFriends;
    private String mDescription;
    private String mTags;
    private String mImageName;
    private File mPortraitImageFile;

    @BindView(R.id.toolbar_1)
    Toolbar mToolbar;

    @BindView(R.id.portrait_iv)
    ImageView mPortraitIv;

    @BindView(R.id.fullscreen_photo_overlay)
    View mFullscreenPhotoOverlay;

    @BindView(R.id.fullscreen_photo)
    PhotoView mFullscreenPhoto;

    @BindView(R.id.close_fullscreen_photo)
    ImageView mCloseFullscreenPhoto;

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

    @BindView(R.id.tags_tv)
    TextView mTagsTv;

    @BindView(R.id.adView)
    AdView mAdView;

    @Inject
    DetailsActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);
        setupBackNavigation();

        // Ensure a consistent blue status bar on this screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        // Only load banner ads if user is not ad-free
        if (!ConnectidApplication.getAppInstance().getSubscriptionManager().isAdFree()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        // Set a Toolbar to act as the ActionBar for this Activity window
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        intent = getIntent();
        databaseId = intent.getIntExtra("id", 0);

        mPortraitIv.setOnClickListener(view -> showFullscreenPhoto());
        mCloseFullscreenPhoto.setOnClickListener(view -> hideFullscreenPhoto());
        mFullscreenPhotoOverlay.setOnClickListener(view -> hideFullscreenPhoto());
        mFullscreenPhoto.setOnClickListener(view -> { });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
        if (databaseId != -1) {
            presenter.loadConnection(databaseId);
        } else {
            Toast.makeText(this, R.string.data_load_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mFullscreenPhotoOverlay.getVisibility() == View.VISIBLE) {
                    hideFullscreenPhoto();
                    return;
                }
                finish();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    private Intent createShareIntent() {
        StringBuffer sb = new StringBuffer();
        sb.append(getString(R.string.share_msg_1) + mFirstName);
        if (!mLastName.equals("")) {
            sb.append(getString(R.string.share_msg_7) + mLastName);
        }
        if (!mMeetVenue.equals("")) {
            sb.append(getString(R.string.share_msg_2) + mMeetVenue);
        }

        if (!mAppearance.equals("")) {
            sb.append(getString(R.string.share_msg_3) + mAppearance);
        }

        if (!mFeature.equals("")) {
            sb.append(getString(R.string.share_msg_4) + mFeature);
        }

        if (!mCommonFriends.equals("")) {
            sb.append(getString(R.string.share_msg_5) + mCommonFriends);
        }


        if (!mDescription.equals("")) {
            sb.append(getString(R.string.share_msg_6) + mDescription);
        }

        int okUnicode = 0x1F44C;
        String okEmoji = new String(Character.toChars(okUnicode));
        sb.append(" " + okEmoji);
        String shareMsg = sb.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareMsg);

        Utilities.logFirebaseEventWithNoParams("profile_shared");

        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Intent intent = createShareIntent();
                if (intent != null) {
                    startActivity(Intent.createChooser(intent, getString(R.string.share_this_profile)));
                }
                break;
            case R.id.action_delete:
                showDeleteDialog();
                Utilities.logFirebaseEventWithNoParams("profile_delete_requested");

                break;
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                getOnBackPressedDispatcher().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        // Create an AlertDialog to confirm the delete
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_connection_dialog_msg);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.deliverDatabaseIdtoDelete();
                if (mTags != null && mTags.length() != 0) {
                    presenter.loadAndUpdateTagTable(mDatabaseId, mTags);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

    @Override
    public void displayConnection(ConnectidConnection connection) {
        this.connection = connection;
        mDatabaseId = String.valueOf(connection.getDatabaseId());
        mFirstName = connection.getFirstName();
        mLastName = connection.getLastName();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mFirstName + " " + mLastName);
        }
        String imageName = connection.getImageName();
        mImageName = imageName;
        mMeetVenue = connection.getMeetVenue();
        mAppearance = connection.getAppearance();
        mFeature = connection.getFeature();
        mCommonFriends = connection.getCommonFriends();
        mDescription = connection.getDescription();
        mTags = connection.getTags();
        if (mTags == null) {
            mTagsTv.setText("");
        } else {
            mTagsTv.setText(mTags);
        }

        RequestOptions myOptions = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.blank_profile_round)
                .error(R.drawable.blank_profile_round);

        // When there is no real stored image (or the placeholder name is used), show the
        // bundled default portrait drawable instead of loading from internal storage.
        if (imageName == null || imageName.equals("") || imageName.equals("blank_profile.jpg")) {
            mPortraitImageFile = null;
            Glide.with(this)
                    .applyDefaultRequestOptions(myOptions)
                    .load(R.drawable.blank_profile_round)
                    .into(mPortraitIv);
        } else {
            File imageFile = Utilities.getContactImageFile(this, imageName);
            File previewFile = Utilities.getBestContactPreviewFile(this, imageName);
            mPortraitImageFile = imageFile.exists() ? imageFile : null;

            Glide.with(this)
                    .applyDefaultRequestOptions(myOptions)
                    .load(Uri.fromFile(previewFile))
                    .into(mPortraitIv);
        }

        mMeetVenueTv.setText(mMeetVenue);
        mAppearanceTv.setText(mAppearance);
        mFeatureTv.setText(mFeature);
        mCommonFriendsTv.setText(mCommonFriends);
        mDescriptionTv.setText(mDescription);
    }

    private void showFullscreenPhoto() {
        if (mPortraitImageFile == null) {
            return;
        }

        Glide.with(this)
                .applyDefaultRequestOptions(new RequestOptions()
                        .dontTransform()
                        .override(Target.SIZE_ORIGINAL))
                .load(mPortraitImageFile)
                .into(mFullscreenPhoto);
        mFullscreenPhoto.setScale(1f, false);
        mFullscreenPhotoOverlay.setVisibility(View.VISIBLE);
    }

    private void hideFullscreenPhoto() {
        if (mFullscreenPhotoOverlay.getVisibility() != View.VISIBLE) {
            return;
        }
        mFullscreenPhoto.setScale(1f, false);
        mFullscreenPhotoOverlay.setVisibility(View.GONE);
    }

    @Override
    public Single<Integer> getConnectionToDelete() {

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
//        Log.i("MVP view", "delete failed");
    }

    @Override
    public void displaySuccess() {

        if (mPortraitImageFile != null && mPortraitImageFile.exists()
                && !mPortraitImageFile.delete()) {
            Utilities.logFirebaseError("error_delete_photo", TAG + ".displaySuccess");
        }
        if (mImageName != null && !mImageName.equals("")
                && !mImageName.equals("blank_profile.jpg")) {
            File previewFile = Utilities.getContactPreviewFile(this, mImageName);
            if (previewFile.exists() && !previewFile.delete()) {
                Utilities.logFirebaseError("error_delete_preview", TAG + ".displaySuccess");
            }
        }

        Toast.makeText(this, R.string.delete_success_msg, Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @OnClick(R.id.edit_fab)
    public void launchEditActivity(View view) {
        Utilities.logFirebaseEventWithNoParams("profile_edit_started");

        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("DETAILS", connection);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @OnClick(R.id.btn_delete)
    public void deleteConnection(View view) {
        showDeleteDialog();

        Utilities.logFirebaseEventWithNoParams("profile_delete_requested");

    }
}
