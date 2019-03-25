/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.File;
import java.util.Collection;

class TestFilesystemAbstraction implements FilesystemAbstraction {
    private File storageFile;
    private String storageContent;

    @Override
    public void storeToFile(final File target, final String content) {
        storageFile = target;
        storageContent = content;
    }

    @Override
    public String retrieveFromFile(final File source) {
        if (!source.equals(storageFile)) {
            throw new IllegalStateException();
        }
        if (storageContent == null) {
            throw new IllegalStateException();
        }
        return storageContent;
    }

    @Override
    public boolean dataAvailable(final File source) {
        return true;
    }

    @Override
    public Collection<File> getAllFiles(final File folder, final String extension) {
        return storageFile != null ? asList(storageFile) : emptyList();
    }

    @Override
    public void deleteFile(final File target) {
        if (!target.equals(storageFile)) {
            throw new IllegalStateException();
        }
        if (storageContent == null) {
            throw new IllegalStateException();
        }
        storageContent = null;
        storageFile = null;
    }

    @Override
    public boolean folderAvailable(final File store, final boolean createIfNot) {
        return true;
    }

    public File getStorageFile() {
        return storageFile;
    }

    public String getStorageContent() {
        return storageContent;
    }
}
