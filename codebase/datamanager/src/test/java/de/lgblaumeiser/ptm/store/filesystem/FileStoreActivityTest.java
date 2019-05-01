/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import static de.lgblaumeiser.ptm.util.Utils.getOnlyFromCollection;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;

public class FileStoreActivityTest {
    private static final String TESTPNAME = "TestProjectName";
    private static final String TESTNAME = "TestName";
    private static final String TESTINDEX = "TestIndex";
    private static final String TESTSUB = "TestSubIndex";
    private static final String USERNAME = "UserX";

    private FileStore<Activity> testee;

    private final TestFilesystemAbstraction stubAccess = new TestFilesystemAbstraction();

    @Before
    public void setUp() {
        testee = new FileStore<Activity>(stubAccess, () -> Activity.class);
    }

    private static final Activity testData = Activity
            .newActivity()
            .setProjectName(TESTPNAME)
            .setActivityName(TESTNAME)
            .setProjectId(TESTINDEX)
            .setActivityId(TESTSUB)
            .setUser(USERNAME)
            .build();

    @Test
    public void testStore() {
        testee.store(testData);
        assertEquals("activity", getExtension(stubAccess.getStorageFile().getName()));
        assertTrue(stubAccess.getStorageContent().contains("projectName"));
        assertTrue(stubAccess.getStorageContent().contains(TESTPNAME));
        assertTrue(stubAccess.getStorageContent().contains("activityName"));
        assertTrue(stubAccess.getStorageContent().contains(TESTNAME));
        assertTrue(stubAccess.getStorageContent().contains("projectId"));
        assertTrue(stubAccess.getStorageContent().contains(TESTINDEX));
        assertTrue(stubAccess.getStorageContent().contains("activityId"));
        assertTrue(stubAccess.getStorageContent().contains(TESTSUB));
        assertTrue(stubAccess.getStorageContent().contains("id"));
        Long id = testData.getId();
        testee.store(testData.changeActivity().setHidden(false).build());
        assertEquals(id, testData.getId());
    }

    @Test
    public void testRetrieveAll() {
        testee.store(testData);
        Collection<Activity> foundObjs = testee.retrieveAll();
        assertEquals(1, foundObjs.size());
        Activity foundObj = getOnlyFromCollection(foundObjs);
        assertEquals(TESTPNAME, foundObj.getProjectName());
        assertEquals(TESTNAME, foundObj.getActivityName());
        assertEquals(TESTINDEX, foundObj.getProjectId());
        assertEquals(TESTSUB, foundObj.getActivityId());
    }

    @Test
    public void testRetrieveById() {
        Activity returnedObject = testee.store(testData);
        Long id = returnedObject.getId();
        Activity foundObj = testee.retrieveById(id).get();
        assertEquals(TESTPNAME, foundObj.getProjectName());
        assertEquals(TESTNAME, foundObj.getActivityName());
        assertEquals(TESTINDEX, foundObj.getProjectId());
        assertEquals(TESTSUB, foundObj.getActivityId());
    }

    @Test
    public void testDeleteById() {
        Activity returnedObject = testee.store(testData);
        Long id = returnedObject.getId();
        assertNotNull(stubAccess.getStorageContent());
        assertNotNull(stubAccess.getStorageFile());
        testee.deleteById(id);
        assertNull(stubAccess.getStorageContent());
        assertNull(stubAccess.getStorageFile());
    }

    @Test
    public void testStorepathByEnv() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        System.setProperty("ptm.filestore", "somedummystring");
        Class<?> myClass = testee.getClass();
        Method method = myClass.getDeclaredMethod("getStore");
        method.setAccessible(true);
        assertEquals(new File("somedummystring"), method.invoke(testee));
    }
}
