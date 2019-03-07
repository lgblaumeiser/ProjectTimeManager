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
import java.util.Optional;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

abstract class AbstractRunAnalysis extends AbstractCommandHandler {
	protected void runAnalysis(final String command, final Optional<YearMonth> month, final Optional<LocalDate> period,
			final Optional<LocalDate> day) {
		String frametype = extractTimeFrameType(month, period, day);
		String timeframe = parseTimeFrame(month, period, day);
		getLogger().log("Run analysis " + command.toLowerCase() + " analysis for " + timeframe + " ...");
		Collection<Collection<String>> result = getServices().getAnalysisService().analyze(command,
				Arrays.asList(frametype, timeframe));
		getPrinter().tablePrint(result);
		getLogger().log("... analysis done");
	}

	private String parseTimeFrame(final Optional<YearMonth> month, final Optional<LocalDate> period,
			final Optional<LocalDate> day) {
		return day.map(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE))
				.orElse(period.map(p -> calculatePeriod(p))
						.orElse(month.map(m -> m.format(DateTimeFormatter.ofPattern("yyyy-MM")))
								.orElse(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")))));
	}

	private String calculatePeriod(LocalDate p) {
		LocalDate current = p;
		while (current.getDayOfWeek() != DayOfWeek.MONDAY) {
			current = current.minusDays(1L);
		}
		return current.format(DateTimeFormatter.ISO_LOCAL_DATE) + ";"
				+ current.plusDays(7L).format(DateTimeFormatter.ISO_LOCAL_DATE);
	}

	private String extractTimeFrameType(final Optional<YearMonth> month, final Optional<LocalDate> period,
			final Optional<LocalDate> day) {
		return day.map(d -> "day").orElse(
				period.map(p -> "period").orElse(month.map(m -> "month").orElseThrow(IllegalStateException::new)));
	}
}
