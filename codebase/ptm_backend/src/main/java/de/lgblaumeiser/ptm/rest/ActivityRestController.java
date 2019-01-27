/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static de.lgblaumeiser.ptm.datamanager.model.Activity.newActivity;
import static java.lang.Long.valueOf;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.net.URI;
import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.lgblaumeiser.ptm.ServiceMapper;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.User;

/**
 * Rest Controller for management of activities
 */
@RestController
@RequestMapping("/activities")
public class ActivityRestController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServiceMapper services;

	@RequestMapping(method = RequestMethod.GET)
	Collection<Activity> getActivities() {
		logger.info("Request: Get all Activities");
		return services.activityStore().retrieveAll().stream()
				.sorted((a1, a2) -> a1.getBookingNumber().compareToIgnoreCase(a2.getBookingNumber()))
				.collect(Collectors.toList());
	}

	static class ActivityBody {
		public String activityName;
		public String bookingNumber;
		public boolean hidden;
	}

	@RequestMapping(method = RequestMethod.POST)
	ResponseEntity<?> addActivity(@RequestBody final ActivityBody activityData) {
		logger.info("Request: Post new Activity");
		// Temporary concept for user, replaced by security mechanism
		User user = services.userStore().retrieveById(1L).orElseThrow(IllegalStateException::new);
		Activity newActivity = services.activityStore()
				.store(newActivity().setUser(user.getUsername()).setActivityName(activityData.activityName)
						.setBookingNumber(activityData.bookingNumber).setHidden(activityData.hidden).build());
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(newActivity.getId()).toUri();
		logger.info("Result: Activity Created with Id " + newActivity.getId());
		return ResponseEntity.created(location).build();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{activityId}")
	Activity getActivity(@PathVariable final String activityId) {
		logger.info("Request: Get Activity with Id " + activityId);
		return services.activityStore().retrieveById(valueOf(activityId)).orElseThrow(IllegalStateException::new);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{activityId}")
	ResponseEntity<?> changeActivity(@PathVariable final String activityId,
			@RequestBody final ActivityBody activityData) {
		logger.info("Request: Post changed Activity, id Id for change: " + activityId);
		services.activityStore().retrieveById(valueOf(activityId)).ifPresent(
				a -> services.activityStore().store(a.changeActivity().setActivityName(activityData.activityName)
						.setBookingNumber(activityData.bookingNumber).setHidden(activityData.hidden).build()));
		logger.info("Result: Activity changed");
		return ResponseEntity.ok().build();
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> handleException(final IllegalStateException e) {
		logger.error("Exception in Request", e);
		return ResponseEntity.status(BAD_REQUEST).body(e.toString());
	}
}
