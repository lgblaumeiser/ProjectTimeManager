/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class that allows to run analysis on the data
 */
public class DataAnalysisService {
	private final Map<String, Analysis> analysisStore = new HashMap<>();

	/**
	 * Run an analysis with id analyzerId and the given parameters
	 *
	 * @param analyzerId The id of the analyzer to run
	 * @param parameter  Parameters given to the analyzer
	 * @return The result as an implemented analysis result interface
	 */
	public Collection<Collection<Object>> analyze(final String analyzerId, final Collection<String> parameter) {
		assertState(stringHasContent(analyzerId));
		assertState(parameter != null);
		Analysis analysis = analysisStore.get(analyzerId);
		assertState(analysis != null);
		return analysis.analyze(parameter);
	}

	DataAnalysisService addAnalysis(final String id, final Analysis analysis) {
		analysisStore.put(id, analysis);
		return this;
	}

	DataAnalysisService() {
		// Only in package creation
	}
}
