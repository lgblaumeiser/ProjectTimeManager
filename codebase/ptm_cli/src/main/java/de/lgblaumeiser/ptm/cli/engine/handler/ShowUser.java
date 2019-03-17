/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.util.Optional;

import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.User;

/**
 * Command to register a new user
 */
@Parameters(commandDescription = "Show user data for the currently set local user")
public class ShowUser extends AbstractCommandHandler {
	@Override
	public void handleCommand() {
		Optional<User> user = getServices().getUserStore().getCurrentUser();
		getLogger().log(user.map(u -> "User: " + u).orElse("No user found in backend for currently set user"));
	}
}
