/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

    public static String emptyString() {
        return "";
    }

    public static void assertState(final boolean condition) {
        if (!condition) {
            throw new IllegalStateException();
        }
    }

    public static LocalDate parseDateString(final String dateString) {
        return LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE);
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

    public static <T> T getLastFromCollection(final Collection<T> col) {
        return getIndexFromCollection(col, col.size() - 1);
    }

    public static <T> T getOnlyFromCollection(final Collection<T> col) {
        assertState(col.size() == 1);
        return getFirstFromCollection(col);
    }
}
