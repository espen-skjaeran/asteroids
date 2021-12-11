package com.harper.asteroids.service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateUtils {

    public boolean isDateInCurrentWeek(long epochDate) {

        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime targetDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochDate), ZoneId.systemDefault());

        TemporalField woy = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear();
        int currentWeek = currentDateTime.get(woy);
        int targetWeek = targetDateTime.get(woy);

        return currentDateTime.getYear() == targetDateTime.getYear() &&
                currentWeek == targetWeek;
    }

    public LocalDate getStartDateOfCurrentWeek() {
        LocalDate today = LocalDate.now();

        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        return today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek));
    }

    public LocalDate getEndDateOfCurrentWeek() {
        LocalDate today = LocalDate.now();

        DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
        DayOfWeek lastDayOfWeek = firstDayOfWeek.plus(6);
        return today.with(TemporalAdjusters.nextOrSame(lastDayOfWeek));
    }
}
