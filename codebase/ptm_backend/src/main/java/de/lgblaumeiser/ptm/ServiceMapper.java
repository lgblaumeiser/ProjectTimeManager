/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import de.lgblaumeiser.ptm.analysis.AnalysisProvider;
import de.lgblaumeiser.ptm.analysis.DataAnalysisService;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.User;
import de.lgblaumeiser.ptm.datamanager.service.BookingService;
import de.lgblaumeiser.ptm.datamanager.service.BookingServiceProvider;
import de.lgblaumeiser.ptm.store.FileStoreProvider;
import de.lgblaumeiser.ptm.store.ObjectStore;
import de.lgblaumeiser.ptm.store.ZipBackupRestore;

/**
 * Small bean that creates and configures the services needed by the Rest
 * interface
 */
@Component
public class ServiceMapper {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private final ObjectStore<Activity> activityStore;
	private final ObjectStore<Booking> bookingStore;
	private final ObjectStore<User> userStore;

	private final BookingService bookingService;

	private final ZipBackupRestore backupService;

	private final DataAnalysisService analysisService;

	private final PasswordEncoder passwordEncoder;

	public ServiceMapper() {
		FileStoreProvider storageProvider = new FileStoreProvider();
		userStore = storageProvider.getUserFileStore();
		activityStore = storageProvider.getActivityFileStore();
		bookingStore = storageProvider.getBookingFileStore();
		bookingService = new BookingServiceProvider().getBookingService(bookingStore);
		backupService = storageProvider.getZipBackupRestore();
		analysisService = new AnalysisProvider().getAnalysisService(activityStore, bookingStore);
		passwordEncoder = new BCryptPasswordEncoder();
		logger.info("PTM services initialized");
	}

	public ObjectStore<User> userStore() {
		return userStore;
	}

	public ObjectStore<Activity> activityStore() {
		return activityStore;
	}

	public ObjectStore<Booking> bookingStore() {
		return bookingStore;
	}

	public BookingService bookingService() {
		return bookingService;
	}

	public ZipBackupRestore backupService() {
		return backupService;
	}

	public DataAnalysisService analysisService() {
		return analysisService;
	}

	public PasswordEncoder passwordEncodingService() {
		return passwordEncoder;
	}
}
