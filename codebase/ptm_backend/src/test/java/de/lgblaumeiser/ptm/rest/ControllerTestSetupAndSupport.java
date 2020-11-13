/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static java.lang.System.setProperty;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.lgblaumeiser.ptm.rest.ActivityRestController.ActivityBody;
import de.lgblaumeiser.ptm.rest.BookingRestController.BookingBody;
import de.lgblaumeiser.ptm.rest.UserRestController.UserBody;

public class ControllerTestSetupAndSupport {
    protected static final String USER_REGISTRATION_API = "/users/register";
    protected static final String ACTIVITY_RESOURCE_API = "/activities";
    protected static final String BOOKING_DAY_API = "/bookings/day/";
    protected static final String BOOKING_RESOURCE_API = "/bookings/id/";

    protected static final String TESTUSER_USERNAME = "TestUser";
    protected static final String TESTUSER_PASSWORD = "DummyPwd";
    protected static final String TESTUSER2_USERNAME = "MyTestUser2";
    protected static final String TESTUSER2_PASSWORD = "AnotherPasswd";
    protected static final String TESTUSER_EMAIL = "abc@xyz.com";
    protected static final String TESTUSER_QUESTION = "What the Heck?";
    protected static final String TESTUSER_ANSWER = "42";
    protected static final String TESTUSER_RESOURCE_URL_SUFFIX = "/users/1";

    protected static final String TESTACT1_PNAME = "MyTestProject";
    protected static final String TESTACT1_NAME = "MyTestActivity";
    protected static final String TESTACT1_PRJ = "0815";
    protected static final String TESTACT1_SUB = "1";
    protected static final String TESTACT2_PNAME = "MyOtherTestProject";
    protected static final String TESTACT2_NAME = "MyOtherTestActivity";
    protected static final String TESTACT2_PRJ = "4711";
    protected static final String TESTACT2_SUB = "2";

    protected static final String ACTIVITY_ID_1 = "1";
    protected static final String ACTIVITY_ID_2 = "2";

    protected static final String TESTBOOKING1_STARTTIME = LocalTime.of(8, 15).format(ISO_LOCAL_TIME);
    protected static final String TESTBOOKING1_ENDTIME = LocalTime.of(9, 0).format(ISO_LOCAL_TIME);
    protected static final String TESTBOOKING2_STARTTIME = LocalTime.of(10, 15).format(ISO_LOCAL_TIME);
    protected static final String TESTBOOKING3_STARTTIME = TESTBOOKING1_ENDTIME;
    protected static final String TESTBOOKING3_ENDTIME = TESTBOOKING2_STARTTIME;
    protected static final String TESTBOOKING4_STARTTIME = LocalTime.of(11, 0).format(ISO_LOCAL_TIME);
    protected static final String TESTBOOKING4_ENDTIME = LocalTime.of(12, 15).format(ISO_LOCAL_TIME);

    protected static final LocalDate DATE = LocalDate.now();
    protected static final String DATE_STRING = DATE.format(ISO_LOCAL_DATE);

    protected static final String EMPTY_JSON_ARRAY = "[]";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    private File tempFolder;

    @Before
    public void before() throws IOException {
        tempFolder = Files.createTempDirectory("ptm").toFile();
        String tempStorage = new File(tempFolder, ".ptm").getAbsolutePath();
        setProperty("ptm.filestore", tempStorage);
    }

    @After
    public void after() throws IOException {
        forceDelete(tempFolder);
    }

    protected void createDefaultUser() throws Exception {
        createDefaultUserdata().stream().forEach(data -> createUser(data));
    }

    protected Collection<UserBody> createDefaultUserdata() {
        UserBody data1 = new UserBody();
        data1.username = TESTUSER_USERNAME;
        data1.password = TESTUSER_PASSWORD;
        data1.email = TESTUSER_EMAIL;
        data1.question = TESTUSER_QUESTION;
        data1.answer = TESTUSER_ANSWER;

        UserBody data2 = new UserBody();
        data2.username = TESTUSER2_USERNAME;
        data2.password = TESTUSER2_PASSWORD;
        data2.email = TESTUSER_EMAIL;
        data2.question = TESTUSER_QUESTION;
        data2.answer = TESTUSER_ANSWER;

        return asList(data1, data2);
    }

    protected void createUser(final UserBody data) {
        try {
            mockMvc.perform(
                    post(USER_REGISTRATION_API)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .content(objectMapper.writeValueAsString(data)))
                    .andDo(print())
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static class DefaultDataEntry<T> {
        private final T body;
        private final Optional<String> credentials;

        private DefaultDataEntry(final T body, final Optional<String> credentials) {
            this.body = body;
            this.credentials = credentials;
        }
    }

    protected void createDefaultActivity(final boolean sameUser) {
        createDefaultActivitydata(sameUser).stream().forEach(body -> createActivity(body.body, body.credentials));
    }

    protected Collection<DefaultDataEntry<ActivityBody>> createDefaultActivitydata(final boolean sameUser) {
        Collection<DefaultDataEntry<ActivityBody>> result = new ArrayList<>();

        Optional<String> user1 = getUser1();
        Optional<String> user2 = sameUser ? user1 : getUser2();

        ActivityBody data1 = new ActivityBody();
        data1.projectName = TESTACT1_PNAME;
        data1.activityName = TESTACT1_NAME;
        data1.projectId = TESTACT1_PRJ;
        data1.activityId = TESTACT1_SUB;
        data1.hidden = false;
        result.add(new DefaultDataEntry<ActivityBody>(data1, user1));

        ActivityBody data2 = new ActivityBody();
        data2.projectName = TESTACT2_NAME;
        data2.activityName = TESTACT2_NAME;
        data2.projectId = TESTACT2_PRJ;
        data2.activityId = TESTACT2_SUB;
        data2.hidden = false;
        result.add(new DefaultDataEntry<ActivityBody>(data2, user2));

        return result;
    }

    protected void createActivity(final ActivityBody data, final Optional<String> authorizationString) {
        try {
            performPost(ACTIVITY_RESOURCE_API, data, authorizationString)
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected void createDefaultBooking(final boolean sameUser) {
        createDefaultBookingdata(sameUser).stream()
                .forEach(body -> createBooking(DATE_STRING, body.body, body.credentials));
    }

    protected Collection<DefaultDataEntry<BookingBody>> createDefaultBookingdata(final boolean sameUser) {
        Collection<DefaultDataEntry<BookingBody>> result = new ArrayList<>();

        Optional<String> user1 = getUser1();
        Optional<String> user2 = sameUser ? user1 : getUser2();

        BookingBody data1 = new BookingBody();
        data1.activityId = ACTIVITY_ID_1;
        data1.starttime = TESTBOOKING1_STARTTIME;
        data1.endtime = TESTBOOKING1_ENDTIME;
        data1.comment = emptyString();
        result.add(new DefaultDataEntry<BookingBody>(data1, user1));

        BookingBody data2 = new BookingBody();
        data2.activityId = ACTIVITY_ID_1;
        data2.starttime = TESTBOOKING2_STARTTIME;
        data2.comment = emptyString();
        result.add(new DefaultDataEntry<BookingBody>(data2, user1));

        BookingBody data3 = new BookingBody();
        data3.activityId = ACTIVITY_ID_2;
        data3.starttime = TESTBOOKING3_STARTTIME;
        data3.endtime = TESTBOOKING3_ENDTIME;
        data3.comment = emptyString();
        result.add(new DefaultDataEntry<BookingBody>(data3, user2));

        BookingBody data4 = new BookingBody();
        data4.activityId = ACTIVITY_ID_2;
        data4.starttime = TESTBOOKING4_STARTTIME;
        data4.endtime = TESTBOOKING4_ENDTIME;
        data4.comment = emptyString();
        result.add(new DefaultDataEntry<BookingBody>(data4, user2));

        return result;
    }

    protected void createBooking(final String dateString, final BookingBody data,
            final Optional<String> authorizationString) {
        try {
            MvcResult result = performPost(BOOKING_DAY_API + dateString, data, authorizationString)
                    .andExpect(status().isCreated()).andReturn();
            assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    protected ResultActions performGet(final String apiname, final Optional<String> authorizationString)
            throws Exception {
        MockHttpServletRequestBuilder request = get(apiname)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        if (authorizationString.isPresent()) {
            request = request.header(HttpHeaders.AUTHORIZATION, authorizationString.get());
        }
        return mockMvc.perform(request).andDo(print());

    }

    protected ResultActions performGet(final String apiname) throws Exception {
        return performGet(apiname, getUser1());
    }

    protected ResultActions performPost(final String apiname, final Object data,
            final Optional<String> authorizationString) throws Exception {
        MockHttpServletRequestBuilder request = post(apiname)
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .content(objectMapper.writeValueAsString(data));
        if (authorizationString.isPresent()) {
            request = request.header(HttpHeaders.AUTHORIZATION, authorizationString.get());
        }
        return mockMvc.perform(request).andDo(print());
    }

    protected ResultActions performPut(final String apiname, final Object data,
            final Optional<String> authorizationString) throws Exception {
        MockHttpServletRequestBuilder request = put(apiname);
        if (data instanceof byte[]) {
            request = request
                    .contentType("application/zip")
                    .content((byte[]) data);
        } else {
            request = request
                    .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .content(objectMapper.writeValueAsString(data));
        }
        if (authorizationString.isPresent()) {
            request = request.header(HttpHeaders.AUTHORIZATION, authorizationString.get());
        }
        return mockMvc.perform(request).andDo(print());
    }

    protected ResultActions performDelete(final String apiname, final Optional<String> authorizationString)
            throws Exception {
        MockHttpServletRequestBuilder request = delete(apiname);
        if (authorizationString.isPresent()) {
            request = request.header(HttpHeaders.AUTHORIZATION, authorizationString.get());
        }
        return mockMvc.perform(request).andDo(print());
    }

    private Optional<String> createAuthorizationString(final String username, final String password) {
        return of("Basic " + Base64Utils.encodeToString((username + ":" + password).getBytes()));
    }

    protected Optional<String> getUser1() {
        return createAuthorizationString(TESTUSER_USERNAME, TESTUSER_PASSWORD);
    }

    protected Optional<String> getUser2() {
        return createAuthorizationString(TESTUSER2_USERNAME, TESTUSER2_PASSWORD);
    }

    protected Optional<String> getUserWrongPassword() {
        return createAuthorizationString(TESTUSER_USERNAME, TESTUSER2_PASSWORD);
    }

    protected String createHourString(final int hour, final int minute) {
        return LocalTime.of(hour, minute).format(ISO_LOCAL_TIME);
    }

}