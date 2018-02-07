
package com.bluelightning.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Maneuver implements Serializable
{

    @SerializedName("position")
    @Expose
    private Position position;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("instruction")
    @Expose
    private String instruction;
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
    @SerializedName("length")
    @Expose
    private Double length;
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
    @SerializedName("note")
    @Expose
    private List<Object> note = new ArrayList<Object>();
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("nextManeuver")
    @Expose
    private String nextManeuver;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("toLink")
    @Expose
    private String toLink;
    @SerializedName("boundingBox")
    @Expose
    private BoundingBox boundingBox;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("shapeQuality")
    @Expose
    private String shapeQuality;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("direction")
    @Expose
    private String direction;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("action")
    @Expose
    private String action;
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
    @SerializedName("signPost")
    @Expose
    private String signPost;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("nextRoadName")
    @Expose
    private String nextRoadName;
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
    @SerializedName("nextRoadNumber")
    @Expose
    private String nextRoadNumber;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("freewayExit")
    @Expose
    private String freewayExit;
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
    @SerializedName("roadShield")
    @Expose
    private RoadShield roadShield;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("startAngle")
    @Expose
    private Double startAngle;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("id")
    @Expose
    private String id;
    /**
     * 
     * (Required)
     * 
     */
    @SerializedName("_type")
    @Expose
    private String type;
    private final static long serialVersionUID = -3030584591806271536L;

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
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

    public List<Object> getNote() {
        return note;
    }

    public void setNote(List<Object> note) {
        this.note = note;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getNextManeuver() {
        return nextManeuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setNextManeuver(String nextManeuver) {
        this.nextManeuver = nextManeuver;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getToLink() {
        return toLink;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setToLink(String toLink) {
        this.toLink = toLink;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getShapeQuality() {
        return shapeQuality;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setShapeQuality(String shapeQuality) {
        this.shapeQuality = shapeQuality;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getDirection() {
        return direction;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setDirection(String direction) {
        this.direction = direction;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getAction() {
        return action;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setAction(String action) {
        this.action = action;
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
    public String getSignPost() {
        return signPost;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setSignPost(String signPost) {
        this.signPost = signPost;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getNextRoadName() {
        return nextRoadName;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setNextRoadName(String nextRoadName) {
        this.nextRoadName = nextRoadName;
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
    public String getNextRoadNumber() {
        return nextRoadNumber;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setNextRoadNumber(String nextRoadNumber) {
        this.nextRoadNumber = nextRoadNumber;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getFreewayExit() {
        return freewayExit;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setFreewayExit(String freewayExit) {
        this.freewayExit = freewayExit;
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

    public RoadShield getRoadShield() {
        return roadShield;
    }

    public void setRoadShield(RoadShield roadShield) {
        this.roadShield = roadShield;
    }

    /**
     * 
     * (Required)
     * 
     */
    public Double getStartAngle() {
        return startAngle;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setStartAngle(Double startAngle) {
        this.startAngle = startAngle;
    }

    /**
     * 
     * (Required)
     * 
     */
    public String getId() {
        return id;
    }

    /**
     * 
     * (Required)
     * 
     */
    public void setId(String id) {
        this.id = id;
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
        return new ToStringBuilder(this).append("position", position).append("instruction", instruction).append("travelTime", travelTime).append("length", length).append("shape", shape).append("firstPoint", firstPoint).append("lastPoint", lastPoint).append("note", note).append("nextManeuver", nextManeuver).append("toLink", toLink).append("boundingBox", boundingBox).append("shapeQuality", shapeQuality).append("direction", direction).append("action", action).append("roadName", roadName).append("signPost", signPost).append("nextRoadName", nextRoadName).append("roadNumber", roadNumber).append("nextRoadNumber", nextRoadNumber).append("freewayExit", freewayExit).append("trafficTime", trafficTime).append("baseTime", baseTime).append("roadShield", roadShield).append("startAngle", startAngle).append("id", id).append("type", type).toString();
    }
    
    public void adjustSpeeds(Leg leg) {
    	if (getTrafficTime().doubleValue() != getTravelTime().doubleValue())
    		return;
		Link link = leg.getLinkMap().get(getId());
		double speed = (getLength() / getTrafficTime());
		if (link != null) {
			if (link.getSpeedLimit() != null && link.getSpeedLimit() > 0) {
				double speedLimit = link.getSpeedLimit();
				this.baseTime = this.length / speedLimit;
				this.trafficTime = this.travelTime = this.baseTime;
			}
		}    	
    }

}
