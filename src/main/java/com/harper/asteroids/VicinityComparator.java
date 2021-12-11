package com.harper.asteroids;

import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.Distances;
import com.harper.asteroids.model.NearEarthObject;
import com.harper.asteroids.service.DateUtils;

import java.util.Comparator;
import java.util.Optional;

public class VicinityComparator implements Comparator<NearEarthObject> {

    private final DateUtils dateUtils;

    public VicinityComparator(DateUtils dateUtils) {
        this.dateUtils = dateUtils;
    }

    public int compare(NearEarthObject neo1, NearEarthObject neo2) {

        Optional<Distances> neo1ClosestPass = neo1.getCloseApproachData().stream()
                .filter(closeApproachData -> dateUtils.isDateInCurrentWeek(closeApproachData.getCloseApproachEpochDate()))
                .min(Comparator.comparing(CloseApproachData::getMissDistance))
                .map(min -> min.getMissDistance());
        Optional<Distances> neo2ClosestPass = neo2.getCloseApproachData().stream()
                .filter(closeApproachData -> dateUtils.isDateInCurrentWeek(closeApproachData.getCloseApproachEpochDate()))
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
