/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine;

import de.lgblaumeiser.ptm.cli.rest.RestAnalysisService;
import de.lgblaumeiser.ptm.cli.rest.RestBookingStore;
import de.lgblaumeiser.ptm.cli.rest.RestInfrastructureServices;
import de.lgblaumeiser.ptm.cli.rest.RestUserStore;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * Small class that allows to access the services needed by the command handler
 * implementations.
 */
public class ServiceManager {
	private RestAnalysisService analysisService;
	private ObjectStore<Activity> activityStore;
	private RestUserStore userStore;
	private RestBookingStore bookingsStore;
	private RestInfrastructureServices infrastructureServices;
	private UserStore currentUserStore;

	public RestAnalysisService getAnalysisService() {
		return analysisService;
	}

	public void setAnalysisService(final RestAnalysisService analysisService) {
		this.analysisService = analysisService;
	}

	public ObjectStore<Activity> getActivityStore() {
		return activityStore;
	}

	public void setActivityStore(final ObjectStore<Activity> activityStore) {
		this.activityStore = activityStore;
	}

	public RestBookingStore getBookingsStore() {
		return bookingsStore;
	}

	public void setBookingsStore(final RestBookingStore bookingsStore) {
		this.bookingsStore = bookingsStore;
	}

	public RestUserStore getUserStore() {
		return userStore;
	}

	public void setUserStore(final RestUserStore userStore) {
		this.userStore = userStore;
	}

	public RestInfrastructureServices getInfrastructureServices() {
		return infrastructureServices;
	}

	public void setInfrastructureServices(final RestInfrastructureServices infrastructureServices) {
		this.infrastructureServices = infrastructureServices;
	}

	public UserStore getCurrentUserStore() {
		return currentUserStore;
	}

	public void setCurrentUserStore(UserStore currentUserStore) {
		this.currentUserStore = currentUserStore;
	}
}
