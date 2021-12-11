package com.harper.asteroids.service;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public boolean isDateInCurrentWeek(long epochDate) {

        TimeZone osloTimezone = TimeZone.getTimeZone("Europe/Oslo");

        Calendar calendar = Calendar.getInstance(osloTimezone);
        Calendar targetCalendar = Calendar.getInstance(osloTimezone);
        targetCalendar.setTime(new Date(epochDate));
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return calendar.get(Calendar.WEEK_OF_YEAR) == targetWeek &&
                calendar.get(Calendar.YEAR) == targetYear;
    }
}
