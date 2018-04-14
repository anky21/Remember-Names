package me.anky.connectid.details;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.microsoft.appcenter.analytics.Analytics;

import java.io.File;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;

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

    @BindView(R.id.tags_tv)
    TextView mTagsTv;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        intent = getIntent();
        databaseId = intent.getIntExtra("id", 0);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    private Intent createShareIntent() {
        StringBuffer sb = new StringBuffer();
        StringBuffer sbAnalytics = new StringBuffer();
        sb.append("Hey, I'm sharing this profile with you. First name: " + mFirstName);
        sbAnalytics.append("First name: " + mFirstName);
        if (!mLastName.equals("")) {
            sb.append(". Last name: " + mLastName);
        }
        if (!mMeetVenue.equals("")) {
            sb.append(". We met at: " + mMeetVenue);
            sbAnalytics.append(". We met at: " + mMeetVenue);
        }

        if (!mAppearance.equals("")) {
            sb.append(". Appearance: " + mAppearance);
            sbAnalytics.append(". Appearance: " + mAppearance);
        }

        if (!mFeature.equals("")) {
            sb.append(". Feature: " + mFeature);
            sbAnalytics.append(". Feature: " + mFeature);
        }

        if (!mCommonFriends.equals("")) {
            sb.append(". Common friends: " + mCommonFriends);
            sbAnalytics.append(". Common friends: " + mCommonFriends);
        }

        if (!mDescription.equals("")) {
            sb.append(". Description: " + mDescription);
            sbAnalytics.append(". Description: " + mDescription);
        }

        int okUnicode = 0x1F44C;
        String okEmoji = new String(Character.toChars(okUnicode));
        sb.append(" " + okEmoji);

        String shareMsg = sb.toString();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareMsg);

        Utilities.eventsOneParam("Message", sbAnalytics.toString(), "Share Profile Msg");
        Utilities.logFirebaseEvents("Share profile msg", sbAnalytics.toString());

        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_share:
                Intent intent = createShareIntent();
                if (intent != null) {
                    startActivity(Intent.createChooser(intent, "Share this profile"));
                }
                break;
            case R.id.action_delete:
                showDeleteDialog();
                Utilities.eventsOneParam("click", "menu_button", "Delete Connection");
                Utilities.logFirebaseEvents("Delete connection", "menu_button");

                break;
            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(this);
                onBackPressed();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
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

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String path = directory.getAbsolutePath() + "/" + imageName;

        RequestOptions myOptions = new RequestOptions()
                .centerCrop();

        Glide.with(this)
                .applyDefaultRequestOptions(myOptions)
                .load(Uri.fromFile(new File(path)))
                .into(mPortraitIv);

        mMeetVenueTv.setText(mMeetVenue);
        mAppearanceTv.setText(mAppearance);
        mFeatureTv.setText(mFeature);
        mCommonFriendsTv.setText(mCommonFriends);
        mDescriptionTv.setText(mDescription);
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

        Toast.makeText(this, R.string.delete_success_msg, Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @OnClick(R.id.edit_fab)
    public void launchEditActivity(View view) {
        Analytics.trackEvent("Click Edit FAB");
        Utilities.logFirebaseEventWithNoParams("Click edit FAB");

        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("DETAILS", connection);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @OnClick(R.id.btn_delete)
    public void deleteConnection(View view) {
        showDeleteDialog();

        Utilities.eventsOneParam("click", "bottom_button", "Delete Connection");
        Utilities.logFirebaseEvents("Delete connection", "bottom_button");

    }
}
