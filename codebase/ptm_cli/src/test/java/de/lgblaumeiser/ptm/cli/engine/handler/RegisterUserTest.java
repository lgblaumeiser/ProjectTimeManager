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

public class RegisterUserTest extends AbstractHandlerTest {
	private static final String REGISTER_USER_COMMAND = "register_user";

	private static final String TESTUSERNAME = "MyTestUser";
	private static final String TESTPASSWORD = "DummyPwd";

	@Test
	public void testRegisterUserClean() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD);
		assertEquals("/users/register", restutils.apiNameGiven);
		assertEquals(TESTUSERNAME, restutils.bodyDataGiven.get("username"));
		assertEquals(TESTPASSWORD, restutils.bodyDataGiven.get("password"));
		assertEquals(2, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserTwoParamFirstNull() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", "-p", TESTPASSWORD);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserTwoParamSecondNull() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p");
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserOneParam() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserNoParam() {
		commandline.runCommand(REGISTER_USER_COMMAND);
	}
}
