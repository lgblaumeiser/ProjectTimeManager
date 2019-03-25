/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.datamanager.model.Booking.newBooking;
import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static java.time.LocalDate.now;
import static java.time.LocalTime.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Test;

/**
 * Test of the Booking class
 */
@SuppressWarnings("unused")
public class BookingTest {
    private static final LocalDate DATE = now();
    private static final LocalTime TIME1 = of(12, 34);
    private static final LocalTime TIME2 = of(13, 57);
    private static final String COMMENT = "Test Comment";
    private static final String USERNAME = "UserX";
    private static final long DIFFERENCEMINUTES = 83;

    /**
     * Positive test method for newBooking
     */
    @Test
    public final void testNewBookingLocalTimeActivity() {
        Booking testee = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setActivity(1L)
                .build();
        assertEquals(Long.valueOf(1L), testee.getActivity());
        assertEquals(TIME1, testee.getStarttime());
        assertFalse(testee.hasEndtime());
        assertEquals(USERNAME, testee.getUser());
    }

    /**
     * Positive test method for newBooking with comment
     */
    @Test
    public final void testNewBookingLocalTimeActivityString() {
        Booking testee = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setActivity(1L)
                .build();
        assertEquals(Long.valueOf(1L), testee.getActivity());
        assertEquals(TIME1, testee.getStarttime());
        assertFalse(testee.hasEndtime());
        assertEquals(USERNAME, testee.getUser());
    }

    /**
     * Positive test method for endBooking
     */
    @Test
    public final void testEndBooking() {
        Booking startBooking = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setActivity(1L)
                .build();
        Booking testee = startBooking.changeBooking().setEndtime(TIME2).build();
        assertEquals(Long.valueOf(1L), testee.getActivity());
        assertEquals(TIME1, testee.getStarttime());
        assertTrue(testee.hasEndtime());
        assertEquals(TIME2, testee.getEndtime());
        assertEquals(USERNAME, testee.getUser());
    }

    @Test(expected = IllegalStateException.class)
    public final void testEndBookingNegative() {
        Booking testee = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME2)
                .setEndtime(TIME1)
                .setActivity(1L)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testNoStarttime() {
        Booking testee = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setActivity(1L)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testNoActivity() {
        Booking testee = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setEndtime(TIME2)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testNoBookingday() {
        Booking testee = newBooking()
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setEndtime(TIME2)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testNoUser() {
        Booking testee = newBooking()
                .setBookingday(DATE)
                .setStarttime(TIME1)
                .setEndtime(TIME2)
                .build();
    }

    @Test
    public final void testNoComment() {
        Booking booking = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setActivity(1L)
                .build();
        assertEquals(emptyString(), booking.getComment());
    }

    @Test
    public final void testComment() {
        Booking booking = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setActivity(1L)
                .setComment(COMMENT)
                .build();
        assertEquals(COMMENT, booking.getComment());
    }

    /**
     * Test method for calculate time span
     */
    @Test
    public final void testCalculateTimeSpan() {
        Booking booking = newBooking()
                .setBookingday(DATE)
                .setUser(USERNAME)
                .setStarttime(TIME1)
                .setEndtime(TIME2)
                .setActivity(1L)
                .build();
        TimeSpan testee = TimeSpan.newTimeSpan(booking);
        assertEquals(DIFFERENCEMINUTES, testee.getLengthInMinutes().toMinutes());
    }

}
