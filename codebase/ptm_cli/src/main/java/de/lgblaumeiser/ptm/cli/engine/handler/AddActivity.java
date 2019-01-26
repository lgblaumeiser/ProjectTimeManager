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

	@Parameter(names = { "-n", "--name" }, description = "Name of the new activity", required = true)
	public String name;

	@Parameter(names = { "-i", "--identifier" }, description = "Project identifier of the activity", required = true)
	public String identifier;

	@Override
	public void handleCommand() {
		getLogger().log("Add activity " + name + " with id " + identifier);
		Activity newAct = getServices().getActivityStore()
				.store(newActivity().setActivityName(name).setBookingNumber(identifier).setUser(DUMMYUSER).build());
		getLogger().log("Activity added: " + newAct.toString() + "\n");
	}
}
