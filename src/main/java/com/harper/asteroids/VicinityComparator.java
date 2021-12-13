package com.harper.asteroids;

import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.Distances;
import com.harper.asteroids.model.NearEarthObject;

import java.util.Comparator;
import java.util.Optional;
import java.util.function.Predicate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class VicinityComparator implements Comparator<NearEarthObject> {
    private static final long WEEK_IN_SECS = 604800;
    private static LocalDate date = LocalDate.now();
    private static LocalTime time = LocalTime.now();
    private static long today = date.toEpochSecond(time, ZoneOffset.of("Z"));

    public int compare(NearEarthObject neo1, NearEarthObject neo2) {

        Optional<Distances> neo1ClosestPass = neo1.getCloseApproachData().parallelStream()
//                .filter(neo -> ((neo.getCloseApproachEpochDate() / 1000) >= today) &&
//                        ((neo.getCloseApproachEpochDate() / 1000) <= (today + WEEK_IN_SECS)))
                .min(Comparator.comparing(CloseApproachData::getMissDistance))
                .map(min -> min.getMissDistance());
        Optional<Distances> neo2ClosestPass = neo2.getCloseApproachData().parallelStream()
//                .filter(neo -> ((neo.getCloseApproachEpochDate() / 1000) >= today) &&
//                        ((neo.getCloseApproachEpochDate() / 1000) <= (today + WEEK_IN_SECS)))
                .min(Comparator.comparing(CloseApproachData::getMissDistance))
                .map(min -> min.getMissDistance());

        if(neo1ClosestPass.isPresent()) {
            if(neo2ClosestPass.isPresent()) {
                return neo1ClosestPass.get().compareTo(neo2ClosestPass.get());
            }
            else return 1;
        }
        else return -1;
    }
}
