/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class that allows to run analysis on the data
 */
public class DataAnalysisService {
	private static final int INDEX_SELECTOR = 0;
	private static final int INDEX_TIMESTRING = 1;
	private static final int INDEX_USER = 2;
	private final Map<String, Analysis> analysisStore = new HashMap<>();

	/**
	 * Run an analysis with id analyzerId and the given parameters
	 *
	 * @param analyzerId The id of the analyzer to run
	 * @param parameter  Parameters given to the analyzer
	 * @return The result as an implemented analysis result interface
	 */
	public Collection<Collection<String>> analyze(final String analyzerId, final Collection<String> parameter) {
		assertState(stringHasContent(analyzerId));
		assertState(parameter != null);
		Analysis analysis = analysisStore.get(analyzerId);
		assertState(analysis != null);
		return analysis.analyze(getCalculationPeriod(getSelector(parameter), getTimestring(parameter)),
				getUser(parameter));
	}

	private String getSelector(Collection<String> parameter) {
		return getIndexFromCollection(parameter, INDEX_SELECTOR);
	}

	private String getTimestring(Collection<String> parameter) {
		return getIndexFromCollection(parameter, INDEX_TIMESTRING);
	}

	private String getUser(Collection<String> parameter) {
		return getIndexFromCollection(parameter, INDEX_USER);
	}

	private CalculationPeriod getCalculationPeriod(String selector, String timestring) {
		switch (selector.toLowerCase()) {
		case "day":
			return getDayPeriod(LocalDate.parse(timestring));
		case "week":
			return getWeekPeriod(LocalDate.parse(timestring));
		case "month":
			return getMonthPeriod(YearMonth.parse(timestring));
		default:
			return getMonthPeriod(YearMonth.now());
		}
	}

	private CalculationPeriod getDayPeriod(final LocalDate day) {
		return new CalculationPeriod(day, day.plusDays(1L));
	}

	private CalculationPeriod getWeekPeriod(final LocalDate dayInWeek) {
		LocalDate current = dayInWeek;
		while (current.getDayOfWeek() != DayOfWeek.MONDAY) {
			current = current.minusDays(1L);
		}
		return new CalculationPeriod(current, current.plusDays(7L));
	}

	private CalculationPeriod getMonthPeriod(final YearMonth month) {
		return new CalculationPeriod(month.atDay(1), month.plusMonths(1L).atDay(1));
	}

	DataAnalysisService addAnalysis(final String id, final Analysis analysis) {
		analysisStore.put(id, analysis);
		return this;
	}

	DataAnalysisService() {
		// Only in package creation
	}
}
