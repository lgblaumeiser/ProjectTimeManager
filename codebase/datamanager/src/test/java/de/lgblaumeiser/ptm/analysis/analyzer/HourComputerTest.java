/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static java.util.Arrays.asList;
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
		Collection<Collection<Object>> analysisResults = testee.analyze(asList("month", "2017-03", USERNAME));
		assertEquals(7, analysisResults.size());
		assertEquals("-07:30", getIndexFromCollection(getIndexFromCollection(analysisResults, 6), 7));
	}

	@Test
	public void testHourComputerWeek() {
		Collection<Collection<Object>> analysisResults = testee.analyze(asList("week", "2017-03-08", USERNAME));
		assertEquals(3, analysisResults.size());
		assertEquals("-05:04", getIndexFromCollection(getIndexFromCollection(analysisResults, 2), 7));
	}
}
