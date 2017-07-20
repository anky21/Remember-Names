package me.anky.connectid.details;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Single;
import me.anky.connectid.R;
import me.anky.connectid.root.ConnectidApplication;

public class DetailsActivity extends AppCompatActivity implements DetailsActivityMVP.View {
    private final static String TAG = DetailsActivity.class.getSimpleName();
    private String mImageName = "profile.jpg";

    @BindView(R.id.portrait_iv)
    ImageView mPortraitIv;

    @BindView(R.id.new_portrait_iv)
    ImageView mNewPortraitIv;

    @Inject
    DetailsActivityPresenter presenter;

    int databaseId;
    String details;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        Intent intent = getIntent();
        databaseId = intent.getIntExtra("ID", 0);
        details = intent.getStringExtra("DETAILS");

        TextView connectionDetailsTv = (TextView) findViewById(R.id.connection_details_tv);
        connectionDetailsTv.setText("Database item id: " + databaseId + "\n" + details);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    @OnClick(R.id.portrait_iv)
    public void changePortraitPhoto() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.addToBackStack(null);
        PickerFragment newFragment = new PickerFragment();
        newFragment.show(fm, "dialog");
        ft.commit();
    }

    public void changePhoto(Object object) {
        Bitmap bitmap;
        try {
            if (object instanceof Uri) {
                Uri imageUri = (Uri) object;
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(imageStream);
                int originalWidth = bitmap.getWidth();
                int originalHeight = bitmap.getHeight();

                final int desiredSize = 1000;
                int maximumSize = Math.max(originalHeight, originalWidth);

                if (maximumSize > desiredSize) {
                    float ratio = (float) desiredSize / maximumSize;
                    int newWidth = Math.round(originalWidth * ratio);
                    int newHeight = Math.round(originalHeight * ratio);

                    bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                }

                // Save bitmap internally and return its path
                String internalPath = saveToInternalStorage(bitmap);

                mNewPortraitIv.setImageBitmap(loadImageFromStorage(mImageName, internalPath));
                mPortraitIv.setImageBitmap(bitmap);

            } else {
                bitmap = (Bitmap) object;
                // Save bitmap internally and return its path
                String internalPath = saveToInternalStorage(bitmap);

                mNewPortraitIv.setImageBitmap(loadImageFromStorage(mImageName, internalPath));
                mPortraitIv.setImageBitmap(bitmap);
                mPortraitIv.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            Log.e(TAG, "error in changing photo");
        }
    }

    // Save Bitmap to internal storage
    private String saveToInternalStorage(Bitmap bitmapImage) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File file = new File(directory, mImageName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return  directory.getAbsolutePath();
    }

    // Load image from internal storage
    private Bitmap loadImageFromStorage(String imageName, String path)
    {
        Bitmap bitmap = null;
        try {
            File f=new File(path, imageName);
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return bitmap;
    }

    public void handleDeleteConnectionClicked(View view) {
        presenter.deliverDatabaseIdtoDelete();
    }

    @Override
    public Single<Integer> getConnectionToDelete() {

        Log.i("MVP view", "getConnectionToDelete returning " + databaseId);

        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {

                System.out.println("Thread db: " + Thread.currentThread().getId());

                return databaseId;
            }
        });
    }

    @Override
    public void displayError() {
        Log.i("MVP view", "delete failed");
    }

    @Override
    public void displaySuccess() {

        Log.i("MVP view", "delete succeeded");

        Toast.makeText(this, details + " deleted!", Toast.LENGTH_SHORT).show();

        Intent data = new Intent();
        setResult(RESULT_OK, data);
        finish();
    }
}
