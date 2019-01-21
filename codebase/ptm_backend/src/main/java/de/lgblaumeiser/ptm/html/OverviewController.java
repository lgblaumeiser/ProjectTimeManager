/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.html;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.rest.ServiceMapper;
import de.lgblaumeiser.ptm.util.Utils;

/**
 * HTML app to get an overview of bookings
 */
@Controller
@RequestMapping("/overview")
public class OverviewController {
	private static final String TEMPLATENAME = "overview";
	private static final String HOURANALYSISID = "HOURS";
	private static final String PROJECTANALYSISID = "PROJECTS";
	private static final String MONTHTIMEFRAME = "month";
	private static final String DAYTIMEFRAME = "day";

	private static final String DATEATTRIBUTE = "date";
	private static final String MONTHATTRIBUTE = "month";
	private static final String ALLACTIVITIESATTRIBUTE = "allActivities";
	private static final String BOOKINGSFORDAYATTRIBUTE = "bookingsForDay";
	private static final String HOURSANALYSISATTRIBUTE = "hourAnalysis";
	private static final String HOURSANALYSISHEADLINEATTRIBUTE = "hourAnalysisHeadline";
	private static final String PROJECTANALYSISTODAYATTRIBUTE = "projectAnalysisToday";
	private static final String PROJECTANALYSISTODAYHEADLINEATTRIBUTE = "projectAnalysisTodayHeadline";
	private static final String PROJECTANALYSISMONTHATTRIBUTE = "projectAnalysisMonth";
	private static final String PROJECTANALYSISMONTHHEADLINEATTRIBUTE = "projectAnalysisMonthHeadline";

	@Autowired
	private ServiceMapper services;

	static class BookingStruct {
		public Long id;
		public String starttime;
		public String endtime;
		public Long activity;
		public String comment;

		BookingStruct(final Long id, final String starttime, final String endtime, final Long activity,
				final String comment) {
			this.id = id;
			this.starttime = starttime;
			this.endtime = endtime;
			this.activity = activity;
			this.comment = comment;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/")
	public String dataPageForToday(final Model model) {
		return createPage(model, LocalDate.now());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{dayString}")
	public String dataPageForDay(@PathVariable final String dayString, final Model model) {
		return createPage(model, LocalDate.parse(dayString));
	}

	private String createPage(final Model model, final LocalDate dateToShow) {
		model.addAttribute(DATEATTRIBUTE, dateToShow.format(DateTimeFormatter.ISO_LOCAL_DATE));
		model.addAttribute(MONTHATTRIBUTE, dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM")));
		model.addAttribute(ALLACTIVITIESATTRIBUTE,
				services.activityStore().retrieveAll().stream().filter(act -> !act.isHidden())
						.sorted((a1, a2) -> a1.getBookingNumber().compareToIgnoreCase(a2.getBookingNumber()))
						.collect(toList()));
		model.addAttribute(BOOKINGSFORDAYATTRIBUTE, services.bookingStore().retrieveAll().stream()
				.filter(b -> b.getBookingday().equals(dateToShow)).sorted(Comparator.comparing(Booking::getStarttime))
				.map(b -> new BookingStruct(b.getId(), b.getStarttime().format(DateTimeFormatter.ofPattern("HH:mm")),
						b.hasEndtime() ? b.getEndtime().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
						b.getActivity(), b.getComment()))
				.collect(toList()));

		setAnalysisData(model, HOURSANALYSISHEADLINEATTRIBUTE, HOURSANALYSISATTRIBUTE, HOURANALYSISID, MONTHTIMEFRAME,
				dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM")));

		setAnalysisData(model, PROJECTANALYSISTODAYHEADLINEATTRIBUTE, PROJECTANALYSISTODAYATTRIBUTE, PROJECTANALYSISID,
				DAYTIMEFRAME, dateToShow.format(DateTimeFormatter.ISO_LOCAL_DATE));

		setAnalysisData(model, PROJECTANALYSISMONTHHEADLINEATTRIBUTE, PROJECTANALYSISMONTHATTRIBUTE, PROJECTANALYSISID,
				MONTHTIMEFRAME, dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM")));
		return TEMPLATENAME;
	}

	private void setAnalysisData(final Model model, final String headlineAttr, final String analysisAttr,
			final String analysisId, final String timeFrameType, final String timeFrame) {
		Collection<Collection<Object>> analysisResult = services.analysisService().analyze(analysisId,
				Arrays.asList(timeFrameType, timeFrame));
		Collection<String> headline = mapToString(Utils.getFirstFromCollection(analysisResult));
		Collection<Collection<String>> bodydata = analysisResult.stream().skip(1).map(col -> mapToString(col))
				.collect(toList());
		model.addAttribute(headlineAttr, headline);
		model.addAttribute(analysisAttr, bodydata);
	}

	private Collection<String> mapToString(final Collection<Object> source) {
		return source.stream().map(o -> o.toString()).collect(toList());
	}
}
