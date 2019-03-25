/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.io.FilenameUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.lgblaumeiser.ptm.store.ObjectStore;
import de.lgblaumeiser.ptm.store.StoreBackupRestore;

/**
 * A file base store for random objects
 */
public class FileStore<T> implements ObjectStore<T>, StoreBackupRestore<T> {
    private static final String ID = "id";

    private final ObjectMapper jsonUtil = new ObjectMapper();
    private final FilesystemAbstraction filesystemAccess;

    private final Supplier<Class<T>> typeFunction;

    public FileStore(final FilesystemAbstraction filesystemAccess, final Supplier<Class<T>> typeFunction) {
        jsonUtil.registerModule(new JavaTimeModule());
        this.typeFunction = typeFunction;
        this.filesystemAccess = filesystemAccess;
    }

    @Override
    public Collection<T> retrieveAll() {
        return getAllFiles()
                .stream()
                .map(f -> extractFileContent(filesystemAccess.retrieveFromFile(f)))
                .collect(toList());
    }

    @Override
    public Optional<T> retrieveById(final Long id) {
        assertState(id != null);
        File searchedFile = getFileInformation(id);
        if (!filesystemAccess.dataAvailable(searchedFile)) {
            return Optional.empty();
        }
        return Optional.of(extractFileContent(filesystemAccess.retrieveFromFile(searchedFile)));
    }

    @Override
    public T store(final T object) {
        assertState(object != null);
        Long index = getIndexObject(object);
        File targetFile = getFileInformation(index);
        String content = createFileContent(object);
        filesystemAccess.storeToFile(targetFile, content);
        return object;
    }

    @Override
    public void deleteById(final Long id) {
        assertState(id != null);
        File deleteFile = getFileInformation(id);
        assertState(filesystemAccess.dataAvailable(deleteFile));
        filesystemAccess.deleteFile(deleteFile);
    }

    @Override
    public Map<String, String> backup() {
        Map<String, String> backupResult = new HashMap<>();
        getAllFiles()
                .stream()
                .forEach(f -> backupResult.put(f.getName(), filesystemAccess.retrieveFromFile(f)));
        return backupResult;
    }

    @Override
    public void restore(final Map<String, String> filenameToContentMap) {
        assertState(getAllFiles().size() == 0);
        filenameToContentMap
                .keySet()
                .stream()
                .forEach(k -> filesystemAccess.storeToFile(new File(getStore(), k), filenameToContentMap.get(k)));
    }

    @Override
    public void delete() {
        getAllFiles()
                .stream()
                .forEach(f -> filesystemAccess.deleteFile(f));
    }

    private T extractFileContent(final String content) {
        try {
            return jsonUtil.readValue(content, typeFunction.get());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private String createFileContent(final T object) {
        try {
            return jsonUtil.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private File getFileInformation(final Long index) {
        File store = getStore();
        return new File(store, index.toString() + "." + getExtension());
    }

    private Collection<File> getAllFiles() {
        File store = getStore();
        return filesystemAccess.getAllFiles(store, getExtension());
    }

    private String getExtension() {
        return typeFunction.get().getSimpleName().toLowerCase();
    }

    private File getStore() {
        String storepath = System.getProperty("ptm.filestore");
        File applicationPath = stringHasContent(storepath) ? new File(storepath) : getDefaultStore();
        assertState(filesystemAccess.folderAvailable(applicationPath, true));
        return applicationPath;
    }

    private File getDefaultStore() {
        File homepath = new File(System.getProperty("user.home"));
        assertState(filesystemAccess.folderAvailable(homepath, false));
        return new File(homepath, ".ptm");
    }

    private Long getIndexObject(final T object) {
        assertState(object != null);
        try {
            Field f = object.getClass().getDeclaredField(ID);
            f.setAccessible(true);
            Long returnVal = (Long) f.get(object);
            if (returnVal == -1) {
                f.set(object, getNextId());
            }
            returnVal = (Long) f.get(object);
            f.setAccessible(false);
            return returnVal;
        } catch (IllegalAccessException | IllegalArgumentException | ClassCastException | NoSuchFieldException
                | SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private Long getNextId() {
        Optional<String> lastId = getAllFiles()
                .stream()
                .map(f -> FilenameUtils.removeExtension(f.getName()))
                .max((n1, n2) -> Long.compare(Long.valueOf(n1), Long.valueOf(n2)));
        return lastId
                .map(s -> Long.parseLong(lastId.get()) + 1)
                .orElse(1L);
    }
}
