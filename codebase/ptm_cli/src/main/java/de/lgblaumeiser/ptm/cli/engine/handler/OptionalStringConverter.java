/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import java.util.Optional;

import com.beust.jcommander.IStringConverter;

import de.lgblaumeiser.ptm.cli.Utils;

public class OptionalStringConverter implements IStringConverter<Optional<String>> {

	@Override
	public Optional<String> convert(final String value) {
		if (Utils.stringHasContent(value)) {
			return Optional.of(value);
		}
		return Optional.empty();
	}
}
