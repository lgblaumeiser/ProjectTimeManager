/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.io.File;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

/**
 * Command to backup all activities and bookings into zip file
 *
 */
@Parameters(commandDescription = "Backup all bookings, activities and users into zip file")
public class Backup extends AbstractCommandHandler {
	@Parameter(names = { "-z",
			"--zipfile" }, description = "Name of zipfile to store the data", required = true, converter = FileConverter.class)
	private File zipfile;

	@Override
	public void handleCommand() {
		getLogger().log("Download Backup ...");
		getServices().getInfrastructureServices().backup(zipfile);
		getLogger().log("... done");
	}
}
