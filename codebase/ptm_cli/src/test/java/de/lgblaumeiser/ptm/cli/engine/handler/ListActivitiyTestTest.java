/*
 * Copyright 2016, 2017 Lars Geyer-Blaumeiser <lgblaumeiser@gmail.com>
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ListActivitiyTestTest extends AbstractHandlerTest {
	private ListActivity testee = new ListActivity();

	@Test
	public void testListActivity() {
		testee.handleCommand(emptyList());
		assertTrue(logger.logMessages.toString().contains("======================================"));
	}
}