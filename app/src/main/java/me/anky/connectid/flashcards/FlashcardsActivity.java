package me.anky.connectid.flashcards;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import me.anky.connectid.R;
import me.anky.connectid.data.ConnectidConnection;

public class FlashcardsActivity extends AppCompatActivity {

    private static final String TAG = FlashcardsActivity.class.getSimpleName();
    public static final String EXTRA_FLASHCARDS = "flashcards";

    private ArrayList<ConnectidConnection> flashcards;
    private int currentIndex = 0;

    private ImageView imageView;
    private TextView nameTextView;
    private TextView detailsTextView;
    private TextView progressTextView;
    private Button nextButton;
    private AdView mAdView;
    private View cardContainer;

    private boolean isShowingBack = false;
    private static final int ANIMATION_DURATION_MS = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcards);

        imageView = findViewById(R.id.flashcard_image);
        nameTextView = findViewById(R.id.flashcard_name);
        detailsTextView = findViewById(R.id.flashcard_details);
        progressTextView = findViewById(R.id.flashcard_progress);
        nextButton = findViewById(R.id.flashcard_next_button);
        mAdView = findViewById(R.id.adView);
        cardContainer = findViewById(R.id.flashcard_content_container);

        if (cardContainer != null) {
            cardContainer.setAlpha(1f);
        }

        // Initialise Google Mobile Ads on a background thread (same pattern as other screens)
        new Thread(
                () -> MobileAds.initialize(this, initializationStatus -> { }))
                .start();

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        flashcards = getIntent().getParcelableArrayListExtra(EXTRA_FLASHCARDS);
        if (flashcards == null || flashcards.isEmpty()) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        imageView.setOnClickListener(v -> toggleCard());
        nextButton.setOnClickListener(v -> showNextFlashcard());

        showCurrentFlashcard();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showCurrentFlashcard() {
        if (flashcards == null || flashcards.isEmpty()) {
            return;
        }
        if (currentIndex < 0 || currentIndex >= flashcards.size()) {
            currentIndex = 0;
        }

        isShowingBack = false;
        updateProgress();

        // Always show front first (image only)
        ConnectidConnection connection = flashcards.get(currentIndex);

        String imageName = connection.getImageName();
        Log.d(TAG, "showCurrentFlashcard index=" + currentIndex
                + ", id=" + connection.getDatabaseId()
                + ", firstName=" + connection.getFirstName()
                + ", lastName=" + connection.getLastName()
                + ", imageName=" + imageName);

        if (imageName != null && !"blank_profile.jpg".equals(imageName)) {
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            String path = directory.getAbsolutePath() + "/" + imageName;
            File imageFile = new File(path);

            Log.d(TAG, "Resolved image path=" + path + ", exists=" + imageFile.exists() + ", length=" + (imageFile.exists() ? imageFile.length() : 0));

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.blank_profile_round)
                    .error(R.drawable.blank_profile_round);

            Glide.with(this)
                    .load(Uri.fromFile(imageFile))
                    .apply(options)
                    .into(imageView);
        } else {
            Log.d(TAG, "Using default image for connection id=" + connection.getDatabaseId());
            imageView.setImageResource(R.drawable.blank_profile_round);
        }

        // Hide details while on front side
        nameTextView.setVisibility(View.GONE);
        detailsTextView.setVisibility(View.GONE);
    }

    private void showBackOfCard() {
        if (flashcards == null || flashcards.isEmpty()) {
            return;
        }
        if (currentIndex < 0 || currentIndex >= flashcards.size()) {
            currentIndex = 0;
        }

        ConnectidConnection connection = flashcards.get(currentIndex);

        String firstName = connection.getFirstName() != null ? connection.getFirstName() : "";
        String lastName = connection.getLastName() != null ? connection.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();
        nameTextView.setText(fullName);

        StringBuilder detailsBuilder = new StringBuilder();

        // Anything significant about their looks
        if (connection.getAppearance() != null && !connection.getAppearance().trim().isEmpty()) {
            detailsBuilder.append(connection.getAppearance().trim()).append("\n");
        }

        // What's special about them
        if (connection.getFeature() != null && !connection.getFeature().trim().isEmpty()) {
            detailsBuilder.append(connection.getFeature().trim());
        }

        String detailsText = detailsBuilder.toString().trim();
        detailsTextView.setText(detailsText);

        nameTextView.setVisibility(View.VISIBLE);
        detailsTextView.setVisibility(detailsText.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void toggleCard() {
        if (flashcards == null || flashcards.isEmpty()) {
            return;
        }
        if (isShowingBack) {
            animateCardFlip(false);
        } else {
            animateCardFlip(true);
        }
    }

    private void animateCardFlip(final boolean showBack) {
        if (cardContainer == null) {
            // Fallback: no container, just switch immediately
            if (showBack) {
                isShowingBack = true;
                showBackOfCard();
            } else {
                isShowingBack = false;
                nameTextView.setVisibility(View.GONE);
                detailsTextView.setVisibility(View.GONE);
            }
            return;
        }

        cardContainer.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION_MS)
                .withEndAction(() -> {
                    if (showBack) {
                        isShowingBack = true;
                        showBackOfCard();
                    } else {
                        isShowingBack = false;
                        nameTextView.setVisibility(View.GONE);
                        detailsTextView.setVisibility(View.GONE);
                    }
                    cardContainer.animate()
                            .alpha(1f)
                            .setDuration(ANIMATION_DURATION_MS)
                            .start();
                })
                .start();
    }

    private void updateProgress() {
        if (progressTextView != null && flashcards != null && !flashcards.isEmpty()) {
            String progressText = (currentIndex + 1) + " / " + flashcards.size();
            progressTextView.setText(progressText);
        }
    }

    private void showNextFlashcard() {
        if (flashcards == null || flashcards.isEmpty()) {
            return;
        }

        // If at last card, ask whether to continue or exit
        if (currentIndex == flashcards.size() - 1) {
            showEndOfGameDialog();
            return;
        }

        currentIndex++;
        showCurrentFlashcard();
    }

    private void showEndOfGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.flashcards_end_of_game_message);
        builder.setPositiveButton(R.string.flashcards_continue, (dialog, which) -> {
            // Restart from first card
            currentIndex = 0;
            showCurrentFlashcard();
            dialog.dismiss();
        });
        builder.setNegativeButton(R.string.flashcards_exit, (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setCancelable(true);
        builder.show();
    }
}
