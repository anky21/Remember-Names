package me.anky.connectid.Utils;

import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Parses the sectioned CSV format produced by {@link SqliteExporter}. */
public final class CsvBackupParser {
    private static final int MAX_CONTACTS = 10_000;
    private static final int MAX_TEXT_CHARACTERS = 10 * 1024 * 1024;
    private static final String CONNECTIONS_SECTION = "connections";
    private static final String TAGS_SECTION = "tags";

    private CsvBackupParser() {
    }

    public static Backup parse(Reader reader) throws IOException {
        List<ContactRecord> contacts = new ArrayList<>();
        int rejectedContacts = 0;
        int databaseVersion = 0;
        int charactersRead = 0;
        boolean foundConnectionsSection = false;
        String currentSection = null;
        String[] headers = null;

        try (CSVReader csvReader = new CSVReader(reader)) {
            String[] row;
            while ((row = csvReader.readNext()) != null) {
                for (String value : row) {
                    if (value != null) {
                        charactersRead += value.length();
                    }
                }
                if (charactersRead > MAX_TEXT_CHARACTERS) {
                    throw new BackupFormatException("The backup file is too large");
                }

                String marker = row.length == 1 ? cleanMarker(row[0]) : null;
                if (marker != null && marker.toLowerCase(Locale.ROOT).startsWith("dbversion")) {
                    databaseVersion = parseDatabaseVersion(marker);
                    continue;
                }
                if (marker != null && CONNECTIONS_SECTION.equalsIgnoreCase(marker)) {
                    currentSection = CONNECTIONS_SECTION;
                    headers = null;
                    foundConnectionsSection = true;
                    continue;
                }
                if (marker != null && TAGS_SECTION.equalsIgnoreCase(marker)) {
                    currentSection = TAGS_SECTION;
                    headers = null;
                    continue;
                }
                if (isBlankRow(row)) {
                    currentSection = null;
                    headers = null;
                    continue;
                }
                if (currentSection == null) {
                    continue;
                }
                if (headers == null) {
                    headers = normalizeHeaders(row);
                    if (CONNECTIONS_SECTION.equals(currentSection)
                            && !contains(headers, "first_name")) {
                        throw new BackupFormatException(
                                "The contacts section does not contain a first_name column");
                    }
                    continue;
                }
                if (!CONNECTIONS_SECTION.equals(currentSection)) {
                    continue;
                }
                if (contacts.size() + rejectedContacts >= MAX_CONTACTS) {
                    throw new BackupFormatException("The backup contains too many contacts");
                }

                ContactRecord contact = toContactRecord(headers, row);
                if (contact.get("first_name").trim().isEmpty()) {
                    rejectedContacts++;
                } else {
                    contacts.add(contact);
                }
            }
        }

        if (!foundConnectionsSection) {
            throw new BackupFormatException("This is not a Remember Names database backup");
        }
        return new Backup(databaseVersion, contacts, rejectedContacts);
    }

    private static ContactRecord toContactRecord(String[] headers, String[] row) {
        Map<String, String> values = new LinkedHashMap<>();
        for (int i = 0; i < headers.length; i++) {
            if (!headers[i].isEmpty()) {
                values.put(headers[i], i < row.length && row[i] != null ? row[i] : "");
            }
        }
        return new ContactRecord(values);
    }

    private static String[] normalizeHeaders(String[] row) {
        String[] headers = new String[row.length];
        for (int i = 0; i < row.length; i++) {
            headers[i] = cleanMarker(row[i]).toLowerCase(Locale.ROOT);
        }
        return headers;
    }

    private static boolean contains(String[] values, String expected) {
        for (String value : values) {
            if (expected.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBlankRow(String[] row) {
        if (row.length == 0) {
            return true;
        }
        for (String value : row) {
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static String cleanMarker(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\uFEFF", "").trim();
    }

    private static int parseDatabaseVersion(String marker) {
        int separator = marker.indexOf('=');
        if (separator < 0 || separator == marker.length() - 1) {
            return 0;
        }
        try {
            return Integer.parseInt(marker.substring(separator + 1).trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static final class Backup {
        private final int databaseVersion;
        private final List<ContactRecord> contacts;
        private final int rejectedContacts;

        private Backup(int databaseVersion, List<ContactRecord> contacts, int rejectedContacts) {
            this.databaseVersion = databaseVersion;
            this.contacts = Collections.unmodifiableList(new ArrayList<>(contacts));
            this.rejectedContacts = rejectedContacts;
        }

        public int getDatabaseVersion() {
            return databaseVersion;
        }

        public List<ContactRecord> getContacts() {
            return contacts;
        }

        public int getRejectedContacts() {
            return rejectedContacts;
        }
    }

    public static final class ContactRecord {
        private final Map<String, String> values;

        private ContactRecord(Map<String, String> values) {
            this.values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }

        public String get(String column) {
            String value = values.get(column);
            return value == null ? "" : value;
        }
    }

    public static class BackupFormatException extends IOException {
        public BackupFormatException(String message) {
            super(message);
        }
    }
}
