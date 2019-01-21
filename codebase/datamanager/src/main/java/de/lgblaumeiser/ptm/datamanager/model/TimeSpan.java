/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static java.lang.String.format;
import static java.time.Duration.ofMinutes;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Objects.hash;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Class for storing a time span, i.e., a span of time defined by a start time
 * and an end time. The class gives calculations on the time span. This is a
 * transient type that is only used for calculation. It is not persisted.
 */
public final class TimeSpan {
	private final LocalTime starttime;
	private final LocalTime endtime;

	/**
	 * Create a new time span object after a consistency check.
	 *
	 * @param starttime Start time of the time span
	 * @param endtime   End time of the time span
	 * @return The time span representation for the given data never null
	 * @throws IllegalArgumentException If the start time is after the end time.
	 *                                  Time spans can only be defined within a day.
	 */
	public static TimeSpan newTimeSpan(final LocalTime starttime, final LocalTime endtime) {
		assertState(starttime != null);
		assertState(endtime != null);
		assertState(endtime.isAfter(starttime));
		return new TimeSpan(starttime, endtime);
	}

	public static TimeSpan newTimeSpan(final Booking booking) {
		return newTimeSpan(booking.getStarttime(), booking.getEndtime());
	}

	private TimeSpan(final LocalTime starttime, final LocalTime endtime) {
		this.starttime = starttime;
		this.endtime = endtime;
	}

	public Duration getLengthInMinutes() {
		return ofMinutes(MINUTES.between(starttime, endtime));
	}

	public boolean overlapsWith(final TimeSpan theOther) {
		if (starttime.equals(theOther.endtime) || endtime.equals(theOther.starttime)
				|| starttime.isAfter(theOther.endtime) || endtime.isBefore(theOther.starttime)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return hash(starttime, endtime);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj instanceof TimeSpan) {
			TimeSpan ts = (TimeSpan) obj;
			return starttime.equals(ts.starttime) && endtime.equals(ts.endtime);
		}
		return false;
	}

	@Override
	public String toString() {
		return format("TimeSpan: Starttime: %s, Endtime %s", starttime.format(ISO_LOCAL_TIME),
				endtime.format(ISO_LOCAL_TIME));
	}
}
