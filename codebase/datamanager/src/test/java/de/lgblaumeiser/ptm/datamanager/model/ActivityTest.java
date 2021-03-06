/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.datamanager.model.Activity.newActivity;
import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the activity class
 */
@SuppressWarnings("unused")
public class ActivityTest {
    private final static String PRJ_1 = "Prj 1";
    private final static String ACTIVITY_1_1 = "ActivityId11";
    private final static String ACTIVITY_1_2 = "ActivityId12";
    private final static String PRJID_1 = "ID1";
    private final static String ACTID_1 = "Cat1";
    private final static String PRJ_2 = "Prj 2";
    private final static String ACTIVITY_2 = "ActivityId2";
    private final static String PRJID_2 = "ID2";
    private final static String ACTID_2 = "Cat2";
    private final static String USERNAME = "UserX";
    private final static String USERNAME2 = "UserY";

    /**
     * Positive test method for newLineActivity with activity id
     */
    @Test
    public final void testNewActivityPositive() {
        Activity newActivity = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(USERNAME)
                .build();
        assertEquals(PRJ_1, newActivity.getProjectName());
        assertEquals(ACTIVITY_1_1, newActivity.getActivityName());
        assertEquals(PRJID_1, newActivity.getProjectId());
        assertEquals(ACTID_1, newActivity.getActivityId());
        assertEquals(USERNAME, newActivity.getUser());
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankProjectName() {
        Activity newActivity = newActivity()
                .setProjectName(emptyString())
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(USERNAME)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankActivityName() {
        Activity newActivity = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(emptyString())
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(USERNAME)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankProjectId() {
        Activity newActivity = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(emptyString())
                .setActivityId(ACTID_1)
                .setUser(USERNAME)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankActivityId() {
        Activity newActivity = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(emptyString())
                .setUser(USERNAME)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithoutUser() {
        Activity newActivity = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithWrongUser() {
        Activity newActivity = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(emptyString())
                .build();
    }

    /**
     * Test for equals and hashcode
     */
    @Test
    public final void testEquals() {
        Activity newActivity1 = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(USERNAME)
                .build();
        Activity newActivity2 = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_2)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(USERNAME)
                .build();
        Activity newActivity3 = newActivity()
                .setProjectName(PRJ_2)
                .setActivityName(ACTIVITY_2)
                .setProjectId(PRJID_2)
                .setActivityId(ACTID_2)
                .setUser(USERNAME)
                .build();
        Activity newActivity4 = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(USERNAME)
                .build();
        Activity newActivity5 = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_1)
                .setUser(USERNAME2)
                .build();
        Activity newActivity6 = newActivity()
                .setProjectName(PRJ_1)
                .setActivityName(ACTIVITY_1_1)
                .setProjectId(PRJID_1)
                .setActivityId(ACTID_2)
                .setUser(USERNAME)
                .build();

        assertTrue(newActivity1.equals(newActivity4));
        assertTrue(newActivity1.hashCode() == newActivity4.hashCode());
        assertFalse(newActivity1.equals(newActivity2));
        assertFalse(newActivity1.equals(newActivity3));
        assertFalse(newActivity4.equals(newActivity5));
        assertFalse(newActivity4.equals(newActivity6));
        assertFalse(newActivity4.hashCode() == newActivity5.hashCode());
        assertFalse(newActivity4.hashCode() == newActivity6.hashCode());
    }
}
