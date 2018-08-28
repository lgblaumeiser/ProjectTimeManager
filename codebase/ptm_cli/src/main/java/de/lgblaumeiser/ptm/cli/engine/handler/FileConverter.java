/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 */
package de.lgblaumeiser.ptm.cli.engine.handler;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;

import com.beust.jcommander.IStringConverter;

/**
 * A parameter converter to convert a string to a file object
 */
public class FileConverter implements IStringConverter<File> {
	@Override
	public File convert(String pathAsString) {
		File file = new File(pathAsString);
		File parent = file.getParentFile();
		if (parent != null) {
			checkState(parent.exists());
		}
		return file;
	}
}
