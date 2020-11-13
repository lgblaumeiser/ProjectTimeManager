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
 * Run an analysis on the data
 */
@Parameters(commandDescription = "Run an analysis activities to hours, separated for all single activities for a month or a day")
public class RunActivitiesAnalysis extends AbstractRunAnalysis {
	private static final String ANALYSIS_ACTIVITIES_ID = "ACTIVITIES";

	@Parameter(names = { "-m",
			"--month" }, description = "Month for the activity analysis", converter = YearMonthConverter.class)
	private YearMonth bookingMonth = null;

	@Parameter(names = { "-w",
			"--week" }, description = "Day in week for activity analysis", converter = LocalDateConverter.class)
	private LocalDate bookingDayInWeek = null;

	@Parameter(names = { "-d",
			"--day" }, description = "Day for activity analysis, either a iso date format or -<days>", converter = LocalDateConverter.class)
	private LocalDate bookingDay = null;

	@Parameter(names = { "-s",
			"--period-start" }, description = "Start day of period", converter = LocalDateConverter.class )
	private LocalDate periodStart = null;

	@Parameter(names = { "-e",
			"--period-end" }, description = "First day after the period", converter = LocalDateConverter.class )
	private LocalDate periodEnd = null;

	@Override
	public void handleCommand() {
		runAnalysis(ANALYSIS_ACTIVITIES_ID,
				calculateTimeFrame(bookingMonth, bookingDayInWeek, bookingDay, periodStart, periodEnd, dayDefault));
	}
}
