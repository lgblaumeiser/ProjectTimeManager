/*
 * Copyright 2016, 2017 Lars Geyer-Blaumeiser <lgblaumeiser@gmail.com>
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.get;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Collection;

import de.lgblaumeiser.ptm.cli.engine.AbstractCommandHandler;

/**
 * Command to add an activity
 */
public class AddActivity extends AbstractCommandHandler {
	@Override
	public void handleCommand(final Collection<String> parameters) {
		checkState(parameters.size() > 1);
		String name = get(parameters, 0);
		String id = get(parameters, 1);
		checkState(isNotBlank(name));
		checkState(isNotBlank(id));
		getLogger().log("Add activity " + name + " with booking number " + id);
		String restId = getServices().getRestUtils().postActivity(name, id);
		getLogger().log("Activity added with internal id " + restId + "\n");
	}

	@Override
	public String toString() {
		return "Adds an activity to the list of available activities, Params: <1> Activity Name, <2> Activity Id";
	}
}
