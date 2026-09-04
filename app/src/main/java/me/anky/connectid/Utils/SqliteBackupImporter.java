package me.anky.connectid.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.anky.connectid.Utilities;
import me.anky.connectid.data.source.local.ConnectidColumns;
import me.anky.connectid.data.source.local.ConnectidDatabase;
import me.anky.connectid.data.source.local.ConnectidProvider;
import me.anky.connectid.data.source.local.TagsColumns;

/** Restores parsed contacts without replacing existing app data. */
public final class SqliteBackupImporter {
    private static final String DEFAULT_IMAGE = "blank_profile.jpg";

    private SqliteBackupImporter() {
    }

    public static ImportResult importBackup(SQLiteDatabase database, Context context,
                                            CsvBackupParser.Backup backup) throws IOException {
        return importBackup(database, context, backup, null);
    }

    public static ImportResult importBackup(SQLiteDatabase database, Context context,
                                            CsvBackupParser.Backup backup,
                                            File importedImages) throws IOException {
        Map<String, ExistingContact> existingContacts = loadContacts(database);
        Map<String, TagRow> tags = loadTags(database);
        List<File> createdImages = new ArrayList<>();
        int imported = 0;
        int skipped = 0;
        int missingPhotos = 0;
        int restoredPhotos = 0;
        boolean transactionSuccessful = false;
        boolean transactionEnded = false;

        database.beginTransaction();
        try {
            for (CsvBackupParser.ContactRecord contact : backup.getContacts()) {
                String signature = contactSignature(contact);
                ExistingContact existingContact = existingContacts.get(signature);
                if (existingContact != null) {
                    if (restoreMissingPhoto(database, context, existingContact, contact,
                            importedImages, createdImages)) {
                        restoredPhotos++;
                    }
                    skipped++;
                    continue;
                }

                ImageValue image = resolveImage(context,
                        contact.get(ConnectidColumns.IMAGE_NAME), importedImages, createdImages);
                if (image.wasMissing) {
                    missingPhotos++;
                } else if (!DEFAULT_IMAGE.equals(image.name)) {
                    restoredPhotos++;
                }

                ContentValues values = contactValues(contact, image.name);
                long newId = database.insertOrThrow(
                        ConnectidDatabase.CONNECTIONS, null, values);
                mergeTags(database, tags, splitTags(contact.get(ConnectidColumns.TAGS)), newId);
                existingContacts.put(signature, new ExistingContact(newId, image.name));
                imported++;
            }
            database.setTransactionSuccessful();
            transactionSuccessful = true;
        } finally {
            try {
                database.endTransaction();
                transactionEnded = true;
            } finally {
                if (!transactionSuccessful || !transactionEnded) {
                    for (File image : createdImages) {
                        image.delete();
                    }
                }
            }
        }
        context.getContentResolver().notifyChange(ConnectidProvider.Connections.CONTENT_URI, null);
        context.getContentResolver().notifyChange(ConnectidProvider.Tags.CONTENT_URI, null);

        return new ImportResult(imported, skipped, backup.getRejectedContacts(),
                missingPhotos, restoredPhotos);
    }

    static String contactSignature(CsvBackupParser.ContactRecord contact) {
        StringBuilder signature = new StringBuilder();
        appendNormalized(signature, contact.get(ConnectidColumns.FIRST_NAME));
        appendNormalized(signature, contact.get(ConnectidColumns.LAST_NAME));
        appendNormalized(signature, contact.get(ConnectidColumns.MEET_WHERE));
        appendNormalized(signature, contact.get(ConnectidColumns.APPEARANCE));
        appendNormalized(signature, contact.get(ConnectidColumns.FEATURE));
        appendNormalized(signature, contact.get(ConnectidColumns.COMMON_FRIENDS));
        appendNormalized(signature, contact.get(ConnectidColumns.DESCRIPTION));
        appendNormalized(signature, normalizedTags(contact.get(ConnectidColumns.TAGS)));
        return signature.toString();
    }

    private static Map<String, ExistingContact> loadContacts(SQLiteDatabase database) {
        Map<String, ExistingContact> contacts = new HashMap<>();
        String[] columns = {
                ConnectidColumns._ID,
                ConnectidColumns.IMAGE_NAME,
                ConnectidColumns.FIRST_NAME,
                ConnectidColumns.LAST_NAME,
                ConnectidColumns.MEET_WHERE,
                ConnectidColumns.APPEARANCE,
                ConnectidColumns.FEATURE,
                ConnectidColumns.COMMON_FRIENDS,
                ConnectidColumns.DESCRIPTION,
                ConnectidColumns.TAGS
        };
        try (Cursor cursor = database.query(ConnectidDatabase.CONNECTIONS, columns,
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                StringBuilder signature = new StringBuilder();
                for (int i = 2; i < columns.length; i++) {
                    String value = cursor.isNull(i) ? "" : cursor.getString(i);
                    if (ConnectidColumns.TAGS.equals(columns[i])) {
                        value = normalizedTags(value);
                    }
                    appendNormalized(signature, value);
                }
                contacts.put(signature.toString(), new ExistingContact(
                        cursor.getLong(0), cursor.isNull(1) ? "" : cursor.getString(1)));
            }
        }
        return contacts;
    }

    private static boolean restoreMissingPhoto(SQLiteDatabase database, Context context,
                                               ExistingContact existingContact,
                                               CsvBackupParser.ContactRecord importedContact,
                                               File importedImages, List<File> createdImages)
            throws IOException {
        String existingName = existingContact.imageName == null
                ? "" : existingContact.imageName.trim();
        boolean hasExistingPhoto = isSafeImageName(existingName)
                && !DEFAULT_IMAGE.equals(existingName)
                && Utilities.getContactImageFile(context, existingName).isFile();
        if (hasExistingPhoto) {
            return false;
        }

        ImageValue restored = resolveImage(context,
                importedContact.get(ConnectidColumns.IMAGE_NAME), importedImages, createdImages);
        if (DEFAULT_IMAGE.equals(restored.name)) {
            return false;
        }
        ContentValues values = new ContentValues();
        values.put(ConnectidColumns.IMAGE_NAME, restored.name);
        database.update(ConnectidDatabase.CONNECTIONS, values,
                ConnectidColumns._ID + " = ?",
                new String[]{String.valueOf(existingContact.id)});
        existingContact.imageName = restored.name;
        return true;
    }

    private static ContentValues contactValues(CsvBackupParser.ContactRecord contact,
                                                String imageName) {
        ContentValues values = new ContentValues();
        values.put(ConnectidColumns.FIRST_NAME,
                contact.get(ConnectidColumns.FIRST_NAME).trim());
        values.put(ConnectidColumns.LAST_NAME, contact.get(ConnectidColumns.LAST_NAME));
        values.put(ConnectidColumns.IMAGE_NAME, imageName);
        values.put(ConnectidColumns.MEET_WHERE, contact.get(ConnectidColumns.MEET_WHERE));
        values.put(ConnectidColumns.APPEARANCE, contact.get(ConnectidColumns.APPEARANCE));
        values.put(ConnectidColumns.FEATURE, contact.get(ConnectidColumns.FEATURE));
        values.put(ConnectidColumns.COMMON_FRIENDS,
                contact.get(ConnectidColumns.COMMON_FRIENDS));
        values.put(ConnectidColumns.DESCRIPTION, contact.get(ConnectidColumns.DESCRIPTION));
        values.put(ConnectidColumns.TAGS, contact.get(ConnectidColumns.TAGS));
        values.put(ConnectidColumns.FLASHCARD_BOX,
                boundedInt(contact.get(ConnectidColumns.FLASHCARD_BOX), 0, 6));
        values.put(ConnectidColumns.FLASHCARD_LAST_REVIEWED,
                nonNegativeLong(contact.get(ConnectidColumns.FLASHCARD_LAST_REVIEWED)));
        values.put(ConnectidColumns.FLASHCARD_NEXT_REVIEW,
                nonNegativeLong(contact.get(ConnectidColumns.FLASHCARD_NEXT_REVIEW)));
        values.put(ConnectidColumns.FLASHCARD_ATTEMPTS,
                nonNegativeInt(contact.get(ConnectidColumns.FLASHCARD_ATTEMPTS)));
        values.put(ConnectidColumns.FLASHCARD_CORRECT,
                nonNegativeInt(contact.get(ConnectidColumns.FLASHCARD_CORRECT)));
        return values;
    }

    private static Map<String, TagRow> loadTags(SQLiteDatabase database) {
        Map<String, TagRow> result = new HashMap<>();
        String[] columns = {TagsColumns._ID, TagsColumns.TAG, TagsColumns.CONNECTION_IDS};
        try (Cursor cursor = database.query(ConnectidDatabase.TAGS, columns,
                null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                long id = cursor.getLong(0);
                String label = cursor.getString(1);
                if (label == null || label.trim().isEmpty()) {
                    continue;
                }
                Set<Long> connectionIds = parseIds(cursor.isNull(2) ? "" : cursor.getString(2));
                result.put(label.trim().toLowerCase(Locale.ROOT),
                        new TagRow(id, connectionIds));
            }
        }
        return result;
    }

    private static void mergeTags(SQLiteDatabase database, Map<String, TagRow> existingTags,
                                  List<String> labels, long connectionId) {
        for (String label : labels) {
            String key = label.toLowerCase(Locale.ROOT);
            TagRow tag = existingTags.get(key);
            if (tag == null) {
                ContentValues values = new ContentValues();
                values.put(TagsColumns.TAG, label);
                values.put(TagsColumns.CONNECTION_IDS, String.valueOf(connectionId));
                long id = database.insertOrThrow(ConnectidDatabase.TAGS, null, values);
                Set<Long> ids = new LinkedHashSet<>();
                ids.add(connectionId);
                existingTags.put(key, new TagRow(id, ids));
            } else if (tag.connectionIds.add(connectionId)) {
                ContentValues values = new ContentValues();
                values.put(TagsColumns.CONNECTION_IDS, joinIds(tag.connectionIds));
                database.update(ConnectidDatabase.TAGS, values,
                        TagsColumns._ID + " = ?", new String[]{String.valueOf(tag.id)});
            }
        }
    }

    private static List<String> splitTags(String serializedTags) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (serializedTags != null) {
            for (String candidate : serializedTags.split(",")) {
                String label = candidate.trim();
                if (!label.isEmpty() && !"null".equalsIgnoreCase(label)) {
                    result.add(label);
                }
            }
        }
        return new ArrayList<>(result);
    }

    private static String normalizedTags(String tags) {
        List<String> labels = splitTags(tags);
        Collections.sort(labels, String.CASE_INSENSITIVE_ORDER);
        return joinStrings(labels);
    }

    private static Set<Long> parseIds(String value) {
        Set<Long> ids = new LinkedHashSet<>();
        for (String candidate : value.split(",")) {
            try {
                ids.add(Long.parseLong(candidate.trim()));
            } catch (NumberFormatException ignored) {
                // Ignore stale values while repairing the tag mapping.
            }
        }
        return ids;
    }

    private static String joinIds(Set<Long> ids) {
        StringBuilder result = new StringBuilder();
        for (Long id : ids) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(id);
        }
        return result.toString();
    }

    private static String joinStrings(List<String> values) {
        StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append(",");
            }
            result.append(value.trim().toLowerCase(Locale.ROOT));
        }
        return result.toString();
    }

    private static void appendNormalized(StringBuilder result, String value) {
        result.append(value == null ? "" : value.trim().toLowerCase(Locale.ROOT));
        result.append('\u001f');
    }

    private static int boundedInt(String value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, nonNegativeInt(value)));
    }

    private static int nonNegativeInt(String value) {
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static long nonNegativeLong(String value) {
        try {
            return Math.max(0L, Long.parseLong(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static ImageValue resolveImage(Context context, String imageName, File importedImages,
                                           List<File> createdImages) throws IOException {
        String cleanName = imageName == null ? "" : imageName.trim();
        if (cleanName.isEmpty() || DEFAULT_IMAGE.equals(cleanName)) {
            return new ImageValue(DEFAULT_IMAGE, false);
        }
        if (!isSafeImageName(cleanName)) {
            return new ImageValue(DEFAULT_IMAGE, true);
        }
        File targetImage = Utilities.getContactImageFile(context, cleanName);
        File sourceImage = importedImages == null ? null : new File(importedImages, cleanName);
        if (!targetImage.isFile() && sourceImage != null && sourceImage.isFile()) {
            copyNewImage(sourceImage, targetImage, createdImages);
        }
        if (!targetImage.isFile()) {
            return new ImageValue(DEFAULT_IMAGE, true);
        }

        String previewName = Utilities.getContactPreviewImageName(cleanName);
        File targetPreview = Utilities.getContactPreviewFile(context, cleanName);
        File sourcePreview = importedImages == null ? null
                : new File(importedImages, previewName);
        if (!targetPreview.isFile() && sourcePreview != null && sourcePreview.isFile()) {
            copyNewImage(sourcePreview, targetPreview, createdImages);
        }
        return new ImageValue(cleanName, false);
    }

    private static boolean isSafeImageName(String imageName) {
        return imageName != null && !imageName.isEmpty()
                && new File(imageName).getName().equals(imageName)
                && !imageName.contains("\\") && imageName.indexOf('\0') < 0;
    }

    private static void copyNewImage(File source, File target, List<File> createdImages)
            throws IOException {
        File parent = target.getParentFile();
        if (parent == null || (!parent.exists() && !parent.mkdirs())) {
            throw new IOException("Unable to prepare contact image storage");
        }
        boolean copied = false;
        try (FileInputStream input = new FileInputStream(source);
             FileOutputStream output = new FileOutputStream(target)) {
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            copied = true;
            createdImages.add(target);
        } finally {
            if (!copied) {
                target.delete();
            }
        }
    }

    private static final class TagRow {
        private final long id;
        private final Set<Long> connectionIds;

        private TagRow(long id, Set<Long> connectionIds) {
            this.id = id;
            this.connectionIds = connectionIds;
        }
    }

    private static final class ImageValue {
        private final String name;
        private final boolean wasMissing;

        private ImageValue(String name, boolean wasMissing) {
            this.name = name;
            this.wasMissing = wasMissing;
        }
    }

    private static final class ExistingContact {
        private final long id;
        private String imageName;

        private ExistingContact(long id, String imageName) {
            this.id = id;
            this.imageName = imageName;
        }
    }

    public static final class ImportResult {
        private final int imported;
        private final int skipped;
        private final int rejected;
        private final int missingPhotos;
        private final int restoredPhotos;

        private ImportResult(int imported, int skipped, int rejected, int missingPhotos,
                             int restoredPhotos) {
            this.imported = imported;
            this.skipped = skipped;
            this.rejected = rejected;
            this.missingPhotos = missingPhotos;
            this.restoredPhotos = restoredPhotos;
        }

        public int getImported() {
            return imported;
        }

        public int getSkipped() {
            return skipped;
        }

        public int getRejected() {
            return rejected;
        }

        public int getMissingPhotos() {
            return missingPhotos;
        }

        public int getRestoredPhotos() {
            return restoredPhotos;
        }
    }
}
