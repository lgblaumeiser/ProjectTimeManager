/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine;

import static org.apache.commons.io.FileUtils.forceDelete;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import de.lgblaumeiser.ptm.cli.engine.UserStore.UserInfo;

public class UserStoreTest {
	private static final String USERNAME = "MyTestUser";
	private static final String PASSWORD = "DummyPwd";
	private static final UserStore.UserInfo TESTUSER = new UserInfo(USERNAME, PASSWORD);

	private UserStore testee;

	@Test
	public void testStoreAndRetrieveUser() throws IOException {
		Path tempFolder = Files.createTempDirectory("ptm");
		testee = new UserStore(tempFolder.toString());
		try {
			testee.storeUserData(TESTUSER);
			UserStore.UserInfo storedUser = testee.loadUserData();
			assertEquals(USERNAME, storedUser.getUsername());
			assertEquals(PASSWORD, storedUser.getPassword());
		} finally {
			forceDelete(tempFolder.toFile());
		}
		assertFalse(Files.exists(tempFolder));
	}

}
