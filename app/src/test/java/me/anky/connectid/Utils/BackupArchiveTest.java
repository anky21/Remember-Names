package me.anky.connectid.Utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import me.anky.connectid.data.source.local.ConnectidColumns;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BackupArchiveTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void inspectsAndExtractsDatabaseAndImages() throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("remember_names.csv", csv().getBytes(StandardCharsets.UTF_8));
        entries.put("images/person.jpg", new byte[]{1, 2, 3});
        entries.put("images/person_preview.jpg", new byte[]{4, 5});
        byte[] archive = zip(entries);

        BackupArchive.Inspection inspection = BackupArchive.inspect(
                new ByteArrayInputStream(archive));
        assertTrue(inspection.isArchive());
        assertEquals(2, inspection.getImageCount());
        assertEquals("Ana", inspection.getBackup().getContacts().get(0)
                .get(ConnectidColumns.FIRST_NAME));

        File staging = temporaryFolder.newFolder("staging");
        BackupArchive.Extraction extraction = BackupArchive.extract(
                new ByteArrayInputStream(archive), staging);
        assertEquals(2, extraction.getImageCount());
        assertTrue(new File(staging, "person.jpg").isFile());
        assertTrue(new File(staging, "person_preview.jpg").isFile());
        try (FileInputStream input = new FileInputStream(new File(staging, "person.jpg"))) {
            assertEquals(1, input.read());
            assertEquals(2, input.read());
            assertEquals(3, input.read());
        }
    }

    @Test
    public void stillAcceptsLegacyCsvBackup() throws Exception {
        BackupArchive.Inspection inspection = BackupArchive.inspect(
                new ByteArrayInputStream(csv().getBytes(StandardCharsets.UTF_8)));

        assertFalse(inspection.isArchive());
        assertEquals(0, inspection.getImageCount());
        assertEquals(1, inspection.getBackup().getContacts().size());
    }

    @Test(expected = java.io.IOException.class)
    public void rejectsArchivePathTraversal() throws Exception {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        entries.put("remember_names.csv", csv().getBytes(StandardCharsets.UTF_8));
        entries.put("images/../outside.jpg", new byte[]{1});

        BackupArchive.extract(new ByteArrayInputStream(zip(entries)),
                temporaryFolder.newFolder("unsafe"));
    }

    private static byte[] zip(Map<String, byte[]> entries) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                zip.putNextEntry(new ZipEntry(entry.getKey()));
                zip.write(entry.getValue());
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private static String csv() {
        return "\"dbVersion = 3\"\n"
                + "\"CONNECTIONS\"\n"
                + "\"_id\",\"first_name\",\"last_name\",\"image_name\","
                + "\"meet_venue\",\"appearance\",\"feature\",\"common_friends\","
                + "\"description\",\"tags\",\"flashcard_box\","
                + "\"flashcard_last_reviewed\",\"flashcard_next_review\","
                + "\"flashcard_attempts\",\"flashcard_correct\"\n"
                + "\"1\",\"Ana\",\"Smith\",\"person.jpg\",\"Work\",\"\","
                + "\"\",\"\",\"\",\"Friends\",\"1\",\"10\",\"20\",\"2\",\"1\"\n";
    }
}
