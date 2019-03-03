/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.cli.engine;

import static java.util.Arrays.asList;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;

public class PrettyPrinter {
	private CommandLogger logger;

	public PrettyPrinter setLogger(final CommandLogger logger) {
		this.logger = logger;
		return this;
	}

	public void tablePrint(final Collection<Collection<Object>> data) {
		List<Integer> sizelist = new ArrayList<>();
		for (Collection<Object> line : data) {
			int index = 0;
			for (Object field : line) {
				setMaxToList(sizelist, index, field.toString().length());
				index++;
			}
		}

		for (Collection<Object> line : data) {
			logger.log(createString(line, sizelist));
		}
	}

	private void setMaxToList(final List<Integer> list, final int index, final int currentLength) {
		if (index < list.size()) {
			list.set(index, Math.max(list.get(index), currentLength));
		} else {
			list.add(index, currentLength);
		}
	}

	public void bookingPrint(final Collection<Booking> data) {
		Collection<Collection<Object>> table = new ArrayList<>();
		table.add(asList("Activity", "Starttime", "Endtime", "Id", "Comment"));
		for (Booking booking : data) {
			table.add(flattenBooking(booking));
		}
		tablePrint(table);
	}

	public void activityPrint(final Collection<Activity> data) {
		Collection<Collection<Object>> table = new ArrayList<>();
		table.add(asList("Activity", "Project Id", "Project Sub Id", "Activity Id"));
		for (Activity activity : data) {
			table.add(flattenActivity(activity));
		}
		tablePrint(table);
	}

	private Collection<Object> flattenBooking(final Booking booking) {
		List<Object> line = new ArrayList<>();
		line.add(booking.getActivity());
		line.add(booking.getStarttime().format(DateTimeFormatter.ofPattern("HH:mm")));
		line.add(booking.hasEndtime() ? booking.getEndtime().format(DateTimeFormatter.ofPattern("HH:mm")) : " ");
		line.add(booking.getId().toString());
		line.add(booking.getComment());
		return line;
	}

	private Collection<Object> flattenActivity(final Activity activity) {
		return asList(activity.getActivityName(), activity.getProjectId(), activity.getProjectActivity(),
				activity.getId().toString());
	}

	private String createString(final Collection<Object> columns, final List<Integer> sizelist) {
		StringBuilder resultString = new StringBuilder();
		resultString.append("| ");
		int index = 0;
		for (Object current : columns) {
			resultString.append(rightPad(current.toString(), sizelist.get(index)));
			resultString.append(" | ");
			index++;
		}
		return resultString.toString();
	}

	private String rightPad(final String source, final int size) {
		int missing = size - source.length();
		StringBuffer result = new StringBuffer();
		result.append(source);
		for (int index = 0; index < missing; index++) {
			result.append(" ");
		}
		return result.toString();
	}
}
