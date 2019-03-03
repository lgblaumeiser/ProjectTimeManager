/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static java.lang.System.setProperty;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test the activity rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ActivityControllerTest {
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
	public void testWithInitialSetupNoActivities() throws Exception {
		UserRestController.UserBody user = new UserRestController.UserBody();
		user.username = "MyTestUser";
		user.password = "DummyPwd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		mockMvc.perform(get("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("[]")));
	}

	@Test
	public void testRoundtripCreateAndRetrieveActivity() throws Exception {
		UserRestController.UserBody user = new UserRestController.UserBody();
		user.username = "MyTestUser";
		user.password = "DummyPwd";
		mockMvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(user))).andDo(print()).andExpect(status().isCreated());

		ActivityRestController.ActivityBody data = new ActivityRestController.ActivityBody();
		data.activityName = "MyTestActivity";
		data.projectId = "0815";
		data.projectActivity = "1";
		data.hidden = false;
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isCreated());

		mockMvc.perform(get("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("MyTestActivity")))
				.andExpect(content().string(containsString("0815")));

		mockMvc.perform(get("/activities/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("MyTestActivity")))
				.andExpect(content().string(containsString("0815")));

		data.hidden = true;
		data.activityName = "MyOtherTestActivity";
		data.projectId = "4711";
		data.projectActivity = "2";
		mockMvc.perform(post("/activities/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isOk());

		mockMvc.perform(get("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("true")))
				.andExpect(content().string(containsString("MyOtherTestActivity")))
				.andExpect(content().string(containsString("4711")))
				.andExpect(content().string(containsString("\"id\":1")));
	}

	@Test
	public void testActivitiesWithDifferentUser() throws Exception {
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
		data.hidden = false;
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isCreated());

		data = new ActivityRestController.ActivityBody();
		data.activityName = "MyOtherTestActivity";
		data.projectId = "4711";
		data.projectActivity = "2";
		data.hidden = false;
		mockMvc.perform(post("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isCreated());

		mockMvc.perform(get("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("MyTestActivity")))
				.andExpect(content().string(containsString("0815")))
				.andExpect(content().string(not(containsString("4711"))));

		mockMvc.perform(get("/activities")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("MyOtherTestActivity")))
				.andExpect(content().string(containsString("4711")))
				.andExpect(content().string(not(containsString("0815"))));

		mockMvc.perform(get("/activities/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("MyTestActivity")))
				.andExpect(content().string(containsString("0815")));

		mockMvc.perform(get("/activities/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("MyOtherTestActivity")))
				.andExpect(content().string(containsString("4711")));

		mockMvc.perform(get("/activities/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser2:DummyPwd2".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print())
				.andExpect(status().is4xxClientError());

		mockMvc.perform(get("/activities/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print())
				.andExpect(status().is4xxClientError());

		data.hidden = true;
		data.activityName = "MyOtherOtherTestActivity";
		data.projectId = "08154711";
		data.projectActivity = "3";
		mockMvc.perform(post("/activities/2")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().is4xxClientError());

		mockMvc.perform(post("/activities/1")
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + Base64Utils.encodeToString("MyTestUser:DummyPwd".getBytes()))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE).content(objectMapper.writeValueAsString(data)))
				.andDo(print()).andExpect(status().isOk());
	}
}
