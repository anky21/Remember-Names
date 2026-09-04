package me.anky.connectid.Utils;

import org.junit.Test;

import java.io.StringReader;

import me.anky.connectid.data.source.local.ConnectidColumns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CsvBackupParserTest {
    @Test
    public void parsesCurrentBackupAndIgnoresOtherTables() throws Exception {
        CsvBackupParser.Backup backup = CsvBackupParser.parse(new StringReader(
                "\"dbVersion = 3\"\n"
                        + "\"CONNECTIONS\"\n"
                        + headers() + "\n"
                        + "\"7\",\"Ana\",\"Smith\",\"photo.jpg\",\"Conference, Brisbane\","
                        + "\"Glasses\",\"Funny\",\"Jo\",\"Met in 2025\",\"Work, Friends\","
                        + "\"3\",\"100\",\"200\",\"8\",\"6\"\n"
                        + "\"\"\n"
                        + "\"TAGS\"\n"
                        + "\"_id\",\"tag\",\"connection_ids\"\n"
                        + "\"1\",\"Work\",\"7\"\n"));

        assertEquals(3, backup.getDatabaseVersion());
        assertEquals(1, backup.getContacts().size());
        assertEquals("Ana", backup.getContacts().get(0).get(ConnectidColumns.FIRST_NAME));
        assertEquals("Conference, Brisbane",
                backup.getContacts().get(0).get(ConnectidColumns.MEET_WHERE));
        assertEquals("3", backup.getContacts().get(0).get(ConnectidColumns.FLASHCARD_BOX));
        assertEquals(0, backup.getRejectedContacts());
    }

    @Test
    public void acceptsOldBackupAndRejectsRowsWithoutAFirstName() throws Exception {
        CsvBackupParser.Backup backup = CsvBackupParser.parse(new StringReader(
                "\"dbVersion = 1\"\n"
                        + "\"CONNECTIONS\"\n"
                        + "\"_id\",\"first_name\",\"last_name\",\"image_name\","
                        + "\"meet_venue\",\"appearance\",\"feature\",\"common_friends\","
                        + "\"description\"\n"
                        + "\"1\",\"\",\"Missing\",\"\",\"\",\"\",\"\",\"\",\"\"\n"
                        + "\"2\",\"Kai\",\"Lee\",\"\",\"School\",\"\",\"\",\"\",\"\"\n"));

        assertEquals(1, backup.getContacts().size());
        assertEquals(1, backup.getRejectedContacts());
        assertEquals("", backup.getContacts().get(0).get(ConnectidColumns.FLASHCARD_BOX));
    }

    @Test(expected = CsvBackupParser.BackupFormatException.class)
    public void rejectsUnrelatedCsvFiles() throws Exception {
        CsvBackupParser.parse(new StringReader("name,email\nAna,ana@example.com\n"));
    }

    @Test
    public void duplicateSignatureIgnoresCaseWhitespaceAndTagOrder() throws Exception {
        CsvBackupParser.Backup backup = CsvBackupParser.parse(new StringReader(
                "CONNECTIONS\n"
                        + headers() + "\n"
                        + row("Ana", "Smith", "Work, Friends", "Met once") + "\n"
                        + row(" ana ", "SMITH", "friends,work", " met once ") + "\n"
                        + row("Ana", "Smith", "Work, Friends", "Different notes") + "\n"));

        String first = SqliteBackupImporter.contactSignature(backup.getContacts().get(0));
        String duplicate = SqliteBackupImporter.contactSignature(backup.getContacts().get(1));
        String different = SqliteBackupImporter.contactSignature(backup.getContacts().get(2));
        assertEquals(first, duplicate);
        assertNotEquals(first, different);
    }

    private static String headers() {
        return "\"_id\",\"first_name\",\"last_name\",\"image_name\",\"meet_venue\","
                + "\"appearance\",\"feature\",\"common_friends\",\"description\",\"tags\","
                + "\"flashcard_box\",\"flashcard_last_reviewed\",\"flashcard_next_review\","
                + "\"flashcard_attempts\",\"flashcard_correct\"";
    }

    private static String row(String firstName, String lastName, String tags,
                              String description) {
        return "\"1\",\"" + firstName + "\",\"" + lastName
                + "\",\"\",\"\",\"\",\"\",\"\",\"" + description
                + "\",\"" + tags + "\",\"0\",\"0\",\"0\",\"0\",\"0\"";
    }
}
