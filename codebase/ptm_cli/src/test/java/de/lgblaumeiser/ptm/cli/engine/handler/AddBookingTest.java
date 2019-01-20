/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.junit.Test;

import com.beust.jcommander.ParameterException;

public class AddBookingTest extends AbstractHandlerTest {
	private static final String ADD_BOOKING_COMMAND = "add_booking";

	@Test
	public void testAddBookingTwoParam() {
		commandline.runCommand(ADD_BOOKING_COMMAND, "-a", "1", "-s", TIME1.toString());
		assertEquals("/bookings/day/" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
				restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME1.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals("", restutils.bodyDataGiven.get("comment"));
		assertEquals(3, restutils.bodyDataGiven.size());
	}

	@Test
	public void testAddBookingThreeParamEndtime() {
		commandline.runCommand(ADD_BOOKING_COMMAND, "-a", "1", "-s", TIME1.toString(), "-e", TIME2.toString());
		assertEquals("/bookings/day/" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
				restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME1.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals(TIME2.toString(), restutils.bodyDataGiven.get("endtime"));
		assertEquals("", restutils.bodyDataGiven.get("comment"));
		assertEquals(4, restutils.bodyDataGiven.size());
	}

	@Test
	public void testAddBookingThreeParamComment() {
		commandline.runCommand(ADD_BOOKING_COMMAND, "-a", "1", "-s", TIME1.toString(), "-c", COMMENT);
		assertEquals("/bookings/day/" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
				restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME1.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals(COMMENT, restutils.bodyDataGiven.get("comment"));
		assertEquals(3, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testAddBookingNoParam() {
		commandline.runCommand(ADD_BOOKING_COMMAND);
	}

	@Test(expected = ParameterException.class)
	public void testAddBookingTimeMissing() {
		commandline.runCommand(ADD_BOOKING_COMMAND, "-a", "1");
	}

	@Test(expected = DateTimeParseException.class)
	public void testAddBookingTwoParamWrongTime() {
		commandline.runCommand(ADD_BOOKING_COMMAND, "-a", "1", "-s", ACTIVITY1NUMBER);
	}

	@Test(expected = DateTimeParseException.class)
	public void testAddBookingThreeParamWrongTime() {
		commandline.runCommand(ADD_BOOKING_COMMAND, "-a", "3", "-s", TIME1.toString(), "-e", ACTIVITY1NUMBER);
	}

	@Test(expected = IllegalStateException.class)
	public void testAddBookingThreeParamWrongTimeSequence() {
		commandline.runCommand(ADD_BOOKING_COMMAND, "-a", "3", "-s", TIME2.toString(), "-e", TIME1.toString());
	}
}
