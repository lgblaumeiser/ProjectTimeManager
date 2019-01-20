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

public class AddBreakTest extends AbstractHandlerTest {
	private static final String ADD_BREAK_COMMAND = "add_break";

	private static final LocalTime TIME3 = LocalTime.of(12, 57);

	@Test
	public void testAddBreakThreeParam() {
		commandline.runCommand(ADD_BREAK_COMMAND, "-b", "10", "-s", TIME3.toString(), "-l", "15");
		assertEquals("/bookings/id/10", restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME1.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals(TIME2.toString(), restutils.bodyDataGiven.get("endtime"));
		assertEquals("", restutils.bodyDataGiven.get("comment"));
		assertEquals(TIME3.toString(), restutils.bodyDataGiven.get("breakstart"));
		assertEquals("15", restutils.bodyDataGiven.get("breaklength"));
		assertEquals(6, restutils.bodyDataGiven.size());
	}

	@Test
	public void testAddBreakTwoParam() {
		commandline.runCommand(ADD_BREAK_COMMAND, "-b", "10", "-s", TIME3.toString());
		assertEquals("/bookings/id/10", restutils.apiNameGiven);
		assertEquals("1", restutils.bodyDataGiven.get("activityId"));
		assertEquals(TIME1.toString(), restutils.bodyDataGiven.get("starttime"));
		assertEquals(TIME2.toString(), restutils.bodyDataGiven.get("endtime"));
		assertEquals("", restutils.bodyDataGiven.get("comment"));
		assertEquals(TIME3.toString(), restutils.bodyDataGiven.get("breakstart"));
		assertEquals(5, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testAddBreakNoParam() {
		commandline.runCommand(ADD_BREAK_COMMAND);
	}

	@Test(expected = ParameterException.class)
	public void testAddBreakNoBookingParam() {
		commandline.runCommand(ADD_BREAK_COMMAND, "-s", TIME3.toString());
	}

	@Test(expected = ParameterException.class)
	public void testAddBreakNoBreakStartParam() {
		commandline.runCommand(ADD_BREAK_COMMAND, "-b", "10");
	}

	@Test(expected = DateTimeParseException.class)
	public void testAddBreakTwoParamWrongTime() {
		commandline.runCommand(ADD_BREAK_COMMAND, "-b", "10", "-s", ACTIVITY1NUMBER);
	}

	@Test(expected = NumberFormatException.class)
	public void testAddBreakNoIntegerLength() {
		commandline.runCommand(ADD_BREAK_COMMAND, "-b", "10", "-s", TIME3.toString(), "-l", "urgs");
	}
}
