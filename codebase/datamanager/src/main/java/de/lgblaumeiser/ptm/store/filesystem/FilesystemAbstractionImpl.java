/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static java.nio.charset.Charset.defaultCharset;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.moveFile;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.write;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Implementation of the real filesystem access
 */
public class FilesystemAbstractionImpl implements FilesystemAbstraction {
    private static final String TEMP_FILE_ENDING = ".tmpstore";

    @Override
    public Collection<File> getAllFiles(final File folder, final String extension) {
        return listFiles(folder, new String[] { extension }, false);
    }

    @Override
    public String retrieveFromFile(final File source) {
        assertState(source != null);
        try {
            return readFileToString(source, defaultCharset());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void storeToFile(final File target, final String content) {
        assertState(target != null);
        assertState(content != null);
        File tmpFile = new File(target.getParentFile(), target.getName() + TEMP_FILE_ENDING);
        try {
            write(tmpFile, content, defaultCharset());
            if (target.exists()) {
                forceDelete(target);
            }
            moveFile(tmpFile, target);
            deleteTmpFiles(target.getParentFile());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void deleteTmpFiles(final File target) throws IOException {
        for (File currentTmpFile : getAllTmpFiles(target)) {
            forceDelete(currentTmpFile);
        }
    }

    private Collection<File> getAllTmpFiles(final File folder) {
        return listFiles(folder, new String[] { TEMP_FILE_ENDING }, true);
    }

    @Override
    public void deleteFile(final File target) {
        try {
            forceDelete(target);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean dataAvailable(final File source) {
        assertState(source != null);
        return source.exists() && source.isFile();
    }

    @Override
    public boolean folderAvailable(final File folder, final boolean createIfNot) {
        assertState(folder != null);
        if (folder.isDirectory() && folder.exists()) {
            return true;
        }
        if (createIfNot) {
            return folder.mkdir();
        }
        return false;
    }
}
