package me.anky.connectid.flashcards;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FlashcardSchedulerTest {
    private static final long NOW = 1_000_000L;

    @Test
    public void againResetsCardAndSchedulesShortRetry() {
        FlashcardScheduler.ReviewResult result = FlashcardScheduler.schedule(
                4, 7, 5, NOW, FlashcardRating.AGAIN);

        assertEquals(0, result.getBox());
        assertEquals(NOW, result.getLastReviewed());
        assertEquals(NOW + 10L * FlashcardScheduler.MINUTE_MS, result.getNextReview());
        assertEquals(8, result.getAttempts());
        assertEquals(5, result.getCorrect());
    }

    @Test
    public void hardKeepsEstablishedCardAndSchedulesTomorrow() {
        FlashcardScheduler.ReviewResult result = FlashcardScheduler.schedule(
                3, 2, 1, NOW, FlashcardRating.HARD);

        assertEquals(3, result.getBox());
        assertEquals(NOW + FlashcardScheduler.DAY_MS, result.getNextReview());
        assertEquals(3, result.getAttempts());
        assertEquals(1, result.getCorrect());
    }

    @Test
    public void hardIntroducesNewCardAtFirstBox() {
        FlashcardScheduler.ReviewResult result = FlashcardScheduler.schedule(
                0, 0, 0, NOW, FlashcardRating.HARD);

        assertEquals(1, result.getBox());
        assertEquals(NOW + FlashcardScheduler.DAY_MS, result.getNextReview());
    }

    @Test
    public void gotItAdvancesAndCapsAtThirtyDays() {
        FlashcardScheduler.ReviewResult result = FlashcardScheduler.schedule(
                6, 10, 8, NOW, FlashcardRating.GOT_IT);

        assertEquals(6, result.getBox());
        assertEquals(NOW + 30L * FlashcardScheduler.DAY_MS, result.getNextReview());
        assertEquals(11, result.getAttempts());
        assertEquals(9, result.getCorrect());
    }
}
