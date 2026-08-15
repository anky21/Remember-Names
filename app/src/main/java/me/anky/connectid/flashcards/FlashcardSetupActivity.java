package me.anky.connectid.flashcards;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;
import me.anky.connectid.root.ConnectidApplication;

public class FlashcardSetupActivity extends AppCompatActivity {
    private static final String PREF_SELECTED_DECK = "flashcards_selected_deck";
    private static final String PREF_SESSION_SIZE = "flashcards_session_size";
    private static final int[] SESSION_SIZES = {5, 10, 20, 30};
    private static final Integer[] DAILY_GOALS = {5, 10, 15, 20};

    @Inject
    ConnectionsDataSource connectionsDataSource;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private final ArrayList<ConnectidConnection> eligibleConnections = new ArrayList<>();
    private final ArrayList<String> deckTags = new ArrayList<>();
    private SharedPreferences preferences;
    private FlashcardProgressStore progressStore;

    private View content;
    private ProgressBar loading;
    private ProgressBar dailyProgress;
    private ProgressBar masteryProgress;
    private TextView dailyProgressText;
    private TextView streakText;
    private TextView dueText;
    private TextView masteredText;
    private TextView accuracyText;
    private TextView breakdownText;
    private TextView deckSummaryText;
    private Spinner deckSpinner;
    private Spinner sessionSizeSpinner;
    private Spinner dailyGoalSpinner;
    private SwitchCompat reminderSwitch;
    private Button reminderTimeButton;
    private Button startButton;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    reminderSwitch.setChecked(false);
                    saveReminder(false);
                    Toast.makeText(this, R.string.flashcards_reminder_permission_error,
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectidApplication.getAppInstance().getApplicationComponent().inject(this);
        setContentView(R.layout.activity_flashcard_setup);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        preferences = getSharedPreferences("shared-prefs", Context.MODE_PRIVATE);
        progressStore = new FlashcardProgressStore(preferences);
        bindViews();
        configureGoalSelector();
        configureSessionSizeSelector();
        configureReminder();
        startButton.setOnClickListener(v -> startSession());
    }

    private void bindViews() {
        content = findViewById(R.id.flashcard_setup_content);
        loading = findViewById(R.id.flashcard_setup_loading);
        dailyProgress = findViewById(R.id.flashcard_daily_progress);
        masteryProgress = findViewById(R.id.flashcard_mastery_progress);
        dailyProgressText = findViewById(R.id.flashcard_daily_progress_text);
        streakText = findViewById(R.id.flashcard_streak);
        dueText = findViewById(R.id.flashcard_due_count);
        masteredText = findViewById(R.id.flashcard_mastered_count);
        accuracyText = findViewById(R.id.flashcard_accuracy);
        breakdownText = findViewById(R.id.flashcard_mastery_breakdown);
        deckSummaryText = findViewById(R.id.flashcard_deck_summary);
        deckSpinner = findViewById(R.id.flashcard_deck_spinner);
        sessionSizeSpinner = findViewById(R.id.flashcard_session_size_spinner);
        dailyGoalSpinner = findViewById(R.id.flashcard_daily_goal_spinner);
        reminderSwitch = findViewById(R.id.flashcard_reminder_switch);
        reminderTimeButton = findViewById(R.id.flashcard_reminder_time);
        startButton = findViewById(R.id.flashcard_start_button);
    }

    private void configureGoalSelector() {
        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, DAILY_GOALS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dailyGoalSpinner.setAdapter(adapter);
        int savedGoal = progressStore.getDailyGoal();
        dailyGoalSpinner.setSelection(indexOfGoal(savedGoal));
        dailyGoalSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            progressStore.setDailyGoal(DAILY_GOALS[position]);
            renderDashboard();
        }));
    }

    private int indexOfGoal(int goal) {
        for (int index = 0; index < DAILY_GOALS.length; index++) {
            if (DAILY_GOALS[index] == goal) {
                return index;
            }
        }
        return 1;
    }

    private void configureSessionSizeSelector() {
        List<String> labels = Arrays.asList(
                getString(R.string.flashcards_session_5),
                getString(R.string.flashcards_session_10),
                getString(R.string.flashcards_session_20),
                getString(R.string.flashcards_session_30));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionSizeSpinner.setAdapter(adapter);
        int savedSize = preferences.getInt(PREF_SESSION_SIZE, 10);
        sessionSizeSpinner.setSelection(indexOfSessionSize(savedSize));
        sessionSizeSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            preferences.edit().putInt(PREF_SESSION_SIZE, SESSION_SIZES[position]).apply();
            updateDeckSummary();
        }));
    }

    private int indexOfSessionSize(int size) {
        for (int index = 0; index < SESSION_SIZES.length; index++) {
            if (SESSION_SIZES[index] == size) {
                return index;
            }
        }
        return 1;
    }

    private void configureReminder() {
        boolean enabled = preferences.getBoolean(FlashcardReminderScheduler.KEY_ENABLED, false);
        reminderSwitch.setChecked(enabled);
        updateReminderTimeText();
        reminderTimeButton.setEnabled(enabled);
        reminderSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            reminderTimeButton.setEnabled(checked);
            saveReminder(checked);
        });
        reminderTimeButton.setOnClickListener(v -> showTimePicker());
    }

    private void showTimePicker() {
        int hour = preferences.getInt(
                FlashcardReminderScheduler.KEY_HOUR, FlashcardReminderScheduler.DEFAULT_HOUR);
        int minute = preferences.getInt(
                FlashcardReminderScheduler.KEY_MINUTE, FlashcardReminderScheduler.DEFAULT_MINUTE);
        new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            FlashcardReminderScheduler.setReminder(this, preferences, true,
                    selectedHour, selectedMinute);
            updateReminderTimeText();
        }, hour, minute, DateFormat.is24HourFormat(this)).show();
    }

    private void saveReminder(boolean enabled) {
        FlashcardReminderScheduler.setReminder(this, preferences, enabled,
                preferences.getInt(FlashcardReminderScheduler.KEY_HOUR,
                        FlashcardReminderScheduler.DEFAULT_HOUR),
                preferences.getInt(FlashcardReminderScheduler.KEY_MINUTE,
                        FlashcardReminderScheduler.DEFAULT_MINUTE));
    }

    private void updateReminderTimeText() {
        int hour = preferences.getInt(
                FlashcardReminderScheduler.KEY_HOUR, FlashcardReminderScheduler.DEFAULT_HOUR);
        int minute = preferences.getInt(
                FlashcardReminderScheduler.KEY_MINUTE, FlashcardReminderScheduler.DEFAULT_MINUTE);
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.set(java.util.Calendar.HOUR_OF_DAY, hour);
        calendar.set(java.util.Calendar.MINUTE, minute);
        String time = DateFormat.getTimeFormat(this).format(calendar.getTime());
        reminderTimeButton.setText(getString(R.string.flashcards_reminder_time, time));
    }

    private void loadConnections() {
        disposables.clear();
        loading.setVisibility(View.VISIBLE);
        content.setVisibility(View.INVISIBLE);
        disposables.add(connectionsDataSource.getConnections(0)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showConnections, error -> {
                    loading.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.data_loading_error, Toast.LENGTH_SHORT).show();
                }));
    }

    private void showConnections(List<ConnectidConnection> connections) {
        eligibleConnections.clear();
        for (ConnectidConnection connection : connections) {
            String imageName = connection.getImageName();
            if (imageName != null && !imageName.trim().isEmpty()
                    && !"blank_profile.jpg".equals(imageName)) {
                eligibleConnections.add(connection);
            }
        }
        configureDeckSelector();
        renderDashboard();
        loading.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    private void configureDeckSelector() {
        deckTags.clear();
        deckTags.addAll(FlashcardDeckFilter.collectTags(eligibleConnections));
        ArrayList<String> labels = new ArrayList<>();
        labels.add(getString(R.string.flashcards_all_contacts));
        labels.addAll(deckTags);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deckSpinner.setAdapter(adapter);

        String savedDeck = preferences.getString(PREF_SELECTED_DECK, null);
        int savedPosition = savedDeck == null ? 0 : labels.indexOf(savedDeck);
        deckSpinner.setSelection(Math.max(0, savedPosition));
        deckSpinner.setOnItemSelectedListener(new SimpleItemSelectedListener(position -> {
            String selectedTag = position == 0 ? null : deckTags.get(position - 1);
            if (selectedTag == null) {
                preferences.edit().remove(PREF_SELECTED_DECK).apply();
            } else {
                preferences.edit().putString(PREF_SELECTED_DECK, selectedTag).apply();
            }
            updateDeckSummary();
        }));
    }

    private void renderDashboard() {
        if (progressStore == null) {
            return;
        }
        FlashcardProgressStore.Snapshot activity = progressStore.getSnapshot();
        dailyProgress.setMax(activity.getDailyGoal());
        dailyProgress.setProgress(Math.min(activity.getReviewsToday(), activity.getDailyGoal()));
        dailyProgressText.setText(getString(R.string.flashcards_daily_progress,
                activity.getReviewsToday(), activity.getDailyGoal()));
        streakText.setText(getString(R.string.flashcards_streak_summary,
                activity.getCurrentStreak(), activity.getBestStreak()));

        FlashcardDashboardStats stats = FlashcardDashboardStats.calculate(
                eligibleConnections, System.currentTimeMillis());
        dueText.setText(getString(R.string.flashcards_due_metric, stats.getDue()));
        masteredText.setText(getString(
                R.string.flashcards_mastered_metric, stats.getMastered(), stats.getTotal()));
        accuracyText.setText(getString(
                R.string.flashcards_accuracy_metric, stats.getAccuracyPercent()));
        masteryProgress.setMax(Math.max(1, stats.getTotal()));
        masteryProgress.setProgress(stats.getMastered());
        breakdownText.setText(getString(R.string.flashcards_mastery_breakdown,
                stats.getNewCards(), stats.getLearning(), stats.getMastered()));
        updateDeckSummary();
    }

    private void updateDeckSummary() {
        if (deckSpinner.getAdapter() == null || deckSpinner.getCount() == 0) {
            return;
        }
        ArrayList<ConnectidConnection> deck = getSelectedDeck();
        FlashcardDashboardStats stats = FlashcardDashboardStats.calculate(
                deck, System.currentTimeMillis());
        int requested = SESSION_SIZES[Math.max(0, sessionSizeSpinner.getSelectedItemPosition())];
        int sessionCount = Math.min(requested, deck.size());
        deckSummaryText.setText(getString(R.string.flashcards_deck_summary,
                deck.size(), stats.getDue(), sessionCount));
        startButton.setEnabled(deck.size() >= 2);
    }

    private ArrayList<ConnectidConnection> getSelectedDeck() {
        int position = deckSpinner.getSelectedItemPosition();
        String selectedTag = position <= 0 ? null : deckTags.get(position - 1);
        return FlashcardDeckFilter.filter(eligibleConnections, selectedTag);
    }

    private void startSession() {
        ArrayList<ConnectidConnection> deck = getSelectedDeck();
        if (deck.size() < 2) {
            Toast.makeText(this, R.string.flashcards_not_enough_deck_images,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        int size = SESSION_SIZES[sessionSizeSpinner.getSelectedItemPosition()];
        ArrayList<ConnectidConnection> selected = FlashcardSessionSelector.select(
                deck, System.currentTimeMillis(), size);
        Collections.shuffle(selected);
        Intent intent = new Intent(this, FlashcardsActivity.class);
        intent.putParcelableArrayListExtra(FlashcardsActivity.EXTRA_FLASHCARDS, selected);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConnections();
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        disposables.clear();
        super.onDestroy();
    }

    private interface SelectionCallback {
        void onSelected(int position);
    }

    private static final class SimpleItemSelectedListener
            implements android.widget.AdapterView.OnItemSelectedListener {
        private final SelectionCallback callback;

        SimpleItemSelectedListener(SelectionCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                                   int position, long id) {
            callback.onSelected(position);
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
        }
    }
}
