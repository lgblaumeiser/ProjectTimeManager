/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.datamanager.model.Activity.newActivity;
import static de.lgblaumeiser.ptm.datamanager.model.Booking.newBooking;
import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static de.lgblaumeiser.ptm.util.Utils.parseDateString;
import static java.lang.Double.parseDouble;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Before;

import de.lgblaumeiser.ptm.analysis.CalculationPeriod;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

public abstract class AbstractComputerTest {
    private static final String ID = "id";

    protected static final double DOUBLE_COMPARISON_DELTA = 0.15;

    protected static final LocalDate OTHERMONTH = LocalDate.of(2015, 12, 1);

    private static final String ACTIVITYNAME1 = "a";
    private static final String ACTIVITYNAME2 = "b";
    private static final String ACTIVITYNAME3 = "c";
    private static final String PROJECTID1 = "d";
    private static final String PROJECTID2 = "e";
    private static final String PROJECTSUBCAT = "f";
    protected static final String USERNAME = "g";

    private static final Activity ACTIVITY1 = newActivity()
            .setActivityName(ACTIVITYNAME1)
            .setProjectId(PROJECTID1)
            .setActivityId(PROJECTSUBCAT)
            .setUser(USERNAME)
            .build();
    private static final Activity ACTIVITY2 = newActivity()
            .setActivityName(ACTIVITYNAME2)
            .setProjectId(PROJECTID2)
            .setActivityId(PROJECTSUBCAT)
            .setUser(USERNAME)
            .build();
    private static final Activity ACTIVITY3 = newActivity()
            .setActivityName(ACTIVITYNAME3)
            .setProjectId(PROJECTID1)
            .setActivityId(PROJECTSUBCAT)
            .setUser(USERNAME)
            .build();

    private static final LocalTime TIME1 = LocalTime.of(12, 34);
    private static final LocalTime TIME2 = LocalTime.of(13, 57);
    private static final LocalTime TIME3 = LocalTime.of(14, 35);
    private static final LocalTime TIME4 = LocalTime.of(8, 15);
    private static final LocalTime TIME5 = LocalTime.of(17, 25);
    private static final LocalTime TIME6 = LocalTime.of(9, 42);
    private static final LocalTime TIME7 = LocalTime.of(15, 39);
    private static final LocalTime TIME8 = LocalTime.of(18, 45);

    private static final Booking BOOKING1 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 1))
            .setStarttime(TIME1)
            .setEndtime(TIME2)
            .setActivity(1L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING2 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 1))
            .setStarttime(TIME2)
            .setEndtime(TIME3)
            .setActivity(2L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING3 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 6))
            .setStarttime(TIME4)
            .setEndtime(TIME6)
            .setActivity(3L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING4 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 6))
            .setStarttime(TIME7)
            .setEndtime(TIME5)
            .setActivity(1L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING5 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 9))
            .setStarttime(TIME6)
            .setEndtime(TIME3)
            .setActivity(2L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING6 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 9))
            .setStarttime(TIME3)
            .setEndtime(TIME5)
            .setActivity(3L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING7 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 15))
            .setStarttime(TIME4)
            .setEndtime(TIME7)
            .setActivity(1L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING8 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 15))
            .setStarttime(TIME7)
            .setEndtime(TIME8)
            .setActivity(2L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING9 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 24))
            .setStarttime(TIME4)
            .setActivity(3L)
            .setUser(USERNAME)
            .build();
    private static final Booking BOOKING10 = newBooking()
            .setBookingday(LocalDate.of(2017, 3, 28))
            .setStarttime(TIME6)
            .setEndtime(TIME8)
            .setActivity(1L)
            .setUser(USERNAME)
            .build();

    private static Collection<Booking> testBStore = Arrays.asList(
            BOOKING1, BOOKING2, BOOKING3, BOOKING4, BOOKING5,
            BOOKING6, BOOKING7, BOOKING8, BOOKING9, BOOKING10);

    private static Collection<Activity> testAStore = Arrays.asList(ACTIVITY1, ACTIVITY2, ACTIVITY3);

    @Before
    public void before() {
        ObjectStore<Booking> testdataB = new ObjectStore<Booking>() {
            @Override
            public Booking store(final Booking object) {
                // Not needed for test
                return object;
            }

            @Override
            public Collection<Booking> retrieveAll() {
                return testBStore;
            }

            @Override
            public Optional<Booking> retrieveById(final Long id) {
                // Not needed here
                return Optional.empty();
            }

            @Override
            public void deleteById(final Long id) {
                // Not needed here
            }
        };
        ObjectStore<Activity> testdataA = new ObjectStore<Activity>() {
            @Override
            public Activity store(final Activity object) {
                // Not needed for test
                return object;
            }

            @Override
            public Collection<Activity> retrieveAll() {
                // Not needed for test
                return testAStore;
            }

            @Override
            public Optional<Activity> retrieveById(final Long id) {
                switch (id.intValue()) {
                case 1:
                    return Optional.of(ACTIVITY1);
                case 2:
                    return Optional.of(ACTIVITY2);
                case 3:
                    return Optional.of(ACTIVITY3);
                default:
                    return Optional.empty();
                }
            }

            @Override
            public void deleteById(final Long id) {
                // Not needed for test
            }
        };
        try {
            Field f = ACTIVITY1.getClass().getDeclaredField(ID);
            f.setAccessible(true);
            f.set(ACTIVITY1, 1L);
            f.setAccessible(false);
            f = ACTIVITY2.getClass().getDeclaredField(ID);
            f.setAccessible(true);
            f.set(ACTIVITY2, 2L);
            f.setAccessible(false);
            f = ACTIVITY3.getClass().getDeclaredField(ID);
            f.setAccessible(true);
            f.set(ACTIVITY3, 3L);
            f.setAccessible(false);
        } catch (IllegalAccessException | IllegalArgumentException | ClassCastException | NoSuchFieldException
                | SecurityException e) {
            throw new IllegalStateException(e);
        }
        createTestee(testdataB, testdataA);
    }

    protected abstract void createTestee(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore);

    protected CalculationPeriod createPeriod(final String firstDay, final String firstDayAfter) {
        return new CalculationPeriod(parseDateString(firstDay), parseDateString(firstDayAfter));
    }

    protected double sumPercentages(final Collection<Collection<String>> analysisResults) {
        double sum = 0.0;
        for (int rowIndex = 1; rowIndex < analysisResults.size(); rowIndex++) {
            sum += parseDoubleFromPercentageString(analysisResults, rowIndex);
        }
        return sum;
    }

    private double parseDoubleFromPercentageString(final Collection<Collection<String>> analysisResults,
            final int rowNumber) {
        return parseDouble(getPercentageFromRow(analysisResults, rowNumber).replaceAll(",", ".")
                .replaceAll("%", ""));
    }

    protected int percentageColumn = 0;

    private String getPercentageFromRow(final Collection<Collection<String>> analysisResults, final int rowNumber) {
        return getIndexFromCollection(getRow(analysisResults, rowNumber), percentageColumn);
    }

    private Collection<String> getRow(final Collection<Collection<String>> analysisResults, final int rowNumber) {
        return getIndexFromCollection(analysisResults, rowNumber);
    }
}