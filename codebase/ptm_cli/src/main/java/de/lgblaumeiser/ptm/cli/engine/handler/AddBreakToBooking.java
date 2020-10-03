/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.time.LocalTime;
import java.util.Optional;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;
import de.lgblaumeiser.ptm.datamanager.model.Booking;

/**
 * Class that adds a break to an existing booking
 */
@Parameters(commandDescription = "Add a break to an existing booking")
public class AddBreakToBooking extends AbstractCommandHandler {
	@Parameter(names = { "-b", "--booking" }, description = "Booking id of the booking to end", required = true)
	private Long id;

	@Parameter(names = { "-s",
			"--starts-at" }, description = "Insert break at the given time", converter = LocalTimeConverter.class, required = true)
	private Optional<LocalTime> breakstart = Optional.empty();

	@Parameter(names = { "-l",
			"--length" }, description = "Length of the break, default is 30 minutes", converter = OptionalStringConverter.class)
	private Optional<String> breaklength = Optional.empty();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler#handleCommand()
	 */
	@Override
	public void handleCommand() {
		getLogger().log("Insert break ...");
		Booking toBeChanged = getServices().getBookingsStore().retrieveById(id).orElseThrow(IllegalStateException::new);
		LocalTime startOfBreak = breakstart.orElseThrow(IllegalStateException::new);
		getServices().getBookingsStore().breakAt(toBeChanged, startOfBreak,
				breaklength.map(length -> Integer.parseInt(length)));
		getLogger().log("... executed successfully");
	}
}
