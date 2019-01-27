/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.lgblaumeiser.ptm.ServiceMapper;

/**
 * General rest services of the PTM backend
 */
@RestController
@RequestMapping("/services")
public class ServiceRestController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServiceMapper services;

	@RequestMapping(method = RequestMethod.GET, value = "/backup")
	public ResponseEntity<?> backup() {
		logger.info("Request: Get backup of data");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType("application/zip"));
		String outputFilename = "ptm_backup.zip";
		headers.setContentDispositionFormData(outputFilename, outputFilename);
		headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		services.backupService().backup(byteOutputStream);
		logger.info("Result: Backup Data acquired, return to requester");
		return ResponseEntity.ok().headers(headers).body(byteOutputStream.toByteArray());
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/restore", consumes = "application/zip")
	public ResponseEntity<?> restore(final InputStream zipdata) {
		logger.info("Request: Put data to restore database");
		services.backupService().emptyDatabase();
		services.backupService().restore(zipdata);
		logger.info("Result: Data restored");
		return ResponseEntity.ok().build();
	}

	@RequestMapping(method = RequestMethod.GET, value = "/license", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<?> license() {
		logger.info("Request: Get all license information for PTM backend");
		try {
			Resource resource = new ClassPathResource("license_info.txt");
			String licenseData = IOUtils.toString(resource.getInputStream(), "UTF-8");
			logger.info("Result: License data read, returning to requester");
			return ResponseEntity.ok(licenseData);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> handleException(final IllegalStateException e) {
		logger.error("Exception in Request", e);
		return ResponseEntity.status(BAD_REQUEST).body(e.toString());
	}
}
