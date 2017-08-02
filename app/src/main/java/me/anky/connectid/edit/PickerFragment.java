package me.anky.connectid.edit;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickerFragment extends android.app.DialogFragment {
    private static final String TAG = "PICKER_FRAGMENT";
    private static final int PICK_PHOTO = 100;
    private static final int TAKE_PHOTO = 101;
    private static final int CROP_PHOTO = 102;
    private Uri imageUri;
    private static final String EXTRA_FILENAME = "me.anky.connectid.EXTRA_FILENAME";
    private static final String FILENAME = "profile.jpeg";
    private static final String AUTHORITY = "me.anky.connectid.provider";
    private static final String PHOTOS = "photos";
    private File output = null;
    private Bundle mSavedInstanceState;

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
        mSavedInstanceState = savedInstanceState;
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        ButterKnife.bind(this, view);

        output = new File(new File(getActivity().getFilesDir(), PHOTOS), FILENAME);

        if (output.exists()) {
            output.delete();
        } else {
            output.getParentFile().mkdirs();
        }

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(EXTRA_FILENAME, output);
    }

    @OnClick(R.id.takeImage_tv)
    public void takeImage() {

        if (mSavedInstanceState == null) {
            output = new File(new File(getActivity().getFilesDir(), PHOTOS), FILENAME);

            if (output.exists()) {
                output.delete();
            } else {
                output.getParentFile().mkdirs();
            }

            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String x = AUTHORITY;
            Uri outputUri = FileProvider.getUriForFile(getActivity(), AUTHORITY, output);

            i.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                ClipData clip =
                        ClipData.newUri(getActivity().getContentResolver(), "A photo", outputUri);

                i.setClipData(clip);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            try {
                startActivityForResult(i, TAKE_PHOTO);
            } catch (ActivityNotFoundException e) {

            }
        } else {
            output = (File) mSavedInstanceState.getSerializable(EXTRA_FILENAME);
        }
    }

    @OnClick(R.id.pickImage_tv)
    public void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra("crop", "true");
        photoPickerIntent.putExtra("outputX", 800);
        photoPickerIntent.putExtra("outputY", 800);
        photoPickerIntent.putExtra("aspectX", 1);
        photoPickerIntent.putExtra("aspectY", 1);
        photoPickerIntent.putExtra("scale", true);

        startActivityForResult(photoPickerIntent, PICK_PHOTO);
    }

    @OnClick(R.id.cancel_tv)
    public void closeDialog() {
        // Remove the fragment
        getActivity().getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    ((EditActivity) getActivity()).changePhoto(imageBitmap);
                    getDialog().dismiss();
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Intent i = new Intent("com.android.camera.action.CROP");
                    Uri outputUri = FileProvider.getUriForFile(getActivity(), AUTHORITY, output);

                    i.setDataAndType(outputUri, "image/jpeg");
                    i.putExtra("aspectX", 1);
                    i.putExtra("aspectY", 1);
                    //indicate output X and Y
                    i.putExtra("outputX", 800);
                    i.putExtra("outputY", 800);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        startActivityForResult(i, CROP_PHOTO);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getActivity(), "error", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
//                    Bundle extras = data.getExtras();
//                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    Uri selectedImage = data.getData();
                    ((EditActivity)getActivity()).changePhoto(selectedImage);
                    getDialog().dismiss();
                }
        }
    }

    private void performCrop() {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(imageUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 800);
            cropIntent.putExtra("outputY", 800);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, CROP_PHOTO);
        } catch (ActivityNotFoundException e) {
            //display an error message
            String errorMessage = getString(R.string.crop_action_error_msg);
            Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
