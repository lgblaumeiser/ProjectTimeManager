/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.store;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.User;
import de.lgblaumeiser.ptm.store.filesystem.FileStore;
import de.lgblaumeiser.ptm.store.filesystem.FilesystemAbstraction;
import de.lgblaumeiser.ptm.store.filesystem.FilesystemAbstractionImpl;

/**
 * A provider for a FileStore instance for a dedicated model class
 */
public class FileStoreProvider {
	private FilesystemAbstraction filesystemAbstraction = new FilesystemAbstractionImpl();
	private FileStore<Activity> activityFileStore = new FileStore<Activity>(filesystemAbstraction) {
		@Override
		protected Class<Activity> getType() {
			return Activity.class;
		}
	};
	private FileStore<Booking> bookingFileStore = new FileStore<Booking>(filesystemAbstraction) {
		@Override
		protected Class<Booking> getType() {
			return Booking.class;
		}
	};
	private FileStore<User> userFileStore = new FileStore<User>(filesystemAbstraction) {
		@Override
		protected Class<User> getType() {
			return User.class;
		}
	};

	public FileStore<Activity> getActivityFileStore() {
		return activityFileStore;
	}

	public FileStore<Booking> getBookingFileStore() {
		return bookingFileStore;
	}

	public FileStore<User> getUserFileStore() {
		return userFileStore;
	}

	public ZipBackupRestore getZipBackupRestore() {
		return new ZipBackupRestore(activityFileStore, bookingFileStore, userFileStore);
	}
}
