package me.anky.connectid.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import me.anky.connectid.Utilities;
import me.anky.connectid.data.source.local.ConnectidColumns;
import me.anky.connectid.data.source.local.ConnectidDatabase;

/** Creates and reads complete backups containing the database CSV and contact photos. */
public final class BackupArchive {
    private static final String DATABASE_ENTRY = "remember_names.csv";
    private static final String IMAGE_PREFIX = "images/";
    private static final int BUFFER_SIZE = 16 * 1024;
    private static final int MAX_ENTRIES = 20_001;
    private static final long MAX_CSV_BYTES = 10L * 1024L * 1024L;
    private static final long MAX_IMAGE_BYTES = 25L * 1024L * 1024L;
    private static final long MAX_TOTAL_BYTES = 1024L * 1024L * 1024L;

    private BackupArchive() {
    }

    public static String export(SQLiteDatabase database, Context context) throws IOException {
        String csvPath = SqliteExporter.export(database, context);
        File csvFile = new File(csvPath);
        File backupDirectory = csvFile.getParentFile();
        if (backupDirectory == null) {
            throw new IOException("Unable to create the backup directory");
        }

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        File archive = new File(backupDirectory,
                "remember_names_" + date + "_with_photos.zip");
        if (archive.exists() && !archive.delete()) {
            throw new IOException("Unable to replace the existing backup archive");
        }

        boolean completed = false;
        try (ZipOutputStream zip = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream(archive)))) {
            addFile(zip, csvFile, DATABASE_ENTRY);
            addReferencedImages(zip, database, context);
            completed = true;
        } finally {
            if (!completed && archive.exists()) {
                archive.delete();
            }
        }
        return archive.getAbsolutePath();
    }

    public static Inspection inspect(InputStream input) throws IOException {
        PushbackInputStream stream = prepare(input);
        if (!isZip(stream)) {
            CsvBackupParser.Backup backup = CsvBackupParser.parse(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
            return new Inspection(backup, false, 0);
        }
        return readZip(stream, null);
    }

    public static Extraction extract(InputStream input, File stagingDirectory) throws IOException {
        PushbackInputStream stream = prepare(input);
        if (!isZip(stream)) {
            CsvBackupParser.Backup backup = CsvBackupParser.parse(
                    new InputStreamReader(stream, StandardCharsets.UTF_8));
            return new Extraction(backup, false, 0, null);
        }
        if ((!stagingDirectory.exists() && !stagingDirectory.mkdirs())
                || !stagingDirectory.isDirectory()) {
            throw new IOException("Unable to prepare images for import");
        }
        try {
            Inspection inspection = readZip(stream, stagingDirectory);
            return new Extraction(inspection.getBackup(), true,
                    inspection.getImageCount(), stagingDirectory);
        } catch (IOException | RuntimeException error) {
            deleteRecursively(stagingDirectory);
            throw error;
        }
    }

    public static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    private static void addReferencedImages(ZipOutputStream zip, SQLiteDatabase database,
                                            Context context) throws IOException {
        Set<String> entries = new HashSet<>();
        String[] columns = {ConnectidColumns.IMAGE_NAME};
        try (Cursor cursor = database.query(true, ConnectidDatabase.CONNECTIONS, columns,
                null, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                String imageName = cursor.isNull(0) ? "" : cursor.getString(0).trim();
                if (!isSafeImageName(imageName) || "blank_profile.jpg".equals(imageName)) {
                    continue;
                }
                addImageIfPresent(zip, entries,
                        Utilities.getContactImageFile(context, imageName), imageName);
                String previewName = Utilities.getContactPreviewImageName(imageName);
                addImageIfPresent(zip, entries,
                        Utilities.getContactPreviewFile(context, imageName), previewName);
            }
        }
    }

    private static void addImageIfPresent(ZipOutputStream zip, Set<String> entries,
                                          File image, String imageName) throws IOException {
        String entryName = IMAGE_PREFIX + imageName;
        if (image.isFile() && entries.add(entryName)) {
            addFile(zip, image, entryName);
        }
    }

    private static void addFile(ZipOutputStream zip, File file, String entryName)
            throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(file.lastModified());
        zip.putNextEntry(entry);
        try (InputStream input = new FileInputStream(file)) {
            copy(input, zip, Long.MAX_VALUE, null);
        }
        zip.closeEntry();
    }

    private static Inspection readZip(InputStream input, File stagingDirectory)
            throws IOException {
        byte[] csv = null;
        int imageCount = 0;
        int entryCount = 0;
        long[] totalBytes = {0L};
        Set<String> entryNames = new HashSet<>();

        try (ZipInputStream zip = new ZipInputStream(input)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++entryCount > MAX_ENTRIES) {
                    throw new IOException("The backup contains too many files");
                }
                String name = entry.getName();
                validateEntryName(name);
                if (!entryNames.add(name)) {
                    throw new IOException("The backup contains duplicate files");
                }
                if (entry.isDirectory()) {
                    zip.closeEntry();
                    continue;
                }

                if (DATABASE_ENTRY.equals(name)) {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    copy(zip, output, MAX_CSV_BYTES, totalBytes);
                    csv = output.toByteArray();
                } else if (name.startsWith(IMAGE_PREFIX)) {
                    String imageName = name.substring(IMAGE_PREFIX.length());
                    if (!isSafeImageName(imageName)) {
                        throw new IOException("The backup contains an invalid image name");
                    }
                    OutputStream output = null;
                    try {
                        if (stagingDirectory != null) {
                            output = new BufferedOutputStream(new FileOutputStream(
                                    new File(stagingDirectory, imageName)));
                        }
                        copy(zip, output, MAX_IMAGE_BYTES, totalBytes);
                    } finally {
                        if (output != null) {
                            output.close();
                        }
                    }
                    imageCount++;
                } else {
                    copy(zip, null, MAX_IMAGE_BYTES, totalBytes);
                }
                zip.closeEntry();
            }
        }

        if (csv == null) {
            throw new IOException("The backup does not contain the database CSV");
        }
        CsvBackupParser.Backup backup = CsvBackupParser.parse(new InputStreamReader(
                new ByteArrayInputStream(csv), StandardCharsets.UTF_8));
        return new Inspection(backup, true, imageCount);
    }

    private static void copy(InputStream input, OutputStream output, long entryLimit,
                             long[] totalBytes) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long entryBytes = 0L;
        int read;
        while ((read = input.read(buffer)) != -1) {
            entryBytes += read;
            if (entryBytes > entryLimit) {
                throw new IOException("A file in the backup is too large");
            }
            if (totalBytes != null) {
                totalBytes[0] += read;
                if (totalBytes[0] > MAX_TOTAL_BYTES) {
                    throw new IOException("The backup is too large");
                }
            }
            if (output != null) {
                output.write(buffer, 0, read);
            }
        }
    }

    private static PushbackInputStream prepare(InputStream input) {
        return input instanceof PushbackInputStream
                ? (PushbackInputStream) input : new PushbackInputStream(input, 4);
    }

    private static boolean isZip(PushbackInputStream input) throws IOException {
        byte[] signature = new byte[4];
        int read = input.read(signature);
        if (read > 0) {
            input.unread(signature, 0, read);
        }
        return read >= 2 && signature[0] == 'P' && signature[1] == 'K';
    }

    private static void validateEntryName(String name) throws IOException {
        if (name == null || name.isEmpty() || name.startsWith("/") || name.indexOf('\0') >= 0
                || name.contains("\\") || name.contains("../") || name.contains("/..")) {
            throw new IOException("The backup contains an unsafe file path");
        }
    }

    private static boolean isSafeImageName(String name) {
        return name != null && !name.isEmpty() && !".".equals(name) && !"..".equals(name)
                && name.indexOf('\0') < 0 && new File(name).getName().equals(name)
                && !name.contains("\\");
    }

    public static class Inspection {
        private final CsvBackupParser.Backup backup;
        private final boolean archive;
        private final int imageCount;

        private Inspection(CsvBackupParser.Backup backup, boolean archive, int imageCount) {
            this.backup = backup;
            this.archive = archive;
            this.imageCount = imageCount;
        }

        public CsvBackupParser.Backup getBackup() {
            return backup;
        }

        public boolean isArchive() {
            return archive;
        }

        public int getImageCount() {
            return imageCount;
        }
    }

    public static final class Extraction extends Inspection {
        private final File imageDirectory;

        private Extraction(CsvBackupParser.Backup backup, boolean archive, int imageCount,
                           File imageDirectory) {
            super(backup, archive, imageCount);
            this.imageDirectory = imageDirectory;
        }

        public File getImageDirectory() {
            return imageDirectory;
        }
    }
}
