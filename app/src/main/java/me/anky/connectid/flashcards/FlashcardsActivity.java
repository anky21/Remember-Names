package me.anky.connectid.flashcards;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import me.anky.connectid.R;
import me.anky.connectid.Utilities;
import me.anky.connectid.data.ConnectidConnection;
import me.anky.connectid.data.ConnectionsDataSource;
import me.anky.connectid.root.ConnectidApplication;

public class FlashcardsActivity extends AppCompatActivity {

    public static final String EXTRA_FLASHCARDS = "flashcards";
    private static final String STATE_FLASHCARDS = "state_flashcards";
    private static final String STATE_REVIEW_CARDS = "state_review_cards";
    private static final String STATE_CURRENT_INDEX = "state_current_index";
    private static final String STATE_SHOWING_BACK = "state_showing_back";
    private static final String STATE_RESULTS_VISIBLE = "state_results_visible";
    private static final String STATE_AGAIN_COUNT = "state_again_count";
    private static final String STATE_HARD_COUNT = "state_hard_count";
    private static final String STATE_GOT_IT_COUNT = "state_got_it_count";
    private static final int ANIMATION_DURATION_MS = 150;

    @Inject
    ConnectionsDataSource connectionsDataSource;

    private final ExecutorService reviewWriteExecutor = Executors.newSingleThreadExecutor();
    private ArrayList<ConnectidConnection> flashcards;
    private ArrayList<ConnectidConnection> reviewCards = new ArrayList<>();
    private int currentIndex;
    private int againCount;
    private int hardCount;
    private int gotItCount;
    private boolean isShowingBack;
    private boolean resultsVisible;
    private FlashcardProgressStore progressStore;

    private ImageView imageView;
    private TextView nameTextView;
    private TextView detailsTextView;
    private TextView progressTextView;
    private TextView hintTextView;
    private TextView resultsSummaryTextView;
    private View cardContainer;
    private View resultsContainer;
    private View ratingActions;
    private Button revealButton;
    private Button reviewAgainButton;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConnectidApplication.getAppInstance().getApplicationComponent().inject(this);
        setContentView(R.layout.activity_flashcards);
        progressStore = new FlashcardProgressStore(
                getSharedPreferences("shared-prefs", MODE_PRIVATE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(
                    ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        bindViews();
        restoreState(savedInstanceState);
        if (flashcards == null || flashcards.isEmpty()) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        configureAds();
        configureActions();
        if (resultsVisible) {
            showResults();
        } else {
            boolean restoreBack = isShowingBack;
            showCurrentFlashcard();
            if (restoreBack) {
                showBackOfCard(false);
            }
        }
    }

    private void bindViews() {
        imageView = findViewById(R.id.flashcard_image);
        nameTextView = findViewById(R.id.flashcard_name);
        detailsTextView = findViewById(R.id.flashcard_details);
        progressTextView = findViewById(R.id.flashcard_progress);
        hintTextView = findViewById(R.id.flashcard_hint);
        resultsSummaryTextView = findViewById(R.id.flashcard_results_summary);
        revealButton = findViewById(R.id.flashcard_reveal_button);
        reviewAgainButton = findViewById(R.id.flashcard_review_again_button);
        ratingActions = findViewById(R.id.flashcard_rating_actions);
        cardContainer = findViewById(R.id.flashcard_content_container);
        resultsContainer = findViewById(R.id.flashcard_results_container);
        adView = findViewById(R.id.adView);
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            flashcards = getIntent().getParcelableArrayListExtra(EXTRA_FLASHCARDS);
            return;
        }
        flashcards = savedInstanceState.getParcelableArrayList(STATE_FLASHCARDS);
        ArrayList<ConnectidConnection> restoredReviewCards =
                savedInstanceState.getParcelableArrayList(STATE_REVIEW_CARDS);
        if (restoredReviewCards != null) {
            reviewCards = restoredReviewCards;
        }
        currentIndex = savedInstanceState.getInt(STATE_CURRENT_INDEX);
        isShowingBack = savedInstanceState.getBoolean(STATE_SHOWING_BACK);
        resultsVisible = savedInstanceState.getBoolean(STATE_RESULTS_VISIBLE);
        againCount = savedInstanceState.getInt(STATE_AGAIN_COUNT);
        hardCount = savedInstanceState.getInt(STATE_HARD_COUNT);
        gotItCount = savedInstanceState.getInt(STATE_GOT_IT_COUNT);
    }

    private void configureAds() {
        if (ConnectidApplication.getAppInstance().getSubscriptionManager().isAdFree()) {
            adView.setVisibility(View.GONE);
            return;
        }
        new Thread(() -> MobileAds.initialize(this, status -> { })).start();
        adView.loadAd(new AdRequest.Builder().build());
    }

    private void configureActions() {
        imageView.setOnClickListener(v -> revealCurrentCard());
        revealButton.setOnClickListener(v -> revealCurrentCard());
        findViewById(R.id.flashcard_again_button)
                .setOnClickListener(v -> rateCurrentCard(FlashcardRating.AGAIN));
        findViewById(R.id.flashcard_hard_button)
                .setOnClickListener(v -> rateCurrentCard(FlashcardRating.HARD));
        findViewById(R.id.flashcard_got_it_button)
                .setOnClickListener(v -> rateCurrentCard(FlashcardRating.GOT_IT));
        reviewAgainButton.setOnClickListener(v -> startReviewRound());
        findViewById(R.id.flashcard_done_button).setOnClickListener(v -> finish());
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }

    private void showCurrentFlashcard() {
        if (currentIndex < 0 || currentIndex >= flashcards.size()) {
            currentIndex = 0;
        }
        resultsVisible = false;
        isShowingBack = false;
        cardContainer.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.GONE);
        progressTextView.setVisibility(View.VISIBLE);
        hintTextView.setVisibility(View.VISIBLE);
        revealButton.setVisibility(View.VISIBLE);
        ratingActions.setVisibility(View.GONE);
        nameTextView.setVisibility(View.GONE);
        detailsTextView.setVisibility(View.GONE);
        hintTextView.setText(R.string.flashcards_hint);
        progressTextView.setText(getString(
                R.string.flashcards_progress, currentIndex + 1, flashcards.size()));

        ConnectidConnection connection = flashcards.get(currentIndex);
        loadImage(connection);
        imageView.setContentDescription(getString(
                R.string.flashcards_reveal_accessibility, currentIndex + 1));
        cardContainer.setAlpha(1f);
    }

    private void loadImage(ConnectidConnection connection) {
        String imageName = connection.getImageName();
        if (imageName == null || "blank_profile.jpg".equals(imageName)) {
            imageView.setImageResource(R.drawable.blank_profile_round);
            return;
        }

        File imageFile = Utilities.getBestContactPreviewFile(this, imageName);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.blank_profile_round)
                .error(R.drawable.blank_profile_round);
        Glide.with(this)
                .load(Uri.fromFile(imageFile))
                .apply(options)
                .into(imageView);
    }

    private void revealCurrentCard() {
        if (isShowingBack || resultsVisible) {
            return;
        }
        cardContainer.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION_MS)
                .withEndAction(() -> {
                    showBackOfCard(true);
                    cardContainer.animate()
                            .alpha(1f)
                            .setDuration(ANIMATION_DURATION_MS)
                            .start();
                })
                .start();
    }

    private void showBackOfCard(boolean announce) {
        ConnectidConnection connection = flashcards.get(currentIndex);
        isShowingBack = true;
        String fullName = getFullName(connection);
        nameTextView.setText(fullName);
        String details = buildDetails(connection);
        detailsTextView.setText(details);
        nameTextView.setVisibility(View.VISIBLE);
        detailsTextView.setVisibility(details.isEmpty() ? View.GONE : View.VISIBLE);
        revealButton.setVisibility(View.GONE);
        ratingActions.setVisibility(View.VISIBLE);
        hintTextView.setText(R.string.flashcards_rate_hint);
        imageView.setContentDescription(getString(
                R.string.flashcards_revealed_accessibility, fullName));
        if (announce) {
            nameTextView.announceForAccessibility(fullName);
        }
    }

    private String getFullName(ConnectidConnection connection) {
        String firstName = connection.getFirstName() == null ? "" : connection.getFirstName();
        String lastName = connection.getLastName() == null ? "" : connection.getLastName();
        return (firstName + " " + lastName).trim();
    }

    private String buildDetails(ConnectidConnection connection) {
        StringBuilder details = new StringBuilder();
        appendDetail(details, R.string.flashcards_met_at, connection.getMeetVenue());
        appendDetail(details, R.string.flashcards_appearance, connection.getAppearance());
        appendDetail(details, R.string.flashcards_memorable_detail, connection.getFeature());
        return details.toString();
    }

    private void appendDetail(StringBuilder details, int label, String value) {
        if (value == null || value.trim().isEmpty() || "null".equals(value)) {
            return;
        }
        if (details.length() > 0) {
            details.append('\n');
        }
        details.append(getString(label, value.trim()));
    }

    private void rateCurrentCard(FlashcardRating rating) {
        if (!isShowingBack || resultsVisible) {
            return;
        }
        ConnectidConnection connection = flashcards.get(currentIndex);
        FlashcardScheduler.ReviewResult result = FlashcardScheduler.schedule(
                connection.getFlashcardBox(),
                connection.getFlashcardAttempts(),
                connection.getFlashcardCorrect(),
                System.currentTimeMillis(),
                rating);
        applyReviewResult(connection, result);
        persistProgress(connection);
        progressStore.recordReview();

        if (rating == FlashcardRating.AGAIN) {
            againCount++;
            reviewCards.add(connection);
        } else if (rating == FlashcardRating.HARD) {
            hardCount++;
            reviewCards.add(connection);
        } else {
            gotItCount++;
        }

        if (currentIndex >= flashcards.size() - 1) {
            showResults();
        } else {
            currentIndex++;
            showCurrentFlashcard();
        }
    }

    private void applyReviewResult(ConnectidConnection connection,
                                   FlashcardScheduler.ReviewResult result) {
        connection.setFlashcardBox(result.getBox());
        connection.setFlashcardLastReviewed(result.getLastReviewed());
        connection.setFlashcardNextReview(result.getNextReview());
        connection.setFlashcardAttempts(result.getAttempts());
        connection.setFlashcardCorrect(result.getCorrect());
    }

    private void persistProgress(ConnectidConnection connection) {
        reviewWriteExecutor.execute(() -> {
            try {
                int updated = connectionsDataSource.updateFlashcardProgress(connection);
                if (updated != 1) {
                    showSaveError();
                }
            } catch (RuntimeException error) {
                Utilities.logFirebaseError("error_save_flashcard_progress",
                        "FlashcardsActivity.persistProgress");
                showSaveError();
            }
        });
    }

    private void showSaveError() {
        runOnUiThread(() -> Toast.makeText(
                this, R.string.flashcards_save_error, Toast.LENGTH_SHORT).show());
    }

    private void showResults() {
        resultsVisible = true;
        cardContainer.setVisibility(View.GONE);
        progressTextView.setVisibility(View.GONE);
        hintTextView.setVisibility(View.GONE);
        revealButton.setVisibility(View.GONE);
        ratingActions.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);
        resultsSummaryTextView.setText(getString(
                R.string.flashcards_results_summary, gotItCount, hardCount, againCount));
        reviewAgainButton.setVisibility(reviewCards.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void startReviewRound() {
        if (reviewCards.isEmpty()) {
            return;
        }
        flashcards = new ArrayList<>(reviewCards);
        reviewCards.clear();
        currentIndex = 0;
        againCount = 0;
        hardCount = 0;
        gotItCount = 0;
        showCurrentFlashcard();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_FLASHCARDS, flashcards);
        outState.putParcelableArrayList(STATE_REVIEW_CARDS, reviewCards);
        outState.putInt(STATE_CURRENT_INDEX, currentIndex);
        outState.putBoolean(STATE_SHOWING_BACK, isShowingBack);
        outState.putBoolean(STATE_RESULTS_VISIBLE, resultsVisible);
        outState.putInt(STATE_AGAIN_COUNT, againCount);
        outState.putInt(STATE_HARD_COUNT, hardCount);
        outState.putInt(STATE_GOT_IT_COUNT, gotItCount);
    }

    @Override
    protected void onDestroy() {
        reviewWriteExecutor.shutdown();
        super.onDestroy();
    }
}
