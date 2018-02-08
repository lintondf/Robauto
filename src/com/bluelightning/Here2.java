package com.bluelightning;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.LocalResponseCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.WaypointPainter;

import com.bluelightning.json.*;
import com.bluelightning.json.Leg.CumulativeTravel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.x5.template.Chunk;
import com.x5.template.Theme;

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
		List<BasicNameValuePair> nvps = getBasicValuePair();
		nvps.add( new BasicNameValuePair("searchtext", address)); 
		String response = getRestResponse( geocodeUrl, nvps );
	    JsonElement jelement = new JsonParser().parse(response);
	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
	    JsonArray view = getNestedJsonArray(jelement, Arrays.asList("Response", "View"));
	    if (view != null) {
	    	JsonArray result = getNestedJsonArray(view.get(0), Arrays.asList("Result"));
	    	if (result != null) {
	    		JsonArray navigationPosition = getNestedJsonArray(result.get(0), Arrays.asList("Location", "NavigationPosition"));
	    		LatLon location = gson.fromJson(navigationPosition.get(0), LatLon.class);
	    		return location;
	    	}
	    }
	    return null;
	}
	
	public static HereRoute getRoute( LatLon from, LatLon to, String mode ) {
		List<BasicNameValuePair> nvps = getBasicValuePair();
		nvps.add(new BasicNameValuePair("waypoint0", from.toGeo()));
		nvps.add(new BasicNameValuePair("waypoint1", to.toGeo()));
		nvps.add(new BasicNameValuePair("metricSystem", "imperial"));
		nvps.add(new BasicNameValuePair("instructionFormat", "text"));
		nvps.add(new BasicNameValuePair("representation", "navigation"));
		nvps.add(new BasicNameValuePair("legAttributes", "maneuvers,waypoint,length,travelTime,links"));
		nvps.add(new BasicNameValuePair("linkAttributes", "speedLimit,truckRestrictions,roadName"));
		nvps.add(new BasicNameValuePair("maneuverAttributes", 
				"position,length,travelTime,roadName,roadNumber,signPost,freewayExit,link"));
		nvps.add(new BasicNameValuePair("avoidAreas", 
//				"52.517100760,13.3905424488;52.5169701849,13.391808451"));
				"41.86547012230937,-73.73199462890625;41.21998578493921,-72.47955322265625" ));
		//LL X:41.21998578493921,Y:-73.73199462890625, UR X:41.86547012230937,Y:-72.47955322265625

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

	public static void main(String[] args) {
//		LatLon lee = Here.geocodeLookup("Lee Service Plaza Eastbound");
//		System.out.println( lee  );
		HereRoute hereRoute = null;
		try {
			String json = IOUtils.toString(new FileInputStream("route.json"), "UTF-8");
			hereRoute = (HereRoute) Here2.gson.fromJson(json, HereRoute.class);
			if (hereRoute == null || hereRoute.getResponse() == null)
				throw new NullPointerException();
		} catch (Exception x) {
			LatLon sullivan = geocodeLookup("7 Manor Lane, Sullivan, ME");
			System.out.println( sullivan  );
			LatLon viera = geocodeLookup("3533 Carambola Cir, Melbourne, FL");
			System.out.println( viera );
			hereRoute = getRoute( viera, sullivan, "fastest;truck;traffic:disabled" );
		}
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
//					System.out.printf("    %s: %s\n", maneuver.getShapeQuality(), toString(maneuver.getShape()));
//					if (maneuver.getId().equals("M40")) {
//						routeShape = Route.parseShape(maneuver.getShape());
//					}
				}
				System.out.printf("LEG: %5.1f mi; %s %s; %5.1f\n", 
						leg.getLength()*METERS_TO_MILES, 
						Here2.toPeriod(leg.getTrafficTime()), 
						Here2.toPeriod(leg.getTravelTime()),
						leg.getLength()*METERS_TO_MILES / (leg.getTrafficTime()/3600.0) );
			} // for leg
			
			printPointsOfInterestAlongRoute( route, "POI/RestAreasCombined_USA.csv");
			
			Here2.showMap(routeShape);
		} // for route
	}

	protected static void printPointsOfInterestAlongRoute(Route route, String poiPath) {
		System.out.println(poiPath);
		POISet pset = POIBase.factory(poiPath);
		pset = pset.filter( route.getBoundingBox() );
		for (Leg leg : route.getLeg()) {
			Map<POI, POISet.POIResult> nearby = pset.nearBy(leg, 2000.0);
			// POISet pset =
			// WalmartPOI.factory("C:\\Users\\NOOK\\GIT\\default\\RobautoFX\\POI\\SamsClubs_USA.csv");
			// POISet pset =
			// TruckStopPOI.factory("C:\\Users\\NOOK\\GIT\\default\\RobautoFX\\POI\\Truck_Stops.csv");
			// Map<POI, POIResult> nearby = pset.nearBy(
			// route.getReduced(), 5000.0 );
	
			ArrayList<POISet.POIResult> byManeuver = new ArrayList<POISet.POIResult>();
			for (Entry<POI, POISet.POIResult> e : nearby.entrySet()) {
				byManeuver.add(e.getValue());
			}
			Collections.sort(byManeuver);
			for (POISet.POIResult r : byManeuver) {
				//CumulativeTravel progress = leg.getProgress(r);
				// System.out.println( r.toString() );
				double angle = r.maneuver.getShapeHeadings().get( r.index );
				String heading = angle2Direction(angle);
				String[] fields = r.poi.getName().split(",");
				if (fields[2].startsWith(heading.substring(0,1))) {
					System.out.printf("%-5s: %10.3f,%5.2f,%s,%s\n", r.maneuver.getId(),
							r.progress.distance / (0.3048 * 5280.0), 
							r.progress.trafficTime / 3600.0,
							heading,
							r.poi.getName());
				}
			}
		}
		System.out.println();
	}
	
	public static void showMap(List<GeoPosition> track) {
			JXMapViewer mapViewer = new JXMapViewer();
	
			// Display the viewer in a JFrame
			JFrame frame = new JFrame("JXMapviewer2 Example 2");
			frame.getContentPane().add(mapViewer);
			frame.setSize(800, 600);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
	
			// Create a TileFactoryInfo for OpenStreetMap
			TileFactoryInfo info = new OSMTileFactoryInfo();
			DefaultTileFactory tileFactory = new DefaultTileFactory(info);
			tileFactory.setThreadPoolSize(8);
			// Setup local file cache
			File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
			LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

			mapViewer.setTileFactory(tileFactory);
			

	
			RoutePainter routePainter = new RoutePainter(track);
	
			// Set the focus
			mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.9);
	
	        // Add interactions
	        MouseInputListener mia = new PanMouseInputListener(mapViewer);
	        mapViewer.addMouseListener(mia);
	        mapViewer.addMouseMotionListener(mia);
	        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
	        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
	        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
	        mapViewer.addMouseListener(new MouseAdapter(){
	            public void mouseClicked(MouseEvent e) {
	                   if(e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3){
	                       java.awt.Point p = e.getPoint();
	                       GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
	                       System.out.println("X:"+geo.getLatitude()+",Y:"+geo.getLongitude());
	                   }
	            }
	       });	
			
			// Create waypoints from the geo-positions
			Set<DefaultWaypoint> waypoints = new HashSet<DefaultWaypoint>(Arrays.asList(
					new DefaultWaypoint(track.get(0)),
					new DefaultWaypoint(track.get(track.size()-1))));
	
			// Create a waypoint painter that takes all the waypoints
			WaypointPainter<DefaultWaypoint> waypointPainter = new WaypointPainter<DefaultWaypoint>();
			waypointPainter.setWaypoints(waypoints);
			
			// Create a compound painter that uses both the route-painter and the waypoint-painter
			List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
			painters.add(routePainter);
			painters.add(waypointPainter);
			
			CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
			mapViewer.setOverlayPainter(painter);
		}

}
