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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.lgblaumeiser.ptm.rest.BookingRestController.BookingBody;

/**
 * Test analysis test controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AnalysisControllerTest extends ControllerTestSetupAndSupport {
    private static final String HUNDERT_PERCENT_STRING = "100.0%";

    private static String ANALYSIS_API_TEMPLATE = "/analysis/%s/month/%s";
    private static String ANALYSIS_HOURS = "hours";
    private static String ANALYSIS_PROJECTS = "projects";
    private static String ANALYSIS_ACTIVITIES = "activities";

    @Test
    public void test() throws Exception {
        createDefaultUser();

        createDefaultActivity(false);

        BookingBody booking = new BookingBody();
        booking.activityId = ACTIVITY_ID_1;
        booking.starttime = createHourString(8, 15);
        booking.endtime = createHourString(16, 45);
        booking.comment = emptyString();
        
        createBooking(DATE_STRING, booking, getUser1());

        performGet(api(ANALYSIS_HOURS))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(DATE_STRING)))
                .andExpect(content().string(containsString(createHourString(8, 15))))
                .andExpect(content().string(containsString(createHourString(16, 45))))
                .andExpect(content().string(containsString(createHourString(8, 30))))
                .andExpect(content().string(containsString(createHourString(0, 0))));

        performGet(api(ANALYSIS_ACTIVITIES))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(createHourString(8, 30))))
                .andExpect(content().string(containsString(HUNDERT_PERCENT_STRING)));
        
        performGet(api(ANALYSIS_PROJECTS))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(TESTACT1_PRJ)))
                .andExpect(content().string(containsString(createHourString(8, 30))))
                .andExpect(content().string(containsString(HUNDERT_PERCENT_STRING)));
    }

    @Override
    protected String createHourString(final int hour, final int minute) {
        return super.createHourString(hour, minute).substring(0, 5);
    }

    private String api(final String analysis) {
        return String.format(ANALYSIS_API_TEMPLATE, analysis, DATE_STRING.substring(0, 7));
    }
}
