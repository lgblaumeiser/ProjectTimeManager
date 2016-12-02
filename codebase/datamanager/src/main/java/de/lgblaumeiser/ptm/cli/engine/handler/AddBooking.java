/*
 * Copyright 2016 Lars Geyer-Blaumeiser <lgblaumeiser@gmail.com>
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static com.google.common.base.Preconditions.checkState;

import java.time.LocalTime;
import java.util.Collection;
import java.util.Iterator;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.DayBookings;

/**
 * Add a booking for the day
 */
public class AddBooking extends AbstractCommandHandler {
	@Override
	public void handleCommand(final Collection<String> parameters) {
		DayBookings currentBookings = getServices().getStateStore().getCurrentDay();
		checkState(parameters.size() > 1);
		getLogger().log("Add new booking ...");
		Iterator<String> paramIter = parameters.iterator();
		String activityAbbrev = paramIter.next();
		Activity activity = getServices().getActivityService().getActivityByAbbreviatedName(activityAbbrev);
		LocalTime starttime = LocalTime.parse(paramIter.next());
		Booking addedBooking = getServices().getBookingService().addBooking(currentBookings, activity, starttime);
		if (paramIter.hasNext()) {
			LocalTime endtime = LocalTime.parse(paramIter.next());
			addedBooking = getServices().getBookingService().endBooking(currentBookings, addedBooking, endtime);
		}
		getLogger().log(" ... booking added with information: " + addedBooking.toString());
		getServices().getBookingsStore().store(currentBookings);
		getLogger().log("... bookings stored");
	}

	@Override
	public String toString() {
		return "Add a booking for the day, Params: <1> Activity, <2> Starttime, <3o> Endtime";
	}
}