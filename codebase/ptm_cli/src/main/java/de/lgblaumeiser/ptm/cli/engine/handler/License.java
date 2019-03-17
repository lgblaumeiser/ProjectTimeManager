/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

/**
 * Command for showing license information
 */
@Parameters(commandDescription = "Show license information for cli and backend")
public class License extends AbstractCommandHandler {
	@Override
	public void handleCommand() {
		getLogger().log("License information of backend:");
		getLogger().log(getServices().getInfrastructureServices().licenses());
		getLogger().log("\n==================================================\n");
		getLogger().log("License information of cli:");
		getLogger().log(getCLILicenses());
	}

	private String getCLILicenses() {
		try {
			return IOUtils.toString(getClass().getClassLoader().getResourceAsStream("license_info.txt"), "UTF-8");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
