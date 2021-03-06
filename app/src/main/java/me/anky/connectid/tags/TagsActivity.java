package me.anky.connectid.tags;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import me.anky.connectid.R;
import me.anky.connectid.connections.DividerItemDecoration;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.root.ConnectidApplication;
import me.anky.connectid.selectedConnections.SelectedConnectionsActivity;

public class TagsActivity extends AppCompatActivity implements TagsActivityMVP.View,
        TagsRecyclerViewAdapter.RecyclerViewClickListener {
    private final static String TAG = "TagsActivity";

    public List<ConnectionTag> data = new ArrayList<>();

    @BindView(R.id.all_tags_recyclerview)
    RecyclerView recyclerView;

    @BindView(R.id.empty_view)
    LinearLayout emptyView;

    @BindView(R.id.adView)
    AdView mAdView;

    TagsRecyclerViewAdapter adapter;

    @Inject
    TagsActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        ButterKnife.bind(this);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

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
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
        presenter.loadTags();
    }

    @Override
    public void displayTags(List<ConnectionTag> allTags) {
        emptyView.setVisibility(View.GONE);
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
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onItemClick(View view, int position) {
        ConnectionTag connectionTag = data.get(position);

        Intent selectedConnectionsIntent = new Intent(TagsActivity.this, SelectedConnectionsActivity.class);
        selectedConnectionsIntent.putExtra("tagId", connectionTag.getDatabaseId());
        selectedConnectionsIntent.putExtra("tag", connectionTag.getTag());
        startActivity(selectedConnectionsIntent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }
}
