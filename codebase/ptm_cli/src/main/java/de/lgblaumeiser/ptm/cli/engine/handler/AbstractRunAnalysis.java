/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import com.beust.jcommander.Parameter;
import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

abstract class AbstractRunAnalysis extends AbstractCommandHandler {
	@Parameter(names = { "-m",
			"--month" }, description = "Month for the analysis", converter = YearMonthConverter.class)
	protected YearMonth bookingMonth = null;

	@Parameter(names = { "-w",
			"--week" }, description = "Day in week for analysis", converter = LocalDateConverter.class)
	protected LocalDate bookingDayInWeek = null;

	@Parameter(names = { "-d",
			"--day" }, description = "Day for analysis, either a iso date format or -<days>", converter = LocalDateConverter.class)
	protected LocalDate bookingDay = null;

	@Parameter(names = { "-s",
			"--period-start" }, description = "Start day of period", converter = LocalDateConverter.class )
	protected LocalDate periodStart = null;

	@Parameter(names = { "-e",
			"--period-end" }, description = "First day after the period", converter = LocalDateConverter.class )
	protected LocalDate periodEnd = null;

	protected abstract String analysisId();

	protected abstract DefaultFunction defaultPeriod();

	@Override
	public void handleCommand() {
		runAnalysis(analysisId(),
				calculateTimeFrame(bookingMonth, bookingDayInWeek, bookingDay, periodStart, periodEnd, defaultPeriod()));
	}

	private void runAnalysis(final String command, List<String> period) {
		getLogger().log("Run analysis " + command.toLowerCase() + " analysis for period " + period.get(0)
				+ " until " + period.get(1) + " ...");
		Collection<Collection<String>> result = getServices().getAnalysisService().analyze(command, period);
		getPrinter().tablePrint(result);
		getLogger().log("... analysis done");
	}

	private List<String> calculateTimeFrame(final YearMonth month, final LocalDate week,
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

	protected DefaultFunction dayDefault = () -> calculatePeriod(LocalDate.now(), LocalDate.now().plusDays(1L));

	protected DefaultFunction monthDefault = () -> calculatePeriod(YearMonth.now().atDay(1), LocalDate.now().plusDays(1L));

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
