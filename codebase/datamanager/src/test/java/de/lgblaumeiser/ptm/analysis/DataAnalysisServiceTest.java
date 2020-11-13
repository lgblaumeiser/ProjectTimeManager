/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.getFirstFromCollection;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class DataAnalysisServiceTest {
    private DataAnalysisService testee;

    private static final String ANALYSISID = "testanalysis";
    private static final String PERIODMONTH = "month";
    private static final String PERIODWEEK = "period";
    private static final String PERIODDAY = "day";
    private static final String DATESTRINGMONTH = "2017-03";
    private static final String DATESTRINGDAY = "2017-03-08";
    private static final String DATESTRINGDAYFIRST = "2017-03-06";
    private static final String DATESTRINGDAYFIRSTAFTER = "2017-03-13";
    private static final String USER = "MyFairLady";

    @Before
    public void before() {
        DataAnalysisService testSetup = new DataAnalysisService().addAnalysis(ANALYSISID, new Analysis() {
            @Override
            public Collection<Collection<String>> analyze(final CalculationPeriod period, final String user) {
                Collection<String> returnParam = new ArrayList<>(period
                        .days()
                        .stream()
                        .map(date -> date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .collect(toList()));
                return asList(returnParam);
            }
        });
        testee = testSetup;
    }

    @Test
    public void testDataAnalysisServicePeriod() {
        Collection<Collection<String>> result = testee.analyze(ANALYSISID, USER,
                new CalculationPeriod(LocalDate.parse(DATESTRINGDAYFIRST), LocalDate.parse(DATESTRINGDAYFIRSTAFTER)));
        assertEquals(1, result.size());
        Collection<String> content = getFirstFromCollection(result);
        assertEquals(7, content.size());
    }

    @Test
    public void testDataAnalysisServiceDay() {
        Collection<Collection<String>> result = testee.analyze(ANALYSISID, USER,
                new CalculationPeriod(LocalDate.parse(DATESTRINGDAY), LocalDate.parse(DATESTRINGDAY).plusDays(1L)));
        assertEquals(1, result.size());
        Collection<String> content = getFirstFromCollection(result);
        assertEquals(1, content.size());
    }

    @Test(expected = IllegalStateException.class)
    public void testDataAnalysisServiceUnknownUser() {
        testee.analyze(ANALYSISID, emptyString(),
                new CalculationPeriod(LocalDate.parse(DATESTRINGDAYFIRST), LocalDate.parse(DATESTRINGDAYFIRSTAFTER)));
    }

    @Test(expected = IllegalStateException.class)
    public void testDataAnalysisServiceEmptyId() {
        testee.analyze(emptyString(), USER,
                new CalculationPeriod(LocalDate.parse(DATESTRINGDAYFIRST), LocalDate.parse(DATESTRINGDAYFIRSTAFTER)));
    }

    @Test(expected = IllegalStateException.class)
    public void testDataAnalysisServiceNullId() {
        testee.analyze(null, USER,
                new CalculationPeriod(LocalDate.parse(DATESTRINGDAYFIRST), LocalDate.parse(DATESTRINGDAYFIRSTAFTER)));
    }

    @Test(expected = IllegalStateException.class)
    public void testDataAnalysisServiceCorruptCalculationPeriod() {
        testee.analyze(ANALYSISID, USER,
                new CalculationPeriod(LocalDate.parse(DATESTRINGDAY), LocalDate.parse(DATESTRINGDAY)));
    }
}
