package me.anky.connectid.flashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.anky.connectid.data.ConnectidConnection;

public final class FlashcardSessionSelector {
    private FlashcardSessionSelector() {
    }

    public static ArrayList<ConnectidConnection> select(List<ConnectidConnection> connections,
                                                        long now, int limit) {
        ArrayList<ConnectidConnection> due = new ArrayList<>();
        ArrayList<ConnectidConnection> upcoming = new ArrayList<>();

        for (ConnectidConnection connection : connections) {
            long nextReview = connection.getFlashcardNextReview();
            if (nextReview == 0L || nextReview <= now) {
                due.add(connection);
            } else {
                upcoming.add(connection);
            }
        }

        Comparator<ConnectidConnection> oldestFirst = (first, second) -> {
            int dateComparison = Long.compare(
                    first.getFlashcardNextReview(), second.getFlashcardNextReview());
            if (dateComparison != 0) {
                return dateComparison;
            }
            return Integer.compare(first.getDatabaseId(), second.getDatabaseId());
        };
        Collections.sort(due, oldestFirst);
        Collections.sort(upcoming, oldestFirst);

        ArrayList<ConnectidConnection> selected = new ArrayList<>();
        appendUntilLimit(selected, due, limit);
        appendUntilLimit(selected, upcoming, limit);
        return selected;
    }

    private static void appendUntilLimit(ArrayList<ConnectidConnection> destination,
                                         List<ConnectidConnection> source, int limit) {
        for (ConnectidConnection connection : source) {
            if (destination.size() >= limit) {
                return;
            }
            destination.add(connection);
        }
    }
}
