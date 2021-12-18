package com.example.common.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class TimeUtility {

    private TimeUtility() {
    }

    public static String readableTimeFromInstant(Instant instant) {
        if (instant == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public static String readableDateFromInstant(Instant instant) {
        if (instant == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public static String readableDateTimeFromInstant(Instant instant) {
        if (instant == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                .withLocale(Locale.US)
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }

    public static Instant getDayStart(Instant instant) {
        LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneOffset.UTC).toLocalDate();
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public static Instant getDayEnd(Instant instant) {
        return instant
                .plus(1, ChronoUnit.DAYS)
                .truncatedTo(ChronoUnit.DAYS)
                .minusMillis(1);
    }

}
