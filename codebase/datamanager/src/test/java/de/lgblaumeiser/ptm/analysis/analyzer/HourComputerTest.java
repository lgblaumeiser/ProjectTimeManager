/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

public class HourComputerTest extends AbstractComputerTest {
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
		assertEquals("-07:30", getIndexFromCollection(getIndexFromCollection(analysisResults, 6), 7));
	}

	@Test
	public void testHourComputerWeek() {
		Collection<Collection<String>> analysisResults = testee.analyze(createPeriod("2017-03-06", "2017-03-13"),
				USERNAME);
		assertEquals(3, analysisResults.size());
		assertEquals("-05:04", getIndexFromCollection(getIndexFromCollection(analysisResults, 2), 7));
	}
}
