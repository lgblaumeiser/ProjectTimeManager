/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.datamanager.model.User.newUser;
import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Tests for the user class
 */
public class UserTest {
	private final static String USERNAME = "myName";
	private final static String USERNAME2 = "anotherName";
	private final static String PASSWORD = "KLJDHSFSZD§(789hdfgjh";
	private final static String PASSWORD2 = "ldsjfaiu98457hHJKHKJ";

	/**
	 * Positive test method for newLineActivity with activity id
	 */
	@Test
	public final void testNewActivityPositive() {
		User newUser = newUser().setUsername(USERNAME).setPassword(PASSWORD).build();
		assertEquals(USERNAME, newUser.getUsername());
		assertEquals(PASSWORD, newUser.getPassword());
	}

	@Test(expected = IllegalStateException.class)
	public final void testWithBlankName() {
		newUser().setUsername(emptyString()).setPassword(PASSWORD).build();
	}

	@Test(expected = IllegalStateException.class)
	public final void testWithBlankNumber() {
		newUser().setUsername(USERNAME).setPassword(emptyString()).build();
	}

	/**
	 * Test for equals and hashcode
	 */
	@Test
	public final void testEquals() {
		User newUser1 = newUser().setUsername(USERNAME).setPassword(PASSWORD).build();
		User newUser2 = newUser().setUsername(USERNAME).setPassword(PASSWORD).build();
		User newUser3 = newUser().setUsername(USERNAME2).setPassword(PASSWORD2).build();

		assertTrue(newUser1.equals(newUser2));
		assertTrue(newUser1.hashCode() == newUser2.hashCode());
		assertFalse(newUser1.equals(newUser3));
	}
}
