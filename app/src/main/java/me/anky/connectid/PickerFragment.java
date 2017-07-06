package me.anky.connectid;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class PickerFragment extends android.app.DialogFragment {


    public PickerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picker, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.takeImage_tv)
    public void takeImage(){
        Toast.makeText(getActivity(), "take a photo", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.pickImage_tv)
    public void pickImage(){
        Toast.makeText(getActivity(), "pick a photo", Toast.LENGTH_SHORT).show();
    }
}
