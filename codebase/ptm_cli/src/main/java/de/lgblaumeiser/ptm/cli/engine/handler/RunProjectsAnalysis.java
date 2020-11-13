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
@Parameters(commandDescription = "Run an analysis projects to hours for all projects aggregating activities with same project id")
public class RunProjectsAnalysis extends AbstractRunAnalysis {
	private static final String ANALYSIS_PROJECTS_ID = "PROJECTS";

	@Parameter(names = { "-m",
			"--month" }, description = "Month for the project analysis", converter = YearMonthConverter.class)
	private YearMonth bookingMonth = null;

	@Parameter(names = { "-w",
			"--week" }, description = "Day in week for project analysis", converter = LocalDateConverter.class)
	private LocalDate bookingDayInWeek = null;

	@Parameter(names = { "-d",
			"--day" }, description = "Day for project analysis, either a iso date format or -<days>", converter = LocalDateConverter.class)
	private LocalDate bookingDay = null;

	@Parameter(names = { "-s",
			"--period-start" }, description = "Start day of period", converter = LocalDateConverter.class )
	private LocalDate periodStart = null;

	@Parameter(names = { "-e",
			"--period-end" }, description = "First day after the period", converter = LocalDateConverter.class )
	private LocalDate periodEnd = null;

	@Override
	public void handleCommand() {
		runAnalysis(ANALYSIS_PROJECTS_ID,
				calculateTimeFrame(bookingMonth, bookingDayInWeek, bookingDay, periodStart, periodEnd, dayDefault));
	}
}
