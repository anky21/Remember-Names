package me.anky.connectid.flashcards;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

import static org.junit.Assert.assertEquals;

public class FlashcardSessionSelectorTest {
    private static final long NOW = 10_000L;

    @Test
    public void dueCardsAreChosenBeforeUpcomingCards() {
        ConnectidConnection upcoming = connection(1, NOW + 500L);
        ConnectidConnection overdue = connection(2, NOW - 100L);
        ConnectidConnection unseen = connection(3, 0L);

        List<ConnectidConnection> selected = FlashcardSessionSelector.select(
                Arrays.asList(upcoming, overdue, unseen), NOW, 2);

        assertEquals(3, selected.get(0).getDatabaseId());
        assertEquals(2, selected.get(1).getDatabaseId());
    }

    @Test
    public void earliestUpcomingCardsFillTheSession() {
        ConnectidConnection later = connection(1, NOW + 900L);
        ConnectidConnection sooner = connection(2, NOW + 100L);

        List<ConnectidConnection> selected = FlashcardSessionSelector.select(
                Arrays.asList(later, sooner), NOW, 10);

        assertEquals(2, selected.get(0).getDatabaseId());
        assertEquals(1, selected.get(1).getDatabaseId());
    }

    private ConnectidConnection connection(int id, long nextReview) {
        ConnectidConnection connection = new ConnectidConnection(
                id, "First", "Last", "photo.jpg", null, null, null, null, null, null);
        connection.setFlashcardNextReview(nextReview);
        return connection;
    }
}
