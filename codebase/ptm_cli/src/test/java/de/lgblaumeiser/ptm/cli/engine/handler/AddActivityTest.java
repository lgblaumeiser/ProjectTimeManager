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
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn", ACTIVITY1PNAME,
                "-an", ACTIVITY1ANAME,
                "-pi", ACTIVITY1PID,
                "-ai", ACTIVITY1AID);
        assertEquals("/activities", restutils.apiNameGiven);
        assertEquals(ACTIVITY1PNAME, restutils.bodyDataGiven.get("projectName"));
        assertEquals(ACTIVITY1ANAME, restutils.bodyDataGiven.get("activityName"));
        assertEquals(ACTIVITY1PID, restutils.bodyDataGiven.get("projectId"));
        assertEquals(ACTIVITY1AID, restutils.bodyDataGiven.get("activityId"));
        assertEquals("false", restutils.bodyDataGiven.get("hidden"));
        assertEquals(5, restutils.bodyDataGiven.size());
    }

    @Test(expected = ParameterException.class)
    public void testAddActivityFourParamFirstNull() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn",
                "-an", ACTIVITY1ANAME,
                "-pi", ACTIVITY1PID,
                "-ai", ACTIVITY1AID);
    }

    @Test(expected = ParameterException.class)
    public void testAddActivityFourParamSecondNull() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn", ACTIVITY1PNAME,
                "-an",
                "-pi", ACTIVITY1PID,
                "-ai", ACTIVITY1AID);
    }

    @Test(expected = ParameterException.class)
    public void testAddActivityFourParamThirdNull() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn", ACTIVITY1PNAME,
                "-an", ACTIVITY1ANAME,
                "-pi",
                "-ai", ACTIVITY1AID);
    }

    @Test(expected = ParameterException.class)
    public void testAddActivityFourParamFourthNull() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn", ACTIVITY1PNAME,
                "-an", ACTIVITY1ANAME,
                "-pi", ACTIVITY1PID,
                "-ai");
    }

    @Test(expected = ParameterException.class)
    public void testAddActivityThreeParamProjectNameMissing() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-an", ACTIVITY1ANAME,
                "-pi", ACTIVITY1PID,
                "-ai", ACTIVITY1AID);
    }

    @Test(expected = ParameterException.class)
    public void testAddActivityThreeParamActNameMissing() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn", ACTIVITY1PNAME,
                "-pi", ACTIVITY1PID,
                "-ai", ACTIVITY1AID);

    }

    @Test(expected = ParameterException.class)
    public void testAddActivityThreeParamProjectIdMissing() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn", ACTIVITY1PNAME,
                "-an", ACTIVITY1ANAME,
                "-ai", ACTIVITY1AID);

    }

    @Test(expected = ParameterException.class)
    public void testAddActivityThreeParamActIdMissing() {
        commandline.runCommand(ADD_ACTIVITY_COMMAND,
                "-pn", ACTIVITY1PNAME,
                "-an", ACTIVITY1ANAME,
                "-pi", ACTIVITY1PID);
    }
}
