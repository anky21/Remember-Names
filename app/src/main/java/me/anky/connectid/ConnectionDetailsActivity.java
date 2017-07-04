package me.anky.connectid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class ConnectionDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_details);

        Intent intent = getIntent();
        int databaseId = intent.getIntExtra("ID", 0);
        String details = intent.getStringExtra("DETAILS");

        TextView connectionDetailsTv = (TextView) findViewById(R.id.connection_details_tv);
        connectionDetailsTv.setText("Database item id: " + databaseId + "\n" + details);
    }
}
