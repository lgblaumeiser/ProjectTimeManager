/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine;

import static java.nio.file.Files.createFile;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.readAllLines;
import static java.nio.file.Files.write;
import static java.nio.file.attribute.PosixFilePermissions.asFileAttribute;
import static java.nio.file.attribute.PosixFilePermissions.fromString;
import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.lgblaumeiser.ptm.datamanager.model.User;

/**
 * Store and retrieve user information
 */
public class UserStore {
	private final Path storageFile;

	public User loadUserData() {
		List<String> lines;
		try {
			lines = readAllLines(storageFile);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		String username = null;
		String password = null;
		for (String line : lines) {
			String decodedLine = new String(decodeBase64(line.getBytes()));
			String[] property = decodedLine.split("=");
			if (property.length != 2) {
				throw new IllegalStateException();
			}
			if (property[0].equals("user.name")) {
				username = property[1];
			} else if (property[0].equals("user.password")) {
				password = property[1];
			}
		}
		return User.newUser().setUsername(username).setPassword(password).build();
	}

	public void storeUserData(final User user) {
		List<String> lines = new ArrayList<>();
		lines.add(encodeBase64String(("user.name=" + user.getUsername()).getBytes()));
		lines.add(encodeBase64String(("user.password=" + user.getPassword()).getBytes()));
		try {
			if (!exists(storageFile)) {
				createFile(storageFile, asFileAttribute(fromString("rw-------")));
			}
			write(storageFile, lines);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public UserStore(final String folder) {
		storageFile = Paths.get(folder, "user.enc");
	}
}
