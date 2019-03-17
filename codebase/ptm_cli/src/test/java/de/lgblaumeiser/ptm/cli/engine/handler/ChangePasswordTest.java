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

public class ChangePasswordTest extends AbstractHandlerTest {
	private static final String CHANGE_PASSWORD_COMMAND = "change_password";

	private static final String TESTPASSWORD = "DummyPwd";

	@Test
	public void testChangePassword() {
		commandline.runCommand(CHANGE_PASSWORD_COMMAND, "-p", TESTPASSWORD);
		assertEquals("/users/name", restutils.apiNameGiven);
		assertEquals(TESTPASSWORD, restutils.bodyDataGiven.get("password"));
		assertEquals(1, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testChangePasswordEmptyParameter() {
		commandline.runCommand(CHANGE_PASSWORD_COMMAND, "-p");
	}

	@Test(expected = ParameterException.class)
	public void testChangePasswordNoParam() {
		commandline.runCommand(CHANGE_PASSWORD_COMMAND);
	}
}
