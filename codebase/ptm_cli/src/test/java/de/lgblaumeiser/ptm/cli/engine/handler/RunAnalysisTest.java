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
	private static final String ACTIVITIES_ANALYSIS_COMMAND = "activities_analysis";
	private static final String ANALYSIS_ACTIVITIES_ID = "ACTIVITIES";
	private static final String PROJECTS_ANALYSIS_COMMAND = "projects_analysis";
	private static final String ANALYSIS_PROJECTS_ID = "PROJECTS";

	private static final String DATE_FOR_ANALYSIS = "2018-04-05";
	private static final String ENDDATE_FOR_ANALYSIS = "2018-05-09";
	private static final String ENDDATE_FOR_DAY = "2018-04-06";
	private static final String STARTDATE_FOR_WEEK = "2018-04-02";
	private static final String ENDDATE_FOR_WEEK = "2018-04-09";
	private static final String MONTH_FOR_ANALYSIS = "2018-04";
	private static final String STARTDATE_FOR_MONTH = "2018-04-01";
	private static final String ENDDATE_FOR_MONTH = "2018-05-01";

	private static final String ANALYSIS_API_TEMPLATE = "/analysis/%s/%s/%s";

	@Test
	public void testRunHoursAnalysisThisMonth() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_HOURS_ID,
				YearMonth.now().atDay(1).format(DateTimeFormatter.ISO_LOCAL_DATE),
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunHoursAnalysisGivenMonth() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND, "-m", MONTH_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_HOURS_ID, STARTDATE_FOR_MONTH, ENDDATE_FOR_MONTH), restutils.apiNameGiven);
	}

	@Test
	public void testRunHoursAnalysisGivenWeekday() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND, "-w", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_HOURS_ID, STARTDATE_FOR_WEEK, ENDDATE_FOR_WEEK), restutils.apiNameGiven);
	}

	@Test
	public void testRunHoursAnalysisGivenPeriod() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND, "-s", DATE_FOR_ANALYSIS, "-e", ENDDATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_HOURS_ID, DATE_FOR_ANALYSIS, ENDDATE_FOR_ANALYSIS), restutils.apiNameGiven);
	}

	@Test
	public void testRunHoursAnalysisGivenPeriodStart() {
		commandline.runCommand(HOURS_ANALYSIS_COMMAND, "-s", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_HOURS_ID, DATE_FOR_ANALYSIS,
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)), restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisDefaultDay() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID,
				LocalDate.now().format(DateTimeFormatter.ISO_DATE),
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)), restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisGivenMonth() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND, "-m", MONTH_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID, STARTDATE_FOR_MONTH, ENDDATE_FOR_MONTH),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisGivenDay() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND, "-d", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID, DATE_FOR_ANALYSIS, ENDDATE_FOR_DAY),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisGivenDayToday() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND, "-d", "0");
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID,
				LocalDate.now().format(DateTimeFormatter.ISO_DATE),
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisGivenDayYesterday() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND, "-d", "-1");
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID,
				LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE),
				LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisGivenWeek() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND, "-w", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID, STARTDATE_FOR_WEEK, ENDDATE_FOR_WEEK),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisGivenPeriod() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND, "-s", DATE_FOR_ANALYSIS, "-e", ENDDATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID, DATE_FOR_ANALYSIS, ENDDATE_FOR_ANALYSIS),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunActivitiesAnalysisGivenPeriodStart() {
		commandline.runCommand(ACTIVITIES_ANALYSIS_COMMAND, "-s", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_ACTIVITIES_ID, DATE_FOR_ANALYSIS,
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)), restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisDefaultDay() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID,
				LocalDate.now().format(DateTimeFormatter.ISO_DATE),
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenMonth() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-m", MONTH_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID, STARTDATE_FOR_MONTH, ENDDATE_FOR_MONTH),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenDay() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-d", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID, DATE_FOR_ANALYSIS, ENDDATE_FOR_DAY),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenDayToday() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-d", "0");
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID,
				LocalDate.now().format(DateTimeFormatter.ISO_DATE),
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenDayYesterday() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-d", "-1");
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID,
				LocalDate.now().minusDays(1).format(DateTimeFormatter.ISO_DATE),
				LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenWeek() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-w", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID, STARTDATE_FOR_WEEK, ENDDATE_FOR_WEEK),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenPeriod() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-s", DATE_FOR_ANALYSIS, "-e", ENDDATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID, DATE_FOR_ANALYSIS, ENDDATE_FOR_ANALYSIS),
				restutils.apiNameGiven);
	}

	@Test
	public void testRunProjectsAnalysisGivenPeriodStart() {
		commandline.runCommand(PROJECTS_ANALYSIS_COMMAND, "-s", DATE_FOR_ANALYSIS);
		assertEquals(String.format(ANALYSIS_API_TEMPLATE, ANALYSIS_PROJECTS_ID, DATE_FOR_ANALYSIS,
				LocalDate.now().plusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE)), restutils.apiNameGiven);
	}
}
