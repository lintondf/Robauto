package com.bluelightning;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.data.TripPlan;
import com.bluelightning.json.*;
import com.bluelightning.json.Leg.CumulativeTravel;
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

	protected static final String routeOptions = "fastest;truck;traffic:disabled"; // "fastest;car;traffic:disabled"; //
	
	
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
	protected static String reverseGeocodeUrl = "https://reverse.geocoder.cit.api.here.com/6.2/reversegeocode.json";

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
	
	@SuppressWarnings("deprecation")
	public static String getRestResponse(String urlBase, List<BasicNameValuePair> parameters) {
		String response = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			UrlEncodedFormEntity x = new UrlEncodedFormEntity(parameters);
			String url = String.format("%s?%s", urlBase, IOUtils.toString(x.getContent()));
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response2 = httpclient.execute(httpGet);
			if (response2.getStatusLine().getStatusCode() != 200) {
				System.out.println( url );
				System.out.println( response2.getStatusLine() );
				System.out.println( response2.getStatusLine().getReasonPhrase() );
				System.out.println( response2.getEntity() );
				System.out.println( response2.getEntity().getContent() );
			    response = IOUtils.toString(response2.getEntity().getContent());
			    System.out.println(response);
			    EntityUtils.consume(response2.getEntity());
				return null;
			}
		    HttpEntity entity2 = response2.getEntity();
		    response = IOUtils.toString(entity2.getContent());
		    EntityUtils.consume(entity2);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
		}		
		return response;
	}
	
	public static String geocodeReverseLookup( GeoPosition where) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		nvps.add( new BasicNameValuePair("prox", 
				String.format("%f,%f,100", where.getLatitude(), where.getLongitude())));
		nvps.add( new BasicNameValuePair("mode", "retrieveAddresses")); 
		nvps.add( new BasicNameValuePair("locationattributes","linkInfo"));
		String response = getRestResponse( reverseGeocodeUrl, nvps );
	    JsonElement jelement = new JsonParser().parse(response);
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    return gson.toJson(jelement).toString();
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
	    RobautoMain.logger.debug(response);
	    return null;
	}
	
	
	protected static class HereRoutePlus {
		HereRoute route;
		String json;
		public HereRoutePlus( HereRoute route, String json) {
			this.route = route;
			this.json = json;
		}
	}
	

	protected static HereRoutePlus getRouteBase(List<BasicNameValuePair> nvps, String mode) {
		nvps.add(new BasicNameValuePair("mode", mode));
		nvps.add(new BasicNameValuePair("alternatives", "3"));
		nvps.add(new BasicNameValuePair("metricSystem", "imperial"));
		nvps.add(new BasicNameValuePair("instructionFormat", "text"));
		nvps.add(new BasicNameValuePair("representation", "navigation"));
		nvps.add(new BasicNameValuePair("RouteAttributes", "waypoints,summary,legs,notes"));
		nvps.add(new BasicNameValuePair("legAttributes", "maneuvers,waypoint,length,travelTime,links"));
		nvps.add(new BasicNameValuePair("linkAttributes", "speedLimit,truckRestrictions,roadName"));
		nvps.add(new BasicNameValuePair("maneuverAttributes", 
				"position,length,travelTime,roadName,roadNumber,signPost,freewayExit,link,notes"));
		nvps.add(new BasicNameValuePair("avoidAreas", //TL/BR
				"42.53689200787314,-71.2738037109375;42.232584749313325,-70.9332275390625" + // Boston
//				"41.86547012230937,-73.73199462890625;41.21998578493921,-72.47955322265625" ));
		        "!40.95812268616409,-74.30740356445312;40.637925243274374,-73.740234375" ));// NYC

		nvps.add(new BasicNameValuePair("limitedWeight", "1"));  //TODO
		nvps.add(new BasicNameValuePair("truckRestrictionPenalty", "soft"));
		nvps.add(new BasicNameValuePair("height", Double.toString(146.0 / 12.0 * 0.3048)));          // TODO
		String json = getRestResponse( routingUrl, nvps );
		if (json == null)
			return null;
		System.out.println("REST bytes: " + json.length() );
	    JsonElement jelement = new JsonParser().parse(json);
	    try {
	    	PrintStream out = new PrintStream("route.json");
		    out.println( Here2.gson.toJson(jelement) );
	    	out.close();
	    } catch (Exception x) {}
	    HereRoute hereRoute = (HereRoute) Here2.gson.fromJson(jelement, HereRoute.class);
	    HereRoutePlus plus = new HereRoutePlus( hereRoute, json );
	    return plus;
	}
	
	protected static HereRoutePlus getRoute(LatLon from, List<LatLon> vias, LatLon to, String mode) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		int i = 0;
		nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), from.toGeo()));
		for (LatLon via : vias) {
			nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), via.toVia()));			
		}
		nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), to.toGeo()));
		return getRouteBase( nvps, mode );
	}

	protected static HereRoutePlus getRoute(List<LatLon> points, String mode) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		int i = 0;
		for (LatLon via : points) {
			nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), via.toGeo()));			
		}
		return getRouteBase( nvps, mode );
	}

	
	protected static HereRoutePlus getRouteFromPlaces(List<VisitedPlace> places, String mode) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		int i = 0;
		for (VisitedPlace place : places) {
			nvps.add(new BasicNameValuePair(String.format("waypoint%d", i++), place.toGeo()));			
		}
		return getRouteBase( nvps, mode );
	}

	
	protected static HereRoutePlus getRoute( LatLon from, LatLon to, String mode ) {
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
	
//	public static Route computeRoute() {
//		HereRoutePlus plus = null;
//		//try { new File("route.json").delete(); } catch (Exception x) {}
//		String[] pointAddresses = {
//				"3533 Carambola Cir, Melbourne, FL",
//				"2110 Bells Hwy, Walterboro, SC 29488",
//				"125 Riverside Dr, Banner Elk, NC",
//				"2350 So Pleasant Valley Rd, Winchester, VA 22601",
//				"10654 Breezewood Dr, Woodstock, MD 21163-1317",
//				"1365 Boston Post Road, Milford, CT",
//				"836 Palmer Avenue, Falmouth, MA" ,
//				"100 Cabelas Blvd, Scarborough, ME",
//				"7 Manor Lane, Sullivan, ME"			
//		};
//		try {
//			String json = IOUtils.toString(new FileInputStream("route.json"), "UTF-8");
//			HereRoute hereRoute = (HereRoute) Here2.gson.fromJson(json, HereRoute.class);
//			if (hereRoute == null || hereRoute.getResponse() == null)
//				throw new NullPointerException();
//			plus.route = hereRoute;
//			plus.json = json;
//		} catch (Exception x) {
//			ArrayList<LatLon> points = new ArrayList<>();
//			for (String address : pointAddresses) {
//				points.add(geocodeLookup(address));
//			}
//			plus = getRoute( points, routeOptions );
//		}
//		return computeRouteBase( plus.route );
//	}
	
	
	public static Route computeRoute(TripPlan tripPlan) {
		if (tripPlan.getRoute() == null) {
			Route route = new Route();
			TreeSet<Leg> legs = new TreeSet<>();
			ArrayList<Object> shape = new ArrayList<>();
			double lastPoint = 0;
			int n = tripPlan.getPlaces().size();
			if (n < 2)
				return null;
			tripPlan.getPlaces().get(0).setPassThru(false);
			tripPlan.getPlaces().get(n-1).setPassThru(false);
			for (int i = 0; i < n-1; i++) {
				int j = i + 1;
				while (j < n-1 && !tripPlan.getPlaces().get(j).isOvernight()) {
					j++;
				}
				HereRoutePlus hereRoute = getRouteFromPlaces( tripPlan.getPlaces().subList(i, j+1), routeOptions  );
				Route r = computeRouteBase( hereRoute.route );
				Leg leg = r.getLeg().iterator().next(); 
				leg.setFirstPoint( leg.getFirstPoint() + lastPoint );
				leg.setLastPoint( leg.getLastPoint() + lastPoint);
				lastPoint = leg.getLastPoint();
				legs.add(leg);
				shape.addAll( leg.getShape() );
				i = j-1;
			}
			route.setLeg(legs);
			route.setShape(shape);
			route.postProcess();
			return route;
		} else {
			return tripPlan.getRoute();
		}
	}


	public static Route computeRoute0(TripPlan tripPlan) {
		if (tripPlan.getRoute() == null) {
			HereRoutePlus hereRoute = getRouteFromPlaces( tripPlan.getPlaces(), routeOptions  );
			return computeRouteBase( hereRoute.route );
		} else {
			return tripPlan.getRoute();
		}
	}

	public static Route computeRoute( List<VisitedPlace> places ) {
		HereRoutePlus hereRoute = getRouteFromPlaces( places, routeOptions  );
		return computeRouteBase( hereRoute.route );
	}
	
	public static void reportRoute( Route route ) {
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
		//List<GeoPosition> routeShape = route.getShape();
		//int pointIndex = 1;
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
				System.out.printf("%-5s: %5.1f mi / %s; %7.1f mi / %s; %5.0f; %d [%s/%s/%s] %s\n", maneuver.getId(), 
						maneuver.getLength()*METERS_TO_MILES,
						Here2.toPeriod(maneuver.getTrafficTime()),
						progress.distance*METERS_TO_MILES,
						Here2.toPeriod(progress.trafficTime),
						speed,
						maneuver.getShape().size(),
						maneuver.getRoadName(), maneuver.getRoadNumber(), linkDetails,
						maneuver.getInstruction() );
				if ( maneuver.getShape().size() > 0)
					System.out.printf("   %12.6f, %12.6f\n", maneuver.getShape().get(0).getLatitude(), maneuver.getShape().get(0).getLongitude());
			}
		} // for leg
	}
	
	public static Route computeRouteBase( HereRoute hereRoute ) {
		//HereRoute hereRoute = plus.route;
		System.out.println(hereRoute.getResponse().getMetaInfo());
		Set<Route> routes = hereRoute.getResponse().getRoute();
		System.out.printf("%d routes\n", routes.size() );
		Iterator<Route> it = routes.iterator();
		Route route = null;
		// find shortest after adjusting speeds
		while (it.hasNext()) {
			Route r = it.next();
			System.out.println( r.getSummary().getText() +  "  " + r.getSummary().getDistance() + " " + r.getSummary().getTrafficTime() );
			if (route == null) {
				route = r;
			} else {
				if (r.getSummary().getTrafficTime() < route.getSummary().getTrafficTime()) {
					route = r;
				}
			}
		}
		if (route != null) {
			reportRoute(route);
			return route;
		} // for route
		return null;
	}
	
	public static void main( String[] args ) {
		/*
M1   :   0.0 mi /  0:00;     0.0 mi /  0:00; Infinity; 1 [MANOR LN/MANOR LN/] Get on Manor Ln and drive south
      44.522825,   -68.208898
M2   :   0.0 mi /  0:01;     0.0 mi /  0:00;     1; 1 [US1/US1/] Turn right onto Us1
      44.522826,   -68.208910
M3   :  10.5 mi /  0:16;     0.0 mi /  0:01;    39; 130 [DOWNEAST HWY/DOWNEAST HWY/] Keep right onto Downeast Hwy
      44.522588,   -68.208983
M4   :  24.1 mi /  0:33;    10.5 mi /  0:17;    44; 229 [I-395 W RAMP/I-395 W RAMP/] Take the I-395 W ramp to the right
      44.531643,   -68.406179		 
		 */
		System.out.println( Here2.geocodeReverseLookup( new GeoPosition(44.522588,   -68.208983) ));
	}

}
