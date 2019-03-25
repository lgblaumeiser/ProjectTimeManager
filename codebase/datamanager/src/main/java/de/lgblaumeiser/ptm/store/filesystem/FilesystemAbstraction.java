/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * Interface to hide the concrete filesystem access implementation. Improved
 * testability for the business functionality
 */
public interface FilesystemAbstraction {
    /**
     * @param The       folder in which files are searched
     * @param extension The extension of the files searched
     * @return A list of files with the given extension stored in the given folder
     */
    Collection<File> getAllFiles(File folder, String extension);

    /**
     * @param source File to read
     * @return Content of file as string, might be empty
     * @throws IOException If access fails
     */
    String retrieveFromFile(File source);

    /**
     * @param target  File reference in which to store content
     * @param content Content to store
     * @throws IOException If access fails
     */
    void storeToFile(File target, String content);

    /**
     * @param target The file to delete
     */
    void deleteFile(File target);

    /**
     * @param source File to check whether it exists
     * @return True, if file really exists
     */
    boolean dataAvailable(File source);

    /**
     * @param store       Store folder to check whether it exists
     * @param createIfNot Create the folder if it does not
     * @return True, if the folder exists now
     */
    boolean folderAvailable(File store, boolean createIfNot);
}