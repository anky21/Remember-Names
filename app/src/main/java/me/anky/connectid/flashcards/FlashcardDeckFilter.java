package me.anky.connectid.flashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import me.anky.connectid.data.ConnectidConnection;

public final class FlashcardDeckFilter {
    private FlashcardDeckFilter() {
    }

    public static List<String> collectTags(List<ConnectidConnection> connections) {
        Set<String> tags = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (ConnectidConnection connection : connections) {
            for (String tag : splitTags(connection.getTags())) {
                tags.add(tag);
            }
        }
        return new ArrayList<>(tags);
    }

    public static ArrayList<ConnectidConnection> filter(List<ConnectidConnection> connections,
                                                        String selectedTag) {
        ArrayList<ConnectidConnection> result = new ArrayList<>();
        if (selectedTag == null) {
            result.addAll(connections);
            return result;
        }
        for (ConnectidConnection connection : connections) {
            for (String tag : splitTags(connection.getTags())) {
                if (tag.equalsIgnoreCase(selectedTag)) {
                    result.add(connection);
                    break;
                }
            }
        }
        return result;
    }

    private static List<String> splitTags(String serializedTags) {
        if (serializedTags == null || serializedTags.trim().isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<String> tags = new ArrayList<>();
        for (String candidate : serializedTags.split(",")) {
            String tag = candidate.trim();
            if (!tag.isEmpty()) {
                tags.add(tag);
            }
        }
        return tags;
    }
}
