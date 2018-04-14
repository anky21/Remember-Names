package me.anky.connectid.selectedConnections;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.microsoft.appcenter.analytics.Analytics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.connections.ConnectionsRecyclerViewAdapter;
import me.anky.connectid.connections.DividerItemDecoration;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.details.DetailsActivity;
import me.anky.connectid.events.TagDeleted;
import me.anky.connectid.root.ConnectidApplication;

public class SelectedConnectionsActivity extends AppCompatActivity implements
        SelectedConnectionsActivityMVP.View,
        ConnectionsRecyclerViewAdapter.RecyclerViewClickListener {
    private final static String TAG = "SelectedConnectionsActivity";

    private int tagId;
    private String mTag;
    public List<ConnectidConnection> data = new ArrayList<>();
    private static final int DETAILS_ACTIVITY_REQUEST = 100;
    private MenuItem searchItem;
    private SearchView searchView;
    private AlertDialog alertDialog;

    ConnectionsRecyclerViewAdapter adapter;

    @BindView(R.id.selected_connections_rv)
    RecyclerView recyclerView;

    @BindView(R.id.empty_view)
    LinearLayout emptyView;

    @Inject
    SelectedConnectionsActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_connections);
        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        Intent intent = getIntent();
        tagId = intent.getIntExtra("tagId", -1);
        mTag = intent.getStringExtra("tag");
        this.setTitle(String.format(getString(R.string.selected_connections_activity_title), mTag));

        adapter = new ConnectionsRecyclerViewAdapter(this, data, false, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_selected_connections, menu);

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
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        if (searchView != null && !searchView.getQuery().equals("")) {
            searchView.setQuery("", false);
            searchView.setIconified(true);
        }
        super.onResume();
        presenter.setView(this);
        presenter.loadTag(tagId);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        presenter.unsubscribe();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setToUpdateTagTable(TagDeleted event) {
        finish();
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
    }

    @Override
    public void displayConnections(List<ConnectidConnection> connections) {
        data.addAll(connections);
//        Log.v("testing", "data size " + data.size());
        recyclerView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        adapter.setConnections(connections);
    }

    @Override
    public void displayNoConnections() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void displayError() {

    }

    @Override
    public void onItemClick(View view, int id) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("id", id);
        startActivityForResult(intent, DETAILS_ACTIVITY_REQUEST);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == DETAILS_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                presenter.loadTag(tagId);
            }
        }
    }

    @OnClick(R.id.btn_delete)
    public void tagDeleteBtnClicked(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_tag_msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.deleteTag(tagId, mTag, data);
                Analytics.trackEvent("Delete A Tag");
                Utilities.logFirebaseEvents("Delete a tag", mTag);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
}
