package me.anky.connectid.tags;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.anky.connectid.R;
import me.anky.connectid.connections.DividerItemDecoration;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.root.ConnectidApplication;
import me.anky.connectid.selectedConnections.SelectedConnectionsActivity;

public class TagsActivity extends AppCompatActivity implements TagsActivityMVP.View,
TagsRecyclerViewAdapter.RecyclerViewClickListener{

    public List<ConnectionTag> data = new ArrayList<>();


    @BindView(R.id.all_tags_recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.empty_tags_tv)
    TextView emptyTv;

    TagsRecyclerViewAdapter adapter;

    @Inject
    TagsActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        adapter = new TagsRecyclerViewAdapter(this, data, this);
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
        presenter.loadTags();
    }

    @Override
    public void displayTags(List<ConnectionTag> allTags) {
        emptyTv.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);

        adapter.setTags(allTags);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    @Override
    public void displayNoTags() {
        emptyTv.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemClick(View view, int position) {
        ConnectionTag connectionTag = data.get(position);
        String ids = connectionTag.getConnection_ids();
        if (ids == null || ids.length() == 0) {
            Toast.makeText(this, "There is no connections for this tag.", Toast.LENGTH_SHORT).show();
        } else {
            Intent selectedConnectionsIntent = new Intent(TagsActivity.this, SelectedConnectionsActivity.class);
            selectedConnectionsIntent.putExtra("ids", ids);
            startActivity(selectedConnectionsIntent);
        }
    }
}
