/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static de.lgblaumeiser.ptm.datamanager.model.User.newUser;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.User;

/**
 * Command to register a new user
 */
@Parameters(commandDescription = "Register a new user")
public class RegisterUser extends AbstractCommandHandler {
	@Parameter(names = { "-u", "--username" }, description = "Username of new user", required = true)
	public String username;

	@Parameter(names = { "-p", "--password" }, description = "Password of new user", required = true)
	public String password;

	@Parameter(names = { "-e", "--email" }, description = "Email address of new user", required = true)
	public String email;

	@Parameter(names = { "-q", "--question" }, description = "Question for password reset of new user", required = true)
	public String question;

	@Parameter(names = { "-a", "--answer" }, description = "Answer for question on password reset", required = true)
	public String answer;

	@Override
	public void handleCommand() {
		getLogger().log("Add user " + username);
		User newUser = getServices().getUserStore().store(newUser().setUsername(username).setPassword(password)
				.setEmail(email).setQuestion(question).setAnswer(answer).build());
		getLogger().log("User added with id: " + newUser.getId() + " and username: " + newUser.getUsername() + "\n");
	}
}
