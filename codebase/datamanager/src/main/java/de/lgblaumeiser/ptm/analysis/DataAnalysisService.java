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
     * @param analyzerId      The id of the analyzer to run
     * @param user            The use whose data is used for the analysis
     * @param period          The period for which the calculation should be done
     * @return The result as an implemented analysis result interface
     */
    public Collection<Collection<String>> analyze(final String analyzerId, final String user, CalculationPeriod period) {
        assertState(stringHasContent(analyzerId));
        assertState(stringHasContent(user));

        Analysis analysis = analysisStore.get(analyzerId);
        assertState(analysis != null);

        return analysis.analyze(period, user);
    }

    DataAnalysisService addAnalysis(final String id, final Analysis analysis) {
        analysisStore.put(id, analysis);
        return this;
    }

    DataAnalysisService() {
        // Only in package creation
    }
}
