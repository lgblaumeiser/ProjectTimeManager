/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ListActivityTest extends AbstractHandlerTest {
    private static final String LIST_ACTIVITY_COMMAND = "list_activities";

    @Test
    public void testListActivity() {
        commandline.runCommand(LIST_ACTIVITY_COMMAND);
        assertTrue(logger.logMessages.toString().contains("| Id | Project | Project Id | Activity | Activity Id |"));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1ANAME));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1PID));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1AID));
        assertFalse(logger.logMessages.toString().contains(ACTIVITY2ANAME));
        assertFalse(logger.logMessages.toString().contains(ACTIVITY2PID));
        assertFalse(logger.logMessages.toString().contains(ACTIVITY2AID));
    }

    @Test
    public void testListActivityWithHidden() {
        commandline.runCommand(LIST_ACTIVITY_COMMAND, "--hidden");
        assertTrue(logger.logMessages.toString().contains("| Id | Project | Project Id | Activity | Activity Id |"));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1ANAME));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1PID));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1AID));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY2ANAME));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY2PID));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY2AID));
    }
}
