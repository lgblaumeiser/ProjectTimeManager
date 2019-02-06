/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.lgblaumeiser.ptm.datamanager.model.User;

/**
 * Store and retrieve user information
 */
public class UserStore {
	private final Path storageFile;
	
	public User loadUserData() {
		Map<String, String> keyValueMap = new HashMap<>();
		List<String> lines;
		try {
			lines = Files.readAllLines(storageFile);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		for (String line : lines) {
			String[] property = line.split("=");
			if (property.length != 2) {
				throw new IllegalStateException();
			}
			keyValueMap.put(property[0], property[1]);
		}
		return User.newUser().setUsername(keyValueMap.get("user.name")).setPassword(keyValueMap.get("user.password")).build();
	}

	public void storeUserData(User user) {
		List<String> lines = new ArrayList<>();
		lines.add("user.name=" + user.getUsername());
		lines.add("user.password=" + user.getPassword());
		try {
			Files.write(storageFile, lines);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public UserStore() {
		storageFile = Paths.get(System.getProperty("user.home"), ".ptm", "user.enc");
	}
	
	public UserStore(String folder) {
		storageFile = Paths.get(folder, "user.enc");
	}
}
