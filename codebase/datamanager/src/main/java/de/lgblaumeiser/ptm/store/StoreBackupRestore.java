/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store;

import java.util.Map;

/**
 * A backup service for the ObjectStore
 */
public interface StoreBackupRestore<T> {
    /**
     * Make a backup of all existing objects of the type. Returns all file contents
     * as a Map of key filename to file content
     * 
     * @return A map of filename to file contents
     */
    Map<String, String> backup();

    /**
     * Restore the objects from the map, works only if no objects of the type exist
     * 
     * @param filenameToContentMap A map of content indexed by the filename of the
     *                             content
     */
    void restore(Map<String, String> filenameToContentMap);

    /**
     * Deletes all objects of the type
     */
    void delete();
}
