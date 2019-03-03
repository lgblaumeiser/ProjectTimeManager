/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static java.lang.Double.parseDouble;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

public class ProjectComputerTest extends AbstractComputerTest {
	private ProjectComputer testee;

	@Override
	protected void createTestee(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
		testee = new ProjectComputer(bStore, aStore);
	}

	@Test
	public void testProjectComputerFixed() {
		Collection<Collection<Object>> analysisResults = testee.analyze(Arrays.asList("month", "2017-03", USERNAME));
		assertEquals(5, analysisResults.size());
		assertEquals(200.0,
				parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 1), 4).toString()
						.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 2), 4).toString()
								.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 3), 4).toString()
								.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 4), 4).toString()
								.replaceAll(",", ".").replaceAll("%", "")),
				0.1);
	}

	@Test
	public void testProjectComputerDay() {
		Collection<Collection<Object>> analysisResults = testee.analyze(Arrays.asList("day", "2017-03-15", USERNAME));
		assertEquals(4, analysisResults.size());
		assertEquals(200.0,
				parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 1), 4).toString()
						.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 2), 4).toString()
								.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 3), 4).toString()
								.replaceAll(",", ".").replaceAll("%", "")),
				0.1);
	}

	@Test
	public void testProjectComputerWeek() {
		Collection<Collection<Object>> analysisResults = testee.analyze(Arrays.asList("week", "2017-03-09", USERNAME));
		assertEquals(5, analysisResults.size());
		assertEquals(200.0,
				parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 1), 4).toString()
						.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 2), 4).toString()
								.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 3), 4).toString()
								.replaceAll(",", ".").replaceAll("%", ""))
						+ parseDouble(getIndexFromCollection(getIndexFromCollection(analysisResults, 4), 4).toString()
								.replaceAll(",", ".").replaceAll("%", "")),
				0.15);
	}
}
