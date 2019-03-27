/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.lgblaumeiser.ptm.rest.BookingRestController.BookingBody;

/**
 * Test the booking rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest extends ControllerTestSetupAndSupport {
    private static final String BOOKING_API = "/bookings";
    private static final String TESTBOOKING_COMMENT = "Test Comment";

    private static final String EXPECTED_USER_JSON_STRING = "user\":\"" + TESTUSER_USERNAME;
    private static final String EXPECTED_ACTIVITY_1_JSON_STRING = "activity\":" + ACTIVITY_ID_1;
    private static final String EXPECTED_ACTIVITY_2_JSON_STRING = "activity\":" + ACTIVITY_ID_2;

    @Test
    public void testWithInitialSetupNoBookings() throws Exception {
        createDefaultUser();

        performGet(BOOKING_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EMPTY_JSON_ARRAY)));
    }

    @Test
    public void testRoundtripCreateAndRetrieveBooking() throws Exception {
        createDefaultUser();

        createDefaultActivity(false);

        createDefaultBooking(false);

        performGet(BOOKING_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(DATE_STRING)));

        performGet(BOOKING_DAY_API + DATE_STRING)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(TESTBOOKING1_STARTTIME)));

        performGet(resourceApi(1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(TESTBOOKING1_STARTTIME)));

        BookingBody booking = new BookingBody();
        booking.starttime = createHourString(15, 30);
        booking.endtime = createHourString(16, 30);
        performPost(resourceApi(1), booking, getUser1())
                .andExpect(status().isOk());

        performGet(resourceApi(1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(booking.starttime)))
                .andExpect(content().string(containsString(booking.endtime)));

        String dateString = LocalDate.now().minusDays(1).format(ISO_LOCAL_DATE);

        booking = new BookingBody();
        booking.activityId = ACTIVITY_ID_1;
        booking.starttime = createHourString(8, 15);
        booking.endtime = createHourString(16, 30);
        booking.comment = TESTBOOKING_COMMENT;

        createBooking(dateString, booking, getUser1());

        performGet(resourceApi(5))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(TESTBOOKING_COMMENT)))
                .andExpect(content().string(containsString(booking.starttime)))
                .andExpect(content().string(containsString(booking.endtime)));

        performGet(BOOKING_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(dateString)))
                .andExpect(content().string(containsString(DATE_STRING)));

        performDelete(resourceApi(1), getUser1())
                .andExpect(status().isOk());

        performDelete(resourceApi(2), getUser1())
                .andExpect(status().isOk());

        performDelete(resourceApi(5), getUser1())
                .andExpect(status().isOk());

        performGet(BOOKING_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EMPTY_JSON_ARRAY)));
    }

    @Test
    public void testBookingCreationWithMixedTimes() throws Exception {
        createDefaultUser();

        createDefaultActivity(true);

        createDefaultBooking(true);
        
        performGet(BOOKING_API)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(DATE_STRING)));

        performGet(BOOKING_DAY_API + DATE_STRING)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_2_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)));

        performGet(resourceApi(1))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(createHourString(8, 15))));

        performGet(resourceApi(2))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(createHourString(10, 15))));

        performGet(resourceApi(3))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_2_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(createHourString(9, 0))));

        performGet(resourceApi(4))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_2_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(createHourString(11, 0))));

        BookingBody booking = new BookingBody();
        booking.starttime = createHourString(9, 15);
        booking.endtime = createHourString(10, 0);
        performPost(resourceApi(3), booking, getUser1())
                .andExpect(status().isOk());

        performGet(resourceApi(3))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_2_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(booking.starttime)))
                .andExpect(content().string(containsString(booking.endtime)));
    }

    @Test
    public void testRoundtripAddBreaksToBookings() throws Exception {
        createDefaultUser();

        createDefaultActivity(true);

        BookingBody booking = new BookingBody();
        booking.activityId = ACTIVITY_ID_1;
        booking.starttime = createHourString(8, 15);
        booking.endtime = createHourString(17, 0);
        booking.breakstart = createHourString(10, 30);
        booking.comment = emptyString();

        createBooking(DATE_STRING, booking, getUser1());

        performGet(BOOKING_DAY_API + DATE_STRING)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(booking.starttime)))
                .andExpect(content().string(containsString(booking.endtime)))
                .andExpect(content().string(containsString(booking.breakstart)))
                .andExpect(content().string(containsString(createHourString(11, 0))));

        booking = new BookingBody();
        booking.breakstart = createHourString(15, 30);
        booking.breaklength = "15";
        
        performPost(resourceApi(2), booking, getUser1())
                .andExpect(status().isOk());

        performGet(BOOKING_DAY_API + DATE_STRING)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(booking.breakstart)))
                .andExpect(content().string(containsString(createHourString(15, 45))));
    }

    @Test
    public void testBookingsWithDifferentUsers() throws Exception {
        createDefaultUser();

        createDefaultActivity(false);

        BookingBody booking = new BookingBody();
        booking.activityId = ACTIVITY_ID_1;
        booking.starttime = createHourString(8, 15);
        booking.endtime = createHourString(17, 0);
        booking.breakstart = createHourString(10, 30);
        booking.comment = emptyString();

        createBooking(DATE_STRING, booking, getUser1());

        performPost(BOOKING_DAY_API + DATE_STRING, booking, getUser2())
                .andExpect(status().is4xxClientError());

        performGet(BOOKING_DAY_API + DATE_STRING, getUser1())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EXPECTED_ACTIVITY_1_JSON_STRING)))
                .andExpect(content().string(containsString(EXPECTED_USER_JSON_STRING)))
                .andExpect(content().string(containsString(booking.starttime)))
                .andExpect(content().string(containsString(booking.endtime)))
                .andExpect(content().string(containsString(booking.breakstart)))
                .andExpect(content().string(containsString(createHourString(11, 0))));

        performGet(BOOKING_DAY_API + DATE_STRING, getUser2())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(EMPTY_JSON_ARRAY)));

        booking = new BookingBody();
        booking.breakstart = LocalTime.of(15, 30).format(ISO_LOCAL_TIME);
        booking.breaklength = "15";

        performPost(resourceApi(2), booking, getUser2())
                .andDo(print()).andExpect(status().is4xxClientError());

        performPost(resourceApi(2), booking, getUser1())
                .andDo(print()).andExpect(status().isOk());

        performDelete(resourceApi(2), getUser2())
                .andExpect(status().is4xxClientError());

        performDelete(resourceApi(2), getUser1())
                .andExpect(status().isOk());
    }

    private String resourceApi(final int id) {
        return BOOKING_RESOURCE_API + id;
    }
}
