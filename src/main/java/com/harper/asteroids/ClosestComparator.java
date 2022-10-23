package com.harper.asteroids;

import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.Distances;
import com.harper.asteroids.model.NearEarthObject;

import java.util.Comparator;
import java.util.Date;
import java.util.Optional;

public class ClosestComparator implements Comparator<NearEarthObject> {

    public int compare(NearEarthObject neo1, NearEarthObject neo2) {

        Optional<Date> neo1ClosestPass = neo1.getCloseApproachData().stream()
                .min(Comparator.comparing(CloseApproachData::getCloseApproachDateTime))
                .map(min -> min.getCloseApproachDateTime());
        Optional<Date> neo2ClosestPass = neo2.getCloseApproachData().stream()
                .min(Comparator.comparing(CloseApproachData::getCloseApproachDateTime))
                .map(min -> min.getCloseApproachDateTime());

        int i = !neo1ClosestPass.isPresent() ? -1 : !neo2ClosestPass.isPresent() ? 1 : neo1ClosestPass.get().compareTo(neo2ClosestPass.get());
        return i;
    }

}
