/**
 * 
 */
package com.bluelightning.data;

import java.io.Serializable;

import com.bluelightning.Here2;
import com.bluelightning.poi.POIResult;

/**
 * @author NOOK
 *
 */
public class FuelStop implements  Comparable<FuelStop>, Serializable {

	private static final long serialVersionUID = 2L;

	public double latitude;
	public double longitude;
	public double distanceFromStart;
	public double timeFromStart;
	public String name;
	public String address;
	
	public FuelStop( POIResult r ) {
		latitude = r.center.getLatitude();
		longitude = r.center.getLongitude();
		distanceFromStart = r.totalProgress.distance;
		timeFromStart = r.totalProgress.travelTime;
		name = r.poi.getName();
		address = r.poi.getAddress();
	}
	
	@Override
	public String toString() {
		return String.format("%10.1f %s (%10.6f, %10.6f) [%s] / [%s]", distanceFromStart, Here2.toPeriod(timeFromStart),
				latitude, longitude, name, address );
	}

	@Override
	public int compareTo(FuelStop that) {
		return (int) (this.distanceFromStart - that.distanceFromStart);
	}
	
}
