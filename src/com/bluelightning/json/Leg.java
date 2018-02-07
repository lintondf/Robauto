
package com.bluelightning.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.bluelightning.PostProcessingEnabler;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Leg implements Serializable, PostProcessingEnabler.PostProcessable
{

    @SerializedName("start")
    @Expose
    private Start start;
    @SerializedName("end")
    @Expose
    private End end;
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
    @SerializedName("travelTime")
    @Expose
    private Double travelTime;
    @SerializedName("maneuver")
    @Expose
    private Set<Maneuver> maneuver = new LinkedHashSet<Maneuver>();
    @SerializedName("link")
    @Expose
    private Set<Link> link = new LinkedHashSet<Link>();
    @SerializedName("boundingBox")
    @Expose
    private BoundingBox boundingBox;
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
    @SerializedName("summary")
    @Expose
    private Summary summary;
    private final static long serialVersionUID = 8974312006998983857L;
    
    protected HashMap<String, Link> linkMap = new HashMap<>(); // TODO move into Leg


    public Start getStart() {
        return start;
    }

    public void setStart(Start start) {
        this.start = start;
    }

    public End getEnd() {
        return end;
    }

    public void setEnd(End end) {
        this.end = end;
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

    public Set<Maneuver> getManeuver() {
        return maneuver;
    }

    public void setManeuver(Set<Maneuver> maneuver) {
        this.maneuver = maneuver;
    }

    public Set<Link> getLink() {
        return link;
    }

    public void setLink(Set<Link> link) {
        this.link = link;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
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

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("start", start).append("end", end).append("length", length).append("travelTime", travelTime).append("maneuver", maneuver).append("link", link).append("boundingBox", boundingBox).append("shape", shape).append("firstPoint", firstPoint).append("lastPoint", lastPoint).append("trafficTime", trafficTime).append("baseTime", baseTime).append("summary", summary).toString();
    }

	@Override
	public void postProcess() {
		for (Link link : getLink()) {
			linkMap.put( link.getManeuver(), link );
		}
		double totalTime = 0;
		for (Maneuver maneuver : getManeuver()) {
			maneuver.adjustSpeeds(this);
			totalTime += maneuver.getTravelTime();
		}
		this.setBaseTime(totalTime);
		this.setTrafficTime(totalTime);
		this.setTravelTime(totalTime);
	}

	public HashMap<String, Link> getLinkMap() {
		return linkMap;
	}

}
