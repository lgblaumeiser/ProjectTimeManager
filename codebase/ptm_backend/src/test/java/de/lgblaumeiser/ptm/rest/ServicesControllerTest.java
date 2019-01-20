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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test class for the services rest controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ServicesControllerTest {

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
	public void testBackupRestore() throws Exception {
		ActivityRestController.ActivityBody data = new ActivityRestController.ActivityBody();
		data.activityName = "MyTestActivity";
		data.bookingNumber = "0815";
		mockMvc.perform(post("/activities").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(data))).andDo(print()).andExpect(status().isCreated());

		LocalDate date = LocalDate.now();
		String dateString = date.format(ISO_LOCAL_DATE);
		BookingRestController.BookingBody booking = new BookingRestController.BookingBody();
		booking.activityId = "1";
		booking.starttime = LocalTime.of(8, 15).format(ISO_LOCAL_TIME);
		booking.endtime = LocalTime.of(16, 45).format(ISO_LOCAL_TIME);
		booking.comment = "";
		mockMvc.perform(post("/bookings/day/" + dateString).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
				.content(objectMapper.writeValueAsString(booking))).andDo(print()).andExpect(status().isCreated());

		MvcResult result = mockMvc.perform(get("/services/backup").contentType("application/zip")).andDo(print())
				.andExpect(status().isOk()).andReturn();
		byte[] zipdata = result.getResponse().getContentAsByteArray();

		mockMvc.perform(put("/services/restore").contentType("application/zip").content(zipdata)).andDo(print())
				.andExpect(status().isOk());

		mockMvc.perform(get("/activities/1").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().string(containsString("MyTestActivity")))
				.andExpect(content().string(containsString("0815")));

		mockMvc.perform(get("/bookings/id/1").contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print())
				.andExpect(status().isOk()).andExpect(content().string(containsString("activity")))
				.andExpect(content().string(containsString("user\":1")))
				.andExpect(content().string(containsString("starttime")))
				.andExpect(content().string(containsString("endtime")));

	}

	@Test
	public void testLicense() throws Exception {
		mockMvc.perform(get("/services/license")).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("Apache-2.0")))
				.andExpect(content().string(containsString("EPL-1.0")))
				.andExpect(content().string(containsString("MIT")))
				.andExpect(content().string(containsString("CDDL-1.1")))
				.andExpect(content().string(containsString("BSD-2-Clause")))
				.andExpect(content().string(containsString("BSD-3-Clause")));
	}
}
