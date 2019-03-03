/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.getIndexFromCollection;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import de.lgblaumeiser.ptm.analysis.Analysis;
import de.lgblaumeiser.ptm.analysis.CalculationPeriod;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.datamanager.model.TimeSpan;
import de.lgblaumeiser.ptm.store.ObjectStore;

/**
 * An analysis to compute the amount of hours per a activity. The computer
 * calculates the percentage of an activity on the overall hours and maps these
 * percentages to the amount of 8 hours per booking day. This way, this fulfills
 * the requirements of the author concerning his time keeping.
 */
public class ProjectComputer implements Analysis {
	private final ObjectStore<Booking> bookingStore;
	private final ObjectStore<Activity> activityStore;

	@Override
	public Collection<Collection<String>> analyze(final CalculationPeriod period, final String user) {
		Map<Long, Duration> activityToMinutesMap = new HashMap<>();
		Map<Long, String> activityToCommentMap = new HashMap<>();
		Duration totalMinutes = calculateTimeActivityMapping(getBookingsForPeriod(period, user), activityToMinutesMap,
				activityToCommentMap);
		return createResultCollection(activityToMinutesMap, activityToCommentMap, totalMinutes, period.isDayPeriod());
	}

	private Collection<Booking> getBookingsForPeriod(final CalculationPeriod period, final String user) {
		return bookingStore.retrieveAll().stream()
				.filter(b -> b.getUser().equals(user) && period.isInPeriod(b.getBookingday()))
				.collect(Collectors.toList());
	}

	private Duration calculateTimeActivityMapping(final Collection<Booking> bookings,
			final Map<Long, Duration> activityToMinutesMap, final Map<Long, String> activityToCommentMap) {
		Duration totalMinutes = Duration.ZERO;
		for (Booking booking : bookings) {
			if (booking.hasEndtime()) {
				totalMinutes = totalMinutes
						.plus(handleActivityMapping(booking, activityToMinutesMap, activityToCommentMap));
			}
		}
		return totalMinutes;
	}

	private Duration handleActivityMapping(final Booking booking, final Map<Long, Duration> activityToMinutesMap,
			final Map<Long, String> activityToCommentMap) {
		Long activity = booking.getActivity();
		Duration accumulatedMinutes = getMinutesForActivity(activity, activityToMinutesMap);
		Duration activityLength = TimeSpan.newTimeSpan(booking).getLengthInMinutes();
		accumulatedMinutes = accumulatedMinutes.plus(activityLength);
		activityToMinutesMap.put(activity, accumulatedMinutes);
		String currentComments = getCommentsForActivity(activity, activityToCommentMap);
		currentComments = currentComments + ", " + booking.getComment();
		activityToCommentMap.put(activity, currentComments);
		return activityLength;
	}

	private Duration getMinutesForActivity(final Long activityId, final Map<Long, Duration> activityToMinutesMap) {
		Duration accumulatedMinutes = activityToMinutesMap.get(activityId);
		if (accumulatedMinutes == null) {
			accumulatedMinutes = Duration.ZERO;
		}
		return accumulatedMinutes;
	}

	private String getCommentsForActivity(final Long activityId, final Map<Long, String> activityToCommentMap) {
		String comments = activityToCommentMap.get(activityId);
		if (!stringHasContent(comments)) {
			comments = emptyString();
		}
		return comments;
	}

	private Collection<Collection<String>> createResultCollection(final Map<Long, Duration> activityToMinutesMap,
			final Map<Long, String> activityToCommentMap, final Duration totalMinutes, final boolean withComments) {
		Collection<Collection<String>> result = new ArrayList<>();
		result.add(createLine(Arrays.asList("Activity", "Project Id", "Project Activity", "Hours", "%"), "Comments",
				withComments));
		result.addAll(computeResultLines(activityToMinutesMap, activityToCommentMap, totalMinutes, withComments));
		result.add(
				createLine(Arrays.asList("Total", emptyString(), emptyString(), formatDuration(totalMinutes), "100.0%"),
						emptyString(), withComments));
		return result;
	}

	private Collection<Collection<String>> computeResultLines(final Map<Long, Duration> activityToMinutesMap,
			final Map<Long, String> activityToCommentsMap, final Duration totalMinutes, final boolean withComments) {
		Collection<Collection<String>> valueList = new ArrayList<>();
		for (Entry<Long, Duration> currentActivity : activityToMinutesMap.entrySet()) {
			valueList.add(calculateResultForActivity(currentActivity.getKey(), currentActivity.getValue(), totalMinutes,
					activityToCommentsMap.get(currentActivity.getKey()), withComments));
		}
		return valueList.stream().sorted((line1, line2) -> getSortCriteriaForResultLine(line1)
				.compareToIgnoreCase(getSortCriteriaForResultLine(line2))).collect(Collectors.toList());
	}

	private String getSortCriteriaForResultLine(final Collection<String> line) {
		return getProjectIdFromResultLine(line) + "_" + getProjectSubidFromResultLine(line);
	}

	private String getProjectIdFromResultLine(final Collection<String> line) {
		return getIndexFromCollection(line, 1);
	}

	private String getProjectSubidFromResultLine(final Collection<String> line) {
		return getIndexFromCollection(line, 2);
	}

	private Collection<String> calculateResultForActivity(final Long activityId, final Duration activityMinutes,
			final Duration totalMinutes, final String activityComments, boolean withComments) {
		String percentage = formatPercentageString(totalMinutes, activityMinutes);
		Activity activity = activityStore.retrieveById(activityId).orElseThrow(IllegalStateException::new);
		return createLine(Arrays.asList(activity.getActivityName(), activity.getProjectId(),
				activity.getProjectActivity(), formatDuration(activityMinutes), percentage), activityComments,
				withComments);
	}

	private Collection<String> createLine(final List<String> baseList, final String additional, final boolean addit) {
		List<String> back = new ArrayList<>(baseList);
		if (addit) {
			back.add(additional);
		}
		return back;

	}

	private String formatPercentageString(final Duration totalMinutes, final Duration totalMinutesId) {
		double percentage = (double) totalMinutesId.toMinutes() / (double) totalMinutes.toMinutes();
		return String.format("%2.1f", percentage * 100.0) + "%";
	}

	private String formatDuration(final Duration duration) {
		long minutes = duration.toMinutes();
		char pre = minutes < 0 ? '-' : ' ';
		minutes = Math.abs(minutes);
		return String.format("%c%02d:%02d", pre, minutes / 60, minutes % 60);
	}

	public ProjectComputer(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
		bookingStore = bStore;
		activityStore = aStore;
	}
}
