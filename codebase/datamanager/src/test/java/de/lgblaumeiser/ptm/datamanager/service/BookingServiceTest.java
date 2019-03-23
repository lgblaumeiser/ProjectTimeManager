/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.service;

import static de.lgblaumeiser.ptm.datamanager.model.Activity.newActivity;
import static de.lgblaumeiser.ptm.datamanager.model.User.newUser;
import static de.lgblaumeiser.ptm.util.Utils.getFirstFromCollection;
import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.User;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * Tests for the DayBookings service
 */
public class BookingServiceTest {
    private static final LocalDate DATE1 = LocalDate.of(2016, 06, 24);
    private static final LocalTime TIME1 = LocalTime.of(10, 12);
    private static final LocalTime TIME2 = LocalTime.of(12, 15);
    private static final LocalTime TIME3 = LocalTime.of(13, 45);
    private static final String USERNAME = "UserX";
    private final static String EMAIL = "abc@xyz.com";
    private final static String QUESTION = "What the Heck?";
    private final static String ANSWER = "42";

    private static final Activity ACTIVITY1 = newActivity()
            .setActivityName("a")
            .setProjectId("b")
            .setProjectActivity("c")
            .setUser(USERNAME)
            .build();
    private static final Activity ACTIVITY2 = newActivity()
            .setActivityName("a1")
            .setProjectId("b1")
            .setProjectActivity("c1")
            .setUser(USERNAME)
            .build();
    private static final User USER = newUser()
            .setUsername(USERNAME)
            .setPassword("DummyPwd")
            .setEmail(EMAIL)
            .setQuestion(QUESTION)
            .setAnswer(ANSWER)
            .build();
    private static final String COMMENT1 = "Test Comment";
    private static final String COMMENT2 = "";
    private static final Integer BREAKDURATION = 45;
    private BookingService testee;
    private ObjectStore<Booking> mockStore;

    @Before
    public void before() {
        mockStore = new ObjectStore<Booking>() {
            private static final String ID = "id";

            private Collection<Booking> storedBookings = new ArrayList<>();
            private Long id = 1L;

            @Override
            public Collection<Booking> retrieveAll() {
                return unmodifiableCollection(storedBookings);
            }

            @Override
            public Optional<Booking> retrieveById(final Long id) {
                return storedBookings
                        .stream()
                        .filter(b -> id == b.getId())
                        .findFirst();
            }

            @Override
            public Booking store(final Booking object) {
                deleteById(object.getId());
                storedBookings
                        .stream()
                        .filter(b -> id == b.getId())
                        .findFirst()
                        .ifPresent(storedBookings::remove);
                storedBookings.add(object);
                Long currentId = object.getId();
                if (currentId == -1) {
                    try {
                        Field f = object.getClass().getDeclaredField(ID);
                        f.setAccessible(true);
                        f.set(object, id);
                        id++;
                        f.setAccessible(false);
                    } catch (IllegalAccessException | IllegalArgumentException | ClassCastException
                            | NoSuchFieldException | SecurityException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return object;
            }

            @Override
            public void deleteById(final Long id) {
                storedBookings
                        .stream()
                        .filter(b -> id == b.getId())
                        .findFirst()
                        .ifPresent(storedBookings::remove);
            }
        };
        testee = new BookingService(mockStore);
    }

    @Test
    public void testAddBooking1Empty() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, empty(), of(COMMENT1));
        assertEquals(1, mockStore.retrieveAll().size());
        assertEquals(booking, getFirstFromCollection(mockStore.retrieveAll()));
    }

    @Test
    public void testAddBooking1With2Bookings() {
        testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, empty(), empty());
        Booking testBooking = testee.addBooking(DATE1, USER, ACTIVITY2, TIME2, empty(), of(COMMENT1));
        assertEquals(2, mockStore.retrieveAll().size());
        assertEquals(testBooking, getIndexFromCollection(mockStore.retrieveAll(), 1));
        assertTrue(getFirstFromCollection(mockStore.retrieveAll()).hasEndtime());
    }

    @Test
    public void testAddBooking1WithEndedLastBooking() {
        Booking firstOne = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, empty(), of(COMMENT1));
        testee.changeBooking(firstOne, empty(), empty(), empty(), of(TIME2), empty());
        Booking secondOne = testee.addBooking(DATE1, USER, ACTIVITY2, TIME3, empty(), of(COMMENT2));
        assertEquals(2, mockStore.retrieveAll().size());
        assertEquals(secondOne, getIndexFromCollection(mockStore.retrieveAll(), 1));
        assertTrue(getFirstFromCollection(mockStore.retrieveAll()).hasEndtime());
        assertEquals(TIME2, getFirstFromCollection(mockStore.retrieveAll()).getEndtime());
    }

    @Test
    public void testEndBooking() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, empty(), of(COMMENT1));
        Booking result = testee.changeBooking(booking, empty(), empty(), empty(), of(TIME2), empty());
        assertEquals(1, mockStore.retrieveAll().size());
        assertEquals(result, getFirstFromCollection(mockStore.retrieveAll()));
    }

    @Test
    public void testEndBookingSecondEnd() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, empty(), of(COMMENT1));
        Booking endedBooking = testee.changeBooking(booking, empty(), empty(), empty(), of(TIME2), empty());
        Booking testBooking = testee.changeBooking(endedBooking, empty(), empty(), empty(), of(TIME3), empty());
        assertTrue(testBooking.hasEndtime());
        assertEquals(TIME3, testBooking.getEndtime());
    }

    @Test(expected = IllegalStateException.class)
    public void testEndBookingWrongBookingTime() {
        try {
            Booking testBooking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME2, empty(), of(COMMENT2));
            testee.changeBooking(testBooking, empty(), empty(), empty(), of(TIME1), empty());
        } finally {
            assertFalse(mockStore.retrieveAll().isEmpty());
        }
    }

    @Test
    public void testCreatBookingWithEndtime() {
        Booking result = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME2), of(COMMENT1));
        assertEquals(1, mockStore.retrieveAll().size());
        assertEquals(result, getFirstFromCollection(mockStore.retrieveAll()));
    }

    @Test
    public void testChangeBooking() {
        Booking first = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME2), of(COMMENT1));
        Booking second = testee.changeBooking(first, of(DATE1.plusDays(2)), of(ACTIVITY2), of(TIME2), of(TIME3),
                empty());
        assertEquals(1, mockStore.retrieveAll().size());
        assertEquals(second, getFirstFromCollection(mockStore.retrieveAll()));
    }

    @Test(expected = IllegalStateException.class)
    public void testChangeBookingWrongBookingTime() {
        try {
            Booking first = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME2), of(COMMENT1));
            testee.changeBooking(first, of(DATE1.plusDays(2)), of(ACTIVITY2), of(TIME3), of(TIME2), empty());
        } finally {
            assertFalse(mockStore.retrieveAll().isEmpty());
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testAddBookingWithHiddenActivity() {
        testee.addBooking(DATE1, USER, ACTIVITY2.changeActivity().setHidden(true).build(), TIME1, empty(),
                of(COMMENT1));
    }

    @Test(expected = IllegalStateException.class)
    public void testChangeBookingWithHiddenActivity() {
        Booking first = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME2), of(COMMENT1));
        testee.changeBooking(first, of(DATE1.plusDays(2)), of(ACTIVITY2.changeActivity().setHidden(true).build()),
                of(TIME3), of(TIME2), empty());
    }

    @Test
    public void testAddBreak() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME3), of(COMMENT1));
        Booking returnedBooking = testee.addBreakToBooking(booking, TIME2, empty());
        assertEquals(2, mockStore.retrieveAll().size());
        assertEquals(TIME2.plusMinutes(30), returnedBooking.getStarttime());
        assertEquals(TIME3, returnedBooking.getEndtime());
        Booking beforeBreak = getFirstFromCollection(mockStore.retrieveAll());
        assertEquals(booking.getId(), beforeBreak.getId());
        assertEquals(TIME1, beforeBreak.getStarttime());
        assertEquals(TIME2, beforeBreak.getEndtime());
    }

    @Test
    public void testAddBreakDuration() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME3), of(COMMENT1));
        Booking returnedBooking = testee.addBreakToBooking(booking, TIME2, of(BREAKDURATION));
        assertEquals(2, mockStore.retrieveAll().size());
        assertEquals(TIME2.plusMinutes(BREAKDURATION), returnedBooking.getStarttime());
        assertEquals(TIME3, returnedBooking.getEndtime());
        Booking beforeBreak = getFirstFromCollection(mockStore.retrieveAll());
        assertEquals(booking.getId(), beforeBreak.getId());
        assertEquals(TIME1, beforeBreak.getStarttime());
        assertEquals(TIME2, beforeBreak.getEndtime());
    }

    @Test(expected = IllegalStateException.class)
    public void testAddBreakWithoutEndtime() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, empty(), of(COMMENT1));
        testee.addBreakToBooking(booking, TIME2, empty());
    }

    @Test(expected = IllegalStateException.class)
    public void testAddBreakTimeAfterBooking() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME2), of(COMMENT1));
        testee.addBreakToBooking(booking, TIME3, empty());
    }

    @Test(expected = IllegalStateException.class)
    public void testAddBreakTimeBeforeBooking() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME2, of(TIME3), of(COMMENT1));
        testee.addBreakToBooking(booking, TIME1, empty());
    }

    @Test(expected = IllegalStateException.class)
    public void testAddBreakTimeDurationTooLong() {
        Booking booking = testee.addBooking(DATE1, USER, ACTIVITY1, TIME1, of(TIME3), of(COMMENT1));
        testee.addBreakToBooking(booking, TIME2, of(120));
    }
}
