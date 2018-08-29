/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;
import org.junit.Test;

import java.util.Collection;

import static com.google.common.collect.Iterables.get;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class HourComputerTest extends AbstractComputerTest {
	private HourComputer testee;

	@Override
	protected void createTestee(final ObjectStore<Booking> store) {
		testee = new HourComputer(store);
	}

	@Test
	public void testHourComputerMonth() {
		Collection<Collection<Object>> analysisResults = testee.analyze(asList("month", "2017-03"));
		assertEquals(6, analysisResults.size());
		assertEquals("-07:30", get(get(analysisResults, 5), 7));
	}

	@Test
	public void testHourComputerWeek() {
		Collection<Collection<Object>> analysisResults = testee.analyze(asList("week", "2017-03-08"));
		assertEquals(3, analysisResults.size());
		assertEquals("-05:04", get(get(analysisResults, 2), 7));
	}
}
