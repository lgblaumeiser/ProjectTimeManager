package de.lgblaumeiser.ptm.analysis.analyzer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class DateFormatterUtil {
    static String formatPercentageString(final Duration totalMinutes, final Duration totalMinutesId) {
        double percentage = (double) totalMinutesId.toMinutes() / (double) totalMinutes.toMinutes();
        return String.format("%2.1f", percentage * 100.0) + "%";
    }

    static String formatDay(final LocalDate day) {
        return day.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    static String formatTime(final LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    static String formatDuration(final Duration duration) {
        long minutes = duration.toMinutes();
        char pre = minutes < 0 ? '-' : ' ';
        minutes = Math.abs(minutes);
        return String.format("%c%02d:%02d", pre, minutes / 60, minutes % 60);
    }

}