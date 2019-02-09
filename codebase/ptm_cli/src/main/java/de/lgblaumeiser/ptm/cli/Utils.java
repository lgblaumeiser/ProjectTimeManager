/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli;

import java.util.Collection;
import java.util.Iterator;

/**
 * Class with some static utils methods to get rid of apache and guava
 * dependencies
 * 
 * Not really well designed a bunch of unrelated one liners
 */
public class Utils {
	public static boolean stringHasContent(final String toBeChecked) {
		return (toBeChecked != null) && !toBeChecked.isEmpty();
	}

	public static void assertState(final boolean condition) {
		if (!condition) {
			throw new IllegalStateException();
		}
	}

	public static void assertState(final boolean condition, final Object message) {
		if (!condition) {
			throw new IllegalStateException(message.toString());
		}
	}

	public static <T> T getIndexFromCollection(final Collection<T> col, final int index) {
		Iterator<T> iter = col.iterator();
		int cur = 0;
		while (iter.hasNext()) {
			T item = iter.next();
			if (cur == index) {
				return item;
			}
			cur++;
		}
		throw new IllegalStateException();
	}

	public static <T> T getFirstFromCollection(final Collection<T> col) {
		return getIndexFromCollection(col, 0);
	}
}
