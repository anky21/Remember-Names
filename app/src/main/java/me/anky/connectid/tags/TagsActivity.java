package me.anky.connectid.tags;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.root.ConnectidApplication;

public class TagsActivity extends AppCompatActivity implements TagsActivityMVP.View{

    @BindView(R.id.all_tags_recyclerview)
    RecyclerView recyclerView;

    @Inject
    TagsActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
        presenter.loadTags();
    }

    @Override
    public void displayTags(List<ConnectionTag> allTags) {
        Toast.makeText(this, "all tags", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayNoTags() {
        Toast.makeText(this, "no tags", Toast.LENGTH_SHORT).show();
    }
}
