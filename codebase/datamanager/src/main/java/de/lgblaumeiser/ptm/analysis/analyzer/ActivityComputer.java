/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static java.lang.String.format;

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
    protected String getHeadlineNameElement() {
        return "Activity";
    }

    @Override
    protected String getHeadlineIdElement() {
        return "Activity Id";
    }

    @Override
    protected String getElementName(final Activity activity) {
        return formatActivityString(activity.getProjectName(), activity.getActivityName());
    }

    @Override
    protected String getElementId(final Activity activity) {
        return formatActivityString(activity.getProjectId(), activity.getActivityId());
    }

    private String formatActivityString(final String part1, final String part2) {
        return format("%s:%s", part1, part2);
    }

    public ActivityComputer(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
        super(bStore, aStore);
    }
}
