/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.Booking.BookingBuilder;

/**
 * Add a booking for the day
 */
@Parameters(commandDescription = "Add a new booking")
public class AddBooking extends AbstractCommandHandler {
    private final static String DUMMYUSER = "Dummy";

    @Parameter(names = { "-d",
            "--day" }, description = "Optional day for booking, either a iso date format or -<days>", converter = LocalDateConverter.class)
    private LocalDate bookingDay = LocalDate.now();

    @Parameter(names = { "-a", "--activity" }, description = "Activity id of the bookings activity", required = true)
    private Long activityId;

    @Parameter(names = { "-s",
            "--starttime" }, description = "Start time of the booked time frame", required = true, converter = LocalTimeConverter.class)
    private Optional<LocalTime> starttime;

    @Parameter(names = { "-e",
            "--endtime" }, description = "End time of the booked time frame", converter = LocalTimeConverter.class)
    private Optional<LocalTime> endtime = Optional.empty();

    @Parameter(names = { "-c",
            "--comment" }, description = "Comment on booked time frame", converter = OptionalStringConverter.class)
    private Optional<String> comment = Optional.empty();

    @Override
    public void handleCommand() {
        getLogger().log("Add new booking ...");
        BookingBuilder newBooking = Booking.newBooking().setBookingday(bookingDay).setActivity(activityId)
                .setUser(DUMMYUSER);
        starttime.ifPresent(newBooking::setStarttime);
        endtime.ifPresent(newBooking::setEndtime);
        comment.ifPresent(newBooking::setComment);
        Booking addedBooking = getServices().getBookingsStore().store(newBooking.build());
        getLogger().log(" ... booking added with data: " + addedBooking.toString());
    }
}