/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static java.util.Arrays.asList;

import java.util.Collection;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * An analysis to compute the amount of hours per a activity. The computer
 * calculates the percentage of an activity on the overall hours and maps these
 * percentages to the amount of 8 hours per booking day. This way, this fulfills
 * the requirements of the author concerning his time keeping.
 */
public class ActivityComputer extends BaseProjectComputer {
    @Override
    protected String indexGetter(final Activity activity) {
        return activity.getId().toString();
    }

    @Override
    protected Collection<String> getHeadlineActivityElements() {
        return asList("Activity", "Project Id", "Project Activity");
    }

    @Override
    protected Collection<String> getFootlineActivityElements() {
        return asList("Total", emptyString(), emptyString());
    }

    @Override
    protected Collection<String> getKeyItems(final Activity activity) {
        return asList(activity.getActivityName(), activity.getProjectId(), activity.getProjectActivity());
    }

    @Override
    protected String getSortCriteriaForResultLine(final Collection<String> line) {
        return getProjectIdFromResultLine(line) + "_" + getProjectSubidFromResultLine(line);
    }

    private String getProjectIdFromResultLine(final Collection<String> line) {
        return getIndexFromCollection(line, 1);
    }

    private String getProjectSubidFromResultLine(final Collection<String> line) {
        return getIndexFromCollection(line, 2);
    }

    public ActivityComputer(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
        super(bStore, aStore);
    }
}
