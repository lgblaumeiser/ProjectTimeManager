/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.html;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.lgblaumeiser.ptm.rest.ControllerTestSetupAndSupport;

/**
 * Test the booking rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class OverviewControllerTest extends ControllerTestSetupAndSupport {
    private static final String API_OVERVIEW = "/overview";

    @Test
    public void testOverviewPage() throws Exception {
        createDefaultUser();

        createDefaultActivity(true);

        createDefaultBooking(true);

        performGet(API_OVERVIEW)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(DATE_STRING)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT2_PRJ)));

        performGet(API_OVERVIEW + "/" + DATE_STRING)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(DATE_STRING)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT2_PRJ)));
    }

    @Test
    public void testOverviewPageTwoUser() throws Exception {
        createDefaultUser();

        createDefaultActivity(false);

        createDefaultBooking(false);

        performGet(API_OVERVIEW, getUser1())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(DATE_STRING)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(not(containsString(TESTACT2_PRJ))));

        performGet(API_OVERVIEW + "/" + DATE_STRING, getUser2())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(DATE_STRING)))
                .andExpect(content().string(not(containsString(TESTACT1_PRJ))))
                .andExpect(content().string(containsString(TESTACT2_PRJ)));
    }
}
