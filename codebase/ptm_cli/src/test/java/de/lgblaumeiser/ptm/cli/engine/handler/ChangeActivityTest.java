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

public class ChangeActivityTest extends AbstractHandlerTest {
    private static final String CHANGE_ACTIVITY_COMMAND = "change_activity";

    @Test
    public void testChangeAndHideActivityClean() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-a", "1",
                "-pn", ACTIVITY1PNAME,
                "-an", ACTIVITY1ANAME,
                "-pi", ACTIVITY1PID,
                "-ai", ACTIVITY1AID,
                "--hidden");
        assertEquals("/activities/1", restutils.apiNameGiven);
        assertEquals(ACTIVITY1PNAME, restutils.bodyDataGiven.get("projectName"));
        assertEquals(ACTIVITY1ANAME, restutils.bodyDataGiven.get("activityName"));
        assertEquals(ACTIVITY1PID, restutils.bodyDataGiven.get("projectId"));
        assertEquals(ACTIVITY1AID, restutils.bodyDataGiven.get("activityId"));
        assertEquals("true", restutils.bodyDataGiven.get("hidden"));
        assertEquals(5, restutils.bodyDataGiven.size());
    }

    @Test
    public void testChangeAndEnableActivityClean() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-a", "2",
                "-pn", ACTIVITY1PNAME,
                "-an", ACTIVITY1ANAME,
                "-pi", ACTIVITY1PID,
                "--visible");
        assertEquals("/activities/2", restutils.apiNameGiven);
        assertEquals(ACTIVITY1PNAME, restutils.bodyDataGiven.get("projectName"));
        assertEquals(ACTIVITY1ANAME, restutils.bodyDataGiven.get("activityName"));
        assertEquals(ACTIVITY1PID, restutils.bodyDataGiven.get("projectId"));
        assertEquals("false", restutils.bodyDataGiven.get("hidden"));
        assertEquals(5, restutils.bodyDataGiven.size());
    }

    @Test(expected = ParameterException.class)
    public void testChangeActivityTwoParamFirstNull() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-a", "1",
                "-an",
                "-pi", ACTIVITY1PID);
    }

    @Test(expected = ParameterException.class)
    public void testChangeActivityTwoParamSecondNull() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-a", "1",
                "-an", ACTIVITY1ANAME,
                "-pi");
    }

    @Test
    public void testChangeActivityOneParam() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-a", "1",
                "-an", ACTIVITY1ANAME);
        assertEquals("/activities/1", restutils.apiNameGiven);
        assertEquals(ACTIVITY1PNAME, restutils.bodyDataGiven.get("projectName"));
        assertEquals(ACTIVITY1ANAME, restutils.bodyDataGiven.get("activityName"));
        assertEquals(ACTIVITY1PID, restutils.bodyDataGiven.get("projectId"));
        assertEquals(ACTIVITY1AID, restutils.bodyDataGiven.get("activityId"));
        assertEquals("false", restutils.bodyDataGiven.get("hidden"));
        assertEquals(5, restutils.bodyDataGiven.size());
    }

    @Test
    public void testChangeActivityNoParam() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-a", "1",
                "--hidden");
        assertEquals("/activities/1", restutils.apiNameGiven);
        assertEquals(ACTIVITY1PNAME, restutils.bodyDataGiven.get("projectName"));
        assertEquals(ACTIVITY1ANAME, restutils.bodyDataGiven.get("activityName"));
        assertEquals(ACTIVITY1PID, restutils.bodyDataGiven.get("projectId"));
        assertEquals(ACTIVITY1AID, restutils.bodyDataGiven.get("activityId"));
        assertEquals("true", restutils.bodyDataGiven.get("hidden"));
        assertEquals(5, restutils.bodyDataGiven.size());
    }

    @Test(expected = ParameterException.class)
    public void testChangeActivityNameNoActivity() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-an", ACTIVITY1ANAME);
    }

    @Test(expected = ParameterException.class)
    public void testChangeActivityNoActivity() {
        commandline.runCommand(CHANGE_ACTIVITY_COMMAND,
                "-hidden");
    }
}
