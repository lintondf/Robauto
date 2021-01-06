package com.bluelightning;

public class TrackPoint extends LatLon {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public double distancePriorToHere;
	public double distanceStartToHere;
	public double heading;
	public double duration; //[s]
	public String maneuver;

	public TrackPoint(LatLon where, String maneuver) {
		super(where);
		this.distanceStartToHere = 0.0;
		this.distancePriorToHere = 0.0;
		this.heading = 0.0;
		this.duration = 0.0;
		this.maneuver = maneuver;
	}
	
	public String toString() {
		return String.format("%s; %7.3f km; %5.0f deg; %6.3f km; %4.0f s; %s", 
				super.toString(), 0.001*distanceStartToHere, heading, 0.001*this.distancePriorToHere, this.duration,
				(this.maneuver == null) ? "" : this.maneuver);
	}
}