/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.beust.jcommander.ParameterException;

public class ResetPasswordTest extends AbstractHandlerTest {
	private static final String RESET_PASSWORD_COMMAND = "reset_password";

	private static final String TESTUSERNAME = "DummyUser";
	private static final String TESTANSWER = "42";

	@Test
	public void testResetPassword() {
		commandline.runCommand(RESET_PASSWORD_COMMAND, "-u", TESTUSERNAME, "-a", TESTANSWER);
		assertEquals("/users/reset", restutils.apiNameGiven);
		assertEquals(TESTUSERNAME, restutils.bodyDataGiven.get("username"));
		assertEquals(TESTANSWER, restutils.bodyDataGiven.get("answer"));
		assertEquals(2, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testResetPasswordUsernameNull() {
		commandline.runCommand(RESET_PASSWORD_COMMAND, "-u", "-a", TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testResetPasswordUsernameNotGiven() {
		commandline.runCommand(RESET_PASSWORD_COMMAND, "-a", TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testResetPasswordAnswerNull() {
		commandline.runCommand(RESET_PASSWORD_COMMAND, "-u", TESTUSERNAME, "-a");
	}

	@Test(expected = ParameterException.class)
	public void testResetPasswordAnswerNotGiven() {
		commandline.runCommand(RESET_PASSWORD_COMMAND, "-u", TESTUSERNAME);
	}
}
