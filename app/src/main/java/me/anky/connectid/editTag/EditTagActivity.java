package me.anky.connectid.editTag;

import android.app.Service;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.edit.EditActivity;
import me.anky.connectid.root.ConnectidApplication;

public class EditTagActivity extends AppCompatActivity implements EditTagActivityMVP.View, View.OnKeyListener {

    public List<ConnectionTag> allTags = new ArrayList<>();

    @BindView(R.id.all_tags)
    LinearLayout allTagsLinear;

    @BindView(R.id.search_tags_listview)
    ListView searchTagsLv;

    @BindView(R.id.add_tag_et)
    EditText addTagEt;

    String input = "";


    @Inject
    EditTagActivityPresenter presenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        addTagEt.setOnKeyListener(this);
        addTagEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Log.v("testing", charSequence + "");
                if (before == 0 && count == 1 && charSequence.charAt(start) == ',') {

                    Log.v("testing", "hit comma");
                    addTagEt.getText().replace(start, start + 1, "");
                    input = addTagEt.getText().toString().trim();
                    if (!input.equals("")){
                        Toast.makeText(EditTagActivity.this, input, Toast.LENGTH_SHORT).show();

                    }
                    addTagEt.getText().clear();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String input = editable.toString().trim();
                if (input.contains(",")) {
                    addTagEt.getText().clear();
                    String modifiedInput = input.split(",")[0].trim();
                    input = modifiedInput;
                    if (!input.equals("")){
                        Toast.makeText(EditTagActivity.this, input, Toast.LENGTH_SHORT).show();

                    }
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
        presenter.loadTags();
    }

    @Override
    public void displayAllTags(List<ConnectionTag> allTags) {
        searchTagsLv.setVisibility(View.GONE);
        for (ConnectionTag tag : allTags) {
            Log.v("testing", "tag is " + tag.getTag());
            TextView tagTv = new TextView(this);
            tagTv.setText(tag.getTag());
            tagTv.setTextSize(14);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 2, 8, 2);
            tagTv.setLayoutParams(params);
            tagTv.setBackgroundResource(R.drawable.round_bg_gray);
            allTagsLinear.addView(tagTv);
        }
    }

    @Override
    public void displayNoTags() {
    }

    @Override
    public void displayError() {

    }

    @Override
    public boolean onKey(View view, int i, KeyEvent event) {
        if (i == KeyEvent.KEYCODE_COMMA || i == KeyEvent.KEYCODE_ENTER) {
            Log.v("testing", "pressed key is " + i);
            input = addTagEt.getText().toString().trim();
            if (!input.equals("")){
                Toast.makeText(EditTagActivity.this, input, Toast.LENGTH_SHORT).show();
            }
            addTagEt.setText("");
            addTagEt.setFocusableInTouchMode(true);
            addTagEt.requestFocus();
            return true;
        }

        return false;
    }
}
