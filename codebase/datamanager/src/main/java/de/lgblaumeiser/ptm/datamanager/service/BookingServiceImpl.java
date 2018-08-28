/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 */
package de.lgblaumeiser.ptm.datamanager.service;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static de.lgblaumeiser.ptm.datamanager.model.Booking.newBooking;

/**
 * The implementation of the DayBookings service
 */
public class BookingServiceImpl implements BookingService {
	private ObjectStore<Booking> bookingStore;

	@Override
	public Booking addBooking(LocalDate bookingday, String user, Activity activity, LocalTime starttime,
							  Optional<LocalTime> endtime, Optional<String> comment) {
		getLastOpenBooking(bookingday).ifPresent(b -> {
			Booking changed = b.changeBooking().setEndtime(starttime).build();
			bookingStore.store(changed);
		});
		checkState(!activity.isHidden());
		Booking.BookingBuilder newBookingBuilder = newBooking().setBookingday(bookingday).setUser(user).setStarttime(starttime)
				.setActivity(activity);
		endtime.ifPresent(newBookingBuilder::setEndtime);
		comment.ifPresent(c ->  { if (StringUtils.isNotBlank(c)) newBookingBuilder.setComment(c); });
		Booking newBooking = newBookingBuilder.build();
		bookingStore.store(newBooking);
		return newBooking;
	}

	private Optional<Booking> getLastOpenBooking(LocalDate bookingday) {
		return bookingStore.retrieveAll().stream().filter(b -> b.getBookingday().equals(bookingday) && !b.hasEndtime())
				.findFirst();
	}

	@Override
	public 	Booking changeBooking(Booking booking, Optional<LocalDate> bookingday, Optional<Activity> activity,
                                    Optional<LocalTime> starttime, Optional<LocalTime> endtime, Optional<String> comment) {
		checkState(booking != null);
		Booking.BookingBuilder bookingBuilder = booking.changeBooking();
		bookingday.ifPresent(bookingBuilder::setBookingday);
		activity.ifPresent(a -> checkState(!a.isHidden()));
		activity.ifPresent(bookingBuilder::setActivity);
		starttime.ifPresent(bookingBuilder::setStarttime);
		endtime.ifPresent(bookingBuilder::setEndtime);
		comment.ifPresent(c -> { if (StringUtils.isNotBlank(c)) bookingBuilder.setComment(c); });
		Booking changedBooking = bookingBuilder.build();
		bookingStore.store(changedBooking);
		return changedBooking;
	}

	/**
	 * @param bookingStore
	 *            Set the booking store
	 */
	public BookingServiceImpl(ObjectStore<Booking> bookingStore) {
		this.bookingStore = bookingStore;
	}
}
