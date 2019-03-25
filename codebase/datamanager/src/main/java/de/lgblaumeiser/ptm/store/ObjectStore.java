/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store;

import java.util.Collection;
import java.util.Optional;

/**
 * A general interface for storing objects
 */
public interface ObjectStore<T> {
    /**
     * Retrieve all objects stored in the store
     * 
     * @return All objects found in the store
     */
    Collection<T> retrieveAll();

    /**
     * Retrieve object by the id
     * 
     * @param id Id of the searched object
     * @return The object with the given id as potentially empty optional
     */
    Optional<T> retrieveById(Long id);

    /**
     * Store an object in the store
     *
     * @param object The Object to store
     * @return The stored object
     */
    T store(T object);

    /**
     * Delete an object identified by its id
     * 
     * @param id The id of the object to be deleted
     */
    void deleteById(Long id);
}
