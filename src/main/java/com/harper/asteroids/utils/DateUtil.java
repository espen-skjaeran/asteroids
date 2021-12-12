package com.harper.asteroids.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {

    public static Calendar getCalendar(Date date){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        return calendar;
    }

    public static int getDayOfWeek(Date date){
        return getCalendar(date).get(Calendar.DAY_OF_WEEK);
    }

    public static Date getDateAfterInterval(Date fromDate, int days, int hours, int minutes, int seconds, int millis){
        Calendar calendar = getCalendar(fromDate);
        calendar.add(Calendar.DATE, days);
        calendar.add(Calendar.HOUR, hours);
        calendar.add(Calendar.MINUTE, minutes);
        calendar.add(Calendar.SECOND, seconds);
        calendar.add(Calendar.MILLISECOND, millis);
        return calendar.getTime();
    }

    public static Date updateDate(Date fromDate, int hours, int minutes, int seconds, int milliseconds){
        Calendar calendar = getCalendar(fromDate);
        calendar.set(Calendar.HOUR, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, seconds);
        calendar.set(Calendar.MILLISECOND, milliseconds);
        return calendar.getTime();
    }
}
