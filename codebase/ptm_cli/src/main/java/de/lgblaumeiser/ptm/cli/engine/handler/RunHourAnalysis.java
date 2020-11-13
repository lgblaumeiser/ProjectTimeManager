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

	@Override
	protected String analysisId() {
		return ANALYSIS_HOURS_ID;
	}

	@Override
	protected DefaultFunction defaultPeriod() {
		return monthDefault;
	}
}
