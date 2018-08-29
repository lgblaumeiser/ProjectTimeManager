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

public class FileStoreTest {
	private static final String TESTINDEX = "TestIndex";
	private static final String TESTCONTENT = "TestContent";

	private FileStore<TestStoreObject> testee;

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
		testee = new FileStore<TestStoreObject>(stubAccess) {
			@Override
			protected Class<TestStoreObject> getType() {
				return TestStoreObject.class;
			}
		};
	}

	private static final TestStoreObject testData = new TestStoreObject(TESTINDEX, TESTCONTENT);

	@Test
	public void testStore() {
		testee.store(testData);
		assertEquals("teststoreobject", getExtension(storageFile.getName()));
		assertTrue(storageContent.contains("index"));
		assertTrue(storageContent.contains(TESTINDEX));
		assertTrue(storageContent.contains("data"));
		assertTrue(storageContent.contains(TESTCONTENT));
		assertTrue(storageContent.contains("id"));
	}

	@Test
	public void testRetrieveAll() {
		testee.store(testData);
		Collection<TestStoreObject> foundObjs = testee.retrieveAll();
		assertEquals(1, foundObjs.size());
		TestStoreObject foundObj = getOnlyFromCollection(foundObjs);
		assertEquals(TESTINDEX, foundObj.getIndex());
		assertEquals(TESTCONTENT, foundObj.getData());
	}

	@Test
	public void testRetrieveById() {
		TestStoreObject returnedObject = testee.store(testData);
		Long id = returnedObject.getId();
		TestStoreObject foundObj = testee.retrieveById(id).get();
		assertEquals(TESTINDEX, foundObj.getIndex());
		assertEquals(TESTCONTENT, foundObj.getData());
	}

	@Test
	public void testDeleteById() {
		TestStoreObject returnedObject = testee.store(testData);
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

class TestStoreObject {
	private String index;
	private String data;
	private Long id = Long.valueOf(-1);

	TestStoreObject() {
		// For json serialization purposes
	}

	TestStoreObject(final String index, final String data) {
		this.index = index;
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public Long getId() {
		return id;
	}

	public String getIndex() {
		return index;
	}
}