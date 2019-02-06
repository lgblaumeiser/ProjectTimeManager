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
	public Collection<Collection<Object>> analyze(final String analyzerId, final Collection<String> parameter) {
		Object[][] result = getRestUtils().get("/analysis/" + analyzerId + "/" + getIndexFromCollection(parameter, 0)
				+ "/" + getIndexFromCollection(parameter, 1),
				Optional.of(getServices().getCurrentUserStore().loadUserData()), Object[][].class);
		return convertToCollection(result);
	}

	private Collection<Collection<Object>> convertToCollection(final Object[][] resultData) {
		Collection<Collection<Object>> converted = new ArrayList<>();
		for (Object[] currentLine : resultData) {
			converted.add(asList(currentLine));
		}
		return converted;
	}
}
