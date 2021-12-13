package com.harper.asteroids;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.NearEarthObject;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * Receives a set of neo ids and rates them after earth proximity.
 * Retrieves the approach data for them and sorts to the n closest.
 * https://api.nasa.gov/neo/rest/v1/neo/
 * Alerts if someone is possibly hazardous.
 */
public class ApproachDetector {
    private static final String NEO_URL = "https://api.nasa.gov/neo/rest/v1/neo/";
    private List<String> nearEarthObjectIds;
    private Client client;
    private ObjectMapper mapper = new ObjectMapper();

    private static final long WEEK_IN_SECS = 604800;
    private static LocalDate date = LocalDate.now();
    private static LocalTime time = LocalTime.now();
    private static long today = date.toEpochSecond(time, ZoneOffset.of("Z"));

    public ApproachDetector(List<String> ids) {
        this.nearEarthObjectIds = ids;
        this.client = ClientBuilder.newClient();
    }

    /**
     * Get the n closest approaches in this period
     * @param limit - n
     */
    public List<NearEarthObject> getClosestApproaches(int limit) {
        List<NearEarthObject> neos = new ArrayList<>(limit);
        for(String id: nearEarthObjectIds) {
            try {
                System.out.println("Check passing of object " + id);
                Response response = client
                    .target(NEO_URL + id)
                    .queryParam("api_key", App.API_KEY)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

                NearEarthObject neo = mapper.readValue(response.readEntity(String.class), NearEarthObject.class);
                neos.add(neo);
            } catch (IOException e) {
                System.err.println("Failed scanning for asteroids: " + e);
            }
        }
        System.out.println("Received " + neos.size() + " neos, now sorting");

        return getClosest(neos, limit);
    }

    /**
     * Get the closest passing.
     * @param neos the NearEarthObjects
     * @param limit
     * @return
     */
    public static List<NearEarthObject> getClosest(List<NearEarthObject> neos, int limit) {
        //TODO: Should ignore the passes that are not today/this week.
        return neos.stream()
                .filter(neo -> neo.getCloseApproachData() != null && ! neo.getCloseApproachData().isEmpty())
                .filter(removeRedundant())
//                .filter(ignoreRedundant())
                .sorted(new VicinityComparator())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Removes the close approach data from a near earth object if it did not happen this week.
     * Fixes the problem "permanently :)"
     * @return
     */
    private static Predicate<NearEarthObject> removeRedundant(){
        return neo -> neo.getCloseApproachData().removeIf(thisNeo -> ((thisNeo.getCloseApproachDateTime().getTime() / 1000) < today) ||
                ((thisNeo.getCloseApproachDateTime().getTime() / 1000) > (today + WEEK_IN_SECS)));
    }

    /**
     * Selects only the near earth objects which have at least one close approach data that happen this week.
     * Fixes the problem "permanently :)"
     * @return
     */
    private static Predicate<NearEarthObject> ignoreRedundant(){
        return neo -> neo.getCloseApproachData().stream()
                .anyMatch(thisNeo -> ((thisNeo.getCloseApproachEpochDate() / 1000) >= today) &&
                        ((thisNeo.getCloseApproachEpochDate() / 1000) <= (today + WEEK_IN_SECS)));

    }
}
