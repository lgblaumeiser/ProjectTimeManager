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
@Parameters(commandDescription = "Reset the password for a user and set it locally")
public class ResetPassword extends AbstractCommandHandler {
    @Parameter(names = { "-u", "--username" }, description = "Username of the user", required = true)
	public String username;

	@Parameter(names = { "-a", "--answer" }, description = "Answer for question on password reset", required = true)
	public String answer;

	@Override
	public void handleCommand() {
		getLogger().log("Reset password for user " + username);
		String newPassword = getServices().getUserStore().resetPassword(username, answer);
		getServices().getCurrentUserStore().storeUserData(new UserInfo(username, newPassword));
		getLogger().log("Password resetted, new password is: " + newPassword);
	}
}
