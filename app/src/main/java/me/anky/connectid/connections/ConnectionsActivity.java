package me.anky.connectid.connections;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import me.anky.connectid.root.ConnectidApplication;

public class ConnectionsActivity extends AppCompatActivity implements
        ConnectionsActivityView,
        ConnectionsRecyclerViewAdapter.ItemClickListener {

    @BindView(R.id.connections_list_rv)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.debug_list_tv)
    TextView debug_tv;

    ConnectionsActivityPresenter presenter;

    @Inject
    ConnectionsDataSource connectionsDataSource;

    ConnectionsRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        Stetho.initializeWithDefaults(this);

        adapter = new ConnectionsRecyclerViewAdapter(this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

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
    protected void onStart() {
        super.onStart();

        presenter = new ConnectionsActivityPresenter(
                this, connectionsDataSource, AndroidSchedulers.mainThread());

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

        StringBuilder builder = new StringBuilder();
        for (ConnectidConnection connection : connections) {
            builder.append(connection.getName());
            builder.append(" - ");
            builder.append(connection.getDescription());
            builder.append("\n");
        }
        debug_tv.setText(builder.toString());

        debug_tv.setVisibility(View.GONE);
        adapter.setConnections(connections);
    }

    @Override
    public void displayNoConnections() {
        Log.i("MVPTEST", "displayNoConnections called because list is empty");
    }

    @Override
    public void displayError() {
        Log.i("MVPTEST", "displayError called due to error");
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
