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
 * Command to change the password for the current user
 */
@Parameters(commandDescription = "Change the password of the currently set local user")
public class ChangePassword extends AbstractCommandHandler {
	@Parameter(names = { "-p", "--password" }, description = "Password of new user", required = true)
	public String password;

	@Override
	public void handleCommand() {
		getLogger().log("Change Password for current user");
		getServices().getUserStore().storeChangedPassword(password);
		UserInfo localUser = getServices().getCurrentUserStore().loadUserData();
		getServices().getCurrentUserStore().storeUserData(new UserInfo(localUser.getUsername(), password));
		getLogger().log("Password changed");
	}
}
