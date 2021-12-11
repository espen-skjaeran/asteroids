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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Receives a set of neo ids and rates them after earth proximity.
 * Retrieves the approach data for them and sorts to the n closest in current week.
 * https://api.nasa.gov/neo/rest/v1/neo/
 * Alerts if someone is possibly hazardous.
 */
public class ApproachDetector {
    private static final String NEO_URL = "https://api.nasa.gov/neo/rest/v1/neo/";
    private final Client client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final DateUtils dateUtils;
    private final String apiKey;

    public ApproachDetector(DateUtils dateUtils, String apiKey) {
        this.client = ClientBuilder.newClient();
        this.dateUtils = dateUtils;
        this.apiKey = apiKey;
    }

    /**
     * Get the n closest approaches in this period
     * @param limit - n
     */
    public List<NearEarthObject> getClosestApproachesInCurrentWeek(int limit, List<String> nearEarthObjectIds) {
        final Queue<NearEarthObject> neos = new ConcurrentLinkedQueue<>();
        nearEarthObjectIds.parallelStream().forEach(neoId -> {
            try {
                NearEarthObject neo = getNeoDetail(neoId);
                neos.add(neo);
            } catch (IOException e) {
                System.err.println("Failed scanning for asteroids: " + e);
            }
        });
        System.out.println("Received " + neos.size() + " neos, now sorting");

        return getFilteredAndSortedApproaches(new ArrayList<>(neos), limit);
    }

    private NearEarthObject getNeoDetail(String neoId) throws IOException {
        System.out.println("Check passing of object " + neoId);
        Response response = client
                .target(NEO_URL + neoId)
                .queryParam("api_key", apiKey)
                .request(MediaType.APPLICATION_JSON)
                .get();

        return mapper.readValue(response.readEntity(String.class), NearEarthObject.class);
    }

    /**
     * Get the closest passing.
     * @param neos the NearEarthObjects
     * @param limit specifies the size of the returned list
     * @return a list of the closest passing in current week
     */
    public List<NearEarthObject> getFilteredAndSortedApproaches(List<NearEarthObject> neos, int limit) {
        return neos.stream()
                .filter(this::hasAnyApproachingInCurrentWeek)
                .sorted(new VicinityComparator(dateUtils))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private boolean hasAnyApproachingInCurrentWeek(NearEarthObject neo) {
        if(neo.getCloseApproachData() == null) {
            return false;
        }

        return neo.getCloseApproachData().stream()
                .anyMatch(closeApproachData ->
                        dateUtils.isDateInCurrentWeek(closeApproachData.getCloseApproachEpochDate()));
    }
}
