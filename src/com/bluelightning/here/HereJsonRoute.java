
package com.bluelightning.here;

import java.io.FileReader;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.BaseCamp;
import com.bluelightning.Day;
import com.bluelightning.LatLon;
import com.bluelightning.TrackPoint;
import com.bluelightning.TripSource;
import com.bluelightning.BaseCamp.Turn;
import com.bluelightning.googlemaps.OpenLocationCode;
import com.bluelightning.here.PolylineEncoderDecoder.LatLngZ;
import com.bluelightning.map.StopMarker;
import com.garmin.fit.Decode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import seedu.addressbook.data.place.VisitedPlace;

public class HereJsonRoute implements TripSource, Serializable {

	@SerializedName("routes")
	@Expose
	private List<Route> routes = new ArrayList<Route>();
	private final static long serialVersionUID = 4607533007625009056L;

	public List<Route> getRoutes() {
		return routes;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}

	public static void main(String[] args) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();

		Gson gson = builder.create();
		try {
			String jsonString = IOUtils.toString(new FileReader("route.json"));// , Charset.defaultCharset());
			HereJsonRoute here = gson.fromJson(jsonString, HereJsonRoute.class);
			here.parse();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public void parse() {
		try {

			StopMarker stopMarker = null;

			for (Route route : this.getRoutes()) {
				System.out.println(route);
				System.out.println(route.getId());
				for (Section section : route.getSections()) {
					System.out.println(section.getId());
					System.out.println(section.getType());
					System.out.println( section.getSummary() );

					Day day = new Day();
					days.add(day);

					Place departure = section.getDeparture().getPlace();
					GeoPosition where = new GeoPosition(departure.getLocation().getLat(),
							departure.getLocation().getLng());
					String departureName = where.toString();
					String text = departure.getType();
					stopMarker = new StopMarker(StopMarker.ORIGIN, departureName, text, where);
					addPlace(new VisitedPlace(stopMarker));
					day.addWaypoint(new Day.Waypoint(departureName, text));

					Place arrival = section.getArrival().getPlace();
					where = new GeoPosition(arrival.getLocation().getLat(), arrival.getLocation().getLng());
					String arrivalName = where.toString();
					text = departure.getType();
					stopMarker = new StopMarker(StopMarker.TERMINUS, arrivalName, text, where);
					addPlace(new VisitedPlace(stopMarker));
					day.addWaypoint(new Day.Waypoint(arrivalName, text));

					List<LatLngZ> pl = PolylineEncoderDecoder.decode(section.getPolyline());
					for (LatLngZ llz : pl) {
						LatLon point = new LatLon(llz.lat, llz.lng);
						day.add(point, null, 0.0);
					}

//					for (Span span : section.getSpans()) { // TODO associate speeds, lengths and durations with polyline
//						System.out.println(span.getNames());
//						System.out.println(span.getLength() / span.getDuration() + " " + span.getSpeedLimit());
//						if (span.getSpeedLimit() != null)
//							System.out.println(span.getSpeedLimit() / 0.3048 * (3600.0 / 5280.0));
//					}

					double durationSoFar = 0.0;
					double distanceSoFar = 0.0;
					turns.add( new BaseCamp.Turn("Start at " + departureName, 0, 0, distanceSoFar, durationSoFar));
					for (Action action : section.getActions()) {
						double distance = action.getLength();
						double duration = action.getDuration();
						BaseCamp.Turn turn = new BaseCamp.Turn(action.getInstruction(), 
								distance, duration, distanceSoFar, durationSoFar);
						distanceSoFar += distance;
						durationSoFar += duration;
						System.out.println(turns.size() + ": " + turn.toString() );
						turns.add(turn);
					}
					turns.add( new BaseCamp.Turn("Arrive at " + arrivalName, 0, 0, distanceSoFar, durationSoFar));
				}
			}
			System.out.println(getDays());
			System.out.println(getPlaces());

		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private ArrayList<Day> days = new ArrayList<>();
	private ArrayList<VisitedPlace> places = new ArrayList<>();
	private ArrayList<BaseCamp.Turn> turns = new ArrayList<>();

	private void addPlace(VisitedPlace place) {
		if (!places.isEmpty()) {
			if (places.get(places.size() - 1).getName().equals(place.getName()))
				return;
		}
		places.add(place);
	}

	@Override
	public ArrayList<Day> getDays() {
		return days;
	}

	@Override
	public ArrayList<VisitedPlace> getPlaces() {
		return places;
	}

	/**
	 * @return the turns
	 */
	public ArrayList<BaseCamp.Turn> getTurns() {
		return turns;
	}

}
