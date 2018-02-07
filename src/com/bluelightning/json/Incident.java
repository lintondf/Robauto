
package com.bluelightning.json;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Incident implements Serializable
{

    @SerializedName("validityPeriod")
    @Expose
    private ValidityPeriod validityPeriod;
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
    @SerializedName("type")
    @Expose
    private String type;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("criticality")
    @Expose
    private Double criticality;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("firstPoint")
    @Expose
    private Double firstPoint;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("lastPoint")
    @Expose
    private Double lastPoint;
    private final static long serialVersionUID = -8847058855295923016L;

    public ValidityPeriod getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(ValidityPeriod validityPeriod) {
        this.validityPeriod = validityPeriod;
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
    public Double getCriticality() {
        return criticality;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setCriticality(Double criticality) {
        this.criticality = criticality;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getFirstPoint() {
        return firstPoint;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setFirstPoint(Double firstPoint) {
        this.firstPoint = firstPoint;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getLastPoint() {
        return lastPoint;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLastPoint(Double lastPoint) {
        this.lastPoint = lastPoint;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("validityPeriod", validityPeriod).append("text", text).append("type", type).append("criticality", criticality).append("firstPoint", firstPoint).append("lastPoint", lastPoint).toString();
    }

}
