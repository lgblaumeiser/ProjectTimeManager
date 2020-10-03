/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.security.Principal;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.lgblaumeiser.ptm.ServiceMapper;

/**
 * Rest controller for running analysis
 */
@RestController
@RequestMapping("/analysis")
public class AnalysisRestController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServiceMapper services;

	@RequestMapping(method = RequestMethod.GET, value = "/{analyzerId}/{timeframe}/{param}")
	Collection<Collection<String>> runAnalysis(Principal principal, @PathVariable final String analyzerId,
			@PathVariable final String timeframe, @PathVariable final String param) {
		logger.info("Request: Get Analysis Data for {} for time frame {} {} and user {}",
				removeTroubleCausingChars(analyzerId),
				removeTroubleCausingChars(timeframe),
				removeTroubleCausingChars(param),
				principal.getName());
		String[] analysisTime = calculateAnalysisTime(timeframe, param);
		return services.analysisService().analyze(analyzerId.toUpperCase(), principal.getName(), timeframe,
				analysisTime);
	}

	private String removeTroubleCausingChars(String troubledString) {
		return troubledString.replaceAll("[\n|\r|\t]", "_");
	}

	private String[] calculateAnalysisTime(String timeframe, String param) {
		if (timeframe.equals("period")) {
			return param.split("_");
		}
		return new String[] { param };
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> handleException(final IllegalStateException e) {
		logger.error("Exception in Request", e);
		return ResponseEntity.status(BAD_REQUEST).body(e.toString());
	}
}
