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

	@Override
	protected String analysisId() {
		return ANALYSIS_PROJECTS_ID;
	}

	@Override
	protected DefaultFunction defaultPeriod() {
		return dayDefault;
	}
}
