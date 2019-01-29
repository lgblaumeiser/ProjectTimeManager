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

import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.TimeSpan;
import de.lgblaumeiser.ptm.store.ObjectStore;
import de.lgblaumeiser.ptm.util.Utils;

/**
 * An analysis that counts all hours in the month given as parameter
 */
public class HourComputer extends AbstractBaseComputer {
	private static final String BREAKTIME_COMMENT = "Break too short!";
	private static final String WORKTIME_COMMENT = "> 10 hours worktime!";
	private static final String INCOMPLETE_COMMENT = "Day has unfinished bookings!";
	private static final String OVERLAPPING_COMMENT = "Day has overlapping bookings!";

	@Override
	public Collection<Collection<Object>> analyze(final Collection<String> parameter) {
		CalculationPeriod requestedPeriod = getCalculationPeriod(parameter);
		String user = getLastFromCollection(parameter);
		Collection<Collection<Object>> result = new ArrayList<>();
		result.add(Arrays.asList("Work Day", "Starttime", "Endtime", "Presence", "Worktime", "Breaktime", "Total",
				"Overtime", "Comment"));
		Duration overtime = Duration.ZERO;
		Duration totaltime = Duration.ZERO;
		LocalDate currentday = requestedPeriod.firstDay;
		while (currentday.isBefore(requestedPeriod.firstDayAfter)) {
			Collection<Booking> currentBookings = getBookingsForDay(currentday, user);
			if (!currentBookings.isEmpty()) {
				String day = currentday.format(DateTimeFormatter.ISO_LOCAL_DATE);
				if (hasCompleteBookings(currentBookings)) {
					if (bookingsWithOverlaps(currentBookings)) {
						result.add(Arrays.asList(day, Utils.emptyString(), Utils.emptyString(), Utils.emptyString(),
								Utils.emptyString(), Utils.emptyString(), Utils.emptyString(), Utils.emptyString(),
								OVERLAPPING_COMMENT));
					} else {
						LocalTime starttime = getFirstFromCollection(currentBookings).getStarttime();
						LocalTime endtime = getLastFromCollection(currentBookings).getEndtime();
						Duration presence = calculatePresence(starttime, endtime);
						Duration worktime = calculateWorktime(currentBookings);
						Duration breaktime = calculateBreaktime(presence, worktime);
						totaltime = totaltime.plus(worktime);
						Duration currentOvertime = calculateOvertime(worktime, currentday);
						overtime = overtime.plus(currentOvertime);
						result.add(Arrays.asList(day, starttime.format(DateTimeFormatter.ofPattern("HH:mm")),
								endtime.format(DateTimeFormatter.ofPattern("HH:mm")), formatDuration(presence),
								formatDuration(worktime), formatDuration(breaktime), formatDuration(totaltime),
								formatDuration(overtime), validate(worktime, breaktime)));
					}
				} else {
					result.add(Arrays.asList(day, Utils.emptyString(), Utils.emptyString(), Utils.emptyString(),
							Utils.emptyString(), Utils.emptyString(), Utils.emptyString(), Utils.emptyString(),
							INCOMPLETE_COMMENT));

				}
			}
			currentday = currentday.plusDays(1);
		}
		return result;
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

	private Collection<Booking> getBookingsForDay(final LocalDate currentday, final String user) {
		return store.retrieveAll().stream()
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

	private String validate(final Duration worktime, final Duration breaktime) {
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

	private String formatDuration(final Duration duration) {
		long minutes = duration.toMinutes();
		char pre = minutes < 0 ? '-' : ' ';
		minutes = Math.abs(minutes);
		return String.format("%c%02d:%02d", pre, minutes / 60, minutes % 60);
	}

	public HourComputer(final ObjectStore<Booking> store) {
		super(store);
	}
}
