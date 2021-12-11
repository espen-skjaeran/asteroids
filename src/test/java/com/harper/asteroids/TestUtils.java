package com.harper.asteroids;

import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.Distances;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TestUtils {

    public CloseApproachData createCloseApproachData(int addDays, Double distance) {

        ZonedDateTime zonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault());
        zonedDateTime = zonedDateTime.plusDays(addDays);

        Distances distances = Distances.createTestInstance(distance);
        return CloseApproachData.createTestInstance(zonedDateTime.toInstant().toEpochMilli(), distances);
    }
}
