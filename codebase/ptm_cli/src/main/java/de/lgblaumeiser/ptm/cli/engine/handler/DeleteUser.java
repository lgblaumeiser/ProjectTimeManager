/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

/**
 * Command to register a new user
 */
@Parameters(commandDescription = "Register a new user")
public class DeleteUser extends AbstractCommandHandler {
	@Parameter(names = {
			"--confirm" }, description = "Explicitely requested, since action deletes all user data", required = true)
	public boolean confirmed;

	@Override
	public void handleCommand() {
		getLogger().log("Delete current user");
		getServices().getUserStore().deleteCurrentUser();
		getLogger().log("User deleted\n");
	}
}
