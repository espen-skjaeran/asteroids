package com.harper.asteroids;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.NearEarthObject;
import com.harper.asteroids.service.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestApproachDetector {

    private final ObjectMapper mapper = new ObjectMapper();
    private TestUtils testUtils;
    private NearEarthObject neo1, neo2;
    private ApproachDetector approachDetector;

    @Before
    public void setUp() throws IOException {
        this.testUtils = new TestUtils();
        neo1 = mapper.readValue(getClass().getResource("/neo_example.json"), NearEarthObject.class);
        neo2 = mapper.readValue(getClass().getResource("/neo_example2.json"), NearEarthObject.class);
        approachDetector = new ApproachDetector(new DateUtils(), "apiKey");
    }

    @Test
    public void testFiltering() {

        neo1.getCloseApproachData().clear();
        neo1.getCloseApproachData().add(testUtils.createCloseApproachData(-10, 1.0));
        neo1.getCloseApproachData().add(testUtils.createCloseApproachData(0, 3.0));

        neo2.getCloseApproachData().clear();
        neo2.getCloseApproachData().add(testUtils.createCloseApproachData(0, 2.0));

        List<NearEarthObject> neos = List.of(neo1, neo2);

        List<NearEarthObject> filtered = approachDetector.getClosestInCurrentWeek(neos, 1);
        assertEquals(1, filtered.size());
        assertEquals(neo2, filtered.get(0));

        filtered = approachDetector.getClosestInCurrentWeek(neos, 2);
        assertEquals(2, filtered.size());
        assertEquals(neo2, filtered.get(0));
        assertEquals(neo1, filtered.get(1));
    }

    @Test
    public void testFiltering_removeNeosWithoutApproachInCurrentWeek() {

        neo1.getCloseApproachData().clear();
        neo1.getCloseApproachData().add(testUtils.createCloseApproachData(0, 3.0));

        neo2.getCloseApproachData().clear();
        neo2.getCloseApproachData().add(testUtils.createCloseApproachData(-10, 2.0));
        neo2.getCloseApproachData().add(testUtils.createCloseApproachData(10, 1.0));

        List<NearEarthObject> neos = List.of(neo1, neo2);

        List<NearEarthObject> filtered = approachDetector.getClosestInCurrentWeek(neos, 2);
        assertEquals(1, filtered.size());
        assertEquals(neo1, filtered.get(0));
    }
}
