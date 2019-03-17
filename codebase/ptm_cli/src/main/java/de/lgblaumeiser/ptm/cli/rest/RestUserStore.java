/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.rest;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.lgblaumeiser.ptm.datamanager.model.User;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * Store that uses the rest utils to access the server, i.e., a proxy
 * implementation of Object Store
 */
public class RestUserStore extends RestBaseService implements ObjectStore<User> {
	@Override
	public Collection<User> retrieveAll() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<User> retrieveById(final Long id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public User store(final User user) {
		try {
			Map<String, String> bodyData = new HashMap<>();
			bodyData.put("username", user.getUsername());
			bodyData.put("password", user.getPassword());
			bodyData.put("email", user.getEmail());
			bodyData.put("question", user.getQuestion());
			bodyData.put("answer", user.getAnswer());
			String apiName = "/users/register";
			Long id = getRestUtils().post(apiName, Optional.empty(), bodyData);
			Field idField = user.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(user, id);
			idField.setAccessible(false);
			return user;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public void storeChangedPassword(final String password) {
		Map<String, String> bodyData = new HashMap<>();
		bodyData.put("password", password);
		String apiName = "/users/name";
		getRestUtils().post(apiName, Optional.empty(), bodyData);
	}

	@Override
	public void deleteById(final Long id) {
		throw new UnsupportedOperationException();
	}

	public void deleteCurrentUser() {
		String apiName = "/users/name";
		getRestUtils().delete(apiName, Optional.of(getServices().getCurrentUserStore().loadUserData()));
	}

	public Optional<User> getCurrentUser() {
		String apiName = "/users/name";
		return Optional.ofNullable(getRestUtils().get(apiName,
				Optional.of(getServices().getCurrentUserStore().loadUserData()), User.class));
	}
}
