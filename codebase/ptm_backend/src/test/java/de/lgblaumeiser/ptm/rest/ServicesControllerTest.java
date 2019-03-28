/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 *
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;

import de.lgblaumeiser.ptm.rest.BookingRestController.BookingBody;

/**
 * Test class for the services rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ServicesControllerTest extends ControllerTestSetupAndSupport {
    private static final String API_BACKUP = "/services/backup";
    private static final String API_RESTORE = "/services/restore";
    private static final String API_LICENSE = "/services/license";

    private static final String EXPECTED_USER_JSON_STRING = "user\":\"" + TESTUSER_USERNAME;
    private static final String EXPECTED_ACTIVITY_1_JSON_STRING = "activity\":" + ACTIVITY_ID_1;

    @Test
    public void testBackupRestore() throws Exception {
        createDefaultUser();

        createDefaultActivity(false);

        BookingBody booking = new BookingBody();
        booking.activityId = ACTIVITY_ID_1;
        booking.starttime = createHourString(8, 15);
        booking.endtime = createHourString(17, 0);
        booking.breakstart = createHourString(10, 30);
        booking.comment = emptyString();

        createBooking(DATE_STRING, booking, getUser1());

        MvcResult result = performGet(API_BACKUP, getUser1())
                .andExpect(status().isOk())
                .andReturn();
        byte[] zipdata = result.getResponse().getContentAsByteArray();

        performGet(API_BACKUP, getUser2())
                .andExpect(status().is4xxClientError());

        performPut(API_RESTORE, zipdata, getUser2())
                .andExpect(status().is4xxClientError());

        performPut(API_RESTORE, zipdata, getUser1())
                .andExpect(status().isOk());

        performGet(ACTIVITY_RESOURCE_API + "/" + ACTIVITY_ID_1)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_NAME)))
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(TESTACT1_SUB)));

        performGet(BOOKING_RESOURCE_API + "1")
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(booking.starttime)))
                .andExpect(content().string(containsString(booking.breakstart)));
    }

    @Test
    public void testLicense() throws Exception {
        performGet(API_LICENSE, Optional.empty())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Apache-2.0")))
                .andExpect(content().string(containsString("EPL-1.0")))
                .andExpect(content().string(containsString("MIT")))
                .andExpect(content().string(containsString("CDDL-1.1")))
                .andExpect(content().string(containsString("BSD-2-Clause")))
                .andExpect(content().string(containsString("BSD-3-Clause")));
    }
}
