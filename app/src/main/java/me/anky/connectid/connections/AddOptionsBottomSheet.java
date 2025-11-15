package me.anky.connectid.connections;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import me.anky.connectid.R;
import me.anky.connectid.edit.EditActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.ContactsContract;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AddOptionsBottomSheet extends BottomSheetDialogFragment {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_CONTACT_REQUEST = 101;

    public static AddOptionsBottomSheet newInstance() {
        return new AddOptionsBottomSheet();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_options, container, false);

        TextView addNewProfile = view.findViewById(R.id.tv_add_new_profile);
        TextView importContacts = view.findViewById(R.id.tv_import_contacts);

        addNewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditActivity.class);
            startActivityForResult(intent, 200);
            dismiss();
        });

        importContacts.setOnClickListener(v -> {
            if (checkContactPermission()) {
                pickContact();
            } else {
                requestContactPermission();
            }
            dismiss();
        });

        return view;
    }

    private boolean checkContactPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestContactPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.READ_CONTACTS},
                PERMISSION_REQUEST_CODE);
    }

    private void pickContact() {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK);
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        requireActivity().startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }
} 