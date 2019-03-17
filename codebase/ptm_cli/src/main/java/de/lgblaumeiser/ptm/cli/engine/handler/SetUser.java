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
import de.lgblaumeiser.ptm.cli.engine.UserStore.UserInfo;

/**
 * Command to register a new user
 */
@Parameters(commandDescription = "Set the user to be used in cli and store it encrypted in local config")
public class SetUser extends AbstractCommandHandler {
	@Parameter(names = { "-u", "--username" }, description = "Username of the user", required = true)
	public String username;

	@Parameter(names = { "-p", "--password" }, description = "Password of the user", required = true)
	public String password;

	@Override
	public void handleCommand() {
		getLogger().log("Set user " + username + " ...");
		getServices().getCurrentUserStore().storeUserData(new UserInfo(username, password));
		getLogger().log("... User stored");
	}
}
