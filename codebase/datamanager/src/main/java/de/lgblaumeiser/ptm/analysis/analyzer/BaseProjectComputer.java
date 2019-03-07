/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.analysis.analyzer;

import static de.lgblaumeiser.ptm.util.Utils.emptyString;
import static de.lgblaumeiser.ptm.util.Utils.stringHasContent;

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

	@Override
	public Collection<Collection<String>> analyze(final CalculationPeriod period, final String user) {
		Map<String, Activity> keyToActivityMap = new HashMap<>();
		Map<String, Duration> keyToMinutesMap = new HashMap<>();
		Map<String, String> keyToCommentMap = new HashMap<>();
		Duration totalMinutes = calculateTimeMapping(getBookingsForPeriod(period, user), keyToActivityMap,
				keyToMinutesMap, keyToCommentMap);
		return createResultCollection(keyToActivityMap, keyToMinutesMap, keyToCommentMap, totalMinutes,
				period.isDayPeriod());
	}

	private Collection<Booking> getBookingsForPeriod(final CalculationPeriod period, final String user) {
		return bookingStore.retrieveAll().stream()
				.filter(b -> b.getUser().equals(user) && period.isInPeriod(b.getBookingday()))
				.collect(Collectors.toList());
	}

	private Duration calculateTimeMapping(final Collection<Booking> bookings,
			final Map<String, Activity> keyToActivityMap, final Map<String, Duration> keyToMinutesMap,
			final Map<String, String> keyToCommentMap) {
		Duration totalMinutes = Duration.ZERO;
		for (Booking booking : bookings) {
			if (booking.hasEndtime()) {
				totalMinutes = totalMinutes
						.plus(calculateBooking(booking, keyToActivityMap, keyToMinutesMap, keyToCommentMap));
			}
		}
		return totalMinutes;
	}

	private Duration calculateBooking(final Booking booking, final Map<String, Activity> keyToActivityMap,
			final Map<String, Duration> keyToMinutesMap, final Map<String, String> keyToCommentMap) {
		Activity activity = activityStore.retrieveById(booking.getActivity()).orElseThrow(IllegalStateException::new);
		String key = indexGetter(activity);
		keyToActivityMap.put(key, activity);
		Duration accumulatedMinutes = getMinutesForKey(Optional.ofNullable(keyToMinutesMap.get(key)));
		Duration activityLength = TimeSpan.newTimeSpan(booking).getLengthInMinutes();
		accumulatedMinutes = accumulatedMinutes.plus(activityLength);
		keyToMinutesMap.put(key, accumulatedMinutes);
		keyToCommentMap.put(key, getCommentForKey(Optional.ofNullable(keyToCommentMap.get(key)), booking.getComment()));
		return activityLength;
	}

	protected abstract String indexGetter(final Activity activity);

	private Duration getMinutesForKey(final Optional<Duration> currentDuration) {
		return currentDuration.orElse(Duration.ZERO);
	}

	private String getCommentForKey(final Optional<String> currentComment, final String newComment) {
		String comments = currentComment.orElse(emptyString());
		if (stringHasContent(newComment)) {
			if (stringHasContent(comments)) {
				comments = comments + ", " + newComment;
			} else {
				comments = newComment;
			}
		}
		return comments;
	}

	private Collection<Collection<String>> createResultCollection(final Map<String, Activity> keyToActivityMap,
			final Map<String, Duration> keyToMinutesMap, final Map<String, String> keyToCommentMap,
			final Duration totalMinutes, final boolean withComments) {
		Collection<Collection<String>> result = new ArrayList<>();
		result.add(createLine(getHeadline(), "Hours", "%", "Comments", withComments));
		result.addAll(
				computeResultLines(keyToActivityMap, keyToMinutesMap, keyToCommentMap, totalMinutes, withComments));
		result.add(createLine(getFootLine(), formatDuration(totalMinutes), "100.0%", emptyString(), withComments));
		return result;
	}

	protected abstract Collection<String> getHeadline(); // Arrays.asList("Project Id", )

	protected abstract Collection<String> getFootLine(); // Arrays.asList("Total", )

	private Collection<Collection<String>> computeResultLines(final Map<String, Activity> keyToActivityMap,
			final Map<String, Duration> keyToMinutesMap, final Map<String, String> keyToCommentsMap,
			final Duration totalMinutes, final boolean withComments) {
		Collection<Collection<String>> valueList = new ArrayList<>();
		for (String key : keyToActivityMap.keySet()) {
			valueList.add(calculateResultForActivity(keyToActivityMap.get(key), keyToMinutesMap.get(key), totalMinutes,
					keyToCommentsMap.get(key), withComments));
		}
		return valueList.stream().sorted((line1, line2) -> getSortCriteriaForResultLine(line1)
				.compareToIgnoreCase(getSortCriteriaForResultLine(line2))).collect(Collectors.toList());
	}

	protected abstract String getSortCriteriaForResultLine(final Collection<String> line);

	private Collection<String> calculateResultForActivity(final Activity activity, final Duration activityMinutes,
			final Duration totalMinutes, final String activityComments, boolean withComments) {
		return createLine(getKeyItems(activity), formatDuration(activityMinutes),
				formatPercentageString(totalMinutes, activityMinutes), activityComments, withComments);
	}

	private Collection<String> createLine(Collection<String> activityinfo, String activityMinutes, String totalMinutes,
			String activityComments, boolean withComments) {
		List<String> back = new ArrayList<>(activityinfo);
		back.add(activityMinutes);
		back.add(totalMinutes);
		if (withComments) {
			back.add(activityComments);
		}
		return back;
	}

	protected abstract Collection<String> getKeyItems(Activity activity);

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

	public BaseProjectComputer(final ObjectStore<Booking> bStore, final ObjectStore<Activity> aStore) {
		bookingStore = bStore;
		activityStore = aStore;
	}
}
