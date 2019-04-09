/*
 * Copyright by Lars Geyer-Blaumeiser <lars@lgblaumeiser.de>
 *
 * Licensed under MIT license
 * 
 * SPDX-License-Identifier: MIT
 */
package de.lgblaumeiser.ptm.html;

import static java.util.stream.Collectors.toList;

import java.security.Principal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.lgblaumeiser.ptm.ServiceMapper;
import de.lgblaumeiser.ptm.datamanager.model.Activity;
import de.lgblaumeiser.ptm.datamanager.model.Booking;
import de.lgblaumeiser.ptm.util.Utils;

/**
 * HTML app to get an overview of bookings
 */
@Controller
@RequestMapping("/overview")
public class OverviewController {
    private static final String TEMPLATENAME = "overview";
    private static final String HOURANALYSISID = "HOURS";
    private static final String ACTIVITIESANALYSISID = "ACTIVITIES";
    private static final String PROJECTSANALYSISID = "PROJECTS";
    private static final String MONTHTIMEFRAME = "month";
    private static final String DAYTIMEFRAME = "day";

    private static final String DATEATTRIBUTE = "date";
    private static final String MONTHATTRIBUTE = "month";
    private static final String ALLACTIVITIESATTRIBUTE = "allActivities";
    private static final String BOOKINGSFORDAYATTRIBUTE = "bookingsForDay";
    private static final String HOURSANALYSISATTRIBUTE = "hourAnalysis";
    private static final String HOURSANALYSISHEADLINEATTRIBUTE = "hourAnalysisHeadline";
    private static final String ACTIVITIESANALYSISTODAYATTRIBUTE = "activityAnalysisToday";
    private static final String ACTIVITIESANALYSISTODAYHEADLINEATTRIBUTE = "activityAnalysisTodayHeadline";
    private static final String ACTIVITYANALYSISMONTHATTRIBUTE = "activityAnalysisMonth";
    private static final String ACTIVITYANALYSISMONTHHEADLINEATTRIBUTE = "activityAnalysisMonthHeadline";
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

    @RequestMapping(method = RequestMethod.GET)
    public String dataPageForToday(final Principal principal, final Model model) {
        return createPage(model, principal.getName(), LocalDate.now());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{dayString}")
    public String dataPageForDay(final Principal principal, @PathVariable final String dayString, final Model model) {
        return createPage(model, principal.getName(), LocalDate.parse(dayString));
    }

    private String createPage(final Model model, final String username, final LocalDate dateToShow) {
        model.addAttribute(DATEATTRIBUTE, dateToShow.format(DateTimeFormatter.ISO_LOCAL_DATE));
        model.addAttribute(MONTHATTRIBUTE, dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        model.addAttribute(ALLACTIVITIESATTRIBUTE,
                services
                        .activityStore()
                        .retrieveAll()
                        .stream()
                        .filter(act -> !act.isHidden() && act.getUser().equals(username))
                        .sorted((a1, a2) -> compareActivities(a1, a2))
                        .collect(toList()));
        model.addAttribute(BOOKINGSFORDAYATTRIBUTE,
                services
                        .bookingStore()
                        .retrieveAll()
                        .stream()
                        .filter(b -> b.getBookingday().equals(dateToShow) && b.getUser().equals(username))
                        .sorted(Comparator.comparing(Booking::getStarttime))
                        .map(b -> new BookingStruct(
                                b.getId(),
                                b.getStarttime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                b.hasEndtime() ? b.getEndtime().format(DateTimeFormatter.ofPattern("HH:mm")) : "",
                                b.getActivity(),
                                b.getComment()))
                        .collect(toList()));

        setAnalysisData(model, HOURSANALYSISHEADLINEATTRIBUTE, HOURSANALYSISATTRIBUTE, HOURANALYSISID, MONTHTIMEFRAME,
                dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM")), username);

        setAnalysisData(model, ACTIVITIESANALYSISTODAYHEADLINEATTRIBUTE, ACTIVITIESANALYSISTODAYATTRIBUTE,
                ACTIVITIESANALYSISID, DAYTIMEFRAME, dateToShow.format(DateTimeFormatter.ISO_LOCAL_DATE), username);

        setAnalysisData(model, ACTIVITYANALYSISMONTHHEADLINEATTRIBUTE, ACTIVITYANALYSISMONTHATTRIBUTE,
                ACTIVITIESANALYSISID, MONTHTIMEFRAME, dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM")),
                username);

        setAnalysisData(model, PROJECTANALYSISMONTHHEADLINEATTRIBUTE, PROJECTANALYSISMONTHATTRIBUTE, PROJECTSANALYSISID,
                MONTHTIMEFRAME, dateToShow.format(DateTimeFormatter.ofPattern("yyyy-MM")), username);

        return TEMPLATENAME;
    }

    private int compareActivities(Activity a1, Activity a2) {
        int actcompare = a1.getProjectId().compareToIgnoreCase(a2.getProjectId());
        if (actcompare == 0) {
            actcompare = a1.getProjectActivity().compareToIgnoreCase(a2.getProjectActivity());
        }
        return actcompare;
    }

    private void setAnalysisData(final Model model, final String headlineAttr, final String analysisAttr,
            final String analysisId, final String timeFrameType, final String timeFrame, final String username) {
        Collection<Collection<String>> analysisResult = services
                .analysisService()
                .analyze(analysisId, username, timeFrameType, timeFrame);
        Collection<String> headline = Utils.getFirstFromCollection(analysisResult);
        Collection<Collection<String>> bodydata = analysisResult.stream().skip(1).collect(toList());
        model.addAttribute(headlineAttr, headline);
        model.addAttribute(analysisAttr, bodydata);
    }
}
