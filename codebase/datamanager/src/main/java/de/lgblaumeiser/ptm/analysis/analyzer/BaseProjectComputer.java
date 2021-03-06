/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.analysis.analyzer.DateFormatterUtil.formatDuration;
import static de.lgblaumeiser.ptm.analysis.analyzer.DateFormatterUtil.formatPercentageString;
import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;
import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import de.lgblaumeiser.ptm.analysis.Analysis;
import de.lgblaumeiser.ptm.analysis.CalculationPeriod;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.TimeSpan;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * An analysis to compute the amount of hours per project. The computer
 * calculates the percentage of a project on the overall hours and maps these
 * percentages to the amount of 8 hours per booking day. This way, this fulfills
 * the requirements of the author concerning his time keeping.
 * 
 * This is the abstract basis that can used for activities and the aggregating
 * projects
 */
public abstract class BaseProjectComputer implements Analysis {
    private final ObjectStore<Booking> bookingStore;
    private final ObjectStore<Activity> activityStore;

    private static class AnalysisData {
        private Map<String, Activity> keyToActivityMap = new HashMap<>();
        private Map<String, Duration> keyToMinutesMap = new HashMap<>();
        private Map<String, String> keyToCommentMap = new HashMap<>();

        void setActivityData(final String key, final Activity activity, final Duration currentLength,
                final String comment) {
            keyToActivityMap.put(key, activity);
            calculateTimeForActivity(key, currentLength);
            calculateAccumulatedComment(key, comment);
        }

        private void calculateAccumulatedComment(final String key, final String newComment) {
            String currentComment = getCurrentComment(key);
            if (stringHasContent(newComment) && !currentComment.contains(newComment)) {
                currentComment = (stringHasContent(currentComment)
                        ? currentComment + ", "
                        : emptyString())
                        + newComment;
            }
            keyToCommentMap.put(key, currentComment);
        }

        private String getCurrentComment(final String key) {
            return Optional.ofNullable(keyToCommentMap.get(key)).orElse(emptyString());
        }

        private void calculateTimeForActivity(final String key, final Duration currentLength) {
            keyToMinutesMap.put(key, getMinutesForKey(key).plus(currentLength));
        }

        private Duration getMinutesForKey(final String key) {
            return Optional.ofNullable(keyToMinutesMap.get(key)).orElse(Duration.ZERO);
        }

        Collection<String> getKeys() {
            return keyToActivityMap.keySet();
        }

        Activity getActivityForKey(final String key) {
            return keyToActivityMap.get(key);
        }

        Duration getBookedMinutesForKey(final String key) {
            return keyToMinutesMap.get(key);
        }

        String getAccumulatedCommentForKey(final String key) {
            return keyToCommentMap.get(key);
        }
    }

    @Override
    public Collection<Collection<String>> analyze(final CalculationPeriod period, final String user) {
        AnalysisData currentAnalysis = new AnalysisData();

        return createResultCollection(
                currentAnalysis,
                calculateTimeMapping(getBookingsForPeriod(period, user), currentAnalysis),
                period.isDayPeriod());
    }

    private Collection<Booking> getBookingsForPeriod(final CalculationPeriod period, final String user) {
        return bookingStore
                .retrieveAll()
                .stream()
                .filter(b -> b.getUser().equals(user) && period.isInPeriod(b.getBookingday()))
                .collect(toList());
    }

    private Duration calculateTimeMapping(final Collection<Booking> bookings, final AnalysisData currentAnalysis) {
        Duration totalMinutes = Duration.ZERO;
        for (Booking booking : bookings) {
            if (booking.hasEndtime()) {
                totalMinutes = totalMinutes.plus(calculateBooking(booking, currentAnalysis));
            }
        }
        return totalMinutes;
    }

    private Duration calculateBooking(final Booking booking, final AnalysisData currentAnalysis) {
        Activity activity = retrieveActivityForBooking(booking);
        Duration activityLength = calculateActivityLengthForBooking(booking);
        currentAnalysis.setActivityData(indexGetter(activity), activity, activityLength, booking.getComment());
        return activityLength;
    }

    private Activity retrieveActivityForBooking(final Booking booking) {
        return activityStore.retrieveById(booking.getActivity()).orElseThrow(IllegalStateException::new);
    }

    private Duration calculateActivityLengthForBooking(final Booking booking) {
        return TimeSpan.newTimeSpan(booking).getLengthInMinutes();
    }

    protected abstract String indexGetter(final Activity activity);

    private Collection<Collection<String>> createResultCollection(final AnalysisData currentAnalysis,
            final Duration totalMinutes, final boolean withComments) {
        Collection<Collection<String>> result = new ArrayList<>();
        result.add(
                createLine(getHeadlineNameElement(), getHeadlineIdElement(), "Hours", "%", "Comments", withComments));
        result.addAll(computeResultLines(currentAnalysis, totalMinutes, withComments));
        result.add(createLine("Total", emptyString(), formatDuration(totalMinutes), "100.0%", emptyString(),
                withComments));
        return result;
    }

    protected abstract String getHeadlineNameElement();

    protected abstract String getHeadlineIdElement();

    private Collection<Collection<String>> computeResultLines(final AnalysisData currentAnalysis,
            final Duration totalMinutes, final boolean withComments) {
        Collection<Collection<String>> valueList = new ArrayList<>();
        for (String key : currentAnalysis.getKeys()) {
            valueList.add(
                    calculateResultForActivity(
                            currentAnalysis.getActivityForKey(key),
                            currentAnalysis.getBookedMinutesForKey(key),
                            totalMinutes,
                            currentAnalysis.getAccumulatedCommentForKey(key),
                            withComments));
        }
        return sortResultList(valueList);
    }

    private Collection<String> calculateResultForActivity(final Activity element, final Duration elementMinutes,
            final Duration totalMinutes, final String elementComments, final boolean withComments) {
        return createLine(
                getElementName(element),
                getElementId(element),
                formatDuration(elementMinutes),
                formatPercentageString(totalMinutes, elementMinutes),
                elementComments,
                withComments);
    }

    private Collection<String> createLine(final String elementName, final String elementId, final String elementMinutes,
            final String totalMinutes, final String elementComments, final boolean withComments) {
        List<String> back = new ArrayList<>();
        back.add(elementName);
        back.add(elementId);
        back.add(elementMinutes);
        back.add(totalMinutes);
        if (withComments) {
            back.add(elementComments);
        }
        return back;
    }

    protected abstract String getElementName(final Activity activity);

    protected abstract String getElementId(final Activity activity);

    private List<Collection<String>> sortResultList(final Collection<Collection<String>> valueList) {
        return valueList
                .stream()
                .sorted((line1, line2) -> compareResultLines(line1, line2))
                .collect(Collectors.toList());
    }

    private int compareResultLines(final Collection<String> line1, final Collection<String> line2) {
        return getIdElementFromLine(line1).compareToIgnoreCase(getIdElementFromLine(line2));
    }

    private String getIdElementFromLine(final Collection<String> line) {
        return getIndexFromCollection(line, 1);
    }

    public BaseProjectComputer(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
        bookingStore = bStore;
        activityStore = aStore;
    }
}
