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

public class SetUserTest extends AbstractHandlerTest {
	private static final String SET_USER_COMMAND = "set_user";

	private static final String TESTUSERNAME = "MyTestUser";
	private static final String TESTPASSWORD = "DummyPwd";

	@Test
	public void testSetUserClean() {
		commandline.runCommand(SET_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD);
		assertEquals(TESTUSERNAME, userstoreutils.storedUser.getUsername());
		assertEquals(TESTPASSWORD, userstoreutils.storedUser.getPassword());
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserTwoParamFirstNull() {
		commandline.runCommand(SET_USER_COMMAND, "-u", "-p", TESTPASSWORD);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserTwoParamSecondNull() {
		commandline.runCommand(SET_USER_COMMAND, "-u", TESTUSERNAME, "-p");
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserOneParam() {
		commandline.runCommand(SET_USER_COMMAND, "-u", TESTUSERNAME);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserNoParam() {
		commandline.runCommand(SET_USER_COMMAND);
	}
}
