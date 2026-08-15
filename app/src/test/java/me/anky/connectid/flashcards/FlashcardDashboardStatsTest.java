package me.anky.connectid.flashcards;

import org.junit.Test;

import java.util.Arrays;

import me.anky.connectid.data.ConnectidConnection;

import static org.junit.Assert.assertEquals;

public class FlashcardDashboardStatsTest {
    private static final long NOW = 10_000L;

    @Test
    public void calculatesDueMasteryAndAccuracy() {
        ConnectidConnection newCard = connection(1, 0, 0, 0, 0L);
        ConnectidConnection learning = connection(2, 2, 4, 2, NOW + 100L);
        ConnectidConnection mastered = connection(3, 4, 5, 5, NOW - 100L);

        FlashcardDashboardStats stats = FlashcardDashboardStats.calculate(
                Arrays.asList(newCard, learning, mastered), NOW);

        assertEquals(3, stats.getTotal());
        assertEquals(2, stats.getDue());
        assertEquals(1, stats.getNewCards());
        assertEquals(1, stats.getLearning());
        assertEquals(1, stats.getMastered());
        assertEquals(78, stats.getAccuracyPercent());
    }

    private ConnectidConnection connection(int id, int box, int attempts, int correct,
                                            long nextReview) {
        ConnectidConnection connection = new ConnectidConnection(
                id, "First", "Last", "photo.jpg", null, null, null, null, null, null);
        connection.setFlashcardBox(box);
        connection.setFlashcardAttempts(attempts);
        connection.setFlashcardCorrect(correct);
        connection.setFlashcardNextReview(nextReview);
        return connection;
    }
}
