package me.anky.connectid.connections;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidsx.rateme.RateMeDialog;
import com.androidsx.rateme.RateMeDialogTimer;
import com.facebook.stetho.Stetho;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.appinvite.FirebaseAppInvite;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.Utils.DialogUtils;
import me.anky.connectid.Utils.SqliteExporter;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.SharedPrefsHelper;
import me.anky.connectid.data.source.local.generated.ConnectidDatabase;
import me.anky.connectid.details.DetailsActivity;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;
import me.anky.connectid.tags.TagsActivity;

public class ConnectionsActivity extends AppCompatActivity implements
        ConnectionsActivityMVP.View,
        ConnectionsRecyclerViewAdapter.RecyclerViewClickListener {

    public List<ConnectidConnection> data = new ArrayList<>();
    private final static String TAG = "ConnectionsActivity";
    private ActionBarDrawerToggle mToggle;
    private AlertDialog alertDialog;
    private MenuItem searchItem;
    private SearchView searchView;

    @BindView(R.id.connections_list_rv)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.empty_view)
    LinearLayout emptyView;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;

    @BindView(R.id.adView)
    AdView mAdView;

    @Inject
    SharedPrefsHelper sharedPrefsHelper;

    @Inject
    ConnectionsActivityPresenter presenter;

    ConnectionsRecyclerViewAdapter adapter;

    private static final int DETAILS_ACTIVITY_REQUEST = 100;
    private static final int NEW_CONNECTION_REQUEST = 200;
    private static final int REQUEST_INVITE = 0;

    boolean shouldScrollToBottom = false;
    boolean shouldScrollToTop = false;
    private int mSortByOption;
    private boolean isBackBtnPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        // Initialise google ads
        MobileAds.initialize(this, getString(R.string.ad_mob_id));

        ButterKnife.bind(this);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mNavigationView.setItemIconTintList(null);

        mNavigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

        Stetho.initializeWithDefaults(this);

        if (!sharedPrefsHelper.get("profile_saved", false)) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture);
            Utilities.saveToInternalStorage(this, bitmap, "blank_profile.jpg");
            sharedPrefsHelper.put("profile_saved", true);
        }

        adapter = new ConnectionsRecyclerViewAdapter(this, data, false, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(dividerItemDecoration);
        setScrollListener(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final int launchTimes = 20;
        final int installDate = 7;

        RateMeDialogTimer.onStart(this);
        if (RateMeDialogTimer.shouldShowRateDialog(this, installDate, launchTimes)) {
            showPlainRateMeDialog();
        }
    }

    private void showPlainRateMeDialog() {
        new RateMeDialog.Builder(getPackageName(), getString(R.string.app_name))
                .showAppIcon(R.mipmap.ic_launcher_round)
                .build()
                .show(getFragmentManager(), "plain-dialog");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (searchView != null && !searchView.getQuery().equals("")) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        }
        super.onResume();
        presenter.setView(this);
        // Get current sort by menu option
        getSortByOption();
        presenter.loadConnections(mSortByOption);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sortby, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
    }

    private SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            closeNavigationMenu(); // Close if it's open
            if (newText.length() == 0) {
                adapter.setNewData(false, newText);
            } else {
                adapter.setNewData(true, newText);
            }
            adapter.filter();
            return true;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        switch (id) {
            case R.id.sortby_date_new: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 1);
                Utilities.logFirebaseEvents("Connections Sort Order", "new_first");
            }
            break;
            case R.id.sortby_date_old: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 2);
                Utilities.logFirebaseEvents("Connections Sort Order", "old_first");
            }
            break;
            case R.id.sortby_fname_a: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 3);
                Utilities.logFirebaseEvents("Connections Sort Order", "first name a-z");
            }
            break;
            case R.id.sortby_fname_z: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 4);
                Utilities.logFirebaseEvents("Connections Sort Order", "first name Z-A");
            }
            break;
            case R.id.sortby_lname_a: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 5);
                Utilities.logFirebaseEvents("Connections Sort Order", "last name a-z");
            }
            break;
            case R.id.sortby_lname_z: {
                closeNavigationMenu();
                sharedPrefsHelper.put(Utilities.SORTBY, 6);
                Utilities.logFirebaseEvents("Connections Sort Order", "last name Z-A");
            }
            break;
        }
        presenter.handleSortByOptionChange();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void displayConnections(List<ConnectidConnection> connections) {
        emptyView.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        data = connections;
//        Log.v("testing", "data size " + data.size());
        adapter.setConnections(connections);

        if (shouldScrollToBottom) {
            recyclerView.smoothScrollToPosition(connections.size() - 1);
            shouldScrollToBottom = false;
        }
        if (shouldScrollToTop) {
            recyclerView.smoothScrollToPosition(0);
            shouldScrollToTop = false;
        }
    }

    private void setScrollListener(RecyclerView recyclerView) {
        // Automatically hide/show the FAB when recycler view scrolls up/down
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void displayNoConnections() {
        recyclerView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getSortByOption() {
        mSortByOption = sharedPrefsHelper.get(Utilities.SORTBY, 0);
        return mSortByOption;
    }

    @Override
    public void displayError() {
        recyclerView.setVisibility(View.INVISIBLE);
        emptyView.setVisibility(View.INVISIBLE);
        Toast.makeText(this, R.string.data_loading_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(View view, int id) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, DETAILS_ACTIVITY_REQUEST);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @OnClick(R.id.fab)
    public void launchEditActivity(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        startActivityForResult(intent, NEW_CONNECTION_REQUEST);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

        Utilities.logFirebaseEventWithNoParams("FAB New Connection");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DETAILS_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                presenter.loadConnections(mSortByOption);
            }
        }

        if (requestCode == NEW_CONNECTION_REQUEST) {
            if (resultCode == RESULT_OK) {

                presenter.loadConnections(mSortByOption);
                if (mSortByOption == 2) {
                    shouldScrollToBottom = true;
                }
                if (mSortByOption == 0 || mSortByOption == 1) {
                    shouldScrollToTop = true;
                }
                Toast.makeText(this, R.string.new_profile_insertion_msg, Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
//                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                    Utilities.logFirebaseEvents("Invite Result id", id);
                }
            } else {
                Toast.makeText(this, getString(R.string.send_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        closeNavigationMenu();
        if (!isBackBtnPressedOnce) {
            Toast.makeText(this, R.string.press_back_to_exit, Toast.LENGTH_SHORT).show();
            isBackBtnPressedOnce = true;

            // if user doesn't press back btn again within 5 sec...
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    isBackBtnPressedOnce = false;
                }
            }, 5000);
        } else {
            finish();
            super.onBackPressed();
        }
    }

    @Override
    public void closeNavigationMenu() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
        }
    }

    @Override
    public void showExitDialog() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_to_exit);
        builder.setPositiveButton("Yes", (dialog, which) -> finish());
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });
        // Create and show the AlertDialog
        alertDialog = builder.show();
    }

    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            menuItem -> {
                switch (menuItem.getItemId()) {
                    case (R.id.nav_tags):
                        Utilities.logFirebaseEventWithNoParams("Start Tags Activity");

                        closeNavigationMenu();
                        Intent intent = new Intent(getApplicationContext(), TagsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                        break;
                    case (R.id.nav_invite):
                        Utilities.logFirebaseEventWithNoParams("Nav Invite Friends");
                        closeNavigationMenu();
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Hi. This app can help you remember people's names and organise your contacts. You can download it here: https://c9479.app.goo.gl/eNh4");
                        sendIntent.setType("text/plain");
                        Intent shareIntent = Intent.createChooser(sendIntent, null);
                        startActivity(shareIntent);
                        break;
                    case (R.id.nav_email_csv):
                        DialogUtils.askQuestionAndThenCancelable(ConnectionsActivity.this,
                                getString(R.string.export_databse_title), getString(R.string.export_database_msg),
                                getString(R.string.yes), getString(R.string.cancel), object -> exportAndEmailCsv(), null);
                        break;
                    case (R.id.nav_exit):
                        closeNavigationMenu();
                        showExitDialog();
                        break;
                    default:
                        break;
                }
                return true;
            };

    private void exportAndEmailCsv() {
        SQLiteOpenHelper database = ConnectidDatabase.getInstance(ConnectionsActivity.this);
        SQLiteDatabase db = database.getWritableDatabase();
        try {
            String csvPath = SqliteExporter.export(db, ConnectionsActivity.this);

            if (csvPath != null && !csvPath.isEmpty()) {
                File file = new File(csvPath);
                Uri uri = Uri.fromFile(file);

                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Remember Names database backup");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Thank you for using this app. \nYou can email this file to yourself or save it to your cloud drive. This file can be opened by Microsoft Excel.");

                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
