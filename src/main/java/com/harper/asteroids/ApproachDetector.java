package com.harper.asteroids;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.NearEarthObject;
import com.harper.asteroids.service.DateUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Receives a set of neo ids and rates them after earth proximity.
 * Retrieves the approach data for them and sorts to the n closest.
 * https://api.nasa.gov/neo/rest/v1/neo/
 * Alerts if someone is possibly hazardous.
 */
public class ApproachDetector {
    private static final String NEO_URL = "https://api.nasa.gov/neo/rest/v1/neo/";
    private final Client client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final DateUtils dateUtils;

    public ApproachDetector(DateUtils dateUtils) {
        this.client = ClientBuilder.newClient();
        this.dateUtils = dateUtils;
    }

    /**
     * Get the n closest approaches in this period
     * @param limit - n
     */
    public List<NearEarthObject> getClosestApproaches(int limit, List<String> nearEarthObjectIds) {
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
     * @param limit specifies the size of the returned list
     * @return a list of the closest passing
     */
    public List<NearEarthObject> getClosest(List<NearEarthObject> neos, int limit) {
        return neos.stream()
                .filter(this::doesNeoHasAnyApproachingInCurrentWeek)
                .sorted(new VicinityComparator(dateUtils))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public boolean doesNeoHasAnyApproachingInCurrentWeek(NearEarthObject neo) {
        if(neo.getCloseApproachData() == null) {
            return false;
        }

        return neo.getCloseApproachData().stream()
                .anyMatch(closeApproachData ->
                        dateUtils.isDateInCurrentWeek(closeApproachData.getCloseApproachEpochDate()));
    }

}
