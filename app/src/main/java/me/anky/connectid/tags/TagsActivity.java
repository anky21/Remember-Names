package me.anky.connectid.tags;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import me.anky.connectid.R;
import me.anky.connectid.data.ConnectionTag;

public class TagsActivity extends AppCompatActivity implements TagsActivityMVP.View{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
    }

    @Override
    public void displayTags(List<ConnectionTag> allTags) {

    }

    @Override
    public void displayNoTags() {

    }
}
