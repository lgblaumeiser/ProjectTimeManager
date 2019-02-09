/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 *
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.rest;

import static de.lgblaumeiser.ptm.datamanager.model.User.newUser;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.lgblaumeiser.ptm.ServiceMapper;
import de.lgblaumeiser.ptm.datamanager.model.User;

@RestController
@RequestMapping("/users")
public class UserRestController {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServiceMapper services;

	public static class UserBody {
		public String username;
		public String password;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/register")
	ResponseEntity<?> addUser(@RequestBody final UserBody userData) {
		logger.info("Request: Register new User");
		checkUsername(userData.username);
		User newUser = services.userStore()
				.store(newUser().setUsername(userData.username).setPassword(encrypt(userData.password)).build());
		if (newUser.getId() == 1L) {
			newUser = services.userStore().store(newUser.changeUser().setAdmin(true).build());
		}
		URI location = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/users/"
				+ newUser.getId());
		logger.info("Result: User Created with Id " + newUser.getId());
		return ResponseEntity.created(location).build();
	}

	private String encrypt(String password) {
		return services.passwordEncodingService().encode(password);
	}

	private void checkUsername(String username) {
		if (services.userStore().retrieveAll().stream().anyMatch(u -> u.getUsername().equals(username))) {
			throw new IllegalStateException();
		}
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> handleException(final IllegalStateException e) {
		logger.error("Exception in Request", e);
		return ResponseEntity.status(BAD_REQUEST).body(e.toString());
	}
}
