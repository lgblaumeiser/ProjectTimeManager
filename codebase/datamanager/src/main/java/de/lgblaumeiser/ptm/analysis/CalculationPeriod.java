/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CalculationPeriod {
    List<LocalDate> daysInPeriod = new ArrayList<>();

    public CalculationPeriod(final LocalDate firstDay, final LocalDate firstDayAfter) {
        LocalDate currentDate = firstDay;
        while (currentDate.isBefore(firstDayAfter)) {
            daysInPeriod.add(currentDate);
            currentDate = currentDate.plusDays(1L);
        }
    }

    public boolean isInPeriod(final LocalDate current) {
        return daysInPeriod.contains(current);
    }

    public Collection<LocalDate> days() {
        return Collections.unmodifiableList(daysInPeriod);
    }

    public boolean isDayPeriod() {
        return daysInPeriod.size() == 1;
    }
}