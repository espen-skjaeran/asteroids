package com.harper.asteroids;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.Feed;
import com.harper.asteroids.model.NearEarthObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestApproachDetector {

    private ObjectMapper mapper = new ObjectMapper();
    private NearEarthObject neo1, neo2;
    private Feed feed;

    @Before
    public void setUp() throws IOException {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        feed = mapper.readValue(getClass().getResource("/feed_example.json"), Feed.class);
        neo1 = mapper.readValue(getClass().getResource("/neo_example.json"), NearEarthObject.class);
        neo2 = mapper.readValue(getClass().getResource("/neo_example2.json"), NearEarthObject.class);

    }

    @Test
    public void testFiltering() {

        List<NearEarthObject> neos = List.of(neo1, neo2);
        ApproachDetector approachDetector = new ApproachDetector(feed.getAllObjectIds());
        List<NearEarthObject> filtered = approachDetector.getClosest(neos, 2);
        //Neo2 has the closest passing at 5261628 kms away.
        // TODO: Neo2's closest passing is in 2028.
        // In Jan 202, neo1 is closer (5390966 km, vs neo2's at 7644137 km)
        assertEquals(1, filtered.size());
        assertEquals(neo2, filtered.get(0));

    }
}
