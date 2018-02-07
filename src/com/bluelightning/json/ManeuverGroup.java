
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class ManeuverGroup implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("firstManeuver")
    @Expose
    private String firstManeuver;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("lastManeuver")
    @Expose
    private String lastManeuver;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("mode")
    @Expose
    private String mode;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("summaryDescription")
    @Expose
    private String summaryDescription;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("arrivalDescription")
    @Expose
    private String arrivalDescription;
    private final static long serialVersionUID = 5750175525428662703L;

    /**
     * 
     * (Required)
     * 
     */
    public String getFirstManeuver() {
        return firstManeuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setFirstManeuver(String firstManeuver) {
        this.firstManeuver = firstManeuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getLastManeuver() {
        return lastManeuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLastManeuver(String lastManeuver) {
        this.lastManeuver = lastManeuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getMode() {
        return mode;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getSummaryDescription() {
        return summaryDescription;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setSummaryDescription(String summaryDescription) {
        this.summaryDescription = summaryDescription;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getArrivalDescription() {
        return arrivalDescription;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setArrivalDescription(String arrivalDescription) {
        this.arrivalDescription = arrivalDescription;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("firstManeuver", firstManeuver).append("lastManeuver", lastManeuver).append("mode", mode).append("summaryDescription", summaryDescription).append("arrivalDescription", arrivalDescription).toString();
    }

}
