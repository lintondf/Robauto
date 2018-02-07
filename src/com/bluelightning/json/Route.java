
package com.bluelightning.json;

import com.bluelightning.LatLon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.bluelightning.PostProcessingEnabler;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jxmapviewer.viewer.GeoPosition;

public class Route implements Serializable, PostProcessingEnabler.PostProcessable
{

    @SerializedName("waypoint")
    @Expose
    private Set<Waypoint> waypoint = new LinkedHashSet<Waypoint>();
    @SerializedName("mode")
    @Expose
    private Mode mode;
    @SerializedName("shape")
    @Expose
    private List<Object> shape = new ArrayList<Object>();
    @SerializedName("boundingBox")
    @Expose
    private BoundingBox boundingBox;
    @SerializedName("leg")
    @Expose
    private Set<Leg> leg = new LinkedHashSet<Leg>();
    @SerializedName("note")
    @Expose
    private Set<Note> note = new LinkedHashSet<Note>();
    @SerializedName("maneuverGroup")
    @Expose
    private Set<ManeuverGroup> maneuverGroup = new LinkedHashSet<ManeuverGroup>();
    @SerializedName("incident")
    @Expose
    private Set<Incident> incident = new LinkedHashSet<Incident>();
    @SerializedName("label")
    @Expose
    private List<Object> label = new ArrayList<Object>();
    @SerializedName("zone")
    @Expose
    private List<Object> zone = new ArrayList<Object>();
    private final static long serialVersionUID = 7863514656284411359L;
    
    private List<GeoPosition> shapePoints;
    
    public Set<Waypoint> getWaypoint() {
        return waypoint;
    }

    public void setWaypoint(Set<Waypoint> waypoint) {
        this.waypoint = waypoint;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public List<GeoPosition> getShape() {
        return shapePoints;
    }

    public void setShape(List<Object> shape) {
        this.shape = shape;
        postProcess();
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(BoundingBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Set<Leg> getLeg() {
        return leg;
    }

    public void setLeg(Set<Leg> leg) {
        this.leg = leg;
    }

    public Set<Note> getNote() {
        return note;
    }

    public void setNote(Set<Note> note) {
        this.note = note;
    }

    public Set<ManeuverGroup> getManeuverGroup() {
        return maneuverGroup;
    }

    public void setManeuverGroup(Set<ManeuverGroup> maneuverGroup) {
        this.maneuverGroup = maneuverGroup;
    }

    public Set<Incident> getIncident() {
        return incident;
    }

    public void setIncident(Set<Incident> incident) {
        this.incident = incident;
    }

    public List<Object> getLabel() {
        return label;
    }

    public void setLabel(List<Object> label) {
        this.label = label;
    }

    public List<Object> getZone() {
        return zone;
    }

    public void setZone(List<Object> zone) {
        this.zone = zone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("waypoint", waypoint).append("mode", mode).append("shape", shape).append("boundingBox", boundingBox).append("leg", leg).append("note", note).append("maneuverGroup", maneuverGroup).append("incident", incident).append("label", label).append("zone", zone).toString();
    }


	public static List<GeoPosition> parseShape( List<Object> points ) {
		ArrayList<GeoPosition> out = new ArrayList<>();
		for (Object point : points) {
			if (point instanceof String) {
				out.add( new LatLon( (String) point ) );
			}
		}
		return out;
	}


	@Override
	public void postProcess() {
		shapePoints = new ArrayList<>();
		for (Object point : shape) {
			if (point instanceof String) {
				shapePoints.add( new LatLon( (String) point ) );
			}
		}
	}

}
