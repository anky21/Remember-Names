package me.anky.connectid.connections;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.stetho.Stetho;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;
import me.anky.connectid.details.DetailsActivity;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;

public class ConnectionsActivity extends AppCompatActivity implements
        ConnectionsActivityView,
        ConnectionsRecyclerViewAdapter.RecyclerViewClickListener {

    @BindView(R.id.connections_list_rv)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.empty_list_tv)
    TextView emptyListTv;

    ConnectionsActivityPresenter presenter;

    @Inject
    ConnectionsDataSource connectionsDataSource;

    ConnectionsRecyclerViewAdapter adapter;

    private static final int EDIT_ACTIVITY_REQUEST = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        Stetho.initializeWithDefaults(this);

        adapter = new ConnectionsRecyclerViewAdapter(this, this);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        setScrollListener(recyclerView);
    }

    @Override
    protected void onStart() {
        super.onStart();

        presenter = new ConnectionsActivityPresenter(
                this, connectionsDataSource, AndroidSchedulers.mainThread());
    }

    @Override
    protected void onResume() {
        super.onResume();

        presenter.loadConnections();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    @Override
    public void displayConnections(List<ConnectidConnection> connections) {
        Log.i("MVP view", "displayConnections received " + connections.size() + " connections");
        emptyListTv.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter.setConnections(connections);
    }

    private void setScrollListener(RecyclerView recyclerView) {
        // Automatically hide/show the FAB when recycler view scrolls up/down
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                if(dy > 0 || dy < 0 && fab.isShown()){
                    fab.hide();
                }
                super.onScrolled(recyclerView, dx, dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    public void displayNoConnections() {
        Log.i("MVP view", "displayNoConnections received empty list");

        // TODO Make pretty strings sometime.
        emptyListTv.setText("Need to remember someone?\nClick the button to get started!");
        recyclerView.setVisibility(View.INVISIBLE);
        emptyListTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayError() {
        Log.i("MVP view", "displayError called due to error");

        // TODO Make pretty strings sometime.
        emptyListTv.setText("Fatal error.\nProbably a database failure.");
        recyclerView.setVisibility(View.INVISIBLE);
        emptyListTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.i("MVP view", "position " + position + " clicked");

        TextView clickedItemTv = (TextView) ((ViewGroup) view).getChildAt(0);

        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("ID", (Integer) clickedItemTv.getTag());
        intent.putExtra("DETAILS", clickedItemTv.getText());
        startActivity(intent);
    }

    public void launchEditActivity(View view) {

        Intent intent = new Intent(this, EditActivity.class);
        startActivityForResult(intent, EDIT_ACTIVITY_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_ACTIVITY_REQUEST) {
            if (resultCode == RESULT_OK) {

                Log.i("MVP view", "recyclerview is automatically refreshed upon insertion");
                // TODO Inform user of insertion success, perhaps with toast
            }
        }
    }
}
