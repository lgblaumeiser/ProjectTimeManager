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

public class DeleteUserTest extends AbstractHandlerTest {
	private static final String DELETE_USER_COMMAND = "delete_user";

	@Test
	public void testDeleteUserClean() {
		commandline.runCommand(DELETE_USER_COMMAND, "--confirm");
		assertEquals("/users/name", restutils.apiNameGiven);
	}

	@Test(expected = ParameterException.class)
	public void testDeleteUserNoParam() {
		commandline.runCommand(DELETE_USER_COMMAND);
	}
}
