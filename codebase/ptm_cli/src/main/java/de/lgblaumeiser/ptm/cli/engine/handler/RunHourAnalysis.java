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
import java.util.Optional;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Run an hour analysis on the data
 */
@Parameters(commandDescription = "Run an hour analysis for a month")
public class RunHourAnalysis extends AbstractRunAnalysis {
	private static final String ANALYSIS_HOURS_ID = "HOURS";

	@Parameter(names = { "-m",
			"--month" }, description = "Month for the hour analysis", converter = YearMonthConverter.class)
	private YearMonth bookingMonth = null;

	@Parameter(names = { "-w",
			"--week" }, description = "Day in week for hour analysis", converter = LocalDateConverter.class)
	private LocalDate bookingDayInWeek = null;

	@Parameter(names = { "-s",
			"--period-start" }, description = "Start day of period", converter = LocalDateConverter.class )
	private LocalDate periodStart = null;

	@Parameter(names = { "-e",
			"--period-end" }, description = "First day after the period", converter = LocalDateConverter.class )
	private LocalDate periodEnd = null;

	@Override
	public void handleCommand() {
		runAnalysis(ANALYSIS_HOURS_ID,
				calculateTimeFrame(bookingMonth, bookingDayInWeek, null, periodStart, periodEnd, monthDefault));
	}
}
