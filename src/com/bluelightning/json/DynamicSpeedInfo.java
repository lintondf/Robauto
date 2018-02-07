
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class DynamicSpeedInfo implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("trafficSpeed")
    @Expose
    private Double trafficSpeed;
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
    @SerializedName("baseSpeed")
    @Expose
    private Double baseSpeed;
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
    @SerializedName("jamFactor")
    @Expose
    private Double jamFactor;
    private final static long serialVersionUID = -2162774507505582431L;

    /**
     * 
     * (Required)
     * 
     */
    public Double getTrafficSpeed() {
        return trafficSpeed;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTrafficSpeed(Double trafficSpeed) {
        this.trafficSpeed = trafficSpeed;
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
    public Double getBaseSpeed() {
        return baseSpeed;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setBaseSpeed(Double baseSpeed) {
        this.baseSpeed = baseSpeed;
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
    public Double getJamFactor() {
        return jamFactor;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setJamFactor(Double jamFactor) {
        this.jamFactor = jamFactor;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("trafficSpeed", trafficSpeed).append("trafficTime", trafficTime).append("baseSpeed", baseSpeed).append("baseTime", baseTime).append("jamFactor", jamFactor).toString();
    }

}
