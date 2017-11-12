package me.anky.connectid.connections;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidsx.rateme.RateMeDialog;
import com.androidsx.rateme.RateMeDialogTimer;
import com.facebook.stetho.Stetho;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.Constant;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.SharedPrefsHelper;
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

    @Inject
    SharedPrefsHelper sharedPrefsHelper;

    @Inject
    ConnectionsActivityPresenter presenter;

    ConnectionsRecyclerViewAdapter adapter;

    private static final int DETAILS_ACTIVITY_REQUEST = 100;
    private static final int NEW_CONNECTION_REQUEST = 200;
    boolean shouldScrollToBottom = false;
    boolean shouldScrollToTop = false;
    private int mSortByOption;
    private boolean isBackBtnPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mNavigationView.setItemIconTintList(null);

        mNavigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);

        Stetho.initializeWithDefaults(this);

        if (!sharedPrefsHelper.get("profile_saved", false)) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture);
            Utilities.saveToInternalStorage(this, bitmap, "blank_profile.jpg");
            sharedPrefsHelper.put("profile_saved", true);
        }

        adapter = new ConnectionsRecyclerViewAdapter(this, data, this);

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
    protected void onResume() {
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        int id = item.getItemId();

        switch (id) {
            case R.id.action_tag: {
                closeNavigationMenu();

                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "btn_tags_activity_clicked");

                Intent intent = new Intent(this, TagsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
            }

            case R.id.sortby_date_new: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 1);
                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "sorby_new");
            }
            break;
            case R.id.sortby_date_old: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 2);
                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "sorby_old");
            }
            break;
            case R.id.sortby_fname_a: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 3);
                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "sorby_firstname_a");
            }
            break;
            case R.id.sortby_fname_z: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 4);
                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "sorby_firstname_z");
            }
            break;
            case R.id.sortby_lname_a: {
                closeNavigationMenu();

                sharedPrefsHelper.put(Utilities.SORTBY, 5);
                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "sorby_lastname_a");
            }
            break;
            case R.id.sortby_lname_z: {
                closeNavigationMenu();
                sharedPrefsHelper.put(Utilities.SORTBY, 6);
                Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "sorby_lastname_z");
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
    public void onItemClick(View view, int position) {
//        Log.i("MVP view", "position " + position + " clicked");

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("DETAILS", data.get(position));
        startActivityForResult(intent, DETAILS_ACTIVITY_REQUEST);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @OnClick(R.id.fab)
    public void launchEditActivity(View view) {
        Intent intent = new Intent(this, EditActivity.class);
        startActivityForResult(intent, NEW_CONNECTION_REQUEST);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

        Utilities.logFirebaseEvent(TAG, Constant.EVENT_TYPE_ACTION, "FAB_btn_edit_activity_clicked");
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
                if(mSortByOption == 2) {
                    shouldScrollToBottom = true;
                }
                if(mSortByOption == 0 || mSortByOption == 1) {
                    shouldScrollToTop = true;
                }
                Toast.makeText(this, R.string.new_profile_insertion_msg, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        closeNavigationMenu();
        if (!isBackBtnPressedOnce) {
            Toast.makeText(this, R.string.press_back_to_exit, Toast.LENGTH_SHORT).show();
            isBackBtnPressedOnce = true;
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
        builder.setMessage("Are you sure you want to exit?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
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
        alertDialog = builder.show();
    }

    private NavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case (R.id.nav_tags):
                            closeNavigationMenu();
                            Intent intent = new Intent(getApplicationContext(), TagsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                            break;
                        case (R.id.nav_invite):
                            closeNavigationMenu();
//                            Intent messageIntent = new Intent(getApplicationContext(), MessagesActivity.class);
//                            messageIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(messageIntent);
                            Toast.makeText(ConnectionsActivity.this, "Invite friends", Toast.LENGTH_SHORT).show();
                            break;
                        case (R.id.nav_exit):
                            closeNavigationMenu();
                            showExitDialog();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            };
}
