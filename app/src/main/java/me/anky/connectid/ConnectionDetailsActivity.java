package me.anky.connectid;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConnectionDetailsActivity extends AppCompatActivity {
    private final static String TAG = ConnectionDetailsActivity.class.getSimpleName();

    @BindView(R.id.portrait_iv)
    ImageView mPortraitIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        int databaseId = intent.getIntExtra("ID", 0);
        String details = intent.getStringExtra("DETAILS");

        TextView connectionDetailsTv = (TextView) findViewById(R.id.connection_details_tv);
        connectionDetailsTv.setText("Database item id: " + databaseId + "\n" + details);
    }


    @OnClick(R.id.portrait_iv)
    public void changePortraitPhoto(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(null);
        PickerFragment newFragment = new PickerFragment();
        newFragment.show(fm, "dialog");
        ft.commit();
    }

    public void changePhoto (Object object){
        Bitmap bitmap;
        try {
            if (object instanceof Uri) {
                Uri imageUri = (Uri) object;
                Picasso.with(this).load(imageUri).into(mPortraitIv);
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(imageStream);
            } else {
                bitmap = (Bitmap)object;
                mPortraitIv.setImageBitmap(bitmap);
            }
        } catch (Exception e){
            Log.e(TAG, "error in changing photo");
        }
    }
}
