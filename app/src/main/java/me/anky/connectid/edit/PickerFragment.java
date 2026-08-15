package me.anky.connectid.edit;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;

import java.io.File;

import androidx.activity.result.ActivityResultLauncher;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickerFragment extends DialogFragment {
    private static final String TAG = "PICKER_FRAGMENT";
    private static final int PICK_PHOTO = 100;
    private static final int TAKE_PHOTO = 101;
    private Uri imageUri;
    private static final String EXTRA_FILENAME = "me.anky.connectid.EXTRA_FILENAME";
    private static final String EXTRA_ORIGINAL_URI = "me.anky.connectid.EXTRA_ORIGINAL_URI";
    private static final String FILENAME = "profile.jpeg";
    private static final String AUTHORITY = "me.anky.connectid.provider";
    private static final String PHOTOS = "photos";
    private File output = null;
    private Bundle mSavedInstanceState;
    private Uri mOriginalImageUri;

    private final ActivityResultLauncher<CropImageContractOptions> cropImage =
            registerForActivityResult(new CropImageContract(), result -> {
                if (result.isSuccessful() && result.getUriContent() != null
                        && mOriginalImageUri != null) {
                    deliverSelectedPhoto(mOriginalImageUri, result.getUriContent());
                } else if (result.getError() != null) {
                    Utilities.logFirebaseError("error_crop_image", TAG + ".cropImage");
                }
            });

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
        if (savedInstanceState != null) {
            String originalUri = savedInstanceState.getString(EXTRA_ORIGINAL_URI);
            if (originalUri != null) {
                mOriginalImageUri = Uri.parse(originalUri);
            }
        }
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
        if (mOriginalImageUri != null) {
            outState.putString(EXTRA_ORIGINAL_URI, mOriginalImageUri.toString());
        }
    }

    @OnClick(R.id.takeImage_tv)
    public void takeImage() {
        Utilities.logFirebaseEventWithNoParams("photo_camera_selected");

        if (mSavedInstanceState == null) {
            output = new File(new File(getActivity().getFilesDir(), PHOTOS), FILENAME);

            if (output.exists()) {
                output.delete();
            } else {
                output.getParentFile().mkdirs();
            }

            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
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
                Utilities.logFirebaseError("error_take_image", TAG + ".takeImage");
            }
        } else {
            output = (File) mSavedInstanceState.getSerializable(EXTRA_FILENAME);
        }
    }

    @OnClick(R.id.pickImage_tv)
    public void pickImage() {
        Utilities.logFirebaseEventWithNoParams("photo_library_selected");

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_PHOTO);
    }

    @OnClick(R.id.cancel_tv)
    public void closeDialog() {
        getDialog().dismiss();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PHOTO:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    launchCrop(data.getData());
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    imageUri = FileProvider.getUriForFile(getActivity(), AUTHORITY, output);
                    launchCrop(imageUri);
                }
                break;
        }
    }

    private void launchCrop(Uri imageUri) {
        if (imageUri == null) {
            return;
        }
        mOriginalImageUri = imageUri;
        CropImageOptions options = new CropImageOptions();
        options.fixAspectRatio = true;
        options.aspectRatioX = 4;
        options.aspectRatioY = 3;
        options.guidelines = CropImageView.Guidelines.ON;
        options.outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG;
        options.outputCompressQuality = 95;
        options.outputRequestWidth = 1600;
        options.outputRequestHeight = 1200;
        options.outputRequestSizeOptions = CropImageView.RequestSizeOptions.RESIZE_INSIDE;
        cropImage.launch(new CropImageContractOptions(imageUri, options));
    }

    private void deliverSelectedPhoto(Uri originalImageUri, Uri previewImageUri) {
        Activity activity = getActivity();
        if (!(activity instanceof EditActivity)) {
            return;
        }
        ((EditActivity) activity).changePhoto(originalImageUri, previewImageUri);
        dismissAllowingStateLoss();
    }
}
