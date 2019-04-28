/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.util.Optional;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.Activity;

/**
 * Command to add an activity
 */
@Parameters(commandDescription = "Change an existing activity for bookings")
public class ChangeActivity extends AbstractCommandHandler {
	@Parameter(names = { "-a", "--activity" }, description = "Id of activity to change", required = true)
	private Long id;

	@Parameter(names = { "-n",
			"--name" }, description = "New name of the activity", converter = OptionalStringConverter.class)
	private Optional<String> name = Optional.empty();

	@Parameter(names = { "-i",
			"--project-id" }, description = "Change the project id", converter = OptionalStringConverter.class)
	private Optional<String> pid = Optional.empty();

	@Parameter(names = { "-s",
			"--project-sub-id" }, description = "Change the project sub id", converter = OptionalStringConverter.class)
	private Optional<String> pactivity = Optional.empty();

	@Parameter(names = { "--hidden" }, description = "Hide the activity")
	private boolean hidden = false;

	@Parameter(names = { "--visible" }, description = "Make activity visible, if hidden")
	private boolean visible = false;

	@Override
	public void handleCommand() {
		getLogger().log("Change activity with id " + id);
		Activity oldAct = getActivityById(id);
		Activity.ActivityBuilder chgAct = oldAct.changeActivity();
		name.ifPresent(chgAct::setActivityName);
		pid.ifPresent(chgAct::setProjectId);
		pactivity.ifPresent(chgAct::setActivityId);
		if (oldAct.isHidden() && visible)
			chgAct.setHidden(false);
		if (!oldAct.isHidden() && hidden)
			chgAct.setHidden(true);
		Activity newAct = getServices().getActivityStore().store(chgAct.build());
		getLogger().log("Activity changed " + newAct.toString() + "\n");
	}

	private Activity getActivityById(final Long id) {
		return getServices().getActivityStore().retrieveById(id).orElseThrow(IllegalStateException::new);
	}
}
