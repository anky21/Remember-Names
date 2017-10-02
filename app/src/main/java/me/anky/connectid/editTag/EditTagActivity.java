package me.anky.connectid.editTag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;

public class EditTagActivity extends AppCompatActivity implements EditTagActivityMVP.View {

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
    }

    @Override
    public void displayAllTags(List<ConnectionTag> allTags) {

    }

    @Override
    public void displayNoTags() {

    }

    @Override
    public void displayError() {

    }
}
