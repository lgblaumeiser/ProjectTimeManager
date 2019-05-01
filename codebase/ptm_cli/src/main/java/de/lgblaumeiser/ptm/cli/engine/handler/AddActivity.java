/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static de.lgblaumeiser.ptm.datamanager.model.Activity.newActivity;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.Activity;

/**
 * Command to add an activity
 */
@Parameters(commandDescription = "Define a new activity for bookings")
public class AddActivity extends AbstractCommandHandler {
    private final static String DUMMYUSER = "Dummy";

    @Parameter(names = { "-pn",
            "--project-name" }, description = "Name of the overall project", required = true)
    public String projectName;

    @Parameter(names = { "-an", "--activity-name" }, description = "Name of the activity", required = true)
    public String activityName;

    @Parameter(names = { "-pi", "--project-id" }, description = "Project identifier", required = true)
    public String projectId;

    @Parameter(names = { "-ai", "--activity-id" }, description = "Activity identifier", required = true)
    public String activityId;

    @Override
    public void handleCommand() {
        getLogger().log("Add activity "
                + projectName
                + ":"
                + activityName
                + " with id "
                + projectId
                + ":"
                + activityId);
        Activity newAct = getServices().getActivityStore().store(
                newActivity()
                        .setProjectName(projectName)
                        .setActivityName(activityName)
                        .setProjectId(projectId)
                        .setActivityId(activityId)
                        .setUser(DUMMYUSER)
                        .build());
        getLogger().log("Activity added: " + newAct.toString() + "\n");
    }
}
