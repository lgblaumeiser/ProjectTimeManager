/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import de.lgblaumeiser.ptm.cli.CLI;
import de.lgblaumeiser.ptm.cli.PTMCLIConfigurator;
import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.cli.engine.CommandLogger;
import de.lgblaumeiser.ptm.cli.engine.PrettyPrinter;
import de.lgblaumeiser.ptm.cli.rest.RestBaseService;
import de.lgblaumeiser.ptm.cli.rest.RestUtils;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import org.junit.Before;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

import static de.lgblaumeiser.ptm.datamanager.model.Activity.newActivity;

public abstract class AbstractHandlerTest {
	private static final String ID = "id";

	static final LocalDate DATE1 = LocalDate.of(2016, 06, 24);
	static final LocalTime TIME1 = LocalTime.of(12, 34);
	static final LocalTime TIME2 = LocalTime.of(13, 57);
	static final String ACTIVITY1NAME = "Act1";
	static final String ACTIVITY1NUMBER = "0815";
	static final String ACTIVITY2NAME = "NewAct2";
	static final String ACTIVITY2NUMBER = "4711";
	static final Activity ACTIVITY1 = newActivity().setActivityName(ACTIVITY1NAME).setBookingNumber(ACTIVITY1NUMBER).setId(1L).build();
	static final Activity ACTIVITY2 = newActivity().setActivityName(ACTIVITY2NAME).setBookingNumber(ACTIVITY2NUMBER).setHidden(true).setId(2L).build();
	static final String USER = "TestUser";
	static final String COMMENT = "TestComment";
	static final Booking BOOKING1 = Booking.newBooking().setActivity(ACTIVITY1).setBookingday(DATE1)
            .setUser(USER).setStarttime(TIME1).setEndtime(TIME2).build();

	protected static class TestCommandLogger implements CommandLogger {
	    StringBuffer logMessages = new StringBuffer();

		@Override
		public void log(String message) {
			logMessages.append(message);
			logMessages.append("xxxnewlinexxx");
		}
	}

	protected static class TestRestUtils extends RestUtils {
	    String apiNameGiven;
	    Map<String, String> bodyDataGiven;

        @Override
        public Long post(String apiName, Map<String, String> bodyData) {
            apiNameGiven = apiName;
            bodyDataGiven = bodyData;
            return 2L;
        }

        @Override
        public <T> T get(String apiName, Class<T> returnClass) {
        	apiNameGiven = apiName;
        	if (apiName.contains("activities")) {
        		if (apiName.contains("1")) {
        			return returnClass.cast(ACTIVITY1);
				}
				else if (apiName.contains("2")) {
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
        public void delete(String apiName) {
        	apiNameGiven = apiName;
        }

		@Override
		public TestRestUtils configure() {
			return this;
		}
	}

	TestCommandLogger logger = new TestCommandLogger();
	TestRestUtils restutils = new TestRestUtils().configure();
	CLI commandline = new PTMCLIConfigurator().configure();

	@Before
	public void before() {
        AbstractCommandHandler.setLogger(logger);
        AbstractCommandHandler.setPrinter(new PrettyPrinter().setLogger(logger));
        RestBaseService.setRestUtils(restutils);
		try {
			Field f = BOOKING1.getClass().getDeclaredField(ID);
			f.setAccessible(true);
			f.set(BOOKING1, 10L);
			f.setAccessible(false);
		} catch (IllegalAccessException | IllegalArgumentException | ClassCastException
				| NoSuchFieldException | SecurityException e) {
			throw new IllegalStateException(e);
		}

	}
}
