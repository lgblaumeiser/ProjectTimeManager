/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import static de.lgblaumeiser.ptm.util.Utils.getOnlyFromCollection;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;

public class FileStoreActivityTest {
	private static final String TESTNAME = "TestName";
	private static final String TESTINDEX = "TestIndex";

	private FileStore<Activity> testee;

	private final FilesystemAbstraction stubAccess = new FilesystemAbstraction() {
		@Override
		public void storeToFile(final File target, final String content) {
			storageFile = target;
			storageContent = content;
		}

		@Override
		public String retrieveFromFile(final File source) throws IOException {
			if (!source.equals(storageFile)) {
				throw new IOException();
			}
			if (storageContent == null) {
				throw new IOException();
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
		public void deleteFile(final File target) throws IOException {
			if (!target.equals(storageFile)) {
				throw new IOException();
			}
			if (storageContent == null) {
				throw new IOException();
			}
			storageContent = null;
			storageFile = null;
		}

		@Override
		public boolean folderAvailable(final File store, final boolean createIfNot) {
			return true;
		}
	};

	private File storageFile;
	private String storageContent;

	@Before
	public void setUp() {
		testee = new FileStore<Activity>(stubAccess) {
			@Override
			protected Class<Activity> getType() {
				return Activity.class;
			}
		};
	}

	private static final Activity testData = Activity.newActivity().setActivityName(TESTNAME)
			.setBookingNumber(TESTINDEX).setUser(1L).build();

	@Test
	public void testStore() {
		testee.store(testData);
		assertEquals("activity", getExtension(storageFile.getName()));
		assertTrue(storageContent.contains("activityName"));
		assertTrue(storageContent.contains(TESTNAME));
		assertTrue(storageContent.contains("bookingNumber"));
		assertTrue(storageContent.contains(TESTINDEX));
		assertTrue(storageContent.contains("id"));
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
		assertEquals(TESTNAME, foundObj.getActivityName());
		assertEquals(TESTINDEX, foundObj.getBookingNumber());
	}

	@Test
	public void testRetrieveById() {
		Activity returnedObject = testee.store(testData);
		Long id = returnedObject.getId();
		Activity foundObj = testee.retrieveById(id).get();
		assertEquals(TESTNAME, foundObj.getActivityName());
		assertEquals(TESTINDEX, foundObj.getBookingNumber());
	}

	@Test
	public void testDeleteById() {
		Activity returnedObject = testee.store(testData);
		Long id = returnedObject.getId();
		assertNotNull(storageContent);
		assertNotNull(storageFile);
		testee.deleteById(id);
		assertNull(storageContent);
		assertNull(storageFile);
	}

	@Test
	public void testStorepathByEnv() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		System.setProperty("ptm.filestore", "somedummystring");
		Class<?> myClass = testee.getClass().getSuperclass();
		Method method = myClass.getDeclaredMethod("getStore");
		method.setAccessible(true);
		assertEquals(new File("somedummystring"), (File) method.invoke(testee));
	}
}
