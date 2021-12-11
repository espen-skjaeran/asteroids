package com.harper.asteroids.service;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public boolean isDateInCurrentWeek(long epochDate) {

        Calendar calendar = Calendar.getInstance();
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.setTime(new Date(epochDate));
        int targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR);
        int targetYear = targetCalendar.get(Calendar.YEAR);

        return calendar.get(Calendar.WEEK_OF_YEAR) == targetWeek &&
                calendar.get(Calendar.YEAR) == targetYear;
    }
}
