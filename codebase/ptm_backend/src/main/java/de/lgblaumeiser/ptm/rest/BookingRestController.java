/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static java.lang.Long.valueOf;
import static java.time.LocalTime.parse;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

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
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.User;

/**
 * Rest Controller for Booking Management
 */
@RestController
@RequestMapping("/bookings")
public class BookingRestController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServiceMapper services;

	@RequestMapping(method = RequestMethod.GET)
	public Collection<String> getDaysForWhichBookingsExist(final Principal principal) {
		logger.info("Request: Get all days for which bookings exist for user " + principal.getName());
		return services.bookingStore().retrieveAll().stream().filter(b -> b.getUser().equals(principal.getName()))
				.map(Booking::getBookingday).distinct().map(d -> d.format(ISO_LOCAL_DATE)).sorted().collect(toList());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/day/{dayString}")
	public Collection<Booking> getBookingsForDay(final Principal principal, @PathVariable final String dayString) {
		logger.info("Request: Get Bookings for Day " + dayString + " and user " + principal.getName());
		LocalDate day = LocalDate.parse(dayString);
		return services.bookingStore().retrieveAll().stream().filter(b -> b.getBookingday().equals(day))
				.filter(b -> checkUser(b.getUser(), principal.getName()))
				.sorted(Comparator.comparing(Booking::getStarttime)).collect(toList());
	}

	public static class BookingBody {
		public String activityId;
		public String starttime;
		public String endtime;
		public String comment;
		public String breakstart;
		public String breaklength;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/day/{dayString}")
	public ResponseEntity<?> addBooking(Principal principal, @PathVariable final String dayString,
			@RequestBody final BookingBody newData) {
		logger.info("Request: Post new Booking for day " + dayString + " by user " + principal.getName());
		LocalDate day = LocalDate.parse(dayString);
		Activity activity = services.activityStore().retrieveById(valueOf(newData.activityId))
				.orElseThrow(IllegalStateException::new);
		checkState(checkUser(activity.getUser(), principal.getName()));
		User user = services.userStore().retrieveAll().stream().filter(u -> u.getUsername().equals(principal.getName()))
				.findFirst().orElseThrow(IllegalStateException::new);
		Booking newBooking = services.bookingService().addBooking(day, user, activity, parse(newData.starttime),
				newData.endtime != null ? Optional.of(parse(newData.endtime)) : Optional.empty(),
				stringHasContent(newData.comment) ? Optional.of(newData.comment) : Optional.empty());
		if (newData.breakstart != null) {
			newBooking = services.bookingService().addBreakToBooking(newBooking, parse(newData.breakstart),
					newData.breaklength != null ? Optional.of(Integer.parseInt(newData.breaklength))
							: Optional.empty());
		}
		URI location = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
				+ "/bookings/id/" + newBooking.getId());
		logger.info("Result: Booking created with Id " + newBooking.getId());
		return ResponseEntity.created(location).build();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/id/{booking}")
	public Booking getBooking(final Principal principal, @PathVariable final String booking) {
		logger.info("Request: Get booking with Id " + booking + " if user is " + principal.getName());
		Booking foundBooking = services.bookingStore().retrieveById(valueOf(booking))
				.orElseThrow(IllegalStateException::new);
		checkState(checkUser(foundBooking.getUser(), principal.getName()));
		return foundBooking;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/id/{booking}")
	public ResponseEntity<?> changeBooking(final Principal principal, @PathVariable final String booking,
			@RequestBody final BookingBody changeData) {
		logger.info("Request: Post changes for Booking with Id " + booking + " if user is " + principal.getName());
		Optional<Activity> activity = services.activityStore().retrieveById(valueOf(changeData.activityId));
		activity.ifPresent(a -> checkState(checkUser(a.getUser(), principal.getName())));
		Optional<Booking> relevantBooking = services.bookingStore().retrieveById(valueOf(booking));
		relevantBooking.ifPresent(b -> {
			checkState(checkUser(b.getUser(), principal.getName()));
			services.bookingService().changeBooking(b, Optional.empty(), activity,
					changeData.starttime != null ? Optional.of(parse(changeData.starttime)) : Optional.empty(),
					changeData.endtime != null ? Optional.of(parse(changeData.endtime)) : Optional.empty(),
					stringHasContent(changeData.comment) ? Optional.of(changeData.comment) : Optional.empty());
		});
		if (changeData.breakstart != null) {
			relevantBooking.ifPresent(b -> services.bookingService().addBreakToBooking(b, parse(changeData.breakstart),
					changeData.breaklength != null ? Optional.of(Integer.parseInt(changeData.breaklength))
							: Optional.empty()));
		}
		logger.info("Result: Booking changed with Id " + booking);
		return ResponseEntity.ok().build();
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/id/{booking}")
	public ResponseEntity<?> deleteBooking(final Principal principal, @PathVariable final String booking) {
		logger.info("Request: Delete Booking with Id " + booking + " if user is " + principal.getName());
		services.bookingStore().retrieveById(valueOf(booking)).ifPresent(b -> {
			checkState(checkUser(b.getUser(), principal.getName()));
			services.bookingStore().deleteById(valueOf(booking));
			logger.info("Result: Booking deleted");
		});
		return ResponseEntity.ok().build();
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> handleException(final IllegalStateException e) {
		logger.error("Exception in Request", e);
		return ResponseEntity.status(BAD_REQUEST).body(e.toString());
	}

	private boolean stringHasContent(final String string) {
		return (string != null) && !string.isEmpty();
	}

	private void checkState(boolean state) {
		if (!state) {
			throw new IllegalStateException();
		}
	}

	private boolean checkUser(String objectUser, String authenticatedUser) {
		return objectUser.equals(authenticatedUser);
	}
}
