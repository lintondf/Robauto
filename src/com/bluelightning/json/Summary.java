
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Summary implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("distance")
    @Expose
    private Double distance;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("trafficTime")
    @Expose
    private Double trafficTime;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("baseTime")
    @Expose
    private Double baseTime;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("text")
    @Expose
    private String text;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("travelTime")
    @Expose
    private Double travelTime;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("_type")
    @Expose
    private String type;
    private final static long serialVersionUID = 3343231962769873178L;

    /**
     * 
     * (Required)
     * 
     */
    public Double getDistance() {
        return distance;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getTrafficTime() {
        return trafficTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTrafficTime(Double trafficTime) {
        this.trafficTime = trafficTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getBaseTime() {
        return baseTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setBaseTime(Double baseTime) {
        this.baseTime = baseTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getText() {
        return text;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getTravelTime() {
        return travelTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTravelTime(Double travelTime) {
        this.travelTime = travelTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("distance", distance).append("trafficTime", trafficTime).append("baseTime", baseTime).append("text", text).append("travelTime", travelTime).append("type", type).toString();
    }

}
