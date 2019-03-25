/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis;

import static de.lgblaumeiser.ptm.util.Utils.assertState;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class that allows to run analysis on the data
 */
public class DataAnalysisService {
    private final Map<String, Analysis> analysisStore = new HashMap<>();

    /**
     * Run an analysis with id analyzerId and the given parameters
     *
     * @param analyzerId      The id of the analyzer to run
     * @param periodId        Identifier of period, either month, day, or period
     * @param user            The use whose data is used for the analysis
     * @param periodIndicator According to the id, the indicator which time period
     *                        is meant, e.g. a day or a month in ISO_LOCAL_DATE
     *                        notation,or two days for a period
     * @return The result as an implemented analysis result interface
     */
    public Collection<Collection<String>> analyze(final String analyzerId, final String user, final String periodId,
            final String... periodIndicator) {
        assertState(stringHasContent(analyzerId));
        assertState(stringHasContent(periodId));
        assertState(periodIndicator.length > 0);
        assertState(stringHasContent(periodIndicator[0]));
        assertState(stringHasContent(user));

        Analysis analysis = analysisStore.get(analyzerId);
        assertState(analysis != null);

        return analysis.analyze(getCalculationPeriod(periodId, periodIndicator), user);
    }

    private CalculationPeriod getCalculationPeriod(final String selector, final String... timestring) {
        switch (selector.toLowerCase()) {
        case "day":
            return getDayPeriod(LocalDate.parse(timestring[0]));
        case "month":
            return getMonthPeriod(YearMonth.parse(timestring[0]));
        case "period":
            assertState(timestring.length == 2);
            assertState(stringHasContent(timestring[1]));

            return getPeriod(LocalDate.parse(timestring[0]), LocalDate.parse(timestring[1]));
        default:
            throw new IllegalStateException();
        }
    }

    private CalculationPeriod getDayPeriod(final LocalDate day) {
        return new CalculationPeriod(day, day.plusDays(1L));
    }

    private CalculationPeriod getPeriod(final LocalDate firstDay, final LocalDate firstDateAfter) {
        return new CalculationPeriod(firstDay, firstDateAfter);
    }

    private CalculationPeriod getMonthPeriod(final YearMonth month) {
        return new CalculationPeriod(month.atDay(1), month.plusMonths(1L).atDay(1));
    }

    DataAnalysisService addAnalysis(final String id, final Analysis analysis) {
        analysisStore.put(id, analysis);
        return this;
    }

    DataAnalysisService() {
        // Only in package creation
    }
}
