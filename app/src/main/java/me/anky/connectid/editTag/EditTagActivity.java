package me.anky.connectid.editTag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;

public class EditTagActivity extends AppCompatActivity implements EditTagActivityMVP.View {

    public List<ConnectionTag> allTags = new ArrayList<>();

    @BindView(R.id.all_tags)
    LinearLayout allTagsLinear;


    @Inject
    EditTagActivityPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

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
    public void displayAllTags(List<ConnectionTag> allTags) {
        for (ConnectionTag tag:allTags){
            Log.v("testing", "tag is " + tag.getTag());
            TextView tagTv = new TextView(this);
            tagTv.setText(tag.getTag());
            tagTv.setTextSize(14);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8,2,8,2);
            tagTv.setLayoutParams(params);
            tagTv.setBackgroundResource(R.drawable.round_bg_blue);
            allTagsLinear.addView(tagTv);
        }
    }

    @Override
    public void displayNoTags() {
    }

    @Override
    public void displayError() {

    }
}
