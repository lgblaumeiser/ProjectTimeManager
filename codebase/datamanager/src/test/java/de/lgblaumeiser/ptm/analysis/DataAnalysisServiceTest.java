/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.getFirstFromCollection;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class DataAnalysisServiceTest {
	private DataAnalysisService testee;

	private static final String ANALYSISID = "testanalysis";
	private static final String PERIODMONTH = "month";
	private static final String PERIODWEEK = "week";
	private static final String PERIODDAY = "day";
	private static final String DATESTRINGMONTH = "2017-03";
	private static final String DATESTRINGDAY = "2017-03-08";
	private static final String USER = "MyFairLady";

	@Before
	public void before() {
		DataAnalysisService testSetup = new DataAnalysisService().addAnalysis(ANALYSISID, new Analysis() {
			@Override
			public Collection<Collection<String>> analyze(final CalculationPeriod period, final String user) {
				Collection<String> returnParam = new ArrayList<>(period.days().stream()
						.map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE)).collect(Collectors.toList()));
				return asList(returnParam);
			}
		});
		testee = testSetup;
	}

	@Test
	public void testDataAnalysisServiceMonth() {
		Collection<Collection<String>> result = testee.analyze(ANALYSISID, asList(PERIODMONTH, DATESTRINGMONTH, USER));
		assertEquals(1, result.size());
		Collection<String> content = getFirstFromCollection(result);
		assertEquals(31, content.size());
	}

	@Test
	public void testDataAnalysisServiceWeek() {
		Collection<Collection<String>> result = testee.analyze(ANALYSISID, asList(PERIODWEEK, DATESTRINGDAY, USER));
		assertEquals(1, result.size());
		Collection<String> content = getFirstFromCollection(result);
		assertEquals(7, content.size());
	}

	@Test
	public void testDataAnalysisServiceDay() {
		Collection<Collection<String>> result = testee.analyze(ANALYSISID, asList(PERIODDAY, DATESTRINGDAY, USER));
		assertEquals(1, result.size());
		Collection<String> content = getFirstFromCollection(result);
		assertEquals(1, content.size());
	}

	@Test(expected = IllegalStateException.class)
	public void testDataAnalysisServiceUnknownId() {
		testee.analyze(ANALYSISID, emptyList());
	}

	@Test(expected = IllegalStateException.class)
	public void testDataAnalysisServiceEmptyId() {
		testee.analyze(emptyString(), emptyList());
	}

	@Test(expected = IllegalStateException.class)
	public void testDataAnalysisServiceNullId() {
		testee.analyze(null, emptyList());
	}

	@Test(expected = IllegalStateException.class)
	public void testDataAnalysisServiceNullParam() {
		testee.analyze(ANALYSISID, null);
	}
}
