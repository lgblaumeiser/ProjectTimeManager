/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.service;

import static de.lgblaumeiser.ptm.datamanager.model.Booking.newBooking;
import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.Booking.BookingBuilder;
import de.lgblaumeiser.ptm.datamanager.model.User;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * Class which offers the services needed for entering the bookings of a day
 */
public class BookingService {
    private static final int DEFAULTBREAKTIME = 30;

    private final ObjectStore<Booking> bookingStore;

    /**
     * Add a booking at the corresponding starttime. If the last bogoking has no
     * ended this booking ends the previous booking at the given starttime.
     *
     * @param bookingday The day for which the booking should be created
     * @param user       The user of the booking
     * @param activity   The activity of the booking
     * @param starttime  The starttime of the booking
     * @param endtime    The endtime of the booking (optional)
     * @param comment    A comment for the booking, e.g., work done during the
     *                   booking (optional)
     * @return The created Booking object, never null
     * @throws IllegalStateException If given data is not valid, e.g. endtime before
     *                               starttime
     */
    public Booking addBooking(final LocalDate bookingday, final User user, final Activity activity,
            final LocalTime starttime, final Optional<LocalTime> endtime, final Optional<String> comment) {
        assertState(!activity.isHidden());
        getLastOpenBooking(bookingday).ifPresent(b -> endOpenBookingWithStarttime(starttime, b));
        return createAndStoreBooking(
                newBooking(),
                of(bookingday),
                of(user),
                of(activity),
                of(starttime),
                endtime,
                comment);
    }

    private Optional<Booking> getLastOpenBooking(final LocalDate bookingday) {
        return bookingStore
                .retrieveAll()
                .stream()
                .filter(b -> b.getBookingday().equals(bookingday) && !b.hasEndtime())
                .findFirst();
    }

    private void endOpenBookingWithStarttime(final LocalTime starttime, final Booking booking) {
        if (booking.getStarttime().isBefore(starttime)) {
            createAndStoreBooking(
                    booking.changeBooking(),
                    empty(),
                    empty(),
                    empty(),
                    empty(),
                    ofNullable(starttime),
                    empty());
        }
    }

    /**
     * Changes the given booking, only user is not changeable. Only changed items
     * are given as parameter, rest remains unchanged
     *
     * @param booking    The booking for which changes are defined
     * @param bookingday The day for which the booking id valid (optional)
     * @param activity   The activity of the booking (optional)
     * @param starttime  The starttime of the booking(optional)
     * @param endtime    The endtime of the booking (optional)
     * @param comment    A comment for the booking, e.g., work done during the
     *                   booking (optional)
     * @return The changed Booking object, never null
     * @throws IllegalStateException If given data is not valid, e.g. endtime before
     *                               starttime
     */
    public Booking changeBooking(final Booking booking, final Optional<LocalDate> bookingday,
            final Optional<Activity> activity, final Optional<LocalTime> starttime, final Optional<LocalTime> endtime,
            final Optional<String> comment) {
        assertState(booking != null);
        return createAndStoreBooking(
                booking.changeBooking(),
                bookingday,
                empty(),
                activity,
                starttime,
                endtime,
                comment);
    }

    /**
     * Breaks the given booking into two bookings introducing a break of length
     * duration at breakstart.
     * 
     * @param booking    The booking during which the break is introduced
     * @param breakstart The starttime of the break, has to be within startime and
     *                   endtim of the booking
     * @param duration   The duration of the break, breaktime + duration of the
     *                   break must be before the endtime of the booking. The value
     *                   is optional, a standard duration of 30 minutes is taken, if
     *                   optional is empty.
     * @return The changed Booking object, never null
     * @throws IllegalStateException If given data is not valid, according to rules
     *                               defined above, or if a parameter is null
     */
    public Booking addBreakToBooking(final Booking booking, final LocalTime breakstart,
            final Optional<Integer> duration) {
        assertState(booking != null);
        assertState(breakstart != null);
        assertState(duration != null);

        long internalDuration = duration.orElse(DEFAULTBREAKTIME);
        checkParameters(booking, breakstart, internalDuration);

        createAndStoreBooking( // Changed booking with breaktime as new endtime
                booking.changeBooking(),
                empty(),
                empty(),
                empty(),
                empty(),
                of(breakstart),
                empty());

        Booking afterBreak = createAndStoreBookingRaw( // New booking after break
                newBooking(),
                of(booking.getBookingday()),
                of(booking.getUser()),
                of(booking.getActivity()),
                of(breakstart.plusMinutes(internalDuration)),
                ofNullable(booking.getEndtime()),
                ofNullable(booking.getComment()).filter(c -> stringHasContent(c)));

        return afterBreak;
    }

    private void checkParameters(final Booking booking, final LocalTime breakstart, final long internalDuration) {
        assertState(booking.getStarttime().isBefore(breakstart));
        assertState(booking.getEndtime() != null);
        assertState(booking.getEndtime().isAfter(breakstart.plusMinutes(internalDuration)));
    }

    private Booking createAndStoreBooking(final BookingBuilder currentBookingData, final Optional<LocalDate> bookingday,
            final Optional<User> user, final Optional<Activity> activity, final Optional<LocalTime> starttime,
            final Optional<LocalTime> endtime, final Optional<String> comment) {
        return createAndStoreBookingRaw(
                currentBookingData,
                bookingday,
                user.map(User::getUsername),
                activity.filter(a -> !a.isHidden()).map(Activity::getId),
                starttime,
                endtime,
                comment.filter(c -> stringHasContent(c)));
    }

    private Booking createAndStoreBookingRaw(final BookingBuilder currentBookingData,
            final Optional<LocalDate> bookingday, final Optional<String> user, final Optional<Long> activity,
            final Optional<LocalTime> starttime, final Optional<LocalTime> endtime, final Optional<String> comment) {
        bookingday.ifPresent(currentBookingData::setBookingday);
        user.ifPresent(currentBookingData::setUser);
        activity.ifPresent(currentBookingData::setActivity);
        starttime.ifPresent(currentBookingData::setStarttime);
        endtime.ifPresent(currentBookingData::setEndtime);
        comment.ifPresent(currentBookingData::setComment);
        Booking booking = currentBookingData.build();
        bookingStore.store(booking);
        return booking;
    }

    /**
     * @param bookingStore Set the booking store
     */
    BookingService(final ObjectStore<Booking> bookingStore) {
        this.bookingStore = bookingStore;
    }
}
