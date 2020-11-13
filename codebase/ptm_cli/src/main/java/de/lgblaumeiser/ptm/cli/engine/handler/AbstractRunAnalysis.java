/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

abstract class AbstractRunAnalysis extends AbstractCommandHandler {
	protected void runAnalysis(final String command, List<String> period) {
		getLogger().log("Run analysis " + command.toLowerCase() + " analysis for period " + period.get(0)
				+ " until " + period.get(1) + " ...");
		Collection<Collection<String>> result = getServices().getAnalysisService().analyze(command, period);
		getPrinter().tablePrint(result);
		getLogger().log("... analysis done");
	}

	protected List<String> calculateTimeFrame(final YearMonth month, final LocalDate week,
			final LocalDate day, final LocalDate periodStart, LocalDate periodEnd, DefaultFunction defaultCalculator) {
		if (day != null) {
			return calculatePeriod(day, day.plusDays(1L));
		} else if (week != null) {
			return calculateWeek(week);
		} else if (month != null) {
			return calculatePeriod(month.atDay(1), month.plusMonths(1L).atDay(1));
		} else if (periodStart!= null) {
			return calculatePeriod(periodStart, Optional.ofNullable(periodEnd).orElse(LocalDate.now().plusDays(1L)));
		} else {
			return defaultCalculator.applyDefault();
		}
	}

	static interface DefaultFunction {
		List<String> applyDefault();
	}

	protected DefaultFunction dayDefault = new DefaultFunction() {
		@Override
		public List<String> applyDefault() {
			return calculatePeriod(LocalDate.now(), LocalDate.now().plusDays(1L));
		}
	};

	protected DefaultFunction monthDefault = new DefaultFunction() {
		@Override
		public List<String> applyDefault() {
			return calculatePeriod(YearMonth.now().atDay(1), LocalDate.now().plusDays(1L));
		}
	};

	private List<String> calculateWeek(LocalDate p) {
		LocalDate current = p;
		while (current.getDayOfWeek() != DayOfWeek.MONDAY) {
			current = current.minusDays(1L);
		}
		return calculatePeriod(current, current.plusDays(7L));
	}

	private List<String> calculatePeriod(LocalDate periodStart, LocalDate periodEnd) {
		return Arrays.asList(periodStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
				periodEnd.format(DateTimeFormatter.ISO_LOCAL_DATE));
	}
}
