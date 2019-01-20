/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static org.junit.Assert.assertEquals;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import org.junit.Test;

import com.beust.jcommander.ParameterException;

public class ChangeBookingTest extends AbstractHandlerTest {
	private static final String CHANGE_BOOKING_COMMAND = "change_booking";

	private static final LocalTime TIME3 = LocalTime.of(12, 57);
	private static final LocalTime TIME4 = LocalTime.of(14, 30);

	@Test
	public void testChangeBookingThreeParam() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND, "-b", "10", "-a", "1", "-s", TIME3.toString());
		assertEquals("/bookings/id/10", restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME3.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals(TIME2.toString(), restutils.bodyDataGiven.get("endtime"));
		assertEquals("", restutils.bodyDataGiven.get("comment"));
		assertEquals(4, restutils.bodyDataGiven.size());
	}

	@Test
	public void testChangeBookingFourParamEndtime() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND, "-b", "10", "-a", "1", "-s", TIME3.toString(), "-e",
				TIME4.toString());
		assertEquals("/bookings/id/10", restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME3.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals(TIME4.toString(), restutils.bodyDataGiven.get("endtime"));
		assertEquals("", restutils.bodyDataGiven.get("comment"));
		assertEquals(4, restutils.bodyDataGiven.size());
	}

	@Test
	public void testChangeBookingFourParamComment() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND, "-b", "10", "-a", "1", "-s", TIME3.toString(), "-c", COMMENT);
		assertEquals("/bookings/id/10", restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME3.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals(TIME2.toString(), restutils.bodyDataGiven.get("endtime"));
		assertEquals(COMMENT, restutils.bodyDataGiven.get("comment"));
		assertEquals(4, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testChangeBookingNoParam() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND);
	}

	@Test(expected = ParameterException.class)
	public void testChangeBookingNoBookingParam() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND, "-a", "1");
	}

	@Test(expected = DateTimeParseException.class)
	public void testChangeBookingTwoParamWrongTime() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND, "-b", "10", "-a", "1", "-s", ACTIVITY1NUMBER);
	}

	@Test(expected = DateTimeParseException.class)
	public void testChangeBookingThreeParamWrongTime() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND, "-b", "10", "-a", "3", "-s", TIME1.toString(), "-e",
				ACTIVITY1NUMBER);
	}

	@Test(expected = IllegalStateException.class)
	public void testChangeBookingFourParamWrongTimeSequence() {
		commandline.runCommand(CHANGE_BOOKING_COMMAND, "-b", "10", "-a", "3", "-s", TIME2.toString(), "-e",
				TIME1.toString());
	}
}
