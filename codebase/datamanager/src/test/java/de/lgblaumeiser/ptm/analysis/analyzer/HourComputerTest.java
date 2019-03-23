/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static de.lgblaumeiser.ptm.util.Utils.getLastFromCollection;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

public class HourComputerTest extends AbstractComputerTest {
    private static final int ACCUMULATED_HOURS_COLUMN = 7;

    private HourComputer testee;

    @Override
    protected void createTestee(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
        testee = new HourComputer(bStore);
    }

    @Test
    public void testHourComputerMonth() {
        Collection<Collection<String>> analysisResults = testee.analyze(createPeriod("2017-03-01", "2017-04-01"),
                USERNAME);
        assertEquals(7, analysisResults.size());
        assertEquals("-07:30", getAccumulatedHoursOfResult(analysisResults));
    }

    @Test
    public void testHourComputerWeek() {
        Collection<Collection<String>> analysisResults = testee.analyze(createPeriod("2017-03-06", "2017-03-13"),
                USERNAME);
        assertEquals(3, analysisResults.size());
        assertEquals("-05:04", getAccumulatedHoursOfResult(analysisResults));
    }

    private String getAccumulatedHoursOfResult(Collection<Collection<String>> analysisResults) {
        return getIndexFromCollection(getLastRow(analysisResults), ACCUMULATED_HOURS_COLUMN);
    }

    private Collection<String> getLastRow(final Collection<Collection<String>> analysisResults) {
        return getLastFromCollection(analysisResults);
    }
}
