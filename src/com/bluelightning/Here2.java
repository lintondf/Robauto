package com.bluelightning;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.json.*;
import com.bluelightning.json.Leg.CumulativeTravel;
import com.bluelightning.map.POIMarker;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.SamsClubPOI;
import com.bluelightning.poi.TruckStopPOI;
import com.bluelightning.poi.WalmartPOI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.x5.template.Chunk;
import com.x5.template.Theme;

import seedu.addressbook.data.place.VisitedPlace;

public class Here2 {
	
	//https://developer.here.com/documentation
	
	public static Gson gson = new GsonBuilder()
		.registerTypeAdapterFactory(new PostProcessingEnabler())
		.setPrettyPrinting()
		.create();

	public static final double METERS_TO_MILES = (1.0/0.3048) / (5280.0);
	public static final double METERS_PER_SECOND_TO_MILES_PER_HOUR = ((1.0/0.3048)*60.0/88.0);
	
	// Blackpearl.Configuration.formatPeriod() joda.time
	public static String toPeriod( double seconds ) {
		int h = (int) (seconds/3600.0);
		int m = (int) ((seconds - 3600.0*h)/60.0);
		return String.format("%2d:%02d", h, m );
	}
	
	public static void writeKml( String path, List<LatLon> points ) {
		int n = points.size();
		writeKml( path, points.get(0), points.subList(2, n-1), points.get(n-1) );
	}
	
	public static void writeKml( String filePath, LatLon origin, List<LatLon> points, LatLon destination ) {
		Theme theme = new Theme();
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%g,%g,0\n", origin.getLongitude(), origin.getLatitude()));
		for (LatLon p : points) {
			sb.append(String.format("%g,%g,0\n", p.getLongitude(), p.getLatitude()));
		}
		sb.append(String.format("%g,%g,0\n", destination.getLongitude(), destination.getLatitude()));
		Chunk t = theme.makeChunk("report#kml");
		t.set("coordinates", sb.toString());
		
		try {
			PrintWriter out = new PrintWriter(filePath);
			out.println(t.toString());
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}
	
	protected static String routingXmlUrl = "https://route.cit.api.here.com/routing/7.2/calculateroute.xml";
	protected static String routingUrl = "https://route.cit.api.here.com/routing/7.2/calculateroute.json";
	protected static String geocodeUrl = "https://geocoder.cit.api.here.com/6.2/geocode.json";

	protected static JsonElement getNestedJsonElement( JsonElement element, List<String> fields ) {
		if (element == null)
			return null;
		for (String name : fields) {
			if (!element.isJsonObject())
				return null;
			element = element.getAsJsonObject().get(name);
		}
		return element;
	}

	protected static JsonArray getNestedJsonArray( JsonElement element, String field ) {
		return getNestedJsonArray(element, Arrays.asList(field));
	}
	
	protected static JsonArray getNestedJsonArray( JsonElement element, List<String> fields ) {
		element = getNestedJsonElement( element, fields);
		if (element == null)
			return null;
		return element.getAsJsonArray();
	}
	
	protected static List<BasicNameValuePair> getBasicValuePair() {
		List<BasicNameValuePair> nvps = new ArrayList <>();
		nvps.add(new BasicNameValuePair("app_id", 
				"gf6QHsctHY6MXZxazFDC"));
		nvps.add(new BasicNameValuePair("app_code", "nH-rpKYk_bREera8aWsXrQ"));
		return nvps;
	}
	
	public static String getRestResponse(String urlBase, List<BasicNameValuePair> parameters) {
		String response = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			UrlEncodedFormEntity x = new UrlEncodedFormEntity(parameters);
			String url = String.format("%s?%s", urlBase, IOUtils.toString(x.getContent()));
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response2 = httpclient.execute(httpGet);
			if (response2.getStatusLine().getStatusCode() != 200)
				return null;
		    HttpEntity entity2 = response2.getEntity();
		    response = IOUtils.toString(entity2.getContent());
		    EntityUtils.consume(entity2);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}		
		return response;
	}
	
	
	public static LatLon geocodeLookup( String address ) {
		return geocodeLookup(address, false);
	}
	
	public static LatLon geocodeLookup( String address, boolean verbose ) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		nvps.add( new BasicNameValuePair("searchtext", address)); 
		String response = getRestResponse( geocodeUrl, nvps );
	    JsonElement jelement = new JsonParser().parse(response);
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    if (verbose) System.out.println( gson.toJson(jelement).toString() );
	    JsonArray view = getNestedJsonArray(jelement, Arrays.asList("Response", "View"));
	    if (view != null && view.size() > 0) {
	    	JsonArray result = getNestedJsonArray(view.get(0), Arrays.asList("Result"));
	    	if (result != null) {
	    		JsonArray navigationPosition = getNestedJsonArray(result.get(0), Arrays.asList("Location", "NavigationPosition"));
	    		LatLon location = gson.fromJson(navigationPosition.get(0), LatLon.class);
	    		return location;
	    	}
	    }
	    // TODO log response
	    return null;
	}
	

	public static HereRoute getRouteBase(List<BasicNameValuePair> nvps, String mode) {
		nvps.add(new BasicNameValuePair("metricSystem", "imperial"));
		nvps.add(new BasicNameValuePair("instructionFormat", "text"));
		nvps.add(new BasicNameValuePair("representation", "navigation"));
		nvps.add(new BasicNameValuePair("legAttributes", "maneuvers,waypoint,length,travelTime,links"));
		nvps.add(new BasicNameValuePair("linkAttributes", "speedLimit,truckRestrictions,roadName"));
		nvps.add(new BasicNameValuePair("maneuverAttributes", 
				"position,length,travelTime,roadName,roadNumber,signPost,freewayExit,link"));
//		nvps.add(new BasicNameValuePair("avoidAreas", 
//				"41.86547012230937,-73.73199462890625;41.21998578493921,-72.47955322265625" ));

		nvps.add(new BasicNameValuePair("mode", mode));
		nvps.add(new BasicNameValuePair("limitedWeight", "1"));  //TODO
		nvps.add(new BasicNameValuePair("height", Double.toString(146.0 / 12.0 * 0.3048)));          // TODO
		String json = getRestResponse( routingUrl, nvps );
		System.out.println("REST bytes: " + json.length() );
	    JsonElement jelement = new JsonParser().parse(json);
	    try {
	    	PrintStream out = new PrintStream("route.json");
		    out.println( Here2.gson.toJson(jelement) );
	    	out.close();
	    } catch (Exception x) {}
	    HereRoute hereRoute = (HereRoute) Here2.gson.fromJson(jelement, HereRoute.class);
	    return hereRoute;
//	    System.out.println( hereRoute );
//	    JsonArray routes = Here.getNestedJsonArray(jelement, Arrays.asList("response", "route"));
//	    if (routes != null && routes.size() > 0) {
//	    	for (JsonElement route : routes) {
//	    		if (route.isJsonObject()) {
//	    			Route r = Route.factory( route );
//	    			return r;
//	    		}
//	    	}
//	    }
//	    /*
//	     * route [ waypoint [] ]
//	     */
//	    return null;
	}
	
	public static HereRoute getRoute(LatLon from, List<LatLon> vias, LatLon to, String mode) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		int i = 0;
		nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), from.toGeo()));
		for (LatLon via : vias) {
			nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), via.toGeo()));			
		}
		nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), to.toGeo()));
		return getRouteBase( nvps, mode );
	}

	public static HereRoute getRoute(List<LatLon> points, String mode) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		int i = 0;
		for (LatLon via : points) {
			nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), via.toGeo()));			
		}
		return getRouteBase( nvps, mode );
	}

	
	public static HereRoute getRouteFromPlaces(List<VisitedPlace> places, String mode) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		int i = 0;
		for (VisitedPlace place : places) {
			nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), place.toGeo()));			
		}
		return getRouteBase( nvps, mode );
	}

	
	public static HereRoute getRoute( LatLon from, LatLon to, String mode ) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		nvps.add(new BasicNameValuePair("waypoint0", from.toGeo()));
		nvps.add(new BasicNameValuePair("waypoint1", to.toGeo()));
		return getRouteBase( nvps, mode );
	}
	
	
	
	public static String angle2Direction( double angle) {
		if (angle < 22.5 || angle > 337.5)
			return "N";
		else if (angle < 67.5)
			return "NE";
		else if (angle < 112.5)
			return "E";
		else if (angle < 157.5)
			return "SE";
		else if (angle < 202.5)
			return "S";
		else if (angle < 247.5)
			return "SW";
		else if (angle < 292.5)
			return "W";
		return "NW";
	}
	
	
	public static String toString( List<Object> shape ) {
		StringBuffer sb = new StringBuffer();
		for (Object obj : shape) {
			sb.append( obj.toString() );
			sb.append("; ");
		}
		return sb.toString();
	}
	
	

//	public static void main(String[] args) {
//		LatLon lee = Here.geocodeLookup("Lee Service Plaza Eastbound");
//		System.out.println( lee  );
	
	public static Route computeRoute() {
		HereRoute hereRoute = null;
		//try { new File("route.json").delete(); } catch (Exception x) {}
		String[] pointAddresses = {
				"3533 Carambola Cir, Melbourne, FL",
				"2110 Bells Hwy, Walterboro, SC 29488",
				"125 Riverside Dr, Banner Elk, NC",
				"2350 So Pleasant Valley Rd, Winchester, VA 22601",
				"10654 Breezewood Dr, Woodstock, MD 21163-1317",
				"1365 Boston Post Road, Milford, CT",
				"836 Palmer Avenue, Falmouth, MA" ,
				"100 Cabelas Blvd, Scarborough, ME",
				"7 Manor Lane, Sullivan, ME"			
		};
		try {
			String json = IOUtils.toString(new FileInputStream("route.json"), "UTF-8");
			hereRoute = (HereRoute) Here2.gson.fromJson(json, HereRoute.class);
			if (hereRoute == null || hereRoute.getResponse() == null)
				throw new NullPointerException();
		} catch (Exception x) {
			ArrayList<LatLon> points = new ArrayList<>();
			for (String address : pointAddresses) {
				points.add(geocodeLookup(address));
			}
			hereRoute = getRoute( points, "fastest;truck;traffic:disabled" );
		}
		return computeRouteBase( hereRoute, pointAddresses );
	}
	
	
	public static Route computeRoute( List<VisitedPlace> places ) {
		String[] pointAddresses = new String[places.size()];
		for (int i = 0; i < pointAddresses.length; i++) {
			pointAddresses[i] = places.get(i).getAddress().value;
		}
		HereRoute hereRoute = getRouteFromPlaces( places, "fastest;truck;traffic:disabled"  );
		return computeRouteBase( hereRoute, pointAddresses );
	}
	
	protected static Route computeRouteBase( HereRoute hereRoute, String[] pointAddresses ) {
		System.out.println(hereRoute.getResponse().getMetaInfo());
		Set<Route> routes = hereRoute.getResponse().getRoute();
		System.out.printf("%d routes\n", routes.size() );
		for (Route route : routes) {
			System.out.printf("%d indicents\n", route.getIncident().size() );
			for (Incident incident : route.getIncident() ) {
				System.out.println(incident);
			}
			System.out.printf("%d maneuvergroups\n", route.getManeuverGroup().size() );
			for (ManeuverGroup group : route.getManeuverGroup()) {
				System.out.println( group );
			}
			System.out.printf("%d shapes\n", route.getShape().size() );
			//writeKml( "shape.kml", route.getShape() );
			System.out.printf("%d waypoints\n", route.getWaypoint().size() );
			for (Waypoint waypoint : route.getWaypoint()) {
				System.out.println( waypoint );
			}
			System.out.printf("%d legs\n", route.getLeg().size() );
			List<GeoPosition> routeShape = route.getShape();
			int pointIndex = 1;
			for (Leg leg : route.getLeg()) {
				System.out.println( leg.getSummary() );
				System.out.printf("%d links\n", leg.getLink().size() );
				System.out.printf("%d Maneuvers\n", leg.getManeuver().size() );
				for (Maneuver maneuver : leg.getManeuver()) {
					Link link = leg.getLinkMap().get(maneuver.getId());
					String linkDetails = "";
					double speed = METERS_PER_SECOND_TO_MILES_PER_HOUR*(maneuver.getLength() / maneuver.getTrafficTime());
					if (link != null) {
						String truckRestrictions = (link.getTruckRestrictions() == null || link.getTruckRestrictions().getHeight() == null) ?
								"None" : String.format("%.1f ft", link.getTruckRestrictions().getHeight()/0.3048);
						double speedLimit = (link.getSpeedLimit() == null) ? 0.0 : METERS_PER_SECOND_TO_MILES_PER_HOUR*link.getSpeedLimit();
						linkDetails = String.format("%.0f,%.0f,%s", speed, speedLimit, truckRestrictions);
					}
					CumulativeTravel progress = leg.getProgress(maneuver);
					System.out.printf("%-5s: %5.1f mi / %s; %7.1f mi / %s  [%s/%s/%s] %s\n", maneuver.getId(), 
							maneuver.getLength()*METERS_TO_MILES,
							Here2.toPeriod(maneuver.getTrafficTime()),
							progress.distance*METERS_TO_MILES,
							Here2.toPeriod(progress.trafficTime),
							maneuver.getRoadName(), maneuver.getRoadNumber(),
							linkDetails,
							//angle2Direction(maneuver.getStartAngle()), // angle at start of maneuver
							maneuver.getInstruction() );
//					int i = maneuver.getInstruction().indexOf(" onto ");
//					if (i >= 0)
//						System.out.println(maneuver.getInstruction().substring(i));
//					System.out.printf("    %s: %s\n", maneuver.getShapeQuality(), toString(maneuver.getShape()));
//					if (maneuver.getId().equals("M40")) {
//						routeShape = Route.parseShape(maneuver.getShape());
//					}
				}
				System.out.printf("LEG TO %s: %5.1f mi; %s %s; %5.1f\n", 
						pointAddresses[pointIndex++],
						leg.getLength()*METERS_TO_MILES, 
						Here2.toPeriod(leg.getTrafficTime()), 
						Here2.toPeriod(leg.getTravelTime()),
						leg.getLength()*METERS_TO_MILES / (leg.getTrafficTime()/3600.0) );
			} // for leg
			
//			printPointsOfInterestAlongRoute( route, "POI/RestAreasCombined_USA.csv");
//			printPointsOfInterestAlongRoute( route, "POI/SamsClubs_USA.csv");
//			POISet pset = WalmartPOI.factory(); //TruckStopPOI.factory(); // POIBase.factory("POI/Costco_USA_Canada.csv");
//			ArrayList<POISet.POIResult> nearby = pset.getPointsOfInterestAlongRoute(route, 5e3 );
//			pset = SamsClubPOI.factory();
//			nearby.addAll(pset.getPointsOfInterestAlongRoute(route, 5e3 ));
//			
//			com.bluelightning.Map.showMap(routeShape, route, POIMarker.factory(nearby));
			return route;
		} // for route
		return null;
	}


}
