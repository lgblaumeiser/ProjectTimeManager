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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
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
		getLastOpenBooking(bookingday).ifPresent(b -> {
			if (b.getStarttime().isBefore(starttime)) {
				Booking changed = b.changeBooking().setEndtime(starttime).build();
				bookingStore.store(changed);
			}
		});
		assertState(!activity.isHidden());
		return createBooking(bookingday, user.getId(), activity.getId(), starttime, endtime, comment);
	}

	private Booking createBooking(final LocalDate bookingday, final Long user, final Long activity,
			final LocalTime starttime, final Optional<LocalTime> endtime, final Optional<String> comment) {
		Booking.BookingBuilder newBookingBuilder = newBooking().setBookingday(bookingday).setUser(user)
				.setStarttime(starttime).setActivity(activity);
		endtime.ifPresent(newBookingBuilder::setEndtime);
		comment.ifPresent(c -> {
			if (stringHasContent(c))
				newBookingBuilder.setComment(c);
		});
		Booking newBooking = newBookingBuilder.build();
		bookingStore.store(newBooking);
		return newBooking;
	}

	private Optional<Booking> getLastOpenBooking(final LocalDate bookingday) {
		return bookingStore.retrieveAll().stream().filter(b -> b.getBookingday().equals(bookingday) && !b.hasEndtime())
				.findFirst();
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
		Booking.BookingBuilder bookingBuilder = booking.changeBooking();
		bookingday.ifPresent(bookingBuilder::setBookingday);
		activity.ifPresent(a -> {
			assertState(!a.isHidden());
			bookingBuilder.setActivity(a.getId());
		});
		starttime.ifPresent(bookingBuilder::setStarttime);
		endtime.ifPresent(bookingBuilder::setEndtime);
		comment.ifPresent(c -> {
			if (stringHasContent(c))
				bookingBuilder.setComment(c);
		});
		Booking changedBooking = bookingBuilder.build();
		bookingStore.store(changedBooking);
		return changedBooking;
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
	public Booking addBreakToBooking(Booking booking, LocalTime breakstart, Optional<Integer> duration) {
		assertState(booking != null);
		assertState(breakstart != null);
		assertState(duration != null);
		long internalDuration = duration.orElse(DEFAULTBREAKTIME);
		checkParameters(booking, breakstart, internalDuration);
		changeBooking(booking, Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(breakstart),
				Optional.empty());
		Booking afterBreak = createBooking(booking.getBookingday(), booking.getUser(), booking.getActivity(),
				breakstart.plusMinutes(internalDuration), Optional.ofNullable(booking.getEndtime()),
				Optional.of(booking.getComment()));
		return afterBreak;
	}

	private void checkParameters(Booking booking, LocalTime breakstart, long internalDuration) {
		assertState(booking.getStarttime().isBefore(breakstart));
		assertState(booking.getEndtime() != null);
		assertState(booking.getEndtime().isAfter(breakstart.plusMinutes(internalDuration)));
	}

	/**
	 * @param bookingStore Set the booking store
	 */
	BookingService(final ObjectStore<Booking> bookingStore) {
		this.bookingStore = bookingStore;
	}
}
