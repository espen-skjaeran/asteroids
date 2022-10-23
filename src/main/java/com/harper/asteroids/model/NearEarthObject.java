package com.harper.asteroids.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Definition for Neo - Near Earth Object
 *
 * TODO: why the h*** must I add this annotation to ignore unknown properties when I set it on ObjectMapper?
 */
//@JsonIgnoreProperties(ignoreUnknown = true)
public class NearEarthObject {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("nasa_jpl_url")
    private String nplUrl;

    @JsonProperty("absolute_magnitude_h")
    private double absoluteMagnitude;

    @JsonProperty("is_potentially_hazardous_asteroid")
    private boolean potentiallyHazardous;

    @JsonProperty("close_approach_data")
    private List<CloseApproachData> closeApproachData;

    @JsonProperty("is_sentry_object")
    private boolean isSentryObject;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNplUrl() {
        return nplUrl;
    }

    public double getAbsoluteMagnitude() {
        return absoluteMagnitude;
    }

    public boolean isPotentiallyHazardous() {
        return potentiallyHazardous;
    }

    public List<CloseApproachData> getCloseApproachData() {
        return closeApproachData;
    }
    public boolean isSentryObject() {
        return isSentryObject;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNplUrl(String nplUrl) {
        this.nplUrl = nplUrl;
    }

    public void setAbsoluteMagnitude(double absoluteMagnitude) {
        this.absoluteMagnitude = absoluteMagnitude;
    }

    public void setPotentiallyHazardous(boolean potentiallyHazardous) {
        this.potentiallyHazardous = potentiallyHazardous;
    }

    public void setCloseApproachData(List<CloseApproachData> closeApproachData) {
        this.closeApproachData = closeApproachData;
    }

    public void setSentryObject(boolean sentryObject) {
        isSentryObject = sentryObject;
    }
}
