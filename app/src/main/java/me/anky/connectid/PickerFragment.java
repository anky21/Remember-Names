package me.anky.connectid;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickerFragment extends android.app.DialogFragment {
    private static final String TAG = "PICKER_FRAGMENT";
    private static final int PICK_PHOTO = 100;
    private static final int TAKE_PHOTO = 101;

    @BindView(R.id.pickImage_tv)
    TextView mPickImageTv;

    @BindView(R.id.takeImage_tv)
    TextView mTakeImageTv;

    public PickerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        ButterKnife.bind(this, view);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @OnClick(R.id.takeImage_tv)
    public void takeImage(){
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(cameraIntent, TAKE_PHOTO);
        }
    }

    @OnClick(R.id.pickImage_tv)
    public void pickImage(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Log.v(TAG, "Picked a photo.");
                    Uri selectedImage = data.getData();
                    Log.v(TAG, "uri is " + selectedImage.toString());
                    ((ConnectionDetailsActivity)getActivity()).changePhoto(selectedImage);
                    getDialog().dismiss();
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Log.v(TAG, "Took a photo.");
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ((ConnectionDetailsActivity)getActivity()).changePhoto(imageBitmap);
                    getDialog().dismiss();
                }
        }
    }
}
