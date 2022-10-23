package com.harper.asteroids;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.CloseApproachData;
import com.harper.asteroids.model.NearEarthObject;


import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Receives a set of neo ids and rates them after earth proximity.
 * Retrieves the approach data for them and sorts to the n closest.
 * https://api.nasa.gov/neo/rest/v1/neo/
 * Alerts if someone is possibly hazardous.
 */
public class ApproachDetector {
    private static final String NEO_URL = "https://api.nasa.gov/neo/rest/v1/neo/";
    private List<String> nearEarthObjectIds;
    private static Client client;
    private static ObjectMapper mapper = new ObjectMapper();

    public ApproachDetector(List<String> ids) {
        this.nearEarthObjectIds = ids;
        this.client = ClientBuilder.newClient();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static NearEarthObject getParallelResponses(String id) {
        System.out.println("Current thread.. " + Thread.currentThread());
        NearEarthObject neo = null;
        try {
            System.out.println("Check passing of object " + id);
            Response response = client
                    .target(NEO_URL + id)
                    .queryParam("api_key", App.API_KEY)
                    .request(MediaType.APPLICATION_JSON)
                    .get();
            neo = mapper.readValue(response.readEntity(String.class), NearEarthObject.class);

        } catch (IOException e) {
            System.err.println("Failed scanning for asteroids: " + e);
        }
        return neo;
    }

    /**
     * Get the n closest approaches in this period
     *
     * @param limit - n
     */
    public List<NearEarthObject> getClosestApproaches(int limit) {
        var start = System.currentTimeMillis();
        List<NearEarthObject> neos = nearEarthObjectIds.stream().parallel().map(ApproachDetector::getParallelResponses).collect(Collectors.toList());
        var end = System.currentTimeMillis();
        System.out.println("Difference time  " + TimeUnit.MILLISECONDS.toSeconds(start - end));
        System.out.println("Received " + neos.size() + " neos, now sorting");
        var nearEarthObjects = getClosest(neos, limit);
        return nearEarthObjects;
    }

    /**
     * Get the closest passing.
     *
     * @param neos  the NearEarthObjects
     * @param limit
     * @return
     */
    public static List<NearEarthObject> getClosest(List<NearEarthObject> neos, int limit) {
        //TODO: Should ignore the passes that are not today/this week.
        var nearEarthObjects = neos.stream()
                .filter(neo -> neo.getCloseApproachData() != null && !neo.getCloseApproachData().isEmpty())
                .sorted(new ClosestComparator())
                .limit(limit)
                .filter(nearEarthObject -> nearEarthObject.getCloseApproachData().stream()
                        .anyMatch(closeApproachData -> closeApproachData.getCloseApproachDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().isAfter(LocalDateTime.now())
                                && closeApproachData.getCloseApproachDateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().isBefore(LocalDateTime.now().plusDays(7))))
                .collect(Collectors.toList());
        System.out.println("size of ..." + nearEarthObjects.size());
        return nearEarthObjects;
    }
}
