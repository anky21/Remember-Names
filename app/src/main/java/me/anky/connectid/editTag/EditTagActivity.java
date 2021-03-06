package me.anky.connectid.editTag;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectionTag;
import me.anky.connectid.root.ConnectidApplication;

public class EditTagActivity extends AppCompatActivity implements EditTagActivityMVP.View, View.OnKeyListener {
    private final static String TAG = "EditTagActivity";
//    public static final int DEFAULT_MSG_LENGTH_LIMIT = 30;

    public List<ConnectionTag> allTags = new ArrayList<>();
    List<String> connectionTags = new ArrayList<>();
    String oldTags;
    private boolean hasSameTag;

    boolean allTagsLayoutReady;

    final static int TAG_BASE_NUMBER = 1000;
    final static int TAG_Base_NUMBER2 = 3000;

    int mDatabaseId = -1;

    @BindView(R.id.all_tags)
    RelativeLayout allTagsLayout;

    @BindView(R.id.search_tags_listview)
    ListView searchTagsLv;

    @BindView(R.id.add_tag_et)
    EditText addTagEt;

    @BindView(R.id.selected_tags)
    RelativeLayout selectedTagRl;

    String input = "";

    @Inject
    EditTagActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tag);

        ButterKnife.bind(this);

        ((ConnectidApplication) getApplication()).getApplicationComponent().inject(this);

        // recovering the instance state
        if (savedInstanceState != null) {
            allTagsLayoutReady = savedInstanceState.getBoolean("AllTagsLayoutReadiness");
        }

        Intent intent = getIntent();
        if (intent.hasExtra("data_id")) {
            mDatabaseId = intent.getIntExtra("data_id", -1);
        }
        oldTags = intent.getStringExtra("tags");
        if (oldTags != null) {
            String[] oldTagsArray = oldTags.split(", ");
            connectionTags = new ArrayList(Arrays.asList(oldTagsArray));
        }

        ViewTreeObserver vto = selectedTagRl.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = selectedTagRl.getMeasuredWidth();
                    // handle viewWidth here...
                    displayConnectionTags();

                    if (viewWidth > 0) {
                        selectedTagRl.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
//        addTagEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
        setMaxWidthForEditText(); // Set maximum width for EditText

        addTagEt.setOnKeyListener(this);
        addTagEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (before == 0 && count == 1 && charSequence.charAt(start) == ',') {

                    addTagEt.getText().replace(start, start + 1, "");
                    input = addTagEt.getText().toString().trim();
                    presenter.createNewTag(input, connectionTags, allTags);
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
                    presenter.createNewTag(input, connectionTags, allTags);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("AllTagsLayoutReadiness", allTagsLayoutReady);

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.setView(this);
        presenter.loadTags();
    }

    @Override
    protected void onStop() {
        super.onStop();
        presenter.unsubscribe();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String connectionTagsString;
        if (connectionTags.size() == 0) {
            connectionTagsString = null;
        } else {
            connectionTagsString = connectionTags.toString();
        }

        switch (item.getItemId()) {
            case R.id.action_save:
                Intent data = new Intent();
                data.putExtra("connectionTags", connectionTagsString);
                setResult(RESULT_OK, data);

                if (mDatabaseId != -1) {
                    presenter.updateConnectionTags(mDatabaseId, connectionTags);
                    presenter.updateTagTable(oldTags, allTags, connectionTags, mDatabaseId);
                } else {
                    presenter.insertBulkNewTags(connectionTags, allTags);
                }

                finish();
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void displayAllTags(final List<ConnectionTag> allTags) {

        if (allTagsLayoutReady) {
            displayAllTagsMethod(allTagsLayout, searchTagsLv, allTags, connectionTags);
        } else {

            ViewTreeObserver vto = allTagsLayout.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int viewWidth = selectedTagRl.getMeasuredWidth();
                        displayAllTagsMethod(allTagsLayout, searchTagsLv, allTags, connectionTags);
                        allTagsLayoutReady = true;
                        // handle viewWidth here...
                        if (viewWidth > 0) {
                            allTagsLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }
    }

    private void setMaxWidthForEditText() {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setText("WWWWWWWWWWWWWWWWWW"); // 18 Ws
        textView.setTextSize(14);
        textView.setBackgroundResource(R.drawable.round_bg_blue);
        textView.setLayoutParams(params);
        textView.measure(0, 0);
        int maxWidth = textView.getMeasuredWidth();

        if (maxWidth != 0) {
            addTagEt.setMaxWidth(maxWidth);
        }
    }

    private void displayAllTagsMethod(RelativeLayout allTagsLayout, ListView searchTagsLv, final List<ConnectionTag> allTags, final List<String> connectionTags) {
        // Clear all views
        allTagsLayout.removeAllViews();
        int containerWidth = allTagsLayout.getMeasuredWidth() - 16;
        int i = 0;

        int count = 0;
        int currentWidth = 0;
        boolean isNewLine;
        boolean isFirstLine = true;

        this.allTags = allTags;

        searchTagsLv.setVisibility(View.GONE);
        for (ConnectionTag tag : allTags) {
            boolean isSelectedTag = false;

            if (connectionTags != null && connectionTags.size() > 0) {
                for (String selectedTag : connectionTags) {
                    if (tag.getTag().equalsIgnoreCase(selectedTag)) {
                        isSelectedTag = true;
                        break;
                    }
                }
            }

            TextView tagTv = new TextView(this);
            tagTv.setId(TAG_Base_NUMBER2 + i);
            tagTv.setText(tag.getTag());
            tagTv.setTextSize(14);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 4, 8, 4);
            tagTv.setEllipsize(TextUtils.TruncateAt.END);
            tagTv.setMaxLines(1);
            tagTv.setLayoutParams(params);

            if (isSelectedTag) {
                tagTv.setTextColor(ContextCompat.getColor(EditTagActivity.this, R.color.colorAccent));
                tagTv.setBackgroundResource(R.drawable.round_bg_blue);
            } else {
                tagTv.setBackgroundResource(R.drawable.round_bg_gray);
            }

            final String tagString = tag.getTag();
            tagTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (connectionTags.contains(tagString)) {
                        connectionTags.remove(tagString);
                    } else {
                        connectionTags.add(tagString);
                    }
                    displayConnectionTags();
                    displayAllTags(allTags);
                }
            });

            tagTv.measure(0, 0);

            int width = tagTv.getMeasuredWidth();

            if (currentWidth + width < containerWidth) {
                currentWidth += width + 16;
                isNewLine = false;
                count++;
            } else {
                currentWidth = width + 16;
                isNewLine = true;
                isFirstLine = false;
                count = 1;
            }

            // Add TextView to the screen
            if (i == 0) {
                params.addRule(RelativeLayout.ALIGN_START);
                tagTv.setLayoutParams(params);
                allTagsLayout.addView(tagTv);
            } else if (isNewLine) {
                params.addRule(RelativeLayout.ALIGN_LEFT);
                params.addRule(RelativeLayout.BELOW, TAG_Base_NUMBER2 - 1 + i);
                tagTv.setLayoutParams(params);
                allTagsLayout.addView(tagTv);
            } else if (isFirstLine) {
                params.addRule(RelativeLayout.RIGHT_OF, TAG_Base_NUMBER2 - 1 + i);
                tagTv.setLayoutParams(params);
                allTagsLayout.addView(tagTv);
            } else {
                params.addRule(RelativeLayout.RIGHT_OF, TAG_Base_NUMBER2 - 1 + i);
                params.addRule(RelativeLayout.BELOW, TAG_Base_NUMBER2 - count + i);
                tagTv.setLayoutParams(params);
                allTagsLayout.addView(tagTv);
            }

            i++;
        }
    }

    @Override
    public void displayNoTags() {
    }

    @Override
    public void displayError() {

    }

    @Override
    public void displayConnectionTags() {
        selectedTagRl.removeAllViews();
        int containerWidth = selectedTagRl.getMeasuredWidth() - 16;
        int i = 0;

        int count = 0;
        int currentWidth = 0;
        boolean isNewLine;
        boolean isFirstLine = true;

        if (connectionTags.size() > 0) {
            for (final String tag : connectionTags) {
                // Check if all Tags has the same tag
                if (!hasSameTag) {
                    if (allTags != null && allTags.size() > 0) {
                        for (ConnectionTag allTagsItem : allTags) {
                            if (allTagsItem.getTag().equals(tag)) {
                                // Refresh allTags
                                hasSameTag = true;
                                break;
                            }
                        }
                    }
                }

                TextView tagTv = new TextView(this);
                tagTv.setId(TAG_BASE_NUMBER + i);
                tagTv.setText(tag);
                tagTv.setTextSize(14);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(8, 4, 8, 4);
                tagTv.setLayoutParams(params);
                tagTv.setBackgroundResource(R.drawable.round_bg_blue);
                tagTv.setTextColor(ContextCompat.getColor(EditTagActivity.this, R.color.colorAccent));
                tagTv.setMaxLines(1);
                tagTv.setEllipsize(TextUtils.TruncateAt.END);
                tagTv.measure(0, 0);

                tagTv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedTagRl.removeView(view);
                        connectionTags.remove(tag);

                        if (allTags != null && allTags.size() > 0) {
                            for (ConnectionTag allTagsItem : allTags) {
                                if (allTagsItem.getTag().equalsIgnoreCase(tag)) {
                                    // Refresh allTags
                                    displayAllTags(allTags);
                                }
                            }
                        }
                        // Refresh the screen
                        displayConnectionTags();
                    }
                });

                int width = tagTv.getMeasuredWidth();

                if (currentWidth + width <= containerWidth) {
                    currentWidth += width + 16;
                    isNewLine = false;
                    count++;
                } else {
                    currentWidth = width + 16;
                    isNewLine = true;
                    isFirstLine = false;
                    count = 1;
                }

                // Add TextView to the screen
                if (i == 0) {
                    params.addRule(RelativeLayout.ALIGN_START);
                    tagTv.setLayoutParams(params);
                    selectedTagRl.addView(tagTv);
                } else if (isNewLine) {
                    params.addRule(RelativeLayout.ALIGN_LEFT);
                    params.addRule(RelativeLayout.BELOW, TAG_BASE_NUMBER - 1 + i);
                    tagTv.setLayoutParams(params);
                    selectedTagRl.addView(tagTv);
                } else if (isFirstLine) {
                    params.addRule(RelativeLayout.RIGHT_OF, TAG_BASE_NUMBER - 1 + i);
                    tagTv.setLayoutParams(params);
                    selectedTagRl.addView(tagTv);
                } else {
                    params.addRule(RelativeLayout.RIGHT_OF, TAG_BASE_NUMBER - 1 + i);
                    params.addRule(RelativeLayout.BELOW, TAG_BASE_NUMBER - count + i);
                    tagTv.setLayoutParams(params);
                    selectedTagRl.addView(tagTv);
                }

                i++;
            }
        }
        // Refresh allTags if the new Tag is in All Tags
        if (hasSameTag) {
            displayAllTags(allTags);
            hasSameTag = false;
        }

    }

    @Override
    public boolean onKey(View view, int i, KeyEvent event) {
        if (i == KeyEvent.KEYCODE_COMMA || i == KeyEvent.KEYCODE_ENTER) {
//            Log.v("testing", "pressed key is " + i);
            input = addTagEt.getText().toString().trim();
            presenter.createNewTag(input, connectionTags, allTags);

            addTagEt.setText("");
            addTagEt.setFocusableInTouchMode(true);
            addTagEt.requestFocus();
            return true;
        }

        return false;
    }
}
