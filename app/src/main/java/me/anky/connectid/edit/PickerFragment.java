package me.anky.connectid.edit;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;

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
                    Uri uri = data.getData();
                    cropImageIntent(uri);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = FileProvider.getUriForFile(getActivity(), AUTHORITY, output);
                    cropImageIntent(imageUri);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == Activity.RESULT_OK) {
                    Uri resultUri = result.getUri();
                    ((EditActivity) getActivity()).changePhoto(resultUri);
                    getDialog().dismiss();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Log.d(TAG, error.toString());
                }
        }
    }

    private void cropImageIntent(Uri uri){
        CropImage.activity(uri)
                .setAllowFlipping(false)
                .setAspectRatio(1,1)
                .start(getActivity(), this);
    }
}
