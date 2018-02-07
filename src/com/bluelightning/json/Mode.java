
package com.bluelightning.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Mode implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("type")
    @Expose
    private String type;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("transportModes")
    @Expose
    private List<Object> transportModes = new ArrayList<Object>();
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("trafficMode")
    @Expose
    private String trafficMode;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("feature")
    @Expose
    private List<Object> feature = new ArrayList<Object>();
    private final static long serialVersionUID = -7826206838238260726L;

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

    /**
     * 
     * (Required)
     * 
     */
    public List<Object> getTransportModes() {
        return transportModes;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTransportModes(List<Object> transportModes) {
        this.transportModes = transportModes;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getTrafficMode() {
        return trafficMode;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTrafficMode(String trafficMode) {
        this.trafficMode = trafficMode;
    }

    /**
     * 
     * (Required)
     * 
     */
    public List<Object> getFeature() {
        return feature;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setFeature(List<Object> feature) {
        this.feature = feature;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("transportModes", transportModes).append("trafficMode", trafficMode).append("feature", feature).toString();
    }

}
