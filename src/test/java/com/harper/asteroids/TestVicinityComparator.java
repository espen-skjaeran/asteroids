package com.harper.asteroids;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.NearEarthObject;
import com.harper.asteroids.service.DateUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


public class TestVicinityComparator {

    private final ObjectMapper mapper = new ObjectMapper();
    private TestUtils testUtils;
    private NearEarthObject neo1, neo2;

    @Before
    public void setUp() throws IOException {
        this.testUtils = new TestUtils();
        neo1 = mapper.readValue(getClass().getResource("/neo_example.json"), NearEarthObject.class);
        neo2 = mapper.readValue(getClass().getResource("/neo_example2.json"), NearEarthObject.class);
    }

    @Test
    public void testOrder() {
        VicinityComparator comparator = new VicinityComparator(new DateUtils());

        neo1.getCloseApproachData().clear();
        neo1.getCloseApproachData().add(testUtils.createCloseApproachData(-10, 1.0));
        neo1.getCloseApproachData().add(testUtils.createCloseApproachData(0, 3.0));

        neo2.getCloseApproachData().clear();
        neo2.getCloseApproachData().add(testUtils.createCloseApproachData(0, 2.0));

        assertThat(comparator.compare(neo1, neo2), greaterThan(0));
        assertThat(comparator.compare(neo2, neo1), lessThan(0));
        assertEquals(comparator.compare(neo1, neo1), 0);
    }
}
