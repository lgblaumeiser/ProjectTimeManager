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
@SuppressWarnings("unused")
public class UserTest {
    private final static String USERNAME = "myName";
    private final static String USERNAME2 = "anotherName";
    private final static String PASSWORD = "KLJDHSFSZD§(789hdfgjh";
    private final static String PASSWORD2 = "ldsjfaiu98457hHJKHKJ";
    private final static String EMAIL = "abc@xyz.com";
    private final static String EMAIL2 = "def@xyz.com";
    private final static String WRONGEMAIL = "abc";
    private final static String QUESTION = "What the Heck?";
    private final static String ANSWER = "42";

    /**
     * Positive test method for newLineActivity with activity id
     */
    @Test
    public final void testNewUserPositive() {
        User newUser = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(EMAIL)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
        assertEquals(USERNAME, newUser.getUsername());
        assertEquals(PASSWORD, newUser.getPassword());
        assertEquals(EMAIL, newUser.getEmail());
        assertEquals(QUESTION, newUser.getQuestion());
        assertEquals(ANSWER, newUser.getAnswer());
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankName() {
        User newUser = newUser()
                .setUsername(emptyString())
                .setPassword(PASSWORD)
                .setEmail(EMAIL)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankNumber() {
        User newUser = newUser()
                .setUsername(USERNAME)
                .setPassword(emptyString())
                .setEmail(EMAIL)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankEmail() {
        User newUser = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(emptyString())
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithWrongEmail() {
        User newUser = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(WRONGEMAIL)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankQuestion() {
        User newUser = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(EMAIL)
                .setQuestion(emptyString())
                .setAnswer(ANSWER)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public final void testWithBlankAnswern() {
        User newUser = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(EMAIL)
                .setQuestion(QUESTION)
                .setAnswer(emptyString())
                .build();
    }

    /**
     * Test for equals and hashcode
     */
    @Test
    public final void testEquals() {
        User newUser1 = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(EMAIL)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
        User newUser2 = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(EMAIL)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
        User newUser3 = newUser()
                .setUsername(USERNAME)
                .setPassword(PASSWORD)
                .setEmail(EMAIL2)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();
        User newUser4 = newUser()
                .setUsername(USERNAME2)
                .setPassword(PASSWORD2)
                .setEmail(EMAIL)
                .setQuestion(QUESTION)
                .setAnswer(ANSWER)
                .build();

        assertTrue(newUser1.equals(newUser2));
        assertTrue(newUser1.hashCode() == newUser2.hashCode());
        assertTrue(newUser1.equals(newUser3));
        assertTrue(newUser1.hashCode() == newUser3.hashCode());
        assertFalse(newUser1.equals(newUser4));
    }
}
