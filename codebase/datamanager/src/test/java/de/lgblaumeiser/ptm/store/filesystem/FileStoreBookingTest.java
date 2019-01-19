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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Booking;

public class FileStoreBookingTest {
	private static final String TESTUSER = "TestUser";
	private static final Long TESTACTID = 105L;
	private static final LocalDate TESTDATE = LocalDate.of(2018, 9, 3);
	private static final LocalTime TESTSTARTTIME = LocalTime.of(8, 15);
	private static final LocalTime TESTENDTIME = LocalTime.of(17, 00);
	private static final String TESTCOMMENT = "TestComment";

	private FileStore<Booking> testee;

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
		testee = new FileStore<Booking>(stubAccess) {
			@Override
			protected Class<Booking> getType() {
				return Booking.class;
			}
		};
	}

	private static final Booking testData = Booking.newBooking().setActivity(TESTACTID).setBookingday(TESTDATE)
			.setComment(TESTCOMMENT).setEndtime(TESTENDTIME).setStarttime(TESTSTARTTIME).setUser(TESTUSER).build();

	@Test
	public void testStore() {
		testee.store(testData);
		assertEquals("booking", getExtension(storageFile.getName()));
		assertTrue(storageContent.contains("comment"));
		assertTrue(storageContent.contains(TESTCOMMENT));
		assertTrue(storageContent.contains("activity"));
		assertTrue(storageContent.contains(TESTACTID.toString()));
		assertTrue(storageContent.contains("id"));
	}

	@Test
	public void testRetrieveAll() {
		testee.store(testData);
		Collection<Booking> foundObjs = testee.retrieveAll();
		assertEquals(1, foundObjs.size());
		Booking foundObj = getOnlyFromCollection(foundObjs);
		assertEquals(testData, foundObj);
	}

	@Test
	public void testRetrieveById() {
		Booking returnedObject = testee.store(testData);
		Long id = returnedObject.getId();
		Booking foundObj = testee.retrieveById(id).get();
		assertEquals(testData, foundObj);
	}

	@Test
	public void testDeleteById() {
		Booking returnedObject = testee.store(testData);
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
