/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.util.Collection;
import java.util.stream.Collectors;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.Activity;

/**
 * Command to list all the activities
 */
@Parameters(commandDescription = "List all activities available")
public class ListActivity extends AbstractCommandHandler {
    @Parameter(names = { "--hidden" }, description = "Show hidden activities")
	private boolean hidden = false;

	@Override
	public void handleCommand() {
		Collection<Activity> result = getServices().getActivityStore().retrieveAll();
		if (!hidden) {
			result = result.stream().filter(a -> !a.isHidden()).collect(Collectors.toList());
		}
		getPrinter().activityPrint(result);
	}
}
