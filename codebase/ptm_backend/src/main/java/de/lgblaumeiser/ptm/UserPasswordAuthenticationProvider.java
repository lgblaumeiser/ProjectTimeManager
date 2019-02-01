/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 *
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.stereotype.Component;

import de.lgblaumeiser.ptm.datamanager.model.User;

@Component
public class UserPasswordAuthenticationProvider implements AuthenticationProvider {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ServiceMapper services;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.authentication.AuthenticationProvider#
	 * authenticate(org.springframework.security.core.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();

		logger.info("Authenticating user " + username);

		Optional<User> user = services.userStore().retrieveAll().stream()
				.filter(cuser -> cuser.getUsername().equals(username)).collect(Collectors.toList()).stream()
				.findFirst();
		if (user.isPresent() && checkPasswords(user.get().getPassword(), password)) {
			logger.info("Authenticating successful");
			List<GrantedAuthority> grantedAuths = new ArrayList<>();
			if (user.get().isAdmin()) {
				grantedAuths.add(() -> "ADMIN");
			}
			return new UsernamePasswordAuthenticationToken(username, password, grantedAuths);
		}
		logger.info("Authentication failed");
		return null;
	}

	private boolean checkPasswords(String expectedPasswordEncrypted, String givenPassword) {
		return services.passwordEncodingService().matches(givenPassword, expectedPasswordEncrypted);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.security.authentication.AuthenticationProvider#supports(
	 * java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
