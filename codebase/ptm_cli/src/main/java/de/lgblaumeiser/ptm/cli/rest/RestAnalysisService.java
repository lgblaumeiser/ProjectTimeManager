/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.rest;

import static de.lgblaumeiser.ptm.cli.Utils.getIndexFromCollection;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Rest Proxy Implementation for accessing analysis results over rest api.
 */
public class RestAnalysisService extends RestBaseService {
	public Collection<Collection<String>> analyze(final String analyzerId, final Collection<String> parameter) {
		String[][] result = getRestUtils().get(
				"/analysis/" + analyzerId + "/" + getIndexFromCollection(parameter, 0) + "/"
						+ getIndexFromCollection(parameter, 1),
				Optional.of(getServices().getCurrentUserStore().loadUserData()), String[][].class);
		return convertToCollection(result);
	}

	private Collection<Collection<String>> convertToCollection(final String[][] resultData) {
		Collection<Collection<String>> converted = new ArrayList<>();
		for (String[] currentLine : resultData) {
			converted.add(asList(currentLine));
		}
		return converted;
	}
}
