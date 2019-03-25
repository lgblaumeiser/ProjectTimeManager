/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis;

import de.lgblaumeiser.ptm.analysis.analyzer.ActivityComputer;
import de.lgblaumeiser.ptm.analysis.analyzer.HourComputer;
import de.lgblaumeiser.ptm.analysis.analyzer.ProjectComputer;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * Provider for the data analysis and the standard implementations
 */
public class AnalysisProvider {
    private static final String ANALYSIS_HOURS_ID = "HOURS";
    private static final String ANALYSIS_ACTIVITIES_ID = "ACTIVITIES";
    private static final String ANALYSIS_PROJECTS_ID = "PROJECTS";

    public DataAnalysisService getAnalysisService(final ObjectStore<Activity> aStore,
            final ObjectStore<Booking> bStore) {
        HourComputer hourComputer = new HourComputer(bStore);
        ActivityComputer activityComputer = new ActivityComputer(bStore, aStore);
        ProjectComputer projectComputer = new ProjectComputer(bStore, aStore);
        return new DataAnalysisService().addAnalysis(ANALYSIS_HOURS_ID, hourComputer)
                .addAnalysis(ANALYSIS_ACTIVITIES_ID, activityComputer)
                .addAnalysis(ANALYSIS_PROJECTS_ID, projectComputer);
    }
}
