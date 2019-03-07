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
 * Test the activity rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
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
	public void testRegisterUser() throws Exception {
		UserRestController.UserBody data = new UserRestController.UserBody();
		data.username = "TestUser";
		data.password = "DummyPwd";
		MvcResult result = mockMvc
				.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/users/1"));

		mockMvc.perform(get("/users/name").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).header(
				HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("TestUser:DummyPwd".getBytes())))
				.andDo(print()).andExpect(status().isOk()).andExpect(content().string(containsString("TestUser")));

		mockMvc.perform(get("/users/name").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).header(
				HttpHeaders.AUTHORIZATION, "Basic " + Base64Utils.encodeToString("TestUser:DummyPwd2".getBytes())))
				.andDo(print()).andExpect(status().is(401));
	}

	@Test
	public void testRegisterUserTwiceFails() throws Exception {
		UserRestController.UserBody data = new UserRestController.UserBody();
		data.username = "TestUser";
		data.password = "DummyPwd";
		MvcResult result = mockMvc
				.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/users/1"));
		data.password = "AnotherPasswd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(data))).andDo(print()).andExpect(status().is4xxClientError());
	}

	@Test
	public void testChangePassword() throws Exception {
		UserRestController.UserBody data = new UserRestController.UserBody();
		data.username = "TestUser";
		data.password = "DummyPwd";
		MvcResult result = mockMvc
				.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/users/1"));

		data.password = "AnotherPasswd";
		mockMvc.perform(post("/users/name").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("TestUser:DummyPwd".getBytes()))
				.content(objectMapper.writeValueAsString(data))).andDo(print()).andExpect(status().isOk());

		data.password = "AnotherPasswdToTryThatChangeWorked";
		mockMvc.perform(post("/users/name").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("TestUser:AnotherPasswd".getBytes()))
				.content(objectMapper.writeValueAsString(data))).andDo(print()).andExpect(status().isOk());
	}

	@Test
	public void testDeleteAllUserData() throws Exception {
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
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
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
								"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
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
								"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
						.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
						.content(objectMapper.writeValueAsString(booking)))
				.andDo(print()).andExpect(status().isCreated()).andReturn();
		assertTrue(result.getResponse().getRedirectedUrl().contains("/bookings/id/4"));

		mockMvc.perform(delete("/users/name").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))).andDo(print())
				.andExpect(status().isOk());

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("activity\":1")))
				.andExpect(content().string(containsString("user\":\"MyTestUser")));

		mockMvc.perform(get("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("MyTestActivity")))
				.andExpect(content().string(containsString("0815")));

		mockMvc.perform(get("/bookings/day/" + dateString)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().is(401));

		mockMvc.perform(get("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().is(401));
	}
}
