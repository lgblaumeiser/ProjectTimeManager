/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 *
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.datamanager.model;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;
import static java.lang.Long.valueOf;
import static java.lang.String.format;
import static java.util.Objects.hash;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data structure for representation of an activity. An activity represents a
 * booking number under which work is done. Each work booking is assigned to an
 * activity and there are reports on hours spent on activities.
 */
public class User {
    public static class UserBuilder {
        private static final String REGEX = "^(.+)@(.+)$";
        private static final Pattern PATTERN = Pattern.compile(REGEX);

        private Long id = valueOf(-1);
        private boolean admin = false;
        private String username;
        private String password;
        private String email;
        private String question;
        private String answer;

        private UserBuilder(final User user) {
            id = user.getId();
            username = user.getUsername();
            password = user.getPassword();
            email = user.getEmail();
            question = user.getQuestion();
            answer = user.getAnswer();
            admin = user.isAdmin();
        }

        private UserBuilder() {
            // Nothing to do
        }

        public UserBuilder setUsername(final String username) {
            this.username = username;
            return this;
        }

        public UserBuilder setPassword(final String password) {
            this.password = password;
            return this;
        }

        /**
         * @param email the email to set
         */
        public UserBuilder setEmail(String email) {
            this.email = email;
            return this;
        }

        /**
         * @param question the question to set
         */
        public UserBuilder setQuestion(String question) {
            this.question = question;
            return this;
        }

        /**
         * @param answer the answer to set
         */
        public UserBuilder setAnswer(String answer) {
            this.answer = answer;
            return this;
        }

        public UserBuilder setAdmin(final boolean admin) {
            this.admin = admin;
            return this;
        }

        /**
         * @return An unmodifiable activity representing the data given to the builder,
         *         Non null, returns with exception if the data is invalid
         */
        public User build() {
            checkData();
            return new User(id, username, password, email, question, answer, admin);
        }

        private void checkData() {
            assertState(stringHasContent(username));
            assertState(stringHasContent(password));
            assertState(stringHasContent(email));
            assertState(isValidEmailAddress(email));
            assertState(stringHasContent(question));
            assertState(stringHasContent(answer));
        }

        private boolean isValidEmailAddress(String email) {
            Matcher matcher = PATTERN.matcher(email);
            return matcher.matches();
        }
    }

    /**
     * Creates a new activity builder with no data set.
     *
     * @return A new activity builder, never null
     */
    public static UserBuilder newUser() {
        return new UserBuilder();
    }

    /**
     * Change an existing activity by providing a builder preset with the activity
     * data
     *
     * @return A new activity builder, never null
     */
    public UserBuilder changeUser() {
        return new UserBuilder(this);
    }

    private String username;
    private String password;
    private String email;
    private String question;
    private String answer;
    private boolean admin;
    private Long id = valueOf(-1);

    /**
     * @return The internal id of the activity. Automatically created by storage
     *         system
     */
    public Long getId() {
        return id;
    }

    /**
     * @return True, if user has Admin rights
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * @return Name of the activity. Non null
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return Booking number of the activities category. Non null
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the question
     */
    public String getQuestion() {
        return question;
    }

    /**
     * @return the answer
     */
    public String getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return format("User: Username: %s, E-Mail: %s, Question: %s, Id: %d%s",
                username, email, question, id, admin ? ", Admin" : "");
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof User) {
            User user = (User) obj;
            return username.equals(user.username);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hash(username);
    }

    private User(final Long id, final String username, final String password, final String email, final String question,
            final String answer, final boolean admin) {
        this.id = id;
        this.admin = admin;
        this.username = username;
        this.password = password;
        this.email = email;
        this.question = question;
        this.answer = answer;
    }

    private User() {
        // Only needed for deserialization
    }
}
