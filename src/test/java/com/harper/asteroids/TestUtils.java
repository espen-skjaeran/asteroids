package com.harper.asteroids;

import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.Distances;

import java.util.Calendar;
import java.util.TimeZone;

public class TestUtils {

    public CloseApproachData createCloseApproachData(int addDays, Double distance) {

        TimeZone osloTimezone = TimeZone.getTimeZone("Europe/Oslo");

        Calendar cal = Calendar.getInstance(osloTimezone);
        cal.add(Calendar.DATE, addDays);

        Distances distances = Distances.createTestInstance(distance);
        return CloseApproachData.createTestInstance(cal.getTime().getTime(), distances);
    }
}
