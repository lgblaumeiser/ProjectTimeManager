/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.time.LocalTime;
import java.util.Optional;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.Booking.BookingBuilder;

/**
 * Change a booking
 */
@Parameters(commandDescription = "Changes properties of an existing booking")
public class ChangeBooking extends AbstractCommandHandler {
	@Parameter(names = { "-b", "--booking" }, description = "Booking id of the booking to end", required = true)
	private Long id;

	@Parameter(names = { "-a", "--activity" }, description = "Activity id of the bookings activity")
	private Long activityId = -1L;

	@Parameter(names = { "-s",
			"--starttime" }, description = "Start time of the booked time frame", converter = LocalTimeConverter.class)
	private Optional<LocalTime> starttime = Optional.empty();

	@Parameter(names = { "-e",
			"--endtime" }, description = "End time of the booked time frame", converter = LocalTimeConverter.class)
	private Optional<LocalTime> endtime = Optional.empty();

	@Parameter(names = { "-c",
			"--comment" }, description = "Comment on booked time frame", converter = OptionalStringConverter.class)
	private Optional<String> comment = Optional.empty();

	@Override
	public void handleCommand() {
		getLogger().log("Change booking ...");
		BookingBuilder changedBookingBuilder = getServices().getBookingsStore().retrieveById(id)
				.orElseThrow(IllegalStateException::new).changeBooking();
		if (activityId > 0) {
			changedBookingBuilder.setActivity(activityId);
		}
		starttime.ifPresent(changedBookingBuilder::setStarttime);
		endtime.ifPresent(changedBookingBuilder::setEndtime);
		comment.ifPresent(changedBookingBuilder::setComment);
		Booking changed = getServices().getBookingsStore().store(changedBookingBuilder.build());
		getLogger().log("... new booking data: " + changed.toString());
	}
}
