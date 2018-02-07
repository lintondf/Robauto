
package com.bluelightning.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Link implements Serializable
{

    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("linkId")
    @Expose
    private String linkId;
    @SerializedName("shape")
    @Expose
    private List<Object> shape = new ArrayList<Object>();
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
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("length")
    @Expose
    private Double length;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("remainDistance")
    @Expose
    private Double remainDistance;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("remainTime")
    @Expose
    private Double remainTime;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("nextLink")
    @Expose
    private String nextLink;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("maneuver")
    @Expose
    private String maneuver;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("speedLimit")
    @Expose
    private Double speedLimit;
    @SerializedName("dynamicSpeedInfo")
    @Expose
    private DynamicSpeedInfo dynamicSpeedInfo;
    @SerializedName("flags")
    @Expose
    private List<Object> flags = new ArrayList<Object>();
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("functionalClass")
    @Expose
    private Double functionalClass;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("roadNumber")
    @Expose
    private String roadNumber;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("truckRestrictions")
    @Expose
    private TruckRestrictions truckRestrictions;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("roadName")
    @Expose
    private String roadName;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("consumption")
    @Expose
    private Double consumption;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("_type")
    @Expose
    private String type;
    private final static long serialVersionUID = -1312519099028172988L;

    /**
     * 
     * (Required)
     * 
     */
    public String getLinkId() {
        return linkId;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }

    public List<Object> getShape() {
        return shape;
    }

    public void setShape(List<Object> shape) {
        this.shape = shape;
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

    /**
     * 
     * (Required)
     * 
     */
    public Double getLength() {
        return length;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setLength(Double length) {
        this.length = length;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getRemainDistance() {
        return remainDistance;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setRemainDistance(Double remainDistance) {
        this.remainDistance = remainDistance;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getRemainTime() {
        return remainTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setRemainTime(Double remainTime) {
        this.remainTime = remainTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getManeuver() {
        return maneuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setManeuver(String maneuver) {
        this.maneuver = maneuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getSpeedLimit() {
        return speedLimit;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setSpeedLimit(Double speedLimit) {
        this.speedLimit = speedLimit;
    }

    public DynamicSpeedInfo getDynamicSpeedInfo() {
        return dynamicSpeedInfo;
    }

    public void setDynamicSpeedInfo(DynamicSpeedInfo dynamicSpeedInfo) {
        this.dynamicSpeedInfo = dynamicSpeedInfo;
    }

    public List<Object> getFlags() {
        return flags;
    }

    public void setFlags(List<Object> flags) {
        this.flags = flags;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getFunctionalClass() {
        return functionalClass;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setFunctionalClass(Double functionalClass) {
        this.functionalClass = functionalClass;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getRoadNumber() {
        return roadNumber;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setRoadNumber(String roadNumber) {
        this.roadNumber = roadNumber;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getTimezone() {
        return timezone;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public TruckRestrictions getTruckRestrictions() {
        return truckRestrictions;
    }

    public void setTruckRestrictions(TruckRestrictions truckRestrictions) {
        this.truckRestrictions = truckRestrictions;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getRoadName() {
        return roadName;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getConsumption() {
        return consumption;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setConsumption(Double consumption) {
        this.consumption = consumption;
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
        return new ToStringBuilder(this).append("linkId", linkId).append("shape", shape).append("firstPoint", firstPoint).append("lastPoint", lastPoint).append("length", length).append("remainDistance", remainDistance).append("remainTime", remainTime).append("nextLink", nextLink).append("maneuver", maneuver).append("speedLimit", speedLimit).append("dynamicSpeedInfo", dynamicSpeedInfo).append("flags", flags).append("functionalClass", functionalClass).append("roadNumber", roadNumber).append("timezone", timezone).append("truckRestrictions", truckRestrictions).append("roadName", roadName).append("consumption", consumption).append("type", type).toString();
    }

}
