/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.rest;

import static java.util.Arrays.asList;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * Store that uses the rest utils to access the server, i.e., a proxy
 * implementation of Object Store
 */
public class RestActivityStore extends RestBaseService implements ObjectStore<Activity> {
	@Override
	public Collection<Activity> retrieveAll() {
		return asList(getRestUtils().<Activity[]>get("/activities",
				Optional.of(getServices().getCurrentUserStore().loadUserData()), Activity[].class));
	}

	@Override
	public Optional<Activity> retrieveById(final Long id) {
		return Optional.ofNullable(getRestUtils().<Activity>get("/activities/" + id.toString(),
				Optional.of(getServices().getCurrentUserStore().loadUserData()), Activity.class));
	}

	@Override
	public Activity store(final Activity activity) {
		try {
			Map<String, String> bodyData = new HashMap<>();
			bodyData.put("activityName", activity.getActivityName());
			bodyData.put("projectId", activity.getProjectId());
			bodyData.put("activityId", activity.getActivityId());
			bodyData.put("hidden", Boolean.toString(activity.isHidden()));
			String apiName = "/activities";
			if (activity.getId() > 0) {
				apiName = apiName + "/" + activity.getId().toString();
			}
			Long id = getRestUtils().post(apiName, Optional.of(getServices().getCurrentUserStore().loadUserData()),
					bodyData);
			Field idField = activity.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(activity, id);
			idField.setAccessible(false);
			return activity;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void deleteById(final Long id) {
		throw new UnsupportedOperationException();
	}
}
