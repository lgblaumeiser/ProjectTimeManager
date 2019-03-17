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
	private static final String TESTEMAIL = "abc@xyz.com";
	private static final String TESTQUESTION = "What the heck?";
	private static final String TESTANSWER = "42";

	@Test
	public void testRegisterUserClean() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD, "-e", TESTEMAIL, "-q",
				TESTQUESTION, "-a", TESTANSWER);
		assertEquals("/users/register", restutils.apiNameGiven);
		assertEquals(TESTUSERNAME, restutils.bodyDataGiven.get("username"));
		assertEquals(TESTPASSWORD, restutils.bodyDataGiven.get("password"));
		assertEquals(TESTEMAIL, restutils.bodyDataGiven.get("email"));
		assertEquals(TESTQUESTION, restutils.bodyDataGiven.get("question"));
		assertEquals(TESTANSWER, restutils.bodyDataGiven.get("answer"));
		assertEquals(5, restutils.bodyDataGiven.size());
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserUsernameNull() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", "-p", TESTPASSWORD, "-e", TESTEMAIL, "-q", TESTQUESTION,
				"-a", TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserUsernameNotGiven() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-p", TESTPASSWORD, "-e", TESTEMAIL, "-q", TESTQUESTION, "-a",
				TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserPasswordNull() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", "-e", TESTEMAIL, "-q", TESTQUESTION,
				"-a", TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserPasswordNotGiven() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-e", TESTEMAIL, "-q", TESTQUESTION, "-a",
				TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserEmailNull() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD, "-e", "-q", TESTQUESTION,
				"-a", TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserEmailNotGiven() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD, "-q", TESTQUESTION, "-a",
				TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserQuestionNull() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD, "-e", TESTEMAIL, "-q",
				"-a", TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserQuestionNotGiven() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD, "-e", TESTEMAIL, "-a",
				TESTANSWER);
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserAnswerNull() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD, "-e", TESTEMAIL, "-q",
				TESTQUESTION, "-a");
	}

	@Test(expected = ParameterException.class)
	public void testRegisterUserAnswerNotGiven() {
		commandline.runCommand(REGISTER_USER_COMMAND, "-u", TESTUSERNAME, "-p", TESTPASSWORD, "-e", TESTEMAIL, "-q",
				TESTQUESTION);
	}
}
