/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static de.lgblaumeiser.ptm.cli.Utils.stringHasContent;
import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

/**
 * Command to add an activity
 */
@Parameters(commandDescription = "Boot the backend, requires an existing docker-compose installation in version 1.22.0 or higher")
public class ControlBackend extends AbstractCommandHandler {
	@Parameter(names = { "--start" }, description = "Start the backend")
	private boolean start;

	@Parameter(names = { "--stop" }, description = "Stop the backend")
	private boolean stop;

	@Parameter(names = { "--status" }, description = "Check status of the backend")
	private boolean status;

    @Override
    public void handleCommand() {
        try {
            getLogger().log("Check status of PTM backend...");
            boolean isRunning = isBackendRunning();

            if (start && !isRunning) {
                getLogger().log("Start PTM backend...");
                startBackendProcess("up", "-d");
                getLogger().log("PTM backend is running, stay tuned until system is initialized...");
            } else if (stop && isRunning) {
                getLogger().log("PTM backend shutting down...");
                startBackendProcess("down");
                getLogger().log("...done");
            } else if (status) {
                getLogger().log(isRunning ? "PTM backend running!" : "PTM backend down!");
            } else {
                getLogger().log("Nothing to do!");
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

	private void startBackendProcess(final String... command) throws IOException {
		BufferedReader outputReader = prepareAndStartProcess(command);
		String line;
		while ((line = outputReader.readLine()) != null) {
			getLogger().log(line);
		}
	}

	private boolean isBackendRunning() throws IOException {
		BufferedReader outputReader = prepareAndStartProcess("ps");
		String message = IOUtils.toString(outputReader);
		if (stringHasContent(message)) {
			return message.contains("_ptm");
		}
		return false;
	}

	private BufferedReader prepareAndStartProcess(final String... command) throws IOException {
		String pathToYaml = createPathToYaml();
		List<String> commands = new ArrayList<>();
		commands.addAll(asList("sudo", "docker-compose", "-f", pathToYaml));
		commands.addAll(asList(command));
		BufferedReader outputReader = startExternalProcess(commands);
		return outputReader;
	}

	private String createPathToYaml() {
		String ptmHome = Optional.ofNullable(System.getenv("PTM_HOME")).orElseThrow(IllegalStateException::new);
		return new File(ptmHome, "ptm_docker_config.yml").getAbsolutePath();
	}

    private BufferedReader startExternalProcess(final List<String> commands) throws IOException {
        String[] commandarray = commands.toArray(new String[commands.size()]);
        Process process = new ProcessBuilder(commandarray).redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.PIPE).start();
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return outputReader;
    }
}
