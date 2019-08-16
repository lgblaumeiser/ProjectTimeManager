/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;
import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Objects.hash;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * This class represents a booking on a day. It is represented by a starting
 * point in time and an ending point in time. An activity defines the action
 * done during that time. The action is a booking number on which work is booked
 * onto.
 */
public class Booking {
    public static class BookingBuilder {
        private Long id = valueOf(-1);
        private LocalDate bookingday;
        private LocalTime starttime;
        private LocalTime endtime = null;
        private Long activity;
        private String user;
        private String comment = emptyString();

        private BookingBuilder(final Booking booking) {
            id = booking.getId();
            bookingday = booking.getBookingday();
            user = booking.getUser();
            starttime = booking.getStarttime();
            if (booking.hasEndtime()) {
                endtime = booking.getEndtime();
            }
            activity = booking.getActivity();
            comment = booking.getComment();
        }

        private BookingBuilder() {
            // Nothing to do
        }

        /**
         * @param bookingDay Day for the booking
         * @return The booking build as fluent api, non null
         */
        public BookingBuilder setBookingday(final LocalDate bookingDay) {
            this.bookingday = bookingDay;
            return this;
        }

        /**
         * @param starttime Start time for the booking to build
         * @return The booking build as fluent api, non null
         */
        public BookingBuilder setStarttime(final LocalTime starttime) {
            this.starttime = starttime;
            return this;
        }

        /**
         * @param endtime End time for the booking to build
         * @return The booking build as fluent api, non null
         */
        public BookingBuilder setEndtime(final LocalTime endtime) {
            this.endtime = endtime;
            return this;
        }

        /**
         * @param activity The if of the activity of the booking to build
         * @return The booking build as fluent api, non null
         */
        public BookingBuilder setActivity(final Long activity) {
            this.activity = activity;
            return this;
        }

        /**
         * @param user The user id of the booking to build
         * @return The booking build as fluent api, non null
         */
        public BookingBuilder setUser(final String user) {
            this.user = user;
            return this;
        }

        /**
         * @param comment A comment of the booking to build
         * @return The booking build as fluent api, non null
         */
        public BookingBuilder setComment(final String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * @return An unmodifiable booking representing the data given to the builder,
         *         Non null, returns with exception if the data is invalid
         */
        public Booking build() {
            checkData();
            return new Booking(id, bookingday, user, starttime, endtime, activity, comment);
        }

        private void checkData() {
            assertState(bookingday != null);
            assertState(starttime != null);
            assertState(activity != null);
            assertState(stringHasContent(user));
            assertState(comment != null);

            if (endtime != null) {
                assertState(endtime.isAfter(starttime));
            }
        }
    }

    /**
     * Creates a new booking builder with no data set.
     *
     * @return A new booking builder, never null
     */
    public static BookingBuilder newBooking() {
        return new BookingBuilder();
    }

    /**
     * Change an existing booking by providing a builder preset with the booking
     * data
     *
     * @return A new booking builder, never null
     */
    public BookingBuilder changeBooking() {
        return new BookingBuilder(this);
    }

    private LocalDate bookingday;
    private LocalTime starttime;
    private LocalTime endtime;
    private Long activity;
    private String user;
    private String comment;
    private Long id;

    /**
     * 
     * @return Bookingday of the booking, never null
     */
    public LocalDate getBookingday() {
        return bookingday;
    }

    /**
     * @return Start time of the booking, never null
     */
    public LocalTime getStarttime() {
        return starttime;
    }

    /**
     * @return Whether booking already has an end time
     */
    public boolean hasEndtime() {
        return endtime != null;
    }

    /**
     * @return End time of the booking or null if not set
     */
    public LocalTime getEndtime() {
        return endtime;
    }

    /**
     * @return Activity of the booking, never null
     */
    public Long getActivity() {
        return activity;
    }

    /**
     * @return User for whom booking was made, never null
     */
    public String getUser() {
        return user;
    }

    /**
     * @return A comment if available, an empty string of not, never null
     */
    public String getComment() {
        return comment;
    }

    /**
     * @return The internal id of the booking. Automatically created by storage
     *         system
     */
    public Long getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return hash(id, bookingday, starttime, endtime, activity, user, comment);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Booking) {
            Booking bkg = (Booking) obj;
            return id == bkg.id && bookingday.equals(bkg.bookingday) && starttime.equals(bkg.starttime)
                    && activity.equals(bkg.activity) && user.equals(bkg.user) && comment.equals(bkg.comment)
                    && endtime != null ? endtime.equals(bkg.endtime) : bkg.endtime == null;
        }
        return false;
    }

    @Override
    public String toString() {
        return format("Booking: Bookingday: %s, User Id: %s, Starttime: %s, %sActivity Id: %d, %sId: %d",
                bookingday.format(ISO_LOCAL_DATE),
                user,
                starttime.format(DateTimeFormatter.ofPattern("HH:mm")),
                endtime != null ? "Endtime: " + endtime.format(DateTimeFormatter.ofPattern("HH:mm")) + ", "
                        : emptyString(),
                activity,
                comment != null ? "Comment: " + comment + ", " : emptyString(),
                id);
    }

    private Booking(final Long id, final LocalDate bookingday, final String user, final LocalTime starttime,
            final LocalTime endtime, final Long activity, final String comment) {
        this.id = id;
        this.bookingday = bookingday;
        this.user = user;
        this.starttime = starttime;
        this.endtime = endtime;
        this.activity = activity;
        this.comment = comment;
    }

    private Booking() {
        // Only needed for deserialization
    }
}
