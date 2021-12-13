package com.harper.asteroids;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harper.asteroids.model.NearEarthObject;
import com.harper.asteroids.utils.DateUtil;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    private ExecutorService executorService;

    public ApproachDetector(List<String> ids) {
        this.nearEarthObjectIds = ids;
        this.client = ClientBuilder.newClient();
        this.executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Get the n closest approaches in this period
     * @param limit - n
     */
    public List<NearEarthObject> getClosestApproaches(int limit) {

        List<CompletableFuture<NearEarthObject>> neos = new ArrayList<>(limit);
        for(String id: nearEarthObjectIds) {
            neos.add(
                    CompletableFuture.supplyAsync(() -> {
                    try {
                        Response response = client.target(NEO_URL + id)
                                .queryParam("api_key", App.API_KEY)
                                .request(MediaType.APPLICATION_JSON)
                                .get();
                        return mapper.readValue(response.readEntity(String.class), NearEarthObject.class);
                    }
                    catch (IOException e) {
                        System.err.println("Failed scanning for asteroids: " + e);
                        throw new IllegalStateException(String.format("Couldn't fetch data from API for objectId %s", id));
                    }
                }, executorService)
            );
            System.out.println("Received " + neos.size() + " neos, now sorting");
        }
        CompletableFuture.allOf(neos.toArray(new CompletableFuture[nearEarthObjectIds.size()])).join();
        List<NearEarthObject> nearEarthObjects = neos.stream().filter(CompletableFuture::isDone).map(neo -> {
            try {
                return neo.get();
            }
            catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                System.err.println("Failed to get data for object " + e);
                return null;
                }
            }
        ).filter(Objects::nonNull).toList();
        return getClosest(nearEarthObjects, limit);
    }

    /**
     * Get the closest passing.
     * @param neos the NearEarthObjects
     * @param limit
     * @return
     */
    public static List<NearEarthObject> getClosest(List<NearEarthObject> neos, int limit) {
        //;TODO: Should ignore the passes that are not today/this week.
        Date today = DateUtil.updateDate(new Date(), 0, 0, 0, 0);
        int remainingDaysInWeek = Calendar.SATURDAY - DateUtil.getDayOfWeek(today) + 1;
        Date lastDateOfTheWeek = DateUtil.getDateAfterInterval(today, remainingDaysInWeek, 0,0, 0, 0);
        return neos.stream()
                .filter(neo ->
                            neo.getCloseApproachData() != null &&
                            ! neo.getCloseApproachData().isEmpty() &&
                            neo.getCloseApproachData().stream().anyMatch(
                                    closeApproachData ->
                                            closeApproachData.getCloseApproachDateTime().after(today) &&
                                            closeApproachData.getCloseApproachDateTime().before(lastDateOfTheWeek)
                            )
                        )
                .sorted(new VicinityComparator())
                .limit(limit)
                .collect(Collectors.toList());
    }

}
