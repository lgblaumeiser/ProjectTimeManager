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
@Parameters(commandDescription = "Run an analysis activities to hours, separated for all single activities for different kinds of periods")
public class RunActivitiesAnalysis extends AbstractRunAnalysis {
	private static final String ANALYSIS_ACTIVITIES_ID = "ACTIVITIES";

	@Override
	protected String analysisId() {
		return ANALYSIS_ACTIVITIES_ID;
	}

	@Override
	protected DefaultFunction defaultPeriod() {
		return dayDefault;
	}
}
