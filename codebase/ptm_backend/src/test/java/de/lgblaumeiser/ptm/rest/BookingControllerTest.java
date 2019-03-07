/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static java.lang.System.setProperty;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test the booking rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

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

	@Test
	public void testWithInitialSetupNoBookings() throws Exception {
		UserRestController.UserBody user = new UserRestController.UserBody();
		user.username = "MyTestUser";
		user.password = "DummyPwd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		mockMvc.perform(get("/bookings").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))).andDo(print())
				.andExpect(status().isOk()).andExpect(content().string(containsString("[]")));
	}

	@Test
	public void testRoundtripCreateAndRetrieveBooking() throws Exception {
		UserRestController.UserBody user = new UserRestController.UserBody();
		user.username = "MyTestUser";
		user.password = "DummyPwd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		ActivityRestController.ActivityBody data = new ActivityRestController.ActivityBody();
		data.activityName = "MyTestActivity";
		data.projectId = "0815";
		data.projectActivity = "1";
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(data))).andDo(print())
				.andExpect(status().isCreated());

		LocalDate date = LocalDate.now();
		String dateString = date.format(ISO_LOCAL_DATE);
		BookingRestController.BookingBody booking = new BookingRestController.BookingBody();
		booking.activityId = "1";
		booking.starttime = LocalTime.of(8, 15).format(ISO_LOCAL_TIME);
		booking.comment = "";
		MvcResult result = mockMvc
				.perform(post("/bookings/day/" + dateString)
						.header(HttpHeaders.AUTHORIZATION,
								"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/1"));

		mockMvc.perform(get("/bookings")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString(dateString)));

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")));

		mockMvc.perform(get("/bookings/id/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")));

		booking.starttime = LocalTime.of(15, 30).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(16, 30).format(ISO_LOCAL_TIME);
		mockMvc.perform(post("/bookings/id/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isOk());

		mockMvc.perform(get("/bookings/id/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")))
				.andExpect(content().string(containsString("endtime")));

		LocalDate date2 = date.minusDays(1);
		String dateString2 = date2.format(ISO_LOCAL_DATE);

		booking = new BookingRestController.BookingBody();
		booking.activityId = "1";
		booking.starttime = LocalTime.of(8, 15).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(16, 30).format(ISO_LOCAL_TIME);
		booking.comment = "Test Comment";
		mockMvc.perform(post("/bookings/day/" + dateString2)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated());

		mockMvc.perform(get("/bookings/id/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("Test Comment")))
				.andExpect(content().string(containsString("starttime")))
				.andExpect(content().string(containsString("endtime")));

		mockMvc.perform(get("/bookings")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString(dateString)))
				.andExpect(content().string(containsString(dateString2)));

		mockMvc.perform(delete("/bookings/id/1").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))).andDo(print())
				.andExpect(status().isOk());
		mockMvc.perform(delete("/bookings/id/2").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))).andDo(print())
				.andExpect(status().isOk());

		mockMvc.perform(get("/bookings")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("[]")));
	}

	@Test
	public void testBookingCreationWithMixedTimes() throws Exception {
		UserRestController.UserBody user = new UserRestController.UserBody();
		user.username = "MyTestUser";
		user.password = "DummyPwd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		ActivityRestController.ActivityBody data1 = new ActivityRestController.ActivityBody();
		data1.activityName = "MyTestActivity";
		data1.projectId = "0815";
		data1.projectActivity = "1";
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(data1))).andDo(print())
				.andExpect(status().isCreated());

		ActivityRestController.ActivityBody data2 = new ActivityRestController.ActivityBody();
		data2.activityName = "MyOtherTestActivity";
		data2.projectId = "4711";
		data2.projectActivity = "2";
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(data2))).andDo(print())
				.andExpect(status().isCreated());

		LocalDate date = LocalDate.now();
		String dateString = date.format(ISO_LOCAL_DATE);
		BookingRestController.BookingBody booking = new BookingRestController.BookingBody();
		booking.activityId = "1";
		booking.starttime = LocalTime.of(8, 15).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(9, 0).format(ISO_LOCAL_TIME);
		booking.comment = "";
		MvcResult result = mockMvc
				.perform(post("/bookings/day/" + dateString)
						.header(HttpHeaders.AUTHORIZATION,
								"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/1"));

		booking = new BookingRestController.BookingBody();
		booking.activityId = "1";
		booking.starttime = LocalTime.of(10, 15).format(ISO_LOCAL_TIME);
		booking.comment = "";
		result = mockMvc
				.perform(post("/bookings/day/" + dateString)
						.header(HttpHeaders.AUTHORIZATION,
								"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/2"));

		booking = new BookingRestController.BookingBody();
		booking.activityId = "2";
		booking.starttime = LocalTime.of(9, 0).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(10, 15).format(ISO_LOCAL_TIME);
		booking.comment = "";
		result = mockMvc
				.perform(post("/bookings/day/" + dateString)
						.header(HttpHeaders.AUTHORIZATION,
								"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/3"));

		booking = new BookingRestController.BookingBody();
		booking.activityId = "2";
		booking.starttime = LocalTime.of(11, 0).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(12, 15).format(ISO_LOCAL_TIME);
		booking.comment = "";
		result = mockMvc
				.perform(post("/bookings/day/" + dateString)
						.header(HttpHeaders.AUTHORIZATION,
								"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/4"));

		mockMvc.perform(get("/bookings")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString(dateString)));

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("activity\":2")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")));

		mockMvc.perform(get("/bookings/id/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")));

		mockMvc.perform(get("/bookings/id/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")));

		mockMvc.perform(get("/bookings/id/3")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":2")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")));

		mockMvc.perform(get("/bookings/id/4")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":2")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")));

		booking.starttime = LocalTime.of(9, 15).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(10, 0).format(ISO_LOCAL_TIME);
		mockMvc.perform(post("/bookings/id/3")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isOk());

		mockMvc.perform(get("/bookings/id/3")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":2")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString("starttime")))
				.andExpect(content().string(containsString("endtime")));
	}

	@Test
	public void testRoundtripAddBreaksToBookings() throws Exception {
		UserRestController.UserBody user = new UserRestController.UserBody();
		user.username = "MyTestUser";
		user.password = "DummyPwd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		ActivityRestController.ActivityBody data = new ActivityRestController.ActivityBody();
		data.activityName = "MyTestActivity";
		data.projectId = "0815";
		data.projectActivity = "1";
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(data))).andDo(print())
				.andExpect(status().isCreated());

		LocalDate date = LocalDate.now();
		String dateString = date.format(ISO_LOCAL_DATE);
		BookingRestController.BookingBody booking = new BookingRestController.BookingBody();
		booking.activityId = "1";
		booking.starttime = LocalTime.of(8, 15).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(17, 0).format(ISO_LOCAL_TIME);
		booking.breakstart = LocalTime.of(10, 30).format(ISO_LOCAL_TIME);
		booking.comment = "";
		MvcResult result = mockMvc
				.perform(post("/bookings/day/" + dateString)
						.header(HttpHeaders.AUTHORIZATION,
								"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/2"));

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString(booking.starttime)))
				.andExpect(content().string(containsString(booking.endtime)))
				.andExpect(content().string(containsString(booking.breakstart)))
				.andExpect(content().string(containsString(LocalTime.of(11, 0).format(ISO_LOCAL_TIME))));

		booking.breakstart = LocalTime.of(15, 30).format(ISO_LOCAL_TIME);
		booking.breaklength = "15";
		mockMvc.perform(post("/bookings/id/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isOk());

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString(booking.breakstart)))
				.andExpect(content().string(containsString(LocalTime.of(15, 45).format(ISO_LOCAL_TIME))));
	}

	@Test
	public void testBookingsWithDifferentUsers() throws Exception {
		UserRestController.UserBody user = new UserRestController.UserBody();
		user.username = "MyTestUser";
		user.password = "DummyPwd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		user = new UserRestController.UserBody();
		user.username = "MyTestUser2";
		user.password = "DummyPwd2";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		ActivityRestController.ActivityBody data = new ActivityRestController.ActivityBody();
		data.activityName = "MyTestActivity";
		data.projectId = "0815";
		data.projectActivity = "1";
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(APPLICATION_JSON).content(objectMapper.writeValueAsString(data))).andDo(print())
				.andExpect(status().isCreated());

		LocalDate date = LocalDate.now();
		String dateString = date.format(ISO_LOCAL_DATE);
		BookingRestController.BookingBody booking = new BookingRestController.BookingBody();
		booking.activityId = "1";
		booking.starttime = LocalTime.of(8, 15).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(17, 0).format(ISO_LOCAL_TIME);
		booking.breakstart = LocalTime.of(10, 30).format(ISO_LOCAL_TIME);
		booking.comment = "";
		MvcResult result = mockMvc
				.perform(post("/bookings/day/" + dateString)
						.header(HttpHeaders.AUTHORIZATION,
								"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/2"));

		mockMvc.perform(post("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().is4xxClientError());

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")))
				.andExpect(content().string(containsString(booking.starttime)))
				.andExpect(content().string(containsString(booking.endtime)))
				.andExpect(content().string(containsString(booking.breakstart)))
				.andExpect(content().string(containsString(LocalTime.of(11, 0).format(ISO_LOCAL_TIME))));

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("[]")));

		booking.breakstart = LocalTime.of(15, 30).format(ISO_LOCAL_TIME);
		booking.breaklength = "15";
		mockMvc.perform(post("/bookings/id/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().is4xxClientError());

		mockMvc.perform(post("/bookings/id/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isOk());

		mockMvc.perform(delete("/bookings/id/2").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))).andDo(print())
				.andExpect(status().is4xxClientError());

		mockMvc.perform(delete("/bookings/id/2").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))).andDo(print())
				.andExpect(status().isOk());
	}
}
