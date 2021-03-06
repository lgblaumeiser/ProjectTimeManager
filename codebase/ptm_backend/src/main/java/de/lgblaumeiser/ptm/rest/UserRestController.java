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
import java.security.Principal;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
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
		public String email;
		public String question;
		public String answer;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/register")
	ResponseEntity<?> addUser(@RequestBody final UserBody userData) {
		logger.info("Request: Register new User");
		checkUsername(userData.username);
		User newUser = services.userStore()
				.store(newUser().setUsername(userData.username).setPassword(encrypt(userData.password))
						.setEmail(userData.email).setQuestion(userData.question).setAnswer(encrypt(userData.answer))
						.build());
		if (newUser.getId() == 1L) {
			newUser = services.userStore().store(newUser.changeUser().setAdmin(true).build());
		}
		URI location = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/users/"
				+ newUser.getId());
		logger.info("Result: User Created with Id " + newUser.getId());
		return ResponseEntity.created(location).build();
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/reset")
	String resetPassword(@RequestBody final UserBody userdata) {
		logger.info("Request: Reset Password");
		checkState(stringHasContent(userdata.username));
		checkState(stringHasContent(userdata.answer));
		User foundUser = services.userStore().retrieveAll().stream()
				.filter(u -> u.getUsername().equals(userdata.username)).findFirst()
				.orElseThrow(IllegalStateException::new);
		checkState(services.passwordEncodingService().matches(userdata.answer, foundUser.getAnswer()));
		String newPassword = nextPassword();
		services.userStore().store(foundUser.changeUser().setPassword(encrypt(newPassword)).build());
		return newPassword;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/name")
	User getUser(final Principal principal) {
		logger.info("Request: Get User item for user " + principal.getName());
		User foundUser = services.userStore().retrieveAll().stream()
				.filter(u -> u.getUsername().equals(principal.getName())).findFirst()
				.orElseThrow(IllegalStateException::new);
		return foundUser;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/name")
	ResponseEntity<?> changePassword(final Principal principal, @RequestBody final UserBody userData) {
		logger.info("Request: Change Password for User " + principal.getName());
		User oldUser = services.userStore().retrieveAll().stream()
				.filter(u -> u.getUsername().equals(principal.getName())).findFirst()
				.orElseThrow(IllegalStateException::new);
		services.userStore().store(oldUser.changeUser().setPassword(encrypt(userData.password)).build());
		logger.info("Result: Password changed for User");
		return ResponseEntity.ok().build();
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/name")
	public ResponseEntity<?> deleteUserAndHisData(final Principal principal) {
		logger.info("Request: Delete User and all his data with username " + principal.getName());
		User user = services.userStore().retrieveAll().stream().filter(u -> u.getUsername().equals(principal.getName()))
				.findFirst().orElseThrow(IllegalStateException::new);
		List<Booking> relatedBookings = services.bookingStore().retrieveAll().stream()
				.filter(b -> b.getUser().equals(principal.getName())).collect(Collectors.toList());
		for (Booking booking : relatedBookings) {
			services.bookingStore().deleteById(booking.getId());
		}
		List<Activity> relatedActivities = services.activityStore().retrieveAll().stream()
				.filter(a -> a.getUser().equals(principal.getName())).collect(Collectors.toList());
		for (Activity activity : relatedActivities) {
			services.activityStore().deleteById(activity.getId());
		}
		services.userStore().deleteById(user.getId());
		logger.info("Result: User and all his data deleted");
		return ResponseEntity.ok().build();
	}

	private String encrypt(String password) {
		checkState(stringHasContent(password));
		return services.passwordEncodingService().encode(password);
	}

	private void checkUsername(String username) {
		if (services.userStore().retrieveAll().stream().anyMatch(u -> u.getUsername().equals(username))) {
			throw new IllegalStateException("Username already exists!");
		}
	}

	private void checkState(boolean state) {
		if (!state) {
			throw new IllegalStateException();
		}
	}

	private boolean stringHasContent(String toCheck) {
		return (toCheck != null && toCheck.trim().length() > 0);
	}

	private final Random random = new SecureRandom();
	private static final char[] ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
			.toCharArray();

	private String nextPassword() {
		char[] buf = new char[10];
		for (int idx = 0; idx < buf.length; ++idx)
			buf[idx] = ALPHANUM[random.nextInt(ALPHANUM.length)];
		return new String(buf);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<?> handleException(final IllegalStateException e) {
		logger.error("Exception in Request", e);
		return ResponseEntity.status(BAD_REQUEST).body(e.toString());
	}
}
