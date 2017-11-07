package me.anky.connectid.selectedConnections;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

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
    private int tagId;
    public List<ConnectidConnection> data = new ArrayList<>();
    private static final int DETAILS_ACTIVITY_REQUEST = 100;

    ConnectionsRecyclerViewAdapter adapter;

    @BindView(R.id.selected_connections_rv)
    RecyclerView recyclerView;

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

        adapter = new ConnectionsRecyclerViewAdapter(this, data, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    protected void onResume() {
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
    public void displayConnections(List<ConnectidConnection> connections) {
        recyclerView.setVisibility(View.VISIBLE);

        adapter.setConnections(connections);
    }

    @Override
    public void displayNoConnections() {
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void displayError() {

    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("DETAILS", data.get(position));
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
