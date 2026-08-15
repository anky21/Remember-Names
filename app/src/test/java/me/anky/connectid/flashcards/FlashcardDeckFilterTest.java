package me.anky.connectid.flashcards;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

import static org.junit.Assert.assertEquals;

public class FlashcardDeckFilterTest {
    @Test
    public void collectsUniqueSortedTags() {
        List<String> tags = FlashcardDeckFilter.collectTags(Arrays.asList(
                connection(1, "Work, Friends"),
                connection(2, "friends, Family")));

        assertEquals(Arrays.asList("Family", "Friends", "Work"), tags);
    }

    @Test
    public void filterUsesExactCaseInsensitiveTag() {
        List<ConnectidConnection> filtered = FlashcardDeckFilter.filter(Arrays.asList(
                connection(1, "Work"),
                connection(2, "Workshop"),
                connection(3, "work, Friends")), "WORK");

        assertEquals(2, filtered.size());
        assertEquals(1, filtered.get(0).getDatabaseId());
        assertEquals(3, filtered.get(1).getDatabaseId());
    }

    private ConnectidConnection connection(int id, String tags) {
        return new ConnectidConnection(
                id, "First", "Last", "photo.jpg", null, null, null, null, null, tags);
    }
}
