package me.anky.connectid.data.source.local;

import java.util.LinkedHashSet;
import java.util.Set;

final class ConnectionIdList {
    private ConnectionIdList() {
    }

    static String remove(String serializedIds, int databaseId) {
        if (serializedIds == null || serializedIds.trim().isEmpty()) {
            return null;
        }
        String idToRemove = String.valueOf(databaseId);
        Set<String> remaining = new LinkedHashSet<>();
        for (String candidate : serializedIds.split(",")) {
            String id = candidate.trim();
            if (!id.isEmpty() && !idToRemove.equals(id)) {
                remaining.add(id);
            }
        }
        if (remaining.isEmpty()) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (String id : remaining) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(id);
        }
        return result.toString();
    }
}
