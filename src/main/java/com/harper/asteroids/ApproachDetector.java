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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import java.util.concurrent.CompletableFuture;

/**
 * Receives a set of neo ids and rates them after earth proximity.
 * Retrieves the approach data for them and sorts to the n closest.
 * https://api.nasa.gov/neo/rest/v1/neo/
 * Alerts if someone is possibly hazardous.
 */
public class ApproachDetector {
    private static final String NEO_URL = "https://api.nasa.gov/neo/rest/v1/neo/";
    private final List<String> nearEarthObjectIds;
    private final Client client;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final long WEEK_IN_SECS = 604800;
    private static final LocalDate date = LocalDate.now();
    private static final LocalTime time = LocalTime.now();
    private static final long today = date.toEpochSecond(time, ZoneOffset.of("Z"));

    public ApproachDetector(List<String> ids) {
        this.nearEarthObjectIds = ids;
        this.client = ClientBuilder.newClient();
    }

    /**
     * Get the n closest approaches in this period
     * @param limit - n
     */
    public List<NearEarthObject> getClosestApproaches(int limit) {
        List<CompletableFuture<NearEarthObject>> neosFutures = new ArrayList<>(limit);
        ExecutorService myExecService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors());
//        List<NearEarthObject> neos = new ArrayList<>(limit);

        for(String id: nearEarthObjectIds) {
            neosFutures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    System.out.println("Check passing of object " + id);
                    Response response = client
                            .target(NEO_URL + id)
                            .queryParam("api_key", App.API_KEY)
                            .request(MediaType.APPLICATION_JSON)
                            .get();

                    return mapper.readValue(response.readEntity(String.class), NearEarthObject.class);
                } catch (IOException e) {
//                    System.err.println("Failed scanning for asteroids: " + e);
                    throw new RuntimeException("IO exception in " + id + " Failed scanning for asteroids: ", e);
                }
            }, myExecService));
        }

        CompletableFuture.allOf(neosFutures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> null)
                .join();

        List<NearEarthObject> neos = neosFutures.stream()
                .filter(neo -> neo.isDone())
                .map(neo-> {
                    try {
                        myExecService.shutdown();
//                        neo.complete(neo.get());
                        return neo.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
//                        neo.cancel(true);
                        return null;
                    }
                })
                .collect(Collectors.toList());


        return getClosest(neos, limit);
    }

    /**
     * Get the closest passing.
     * @param neos the NearEarthObjects
     * @param limit the number of the neos shown
     * @return list of neos
     */
    public static List<NearEarthObject> getClosest(List<NearEarthObject> neos, int limit) {
        //TODO: Should ignore the passes that are not today/this week.
        return neos.parallelStream()
                .filter(neo -> neo.getCloseApproachData() != null && ! neo.getCloseApproachData().isEmpty())
                .filter(removeRedundant())
                .sorted(new VicinityComparator())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Removes the close approach data from a near earth object if it did not happen this week.
     * Fixes the problem "permanently :)"
     * @return Predicate<NearEarthObject> to be used for filtering
     */
    private static Predicate<NearEarthObject> removeRedundant(){
        return neo -> neo.getCloseApproachData().removeIf(thisNeo -> ((thisNeo.getCloseApproachDateTime().getTime() / 1000) < today) ||
                ((thisNeo.getCloseApproachDateTime().getTime() / 1000) > (today + WEEK_IN_SECS)));
    }
}
