/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store;

import static java.nio.charset.Charset.defaultCharset;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.User;

/**
 * A implementation of a backup/restore mechanism using StoreBackupRestore. The
 * implementation is using zip streams for input and output.
 */
public class ZipBackupRestore {
    private final StoreBackupRestore<Activity> activityStorage;
    private final StoreBackupRestore<Booking> bookingStorage;
    private final StoreBackupRestore<User> userStorage;

    /**
     * Make a backup to the provided output stream as a zip file content
     * 
     * @param outputStream The output stream to which the backup is provided
     */
    public void backup(final OutputStream outputStream) {
        try (ZipOutputStream zipstream = new ZipOutputStream(outputStream)) {
            Map<String, String> filemap = new HashMap<>();
            filemap.putAll(activityStorage.backup());
            filemap.putAll(bookingStorage.backup());
            filemap.putAll(userStorage.backup());
            filemap.keySet().stream().forEach(k -> createZipEntry(zipstream, k, filemap.get(k)));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void createZipEntry(final ZipOutputStream zipstream, final String name, final String content) {
        try {
            zipstream.putNextEntry(new ZipEntry(name));
            zipstream.write(content.getBytes(defaultCharset()));
            zipstream.closeEntry();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Restore from a backup provided as a zip file content
     * 
     * @param inputStream The input stream in which the zipped backup is provided
     */
    public void restore(final InputStream inputStream) {
        Map<String, String> activityMap = new HashMap<>();
        Map<String, String> bookingMap = new HashMap<>();
        Map<String, String> userMap = new HashMap<>();

        try (ZipInputStream zipstream = new ZipInputStream(inputStream)) {
            ZipEntry currentEntry;
            while ((currentEntry = zipstream.getNextEntry()) != null) {
                String key = currentEntry.getName();
                String data = extractFromZip(zipstream);
                zipstream.closeEntry();
                if (key.endsWith("activity")) {
                    activityMap.put(key, data);
                } else if (key.endsWith("booking")) {
                    bookingMap.put(key, data);
                } else if (key.endsWith("user")) {
                    userMap.put(key, data);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        activityStorage.restore(activityMap);
        bookingStorage.restore(bookingMap);
        userStorage.restore(userMap);
    }

    private String extractFromZip(final ZipInputStream inputStream) {
        try (ByteArrayOutputStream stringOutputSink = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = inputStream.read(buffer, 0, buffer.length)) > 0) {
                stringOutputSink.write(buffer, 0, read);
            }
            return stringOutputSink.toString(defaultCharset().name());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Empty the database to prepare e.g. restoring of a backup
     */
    public void emptyDatabase() {
        activityStorage.delete();
        bookingStorage.delete();
        userStorage.delete();
    }

    /**
     * Standard constructor used to provide dependencies
     * 
     * @param activityStorage
     * @param bookingStorage
     * @param userStorage
     */
    public ZipBackupRestore(final StoreBackupRestore<Activity> activityStorage,
            final StoreBackupRestore<Booking> bookingStorage, final StoreBackupRestore<User> userStorage) {
        this.activityStorage = activityStorage;
        this.bookingStorage = bookingStorage;
        this.userStorage = userStorage;
    }
}
