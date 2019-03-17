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

public class ShowUserTest extends AbstractHandlerTest {
	private static final String SHOW_USER_COMMAND = "show_user";

	@Test
	public void testShowUserClean() {
		commandline.runCommand(SHOW_USER_COMMAND);
		assertEquals("/users/name", restutils.apiNameGiven);
	}
}
