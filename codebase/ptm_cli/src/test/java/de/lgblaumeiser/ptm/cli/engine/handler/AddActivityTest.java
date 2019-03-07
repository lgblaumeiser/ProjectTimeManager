/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.beust.jcommander.ParameterException;

public class AddActivityTest extends AbstractHandlerTest {
	private static final String ADD_ACTIVITY_COMMAND = "add_activity";

	@Test
	public void testAddActivityTwoParamClean() {
		commandline.runCommand(ADD_ACTIVITY_COMMAND, "-n", ACTIVITY1NAME, "-i", ACTIVITY1ID, "-s", ACTIVITY1SUB);
		assertEquals("/activities", restutils.apiNameGiven);
		assertEquals(ACTIVITY1NAME, restutils.bodyDataGiven.get("activityName"));
		assertEquals(ACTIVITY1ID, restutils.bodyDataGiven.get("projectId"));
		assertEquals(ACTIVITY1SUB, restutils.bodyDataGiven.get("projectActivity"));
		assertEquals("false", restutils.bodyDataGiven.get("hidden"));
		assertEquals(4, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testAddActivityThreeParamFirstNull() {
		commandline.runCommand(ADD_ACTIVITY_COMMAND, "-n", "-i", ACTIVITY1ID, "-s", ACTIVITY1SUB);
	}

	@Test(expected = ParameterException.class)
	public void testAddActivityThreeParamSecondNull() {
		commandline.runCommand(ADD_ACTIVITY_COMMAND, "-n", ACTIVITY1NAME, "-i", "-s", ACTIVITY1SUB);
	}

	@Test(expected = ParameterException.class)
	public void testAddActivityThreeParamThirsNull() {
		commandline.runCommand(ADD_ACTIVITY_COMMAND, "-n", ACTIVITY1NAME, "-i", ACTIVITY1ID, "-s");
	}

	@Test(expected = ParameterException.class)
	public void testAddActivityTwoParamNameMissing() {
		commandline.runCommand(ADD_ACTIVITY_COMMAND, "-i", ACTIVITY1ID, "-s", ACTIVITY1SUB);
	}

	@Test(expected = ParameterException.class)
	public void testAddActivityTwoParamIdMissing() {
		commandline.runCommand(ADD_ACTIVITY_COMMAND, "-n", ACTIVITY1NAME, "-s", ACTIVITY1SUB);
	}

	@Test(expected = ParameterException.class)
	public void testAddActivityTwoParamSubMissing() {
		commandline.runCommand(ADD_ACTIVITY_COMMAND, "-n", ACTIVITY1NAME, "-i", ACTIVITY1ID);
	}
}
