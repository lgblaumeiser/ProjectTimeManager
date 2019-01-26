/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;
import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.util.Objects.hash;

/**
 * Data structure for representation of an activity. An activity represents a
 * booking number under which work is done. Each work booking is assigned to an
 * activity and there are reports on hours spent on activities.
 */
public class User {
	public static class UserBuilder {
		private Long id = valueOf(-1);
		private String username;
		private String password;

		private UserBuilder(final User user) {
			id = user.getId();
			username = user.getUsername();
			password = user.getPassword();
		}

		private UserBuilder() {
			// Nothing to do
		}

		public UserBuilder setUsername(final String username) {
			this.username = username;
			return this;
		}

		public UserBuilder setPassword(final String password) {
			this.password = password;
			return this;
		}

		/**
		 * @return An unmodifiable activity representing the data given to the builder,
		 *         Non null, returns with exception if the data is invalid
		 */
		public User build() {
			checkData();
			return new User(id, username, password);
		}

		private void checkData() {
			assertState(stringHasContent(username));
			assertState(stringHasContent(password));
		}
	}

	/**
	 * Creates a new activity builder with no data set.
	 *
	 * @return A new activity builder, never null
	 */
	public static UserBuilder newUser() {
		return new UserBuilder();
	}

	/**
	 * Change an existing activity by providing a builder preset with the activity
	 * data
	 *
	 * @return A new activity builder, never null
	 */
	public UserBuilder changeUser() {
		return new UserBuilder(this);
	}

	private String username;
	private String password;
	private Long id = valueOf(-1);

	/**
	 * @return The internal id of the activity. Automatically created by storage
	 *         system
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return Name of the activity. Non null
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return Booking number of the activities category. Non null
	 */
	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return format("User: Username: %s, Password: %s, Id: %d", username, password, id);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof User) {
			User user = (User) obj;
			return id == user.id && username.equals(user.username) && password.equals(user.password);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hash(id, username, password);
	}

	private User(final Long id, final String username, final String password) {
		this.id = id;
		this.username = username;
		this.password = password;
	}

	private User() {
		// Only needed for deserialization
	}
}
