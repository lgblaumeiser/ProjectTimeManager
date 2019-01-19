/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static java.lang.System.getProperty;
import static java.lang.System.setProperty;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import de.lgblaumeiser.ptm.analysis.AnalysisProvider;
import de.lgblaumeiser.ptm.analysis.DataAnalysisService;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
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
	private final BookingService bookingService;

	private final ZipBackupRestore backupService;

	private final DataAnalysisService analysisService;

	public ServiceMapper() {
		setProperty("filestore.folder", new File(getProperty("user.home"), ".ptm").getAbsolutePath());
		FileStoreProvider storageProvider = new FileStoreProvider();
		activityStore = storageProvider.getActivityFileStore();
		bookingStore = storageProvider.getBookingFileStore();
		bookingService = new BookingServiceProvider().getBookingService(bookingStore);
		backupService = storageProvider.getZipBackupRestore();
		analysisService = new AnalysisProvider().getAnalysisService(activityStore, bookingStore);
		logger.info("PTM services initialized");
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
}
