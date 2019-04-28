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
        assertTrue(logger.logMessages.toString().contains("| Activity | Project Id | Activity Id | Id |"));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1NAME));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1ID));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1SUB));
        assertFalse(logger.logMessages.toString().contains(ACTIVITY2NAME));
        assertFalse(logger.logMessages.toString().contains(ACTIVITY2ID));
        assertFalse(logger.logMessages.toString().contains(ACTIVITY2SUB));
    }

    @Test
    public void testListActivityWithHidden() {
        commandline.runCommand(LIST_ACTIVITY_COMMAND, "--hidden");
        assertTrue(logger.logMessages.toString().contains("| Activity | Project Id | Activity Id | Id |"));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1NAME));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1ID));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY1SUB));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY2NAME));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY2ID));
        assertTrue(logger.logMessages.toString().contains(ACTIVITY2SUB));
    }
}
