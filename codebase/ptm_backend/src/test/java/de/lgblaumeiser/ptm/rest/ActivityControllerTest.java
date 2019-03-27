/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.lgblaumeiser.ptm.rest.ActivityRestController.ActivityBody;

/**
 * Test the activity rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ActivityControllerTest extends ControllerTestSetupAndSupport {
    private static final String ACTIVITY_HIDDEN_JSON = "\"hidden\":true";

    private static final String TESTACT_HIDDEN_NAME = "MyHiddenActivity";
    private static final String TESTACT_HIDDEN_PROJ = "1234";
    private static final String TESTACT_HIDDEN_SUB = "4";

    @Test
    public void testWithInitialSetupNoActivities() throws Exception {
        createDefaultUser();

        performGet(ACTIVITY_RESOURCE_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EMPTY_JSON_ARRAY)));
    }

    @Test
    public void testRoundtripCreateAndRetrieveActivity() throws Exception {
        createDefaultUser();

        createDefaultActivity(false);

        performGet(ACTIVITY_RESOURCE_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_NAME)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT1_SUB)));

        performGet(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_1)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_NAME)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT1_SUB)));

        ActivityBody data = new ActivityBody();
        data.activityName = TESTACT_HIDDEN_NAME;
        data.projectId = TESTACT_HIDDEN_PROJ;
        data.projectActivity = TESTACT_HIDDEN_SUB;
        data.hidden = true;
        createActivity(data, getUser1());

        performGet(ACTIVITY_RESOURCE_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(ACTIVITY_HIDDEN_JSON)))
                .andExpect(content().string(containsString(TESTACT_HIDDEN_NAME)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT1_SUB)));
    }

    @Test
    public void testActivitiesWithDifferentUser() throws Exception {
        createDefaultUser();

        createDefaultActivity(false);

        performGet(ACTIVITY_RESOURCE_API, getUser1())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_NAME)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT1_SUB)))
                .andExpect(content().string(not(containsString(TESTACT2_PRJ))));

        performGet(ACTIVITY_RESOURCE_API, getUser2())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT2_NAME)))
                .andExpect(content().string(containsString(TESTACT2_PRJ)))
                .andExpect(content().string(containsString(TESTACT2_SUB)))
                .andExpect(content().string(not(containsString(TESTACT1_PRJ))));

        performGet(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_1, getUser1())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_NAME)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT1_SUB)));

        performGet(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_2, getUser2())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT2_NAME)))
                .andExpect(content().string(containsString(TESTACT2_PRJ)))
                .andExpect(content().string(containsString(TESTACT2_SUB)));

        performGet(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_1, getUser2())
                .andExpect(status().is4xxClientError());

        performGet(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_2, getUser1())
                .andExpect(status().is4xxClientError());

        ActivityBody data = new ActivityBody();
        data.activityName = TESTACT_HIDDEN_NAME;
        data.projectId = TESTACT_HIDDEN_PROJ;
        data.projectActivity = TESTACT_HIDDEN_SUB;
        data.hidden = true;

        performPost(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_2, data, getUser1())
                .andExpect(status().is4xxClientError());

        performPost(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_1, data, getUser1())
                .andExpect(status().isOk());
    }
}
