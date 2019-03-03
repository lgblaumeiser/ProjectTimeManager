/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

public class RunAnalysisTest extends AbstractHandlerTest {
	private static final String HOURS_ANALYSIS_COMMAND = "hour_analysis";
	private static final String ANALYSIS_HOURS_ID = "HOURS";
	private static final String PROJECTS_ANALYSIS_COMMAND = "project_analysis";
	private static final String ANALYSIS_PROJECTS_ID = "PROJECTS";

	private static final String DATE_FOR_ANALYSIS = "2018-04-05";
	private static final String PERIOD_EXPECTED_FOR_WEEK = "2018-04-02;2018-04-09";
	private static final String MONTH_FOR_ANALYSIS = "2018-04";

	@Test
	public void testRunHoursAnalysisThisMonth() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND);
		assertEquals("/analysis/" + ANALYSIS_HOURS_ID + "/month/"
				+ YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")), restutils.apiNameGiven);
	}

	@Test
	public void testRunHoursAnalysisGivenMonth() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND, "-m", MONTH_FOR_ANALYSIS);
		assertEquals("/analysis/" + ANALYSIS_HOURS_ID + "/month/" + MONTH_FOR_ANALYSIS, restutils.apiNameGiven);
	}

	@Test
	public void testRunHoursAnalysisGivenWeekday() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND, "-w", DATE_FOR_ANALYSIS);
		assertEquals("/analysis/" + ANALYSIS_HOURS_ID + "/period/" + PERIOD_EXPECTED_FOR_WEEK, restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisThisMonth() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND);
		assertEquals("/analysis/" + ANALYSIS_PROJECTS_ID + "/month/"
				+ YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM")), restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenMonth() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-m", MONTH_FOR_ANALYSIS);
		assertEquals("/analysis/" + ANALYSIS_PROJECTS_ID + "/month/" + MONTH_FOR_ANALYSIS, restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenDay() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-d", DATE_FOR_ANALYSIS);
		assertEquals("/analysis/" + ANALYSIS_PROJECTS_ID + "/day/" + DATE_FOR_ANALYSIS, restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenDayToday() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-d", "0");
		assertEquals("/analysis/" + ANALYSIS_PROJECTS_ID + "/day/" + LocalDate.now().format(DateTimeFormatter.ISO_DATE),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenDayYesterday() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-d", "-1");
		assertEquals("/analysis/" + ANALYSIS_PROJECTS_ID + "/day/"
				+ LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE), restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenWeek() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-w", DATE_FOR_ANALYSIS);
		assertEquals("/analysis/" + ANALYSIS_PROJECTS_ID + "/period/" + PERIOD_EXPECTED_FOR_WEEK,
				restutils.apiNameGiven);
	}
}
