/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

abstract class AbstractRunAnalysis extends AbstractCommandHandler {
	protected void runAnalysis(final String command, final Optional<YearMonth> month, final Optional<LocalDate> week,
			final Optional<LocalDate> day) {
		String frametype = extractTimeFrameType(month, week, day);
		String timeframe = parseTimeFrame(month, week, day);
		getLogger().log("Run analysis " + command.toLowerCase() + " analysis for " + timeframe + " ...");
		Collection<Collection<String>> result = getServices().getAnalysisService().analyze(command,
				Arrays.asList(frametype, timeframe));
		getPrinter().tablePrint(result);
		getLogger().log("... analysis done");
	}

	private String parseTimeFrame(final Optional<YearMonth> month, final Optional<LocalDate> week,
			final Optional<LocalDate> day) {
		return day.map(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE))
				.orElse(week.map(w -> w.format(DateTimeFormatter.ISO_LOCAL_DATE))
						.orElse(month.map(m -> m.format(DateTimeFormatter.ofPattern("yyyy-MM")))
								.orElse(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")))));
	}

	private String extractTimeFrameType(final Optional<YearMonth> month, final Optional<LocalDate> week,
			final Optional<LocalDate> day) {
		return day.map(d -> "day")
				.orElse(week.map(w -> "week").orElse(month.map(m -> "month").orElseThrow(IllegalStateException::new)));
	}
}
