/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.getFirstFromCollection;
import static de.lgblaumeiser.ptm.util.Utils.getLastFromCollection;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import de.lgblaumeiser.ptm.analysis.Analysis;
import de.lgblaumeiser.ptm.analysis.CalculationPeriod;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.TimeSpan;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * An analysis that counts all hours in the month given as parameter
 */
public class HourComputer implements Analysis {
	private static final String BREAKTIME_COMMENT = "Break too short!";
	private static final String WORKTIME_COMMENT = "> 10 hours worktime!";
	private static final String INCOMPLETE_COMMENT = "Day has unfinished bookings!";
	private static final String OVERLAPPING_COMMENT = "Day has overlapping bookings!";

	private final ObjectStore<Booking> bookingStore;

	private static class ValidationResult {
		boolean hasBookings = true;
		boolean bookingsValid = true;
		String validationComment = emptyString();
	}

	private static class AccumulatedTimes {
		Duration overtime = Duration.ZERO;
		Duration totaltime = Duration.ZERO;

		void add(final Duration overtimeAddition, final Duration totaltimeAddition) {
			overtime = overtime.plus(overtimeAddition);
			totaltime = totaltime.plus(totaltimeAddition);
		}
	}

	@Override
	public Collection<Collection<String>> analyze(final CalculationPeriod period, final String user) {
		Collection<Collection<String>> result = new ArrayList<>();
		result.add(Arrays.asList("Work Day", "Starttime", "Endtime", "Presence", "Worktime", "Breaktime", "Total",
				"Overtime", "Comment"));
		AccumulatedTimes accutimes = new AccumulatedTimes();
		for (LocalDate currentday : period.days()) {
			Collection<Booking> currentBookings = getBookingsForDay(currentday, user);
			ValidationResult validation = validateBookings(currentBookings);
			if (validation.hasBookings) {
				if (validation.bookingsValid) {
					result.add(createEntry(currentday, currentBookings, accutimes));
				} else {
					result.add(errorEntry(currentday, validation.validationComment));
				}
			}
		}
		return result;
	}

	private Collection<String> createEntry(final LocalDate day, final Collection<Booking> bookings,
			final AccumulatedTimes accutimes) {
		LocalTime starttime = getFirstFromCollection(bookings).getStarttime();
		LocalTime endtime = getLastFromCollection(bookings).getEndtime();
		Duration presence = calculatePresence(starttime, endtime);
		Duration worktime = calculateWorktime(bookings);
		Duration breaktime = calculateBreaktime(presence, worktime);
		Duration currentOvertime = calculateOvertime(worktime, day);
		accutimes.add(currentOvertime, worktime);
		return Arrays.asList(formatDay(day), formatTime(starttime), formatTime(endtime), formatDuration(presence),
				formatDuration(worktime), formatDuration(breaktime), formatDuration(accutimes.totaltime),
				formatDuration(accutimes.overtime), validateTimes(worktime, breaktime));
	}

	private ValidationResult validateBookings(final Collection<Booking> currentBookings) {
		ValidationResult back = new ValidationResult();

		if (currentBookings.isEmpty()) {
			back.hasBookings = false;
			return back;
		}
		if (!hasCompleteBookings(currentBookings)) {
			back.bookingsValid = false;
			back.validationComment = INCOMPLETE_COMMENT;
			return back;
		}
		if (bookingsWithOverlaps(currentBookings)) {
			back.bookingsValid = false;
			back.validationComment = OVERLAPPING_COMMENT;
		}

		return back;
	}

	private Collection<Booking> getBookingsForDay(final LocalDate currentday, final String user) {
		return bookingStore.retrieveAll().stream()
				.filter(b -> b.getBookingday().equals(currentday) && b.getUser().equals(user))
				.sorted((b1, b2) -> b1.getStarttime().compareTo(b2.getStarttime())).collect(Collectors.toList());
	}

	private boolean hasCompleteBookings(final Collection<Booking> bookings) {
		if (bookings.isEmpty()) {
			return false;
		}
		for (Booking current : bookings) {
			if (!current.hasEndtime()) {
				return false;
			}
		}
		return true;
	}

	private boolean bookingsWithOverlaps(final Collection<Booking> bookings) {
		for (Booking current : bookings) {
			TimeSpan currentSpan = TimeSpan.newTimeSpan(current);
			for (Booking toCheck : bookings) {
				if (!current.equals(toCheck)) {
					if (currentSpan.overlapsWith(TimeSpan.newTimeSpan(toCheck))) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private Duration calculateBreaktime(final Duration presence, final Duration worktime) {
		return presence.minus(worktime);
	}

	private Duration calculateOvertime(final Duration worktime, final LocalDate day) {
		Duration minutes = worktime;
		if (isWeekDay(day)) {
			minutes = minutes.minus(Duration.ofMinutes(480)); // Overtime is time after 8 hours
		}
		return minutes;
	}

	private boolean isWeekDay(final LocalDate day) {
		return !(day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY);
	}

	private String validateTimes(final Duration worktime, final Duration breaktime) {
		long worktimeMinutes = worktime.toMinutes();
		long breaktimeMinutes = breaktime.toMinutes();
		if (worktimeMinutes > 600) {
			return WORKTIME_COMMENT;
		}
		if (worktimeMinutes > 540 && breaktimeMinutes < 45) { // longer than 9 hours => 45 minutes break
			return BREAKTIME_COMMENT;
		}
		if (worktimeMinutes > 360 && breaktimeMinutes < 30) { // longer than 6 hours => 30 minutes break
			return BREAKTIME_COMMENT;
		}
		return emptyString();
	}

	private Duration calculatePresence(final LocalTime starttime, final LocalTime endtime) {
		return Duration.between(starttime, endtime);
	}

	private Duration calculateWorktime(final Collection<Booking> bookings) {
		Duration minutes = Duration.ZERO;
		for (Booking current : bookings) {
			minutes = minutes.plus(TimeSpan.newTimeSpan(current).getLengthInMinutes());
		}
		return minutes;
	}

	private String formatDay(final LocalDate day) {
		return day.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private String formatTime(final LocalTime time) {
		return time.format(DateTimeFormatter.ofPattern("HH:mm"));
	}

	private String formatDuration(final Duration duration) {
		long minutes = duration.toMinutes();
		char pre = minutes < 0 ? '-' : ' ';
		minutes = Math.abs(minutes);
		return String.format("%c%02d:%02d", pre, minutes / 60, minutes % 60);
	}

	private Collection<String> errorEntry(final LocalDate day, final String comment) {
		return Arrays.asList(formatDay(day), emptyString(), emptyString(), emptyString(), emptyString(), emptyString(),
				emptyString(), emptyString(), comment);
	}

	public HourComputer(final ObjectStore<Booking> store) {
		bookingStore = store;
	}
}
