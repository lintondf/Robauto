
package com.bluelightning.json;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bluelightning.PostProcessingEnabler;
import com.bluelightning.json.Leg.CumulativeTravel;
import com.bluelightning.poi.POIResult;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Leg implements Serializable, PostProcessingEnabler.PostProcessable, Comparable<Leg>
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
    
    public static class CumulativeTravel implements Serializable {
		public double  heading;
    	public double  distance;
    	public double  travelTime;
    	public double  trafficTime;
		public CumulativeTravel plus(CumulativeTravel legProgress) {
			CumulativeTravel t = new CumulativeTravel();
			t.heading = legProgress.heading;
			t.distance = distance + legProgress.distance;
			t.travelTime = travelTime + legProgress.travelTime;
			t.trafficTime = trafficTime + legProgress.trafficTime;
			return t;
		}
    }
    
    protected Map<String, CumulativeTravel> progressMap = new HashMap<>();
    
    public CumulativeTravel getProgress( Maneuver maneuver ) {
    	CumulativeTravel p1 = progressMap.get( maneuver.getId() );
    	return p1;
    }
    
    public CumulativeTravel getProgress( POIResult r ) {
    	Maneuver maneuver = r.maneuver;
    	CumulativeTravel p1 = progressMap.get( maneuver.getId() );
    	CumulativeTravel p2 = progressMap.get( maneuver.nextId() );
    	if (p2 == null)
    		return p1;
    	double deltaDistance = p2.distance - p1.distance;
    	double deltaTravelTime = p2.travelTime - p1.travelTime;
    	double deltaTrafficTime = p2.trafficTime - p1.trafficTime;
    	double scale = maneuver.getShapeDistances().get(r.index) / deltaDistance;
    	CumulativeTravel o = new CumulativeTravel();
    	o.distance = p1.distance + scale*deltaDistance;
    	o.trafficTime = p1.trafficTime + scale*deltaTravelTime;
    	o.travelTime = p1.travelTime + scale*deltaTrafficTime;
    	return o;
    }

	@Override
	public void postProcess() {
		for (Link link : getLink()) {
			linkMap.put( link.getManeuver(), link );
		}
		double travelTime = 0;
		double trafficTime = 0;
		double distance = 0;
		for (Maneuver maneuver : getManeuver()) {
			CumulativeTravel note = new CumulativeTravel();
			note.distance = distance;
			note.travelTime = travelTime;
			note.trafficTime = trafficTime;
			progressMap.put( maneuver.getId(), note);
			maneuver.adjustSpeeds(this);
			distance += maneuver.getLength();
			trafficTime += maneuver.getTrafficTime();
			travelTime += maneuver.getTravelTime();
		}
		this.setBaseTime(travelTime);
		this.setTrafficTime(trafficTime);
		this.setTravelTime(travelTime);
	}

	public HashMap<String, Link> getLinkMap() {
		return linkMap;
	}

	@Override
	public int compareTo(Leg o) {
		// TODO Auto-generated method stub
		return (int) (this.firstPoint - o.firstPoint);
	}

}
