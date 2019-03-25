/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static java.util.Arrays.asList;

import java.util.Collection;

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
    protected Collection<String> getHeadlineActivityElements() {
        return asList("Project Id");
    }

    @Override
    protected Collection<String> getFootlineActivityElements() {
        return asList("Total");
    }

    @Override
    protected Collection<String> getKeyItems(final Activity activity) {
        return asList(activity.getProjectId());
    }

    @Override
    protected String getSortCriteriaForResultLine(final Collection<String> line) {
        return getIndexFromCollection(line, 0);
    }

    public ProjectComputer(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
        super(bStore, aStore);
    }
}
