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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test analysis test controller
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AnalysisControllerTest {

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
	public void test() throws Exception {
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

		mockMvc.perform(get("/analysis/hours/month/" + dateString.substring(0, 7))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString(dateString)))
				.andExpect(content().string(containsString("08:15")))
				.andExpect(content().string(containsString("16:45")))
				.andExpect(content().string(containsString("08:30")))
				.andExpect(content().string(containsString("00:00")));

		mockMvc.perform(get("/analysis/projects/month/" + dateString.substring(0, 7))
				.contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)).andDo(print()).andExpect(status().isOk())
				.andExpect(content().string(containsString("0815")))
				.andExpect(content().string(containsString("08:30"))).andExpect(content().string(containsString("100")))
				.andExpect(content().string(containsString("8")));

	}

}
