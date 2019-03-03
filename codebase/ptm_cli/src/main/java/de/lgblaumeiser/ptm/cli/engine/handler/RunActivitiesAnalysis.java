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
@Parameters(commandDescription = "Run an anaylsis activities to hours, separated for all single activities for a month or a day")
public class RunActivitiesAnalysis extends AbstractRunAnalysis {
	private static final String ANALYSIS_ACTIVITIES_ID = "ACTIVITIES";

	@Parameter(names = { "-m",
			"--month" }, description = "Month for the hour analysis", converter = YearMonthConverter.class)
	private YearMonth bookingMonth = YearMonth.now();

	@Parameter(names = { "-w",
			"--week" }, description = "Day in week for project analysis", converter = LocalDateConverter.class)
	private LocalDate bookingDayInWeek = null;

	@Parameter(names = { "-d",
			"--day" }, description = "Day for project analysis, either a iso date format or -<days>", converter = LocalDateConverter.class)
	private LocalDate bookingDay = null;

	@Override
	public void handleCommand() {
		runAnalysis(ANALYSIS_ACTIVITIES_ID, Optional.ofNullable(bookingMonth), Optional.ofNullable(bookingDayInWeek),
				Optional.ofNullable(bookingDay));
	}
}
