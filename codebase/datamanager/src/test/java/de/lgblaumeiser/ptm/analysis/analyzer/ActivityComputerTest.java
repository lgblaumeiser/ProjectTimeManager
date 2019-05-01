/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

public class ActivityComputerTest extends AbstractComputerTest {
    private ActivityComputer testee;

    @Override
    protected void createTestee(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
        testee = new ActivityComputer(bStore, aStore);
    }

    @Test
    public void testActivityComputerFixed() {
        Collection<Collection<String>> analysisResults = testee.analyze(createPeriod("2017-03-01", "2017-04-01"),
                USERNAME);
        assertEquals(5, analysisResults.size());
        assertEquals(200.0, sumPercentages(analysisResults), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void testActivityComputerDay() {
        Collection<Collection<String>> analysisResults = testee.analyze(createPeriod("2017-03-15", "2017-04-16"),
                USERNAME);
        assertEquals(4, analysisResults.size());
        assertEquals(200.0, sumPercentages(analysisResults), DOUBLE_COMPARISON_DELTA);
    }

    @Test
    public void testActivityComputerWeek() {
        Collection<Collection<String>> analysisResults = testee.analyze(createPeriod("2017-03-06", "2017-03-13"),
                USERNAME);
        assertEquals(5, analysisResults.size());
        assertEquals(200.0, sumPercentages(analysisResults), DOUBLE_COMPARISON_DELTA);
    }
}
