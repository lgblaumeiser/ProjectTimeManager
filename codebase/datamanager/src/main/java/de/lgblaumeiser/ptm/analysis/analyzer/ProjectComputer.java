/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * An analysis to compute the amount of hours per project. The computer
 * calculates the percentage of a project on the overall hours and maps these
 * percentages to the amount of 8 hours per booking day. This way, this fulfills
 * the requirements of the author concerning his time keeping.
 */
public class ProjectComputer extends BaseProjectComputer {
    @Override
    protected String indexGetter(final Activity activity) {
        return activity.getProjectId();
    }

    @Override
    protected String getHeadlineNameElement() {
        return "Project Name";
    }

    @Override
    protected String getHeadlineIdElement() {
        return "Project Id";
    }

    @Override
    protected String getElementName(Activity activity) {
        return activity.getProjectName();
    }

    @Override
    protected String getElementId(final Activity activity) {
        return activity.getProjectId();
    }

    public ProjectComputer(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
        super(bStore, aStore);
    }
}
