package me.anky.connectid.selectedConnections;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.anky.connectid.R;
import me.anky.connectid.connections.ConnectionsRecyclerViewAdapter;
import me.anky.connectid.connections.DividerItemDecoration;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.details.DetailsActivity;
import me.anky.connectid.root.ConnectidApplication;

public class SelectedConnectionsActivity extends AppCompatActivity implements
        SelectedConnectionsActivityMVP.View,
        ConnectionsRecyclerViewAdapter.RecyclerViewClickListener {
    private final static String TAG = "SelectedConnectionsActivity";

    private int tagId;
    public List<ConnectidConnection> data = new ArrayList<>();
    private static final int DETAILS_ACTIVITY_REQUEST = 100;
    private MenuItem searchItem;
    private SearchView searchView;

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
        String tag = intent.getStringExtra("tag");
        this.setTitle(String.format(getString(R.string.selected_connections_activity_title), tag));

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
    protected void onResume() {
        if (searchView != null) {
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

    @Override
    public void displayConnections(List<ConnectidConnection> connections) {
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
}
