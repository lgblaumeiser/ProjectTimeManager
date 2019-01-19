/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store.filesystem;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ZipBackupRestore;

/**
 * Test class for StoreBackupRestore in implementation of ZipBackupRestore
 */
public class ZipBackupRestoreTest {
	private static final String FILENAME1 = "1.activity";
	private static final String CONTENT1 = "Activity1_content";
	private static final String FILENAME2 = "1.booking";
	private static final String CONTENT2 = "Booking1_content";
	private static final String FILENAME3 = "2.booking";
	private static final String CONTENT3 = "Booking2_content";

	private ZipBackupRestore testee;
	private FilesystemAbstraction fileact;
	private File tempfolder;
	private File file1;
	private File file2;
	private File file3;

	@Before
	public void setUp() throws IOException {
		createTestFiles();
		fileact = new FilesystemAbstractionImpl();
		FileStore<Activity> actStore = new FileStore<Activity>(fileact) {
			@Override
			protected Class<Activity> getType() {
				return Activity.class;
			}
		};
		FileStore<Booking> bookStore = new FileStore<Booking>(fileact) {
			@Override
			protected Class<Booking> getType() {
				return Booking.class;
			}
		};
		testee = new ZipBackupRestore(actStore, bookStore);
	}

	private void createTestFiles() throws IOException {
		tempfolder = Files.createTempDirectory("ptm").toFile();
		System.setProperty("ptm.filestore", tempfolder.getAbsolutePath());
		file1 = new File(tempfolder, FILENAME1);
		file2 = new File(tempfolder, FILENAME2);
		file3 = new File(tempfolder, FILENAME3);
		FileUtils.write(file1, CONTENT1, "UTF-8");
		FileUtils.write(file2, CONTENT2, "UTF-8");
		FileUtils.write(file3, CONTENT3, "UTF-8");
	}

	@After
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(tempfolder);
	}

	@Test
	public void testBackupRestore() throws IOException, InterruptedException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		testee.backup(output);
		ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
		testee.emptyDatabase();
		assertEquals(0, fileact.getAllFiles(tempfolder, "activity").size());
		assertEquals(0, fileact.getAllFiles(tempfolder, "booking").size());
		testee.restore(input);
		assertEquals(1, fileact.getAllFiles(tempfolder, "activity").size());
		assertEquals(2, fileact.getAllFiles(tempfolder, "booking").size());
		assertEquals(CONTENT1, fileact.retrieveFromFile(file1));
		assertEquals(CONTENT2, fileact.retrieveFromFile(file2));
		assertEquals(CONTENT3, fileact.retrieveFromFile(file3));
	}
}
