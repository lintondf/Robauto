/* 
 *  Geodesy by Mike Gavaghan
 * 
 *      http://www.gavaghan.org/blog/free-source-code/geodesy-library-vincentys-formula/
 * 
 *  Copyright 2007 Mike Gavaghan - mike@gavaghan.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
/*
 * BitCoin tips graciously accepted at 1FB63FYQMy7hpC2ANVhZ5mSgAZEtY1aVLf
 */
package org.gavaghan.geodesy;

import java.io.Serializable;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * <p>
 * Encapsulation of latitude and longitude coordinates on a globe. Negative
 * latitude is southern hemisphere. Negative longitude is western hemisphere.
 * </p>
 * <p>
 * Any angle may be specified for longtiude and latitude, but all angles will be
 * canonicalized such that:
 * </p>
 * 
 * <pre>
 * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
 * </pre>
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
public class GlobalCoordinates extends GeoPosition implements Comparable<GlobalCoordinates>, Serializable
{
	/**
	 * Canonicalize the current latitude and longitude values such that:
	 * 
	 * <pre>
	 * -90 &lt;= latitude &lt;= +90 - 180 &lt; longitude &lt;= +180
	 * </pre>
	 */
	private void canonicalize()
	{
		latitude = (latitude + 180) % 360;
		if (latitude < 0) latitude += 360;
		latitude -= 180;

		if (latitude > 90)
		{
			latitude = 180 - latitude;
			longitude += 180;
		}
		else if (latitude < -90)
		{
			latitude = -180 - latitude;
			longitude += 180;
		}

		longitude = ((longitude + 180) % 360);
		if (longitude <= 0) longitude += 360;
		longitude -= 180;
	}

	/**
	 * Construct a new GlobalCoordinates. Angles will be canonicalized.
	 * 
	 * @param latitude
	 *            latitude in degrees
	 * @param longitude
	 *            longitude in degrees
	 */
	public GlobalCoordinates(double latitude, double longitude)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		canonicalize();
	}
	
	public GlobalCoordinates( GeoPosition that ) {
		latitude = that.getLatitude();
		longitude = that.getLongitude();
		canonicalize();
	}

	/**
	 * Get latitude.
	 * 
	 * @return latitude in degrees
	 */
	public double getLatitude()
	{
		return latitude;
	}

	/**
	 * Set latitude. The latitude value will be canonicalized (which might
	 * result in a change to the longitude). Negative latitude is southern
	 * hemisphere.
	 * 
	 * @param latitude
	 *            in degrees
	 */
	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
		canonicalize();
	}

	/**
	 * Get longitude.
	 * 
	 * @return longitude in degrees
	 */
	public double getLongitude()
	{
		return longitude;
	}

	/**
	 * Set longitude. The longitude value will be canonicalized. Negative
	 * longitude is western hemisphere.
	 * 
	 * @param longitude
	 *            in degrees
	 */
	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
		canonicalize();
	}

	/**
	 * Compare these coordinates to another set of coordiates. Western
	 * longitudes are less than eastern logitudes. If longitudes are equal, then
	 * southern latitudes are less than northern latitudes.
	 * 
	 * @param other
	 *            instance to compare to
	 * @return -1, 0, or +1 as per Comparable contract
	 */
	public int compareTo(GlobalCoordinates other)
	{
		int retval;

		if (longitude < other.longitude) retval = -1;
		else if (longitude > other.longitude) retval = +1;
		else if (latitude < other.latitude) retval = -1;
		else if (latitude > other.latitude) retval = +1;
		else retval = 0;

		return retval;
	}

	/**
	 * Get a hash code for these coordinates.
	 * 
	 * @return hash code
	 */
	@Override
	public int hashCode()
	{
		return ((int) (longitude * latitude * 1000000 + 1021)) * 1000033;
	}

	/**
	 * Compare these coordinates to another object for equality.
	 * 
	 * @param obj object to compare to
	 * @return 'true' if object are equal
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof GlobalCoordinates)) return false;

		GlobalCoordinates other = (GlobalCoordinates) obj;

		return (longitude == other.longitude) && (latitude == other.latitude);
	}

	/**
	 * Get coordinates as a string.
	 */
	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append(Math.abs(latitude));
		buffer.append((latitude >= 0) ? 'N' : 'S');
		buffer.append(';');
		buffer.append(Math.abs(longitude));
		buffer.append((longitude >= 0) ? 'E' : 'W');
		buffer.append(';');

		return buffer.toString();
	}
}
