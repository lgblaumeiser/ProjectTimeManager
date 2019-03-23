/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static de.lgblaumeiser.ptm.util.Utils.getFirstFromCollection;
import static java.util.Optional.empty;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import de.lgblaumeiser.ptm.rest.UserRestController.UserBody;

/**
 * Test the activity rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest extends AbstractControllerTest {
    private static final String USER_RESET_API = "/users/reset";
    private static final String USER_RESOURCE_API = "/users/name";

    @Test
    public void testRegisterUser() throws Exception {
        createDefaultUser();

        performGet(USER_RESOURCE_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTUSER_USERNAME)));

        performGet(USER_RESOURCE_API, TESTUSER_USERNAME, TESTUSER_PASSWORD2)
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testRegisterUserTwiceFails() throws Exception {
        createDefaultUser();

        UserBody data = getFirstFromCollection(createDefaultUserdata());
        data.password = TESTUSER_PASSWORD2;
        performPost(USER_REGISTRATION_API, data, empty())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testChangePassword() throws Exception {
        createDefaultUser();

        UserBody data = new UserBody();
        data.password = TESTUSER_PASSWORD2;
        performPost(USER_RESOURCE_API, data, TESTUSER_USERNAME, TESTUSER_PASSWORD)
                .andExpect(status().isOk());

        performGet(USER_RESOURCE_API, TESTUSER_USERNAME, TESTUSER_PASSWORD2)
                .andExpect(status().isOk());
    }

    @Test
    public void testResetUser() throws Exception {
        createDefaultUser();

        UserBody data = new UserBody();
        data.username = "TestUser";
        data.answer = "42";
        MvcResult result = performPut(USER_RESET_API, data, empty())
                .andExpect(status().isOk())
                .andReturn();
        assertEquals(10, result.getResponse().getContentAsString().length());
    }

    @Test
    public void testDeleteAllUserData() throws Exception {
        createDefaultUser();

        createDefaultActivity();

        createDefaultBooking();

        performDelete(USER_RESOURCE_API, TESTUSER_USERNAME2, TESTUSER_PASSWORD2)
                .andExpect(status().isOk());

        performGet(BOOKING_DAY_API + DATE_STRING, TESTUSER_USERNAME, TESTUSER_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("activity\":" + ACTIVITY_ID_1)))
                .andExpect(content().string(containsString("user\":\"" + TESTUSER_USERNAME)));

        performGet(ACTIVITY_RESOURCE_API, TESTUSER_USERNAME, TESTUSER_PASSWORD)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_NAME)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)));

        performGet(BOOKING_DAY_API + DATE_STRING, TESTUSER_USERNAME2, TESTUSER_PASSWORD2)
                .andExpect(status().is4xxClientError());

        performGet(ACTIVITY_RESOURCE_API, TESTUSER_USERNAME2, TESTUSER_PASSWORD2)
                .andExpect(status().is4xxClientError());
    }
}
