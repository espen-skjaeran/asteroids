package com.harper.asteroids;

import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.Distances;

import java.util.Calendar;

public class TestUtils {

    public CloseApproachData createCloseApproachData(int addDays, Double distance) {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, addDays);

        Distances distances = Distances.createTestInstance(distance);
        return CloseApproachData.createTestInstance(cal.getTime().getTime(), distances);
    }
}
