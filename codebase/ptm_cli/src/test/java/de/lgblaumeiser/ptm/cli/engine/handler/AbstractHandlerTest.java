/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static de.lgblaumeiser.ptm.datamanager.model.Activity.newActivity;
import static de.lgblaumeiser.ptm.datamanager.model.User.newUser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.junit.Before;

import de.lgblaumeiser.ptm.cli.CLI;
import de.lgblaumeiser.ptm.cli.PTMCLIConfigurator;
import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.cli.engine.CommandLogger;
import de.lgblaumeiser.ptm.cli.engine.PrettyPrinter;
import de.lgblaumeiser.ptm.cli.engine.ServiceManager;
import de.lgblaumeiser.ptm.cli.engine.UserStore;
import de.lgblaumeiser.ptm.cli.rest.RestBaseService;
import de.lgblaumeiser.ptm.cli.rest.RestUtils;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.User;

public abstract class AbstractHandlerTest {
	private static final String ID = "id";

	static final LocalDate DATE1 = LocalDate.of(2016, 06, 24);
	static final LocalTime TIME1 = LocalTime.of(12, 34);
	static final LocalTime TIME2 = LocalTime.of(13, 57);
	static final String ACTIVITY1NAME = "Act1";
	static final String ACTIVITY1NUMBER = "0815";
	static final String ACTIVITY2NAME = "NewAct2";
	static final String ACTIVITY2NUMBER = "4711";
	private final static String DUMMYUSER = "Dummy";
	static final Activity ACTIVITY1 = newActivity().setActivityName(ACTIVITY1NAME).setBookingNumber(ACTIVITY1NUMBER)
			.setUser(DUMMYUSER).build();
	static final Activity ACTIVITY2 = newActivity().setActivityName(ACTIVITY2NAME).setBookingNumber(ACTIVITY2NUMBER)
			.setUser(DUMMYUSER).setHidden(true).build();
	static final String COMMENT = "TestComment";
	static final Booking BOOKING1 = Booking.newBooking().setActivity(1L).setBookingday(DATE1).setUser(DUMMYUSER)
			.setStarttime(TIME1).setEndtime(TIME2).build();

	protected static class TestCommandLogger implements CommandLogger {
		StringBuffer logMessages = new StringBuffer();

		@Override
		public void log(final String message) {
			logMessages.append(message);
			logMessages.append("xxxnewlinexxx");
		}
	}

	protected static class TestUserStore extends UserStore {
		public TestUserStore() {
			super("donotcare");
		}

		protected User storedUser;

		@Override
		public User loadUserData() {
			return newUser().setUsername("DummyName").setPassword("DummyPwd").build();
		}

		@Override
		public void storeUserData(User user) {
			storedUser = user;
		}

	}

	protected static class TestRestUtils extends RestUtils {
		String apiNameGiven;
		Map<String, String> bodyDataGiven;
		byte[] rawDataGiven;

		@Override
		public Long post(final String apiName, final Optional<User> user, final Map<String, String> bodyData) {
			apiNameGiven = apiName;
			bodyDataGiven = bodyData;
			return 2L;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.lgblaumeiser.ptm.cli.rest.RestUtils#put(java.lang.String, byte[])
		 */
		@Override
		public void put(final String apiName, final Optional<User> user, final byte[] sendData) {
			apiNameGiven = apiName;
			rawDataGiven = sendData;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.lgblaumeiser.ptm.cli.rest.RestUtils#get(java.lang.String)
		 */
		@Override
		public InputStream get(final String apiName, final Optional<User> user) {
			apiNameGiven = apiName;
			if (apiName.contains("services/license")) {
				try {
					return IOUtils.toInputStream("BackendLicenseText", "UTF-8");
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
			return new ByteArrayInputStream(rawDataGiven);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T get(final String apiName, final Optional<User> user, final Class<T> returnClass) {
			apiNameGiven = apiName;
			if (apiName.contains("activities")) {
				if (apiName.contains("1")) {
					return returnClass.cast(ACTIVITY1);
				} else if (apiName.contains("2")) {
					return returnClass.cast(ACTIVITY2);
				}
			}
			if (apiName.contains("bookings/id/10")) {
				return returnClass.cast(BOOKING1);
			}
			if (returnClass.isArray()) {
				if (returnClass.getComponentType().getName().contains("Booking")) {
					return (T) new Booking[] { BOOKING1 };
				} else if (returnClass.getComponentType().getName().contains("Activity")) {
					return (T) new Activity[] { ACTIVITY1, ACTIVITY2 };
				}
				return returnClass.cast(Array.newInstance(returnClass.getComponentType(), 0));
			}
			return null;
		}

		@Override
		public void delete(final String apiName, final Optional<User> user) {
			apiNameGiven = apiName;
		}

		@Override
		public TestRestUtils configure() {
			return this;
		}
	}

	TestCommandLogger logger = new TestCommandLogger();
	TestRestUtils restutils = new TestRestUtils().configure();
	TestUserStore userstoreutils = new TestUserStore();
	CLI commandline = new PTMCLIConfigurator().configure();

	@Before
	public void before() throws IOException {
		AbstractCommandHandler.setLogger(logger);
		AbstractCommandHandler.setPrinter(new PrettyPrinter().setLogger(logger));
		RestBaseService.setRestUtils(restutils);
		try {
			Field f = BOOKING1.getClass().getDeclaredField(ID);
			f.setAccessible(true);
			f.set(BOOKING1, 10L);
			f.setAccessible(false);
			f = ACTIVITY1.getClass().getDeclaredField(ID);
			f.setAccessible(true);
			f.set(ACTIVITY1, 1L);
			f.setAccessible(false);
			f = ACTIVITY2.getClass().getDeclaredField(ID);
			f.setAccessible(true);
			f.set(ACTIVITY2, 2L);
			f.setAccessible(false);
			f = AbstractCommandHandler.class.getDeclaredField("services");
			f.setAccessible(true);
			ServiceManager manager = (ServiceManager) f.get(null);
			f.setAccessible(false);
			manager.setCurrentUserStore(userstoreutils);
		} catch (IllegalAccessException | IllegalArgumentException | ClassCastException | NoSuchFieldException
				| SecurityException e) {
			throw new IllegalStateException(e);
		}
	}
}
