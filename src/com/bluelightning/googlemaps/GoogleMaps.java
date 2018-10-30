package com.bluelightning.googlemaps;

/***
 * https://www.allstays.com/DL/join.htm
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.slf4j.LoggerFactory;

import com.bluelightning.RobautoMain;
import com.google.maps.GeoApiContext;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.RoadsApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.SnappedPoint;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.x5.util.Base64;

import ch.qos.logback.classic.LoggerContext;

@SuppressWarnings("deprecation")
public class GoogleMaps {
	
	static final String apiKey = "AIzaSyCA9cRJXpWLwxM4IccF9o3BCp2varIqm24";
	
	
	public static CloseableHttpClient createHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
	    HttpClientBuilder b = HttpClientBuilder.create();
	 
	    // setup a Trust Strategy that allows all certificates.
	    //
	    SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
	    	
//	        public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
//	            return true;
//	        }

			@Override
			public boolean isTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
					throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
				return true;
			}
	    }).build();
	    b.setSslcontext( sslContext);
	 
	    // don't check Hostnames, either.
	    //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
	    HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
	 
	    // here's the special part:
	    //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
	    //      -- and create a Registry, to register it.
	    //
	    SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
	    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
	            .register("http", PlainConnectionSocketFactory.getSocketFactory())
	            .register("https", sslSocketFactory)
	            .build();
	 
	    // now, we create connection-manager using our Registry.
	    //      -- allows multi-threaded use
	    PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
	    b.setConnectionManager( connMgr);
	 
	    // finally, build the HttpClient;
	    //      -- done!
	    CloseableHttpClient client = b.build();
	    return client;
	}
	
	public static class Route {
		public static class Location {
			public String addr;
			public String name;
			public String[] details;
			public Double lat;  // degrees
			public Double lng;  // degrees
		}
		
		public static class Distance {
			public Integer val;  // meters
			public String  txt;   
			public Integer total; // meters
		}
		
		public static class Point {
			public Double lat;  // degrees
			public Double lng;  // degrees
			public Distance dist;
			public String step;
			public String dir;
			public String nextturn;
			public String type;
		}
		
		public static class Waypoint {
			public Double lat;  // degrees
			public double lng;  // degrees
		}
		
		Location start;
		Location end;
		Location[] dest;
		
		String[] countries;
		
		String  mode;
		
		Point[] points;
		
		Integer totaldist;
		
		Waypoint[] wpts;
		
	}
	
	
//	@SuppressWarnings("deprecation")
//	private static String mapToJson( String mapUrl ) {
//		String base = "https://mapstogpx.com/load.php" + 
//				"?d=default" + 
//				"&lang=en" + 
//				"&elev=off" + 
//				"&tmode=off" + 
//				"&pttype=route" + 
//				"&o=json" + 
//				"&cmt=on" + 
//				"&desc=on" + 
//				"&descasname=off" + 
//				"&w=on" + 
//				"&dtstr=20170411_083945" + 
//				"&gdata=";
//		mapUrl = mapUrl.replace("https://www.google.com/maps/dir/", "");
//		try {
//			URL url = new URL(base + URLEncoder.encode(mapUrl) );
//			System.out.println(url);
////			url = new URL("https://mapstogpx.com/load.php?d=default&lang=en&elev=on&tmode=off&pttype=route&o=json&cmt=on&desc=on&descasname=off&w=on&dtstr=20170411_091225&gdata=3533%2BCarambola%2BCir%2C%2BMelbourne%2C%2BFL%2B32940%2FNew%2BBaltimore%2BTravel%2BPlaza%2C%2B127%2BNew%2BYork%2BState%2BThruway%2C%2BHannacroix%2C%2BNY%2B12087%2F7%2BManor%2BLn%2C%2BSullivan%2C%2BME%2B04664%2F%4036.1938353%2C-83.9469478%2C5z%2Fam%3Dt%2Fdata%3D!3m1!4b1!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1%3A0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-77.2875753!2d37.4965621!3s0x89b11b7c8ac52005%3A0x8afb4d10805f3f31!1m5!1m1!1s0x89dde9a4a6faef3d%3A0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295%3A0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0%3Fhl%3Den-US");
////			System.out.println(url);
//			CloseableHttpClient httpClient = createHttpClient();
//			
//			HttpGet  conn = new HttpGet(url.toString());
//			CloseableHttpResponse response = httpClient.execute(conn);
//			int responseCode = response.getStatusLine().getStatusCode();
//			if (responseCode == 200) {
//				if ( response.getEntity().isStreaming() ) {
//					StringBuffer sb = new StringBuffer();
//					BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
//					String line;
//					while ((line = rd.readLine()) != null) {
//					   sb.append(line);
//					}
//					rd.close();	
//					JsonParser parser = new JsonParser();
//					JsonElement element = parser.parse(sb.toString());
//
//					Gson gson = new GsonBuilder().setPrettyPrinting().create();
//					String json = gson.toJson(element);
//					return json;
//				}
//				System.err.println( response.getEntity().toString() );
//			}
//			System.err.println("updateUser Response: " + response);
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (KeyManagementException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (NoSuchAlgorithmException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (KeyStoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
	
//	private static List<SnappedPoint> snapToRoads(GeoApiContext context, List<LatLng> pathList ) throws ApiException, InterruptedException, IOException {
//		List<SnappedPoint> path = new ArrayList<SnappedPoint>();
//		int j = 0;
//		LatLng[] empty = new LatLng[100];
//		for (int i = 0; (i+100) < pathList.size(); i += 100) {
//			j = i + 100;
//			path.addAll( Arrays.asList(
//					RoadsApi.snapToRoads(context, pathList.subList(i, j).toArray(empty) ).await() ) );
//		}
//		empty = new LatLng[pathList.size() - j];
//		path.addAll( Arrays.asList(
//				RoadsApi.snapToRoads(context, pathList.subList(j, pathList.size()).toArray(empty) ).await() ) );
//		return path;
//	}
	/*
<step maneuver="DEPART" meters="526">
	Head 
	<direction dir="NORTH_EAST">
		north-east
	</direction> 
	on
	<roadlist>
		<road lang="en">
			Carambola Cir
		</road>
	</roadlist> 
	towards 
	<roadlist>
		<road lang="en">
			Floristana Dr
		</road>
	</roadlist>
</step>
	 */
	
	protected static PlaceDetails randomNearby(GeoApiContext context, LatLng  center ) {
		PlacesSearchResponse placeResponse;
		try {
			placeResponse = PlacesApi.nearbySearchQuery(context, center )
					.radius(50000)
					.type(PlaceType.GAS_STATION)
					.await();
		} catch (ApiException e) {
			RobautoMain.logger.error("Exception: ", e);
			return null;
		} catch (InterruptedException e) {
			RobautoMain.logger.error("Exception: ", e);
			return null;
		} catch (IOException e) {
			RobautoMain.logger.error("Exception: ", e);
			return null;
		}
		if (placeResponse == null) return null;
		if (placeResponse.results == null) return null;
		int n = placeResponse.results.length;
		int i = (int) Math.floor( (double)n * Math.random() );
		PlacesSearchResult placeResult = placeResponse.results[i];
		PlaceDetails details = new PlaceDetailsRequest(context)
				.placeId(placeResult.placeId).awaitIgnoreError();
		return details;
	}
	
	protected static GeodeticCalculator geoCalc = new GeodeticCalculator();
	protected static Ellipsoid wgs84 = Ellipsoid.WGS84;
	
	protected static PlaceDetails furthestNearby(GeoApiContext context, LatLng  center ) {
		PlacesSearchResponse placeResponse = PlacesApi.nearbySearchQuery(context, center )
				.radius(50000)
				.type(PlaceType.GAS_STATION)
				.awaitIgnoreError();
		double max = 0.0;
		PlaceDetails best = null;
		for (PlacesSearchResult placeResult : placeResponse.results) {
			PlaceDetails details = new PlaceDetailsRequest(context)
					.placeId(placeResult.placeId).awaitIgnoreError();
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84, 
					new GlobalCoordinates(center.lat, center.lng), 
					new GlobalCoordinates(details.geometry.location.lat, details.geometry.location.lng));
			double d = curve.getEllipsoidalDistance();
			if (d > max) {
				max = d;
				best = details;
			}
		}
		return best;
	}
	
	public static String serialize(TreeSet<String> set) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
	        ObjectOutputStream out = new ObjectOutputStream(baos);
	        out.writeObject(set);
	        out.close();
		} catch (Exception x) {
			return null;
		}
		return Base64.encodeBytes( baos.toByteArray() ).replaceAll("\n", "");
	}
	
	
	protected static boolean roadsFromPoints(GeoApiContext context) {
		ArrayList<GlobalCoordinates> points = new ArrayList<GlobalCoordinates>();
		BufferedReader b = null;
		try {
            File f = new File("../RobautoFX/Directions from mymaps.kml");

            b = new BufferedReader(new FileReader(f));

            String line = "";
            while ((line = b.readLine()) != null) {
            	if (line.contains("<coordinates>"))
            		break;
            }
            while ((line = b.readLine()) != null) {
            	if (line.contains("</coordinates>"))
            		break;
            	int i = line.indexOf(',');
            	int j = line.lastIndexOf(',');
            	double lng = Double.parseDouble( line.substring(0, i));
            	double lat = Double.parseDouble(line.substring(i+1, j));
            	points.add( new GlobalCoordinates( lat, lng ) ); 
            }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (b != null)
				try {
					b.close();
				} catch (IOException e) {
				}
		}
		
		double cumd = 0.0;
		for (int i = 1; i < points.size(); i++) {
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84, 
					points.get(i-1), 
					points.get(i));
			double d = curve.getEllipsoidalDistance();
			double az = curve.getAzimuth();
			cumd += d;
			RobautoMain.logger.debug(String.format("%10.3f mi %3.0f deg  %10.3f mi ", 
					d/0.3048/5280.0, az, cumd/0.3048/5280.0));
		}	
		RobautoMain.logger.debug( points.size() + " " + cumd / (double) points.size() );
		
		ArrayList<LatLng> subset = new ArrayList<LatLng>();
		for (int i = 360; i < 5*120+0*points.size(); i += 120) {
			subset.add( new LatLng( points.get(i).getLatitude(), points.get(i).getLongitude()) );
		}
		SnappedPoint[] snaps = RoadsApi.nearestRoads(context, subset.toArray( new LatLng[subset.size()]))
				.awaitIgnoreError();
		
		for (SnappedPoint snap : snaps) {
			PlaceDetails details = new PlaceDetailsRequest(context)
				.placeId(snap.placeId).awaitIgnoreError();
			RobautoMain.logger.debug(details.name + " " + details.vicinity );
		}
		return true;
	}
	
	protected static String toDms( double angle, String hemispheres ) {
		StringBuffer sb = new StringBuffer();
		String hemisphere = hemispheres.substring(0,1);
		if (angle < 0) {
			hemisphere = hemispheres.substring(1,2);
			angle = -angle;
		}
		sb.append( String.format("%d", (int) Math.floor(angle)) );
		sb.append("�");
		angle = 60.0 * (angle - Math.floor(angle));
		sb.append( String.format("%d", (int) Math.floor(angle)) );
		sb.append("'");
		angle = 60.0 * (angle - Math.floor(angle));
		sb.append( String.format("%f", angle) );
		sb.append("\"");
		sb.append(hemisphere);
		return sb.toString();
	}
		
	public static void main(String[] args) {
		System.out.println(String.format("https://www.google.com/maps/place/%s+%s",
				toDms(29.697742, "NS"), toDms(-81.326182, "EW")) );
		
		//Google Maps to Lat/Lng: -81.326182	29.697742
 		//https://www.google.com/maps/place/29�41'51.9"N+81�19'34.3"W
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.stop();
//		String mapUrl = "https://www.google.com/maps/dir/3533+Carambola+Cir,+Melbourne,+FL+32940,+USA/Jekyll+Island+Campground,+Riverview+Drive,+Brunswick,+GA/Walmart+Vision+%26+Glasses,+Smithfield,+NC/38.67118,-77.17452/Patapsco+Valley+State+Park,+8020+Baltimore+National+Pike,+Ellicott+City,+MD+21043/@33.6805702,-83.7376603,6z/am=t/data=!4m27!4m26!1m5!1m1!1s0x88de06ef0ba04fe1:0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!1m5!1m1!1s0x88e4dbc92d7c68d7:0x29d0308d10819d72!2m2!1d-81.4128!2d31.1072326!1m5!1m1!1s0x89ac6d5928ae5d0f:0x6cd3177e217eabf7!2m2!1d-78.3093638!2d35.5232034!1m0!1m5!1m1!1s0x89c81f7c74c6818d:0xbb46cc34aae2e03f!2m2!1d-76.7828076!2d39.2885596!3e0"; 
//				//"https://www.google.com/maps/dir/3533+Carambola+Cir,+Melbourne,+FL+32940/New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087/7+Manor+Ln,+Sullivan,+ME+04664/@36.1938353,-83.9469478,5z/am=t/data=!3m1!4b1!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1:0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-77.2875753!2d37.4965621!3s0x89b11b7c8ac52005:0x8afb4d10805f3f31!1m5!1m1!1s0x89dde9a4a6faef3d:0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295:0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0?hl=en-US";
//		String json = mapToJson( mapUrl );
//		if (json != null) {
//			System.out.println(json);
//			Gson gson = new Gson();
//			Route route = gson.fromJson(json, Route.class );
//			for (Route.Point point : route.points) {
//				System.out.println( /*point.nextturn + " | " + point.dir + " | " +*/ point.step );
//				if (point.step != null && !point.step.isEmpty()) {
//					String embed = String.format("<?xml version=\"1.0\"?>%s", point.step );
//					try {
//						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//						Document doc = dBuilder.parse(new ByteArrayInputStream(embed.getBytes()));
//						doc.getDocumentElement().normalize();
//						Element top = doc.getDocumentElement();
//						System.out.println( top.getTextContent() );
//	//					NodeList nodes = top.getChildNodes();
//	//					for (int i = 0; i < nodes.getLength(); i++) {
//	//						Node node = nodes.item(i);
//	//						//System.out.println( node.getNodeType() + " " + node.getNodeName() );
//	//						switch (node.getNodeType()) {
//	//						case Node.TEXT_NODE:
//	//							System.out.println(node.getTextContent());
//	//							break;
//	//						case Node.ELEMENT_NODE:
//	//							System.out.println(node.getNodeName());
//	//							break;
//	//						}
//	//					}
//					} catch (Exception x) {
//						x.printStackTrace();
//					}
//				} else {
//					System.out.println(point.dir);
//				}
//			}
//			return;
//		}
		
		LatLng start1 = new LatLng(32.715, -117.1625); //(30.336944, -81.661389);
		LatLng start2 = new LatLng(40.728333, -73.994167); //(47.609722, -122.333056);
		HttpInterceptorHandler interceptor = new HttpInterceptorHandler();
		GeoApiContext context = new GeoApiContext( interceptor ).setApiKey(apiKey);  
				//.setEnterpriseCredentials(clientID, clientSecret);

		if (roadsFromPoints(context)) return;
		
		TreeSet<String> places = new TreeSet<String>();
		try {
		     CSVReader reader = new CSVReader(new FileReader("places.csv"));
		     String [] nextLine;
		     while ((nextLine = reader.readNext()) != null) {
		        places.add(nextLine[0]);
		     }
		     reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		for (int i = 0; i < 50; i++) {
			PlaceDetails p1 = randomNearby( context, start1 );
			if (p1 != null) {
				start1 = p1.geometry.location;
				places.add(p1.formattedAddress);
			}
			PlaceDetails p2 = randomNearby( context, start2 );
			if (p2 != null) {
				start2 = p2.geometry.location;
				places.add(p2.formattedAddress);
			}
		}
		try {
			CSVWriter writer = new CSVWriter(new FileWriter("places.csv"), '\t');
			String[] string = new String[1];
			for (String address : places ) {
				System.out.println( address );
				string[0] = address;
				writer.writeNext(string);
			}
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		String serial = serialize( places );
//		try {
//			FileOutputStream fos = new FileOutputStream( "places.obj" );
//			fos.write( serial.getBytes() );
//			fos.close();
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		if (start2 != null) return;
		
		//GeocodingResult[] results;
		try {
////			results = GeocodingApi.geocode(context,
////			    "1600 Amphitheatre Parkway Mountain View, CA 94043").await();
////			System.out.println(results[0].formattedAddress);
////https://www.google.com/maps?ll=36.532674,-74.947547&z=4&t=m&hl=en-US&gl=US&mapclient=embed&saddr=3533+Carambola+Cir,+Melbourne,+FL+32940&daddr=New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087+to:7+Manor+Ln,+Sullivan,+ME+04664&dirflg=d
///*			
//https://www.google.com/maps/dir
// * /3533+Carambola+Cir,+Melbourne,+FL+32940
// * /New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087
// * /7+Manor+Lane,+Sullivan,+ME+04664-3126,+USA
// * /@40.2305511,-76.0472951,13.75z
// * /data=!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1:0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-76.036814!2d40.2489107!3s0x89c66da0509541c3:0x8e8c1db0911f4e88!1m5!1m1!1s0x89dde9a4a6faef3d:0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295:0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0
// * ?hl=en-US
//*/
////https://www.google.com/maps/dir/3533+Carambola+Cir,+Melbourne,+FL+32940/New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087/7+Manor+Lane,+Sullivan,+ME+04664-3126,+USA/@34.7716243,-80.3462902,5z
/*
 * https://www.google.com/maps?ll=36.532674,-74.947547&z=4&t=m&hl=en-US&gl=US&mapclient=embed&saddr=3533+Carambola+Cir,+Melbourne,+FL+32940&daddr=New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087+to:7+Manor+Ln,+Sullivan,+ME+04664&dirflg=d
 *
 * https://www.google.com/maps/dir/3533+Carambola+Cir,+Melbourne,+FL+32940/New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087/7+Manor+Ln,+Sullivan,+ME+04664/@30.5437821,-82.2191229,10z/data=!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1:0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-81.6193294!2d30.2797014!3s0x88e5b61180d9a675:0x39dc886f893f57a9!1m5!1m1!1s0x89dde9a4a6faef3d:0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295:0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0?hl=en-US
 * 
 * <iframe src="https://www.google.com/maps/embed?pb=!1m34!1m12!1m3!1d13188622.556758398!2d-83.94694780617614!3d36.19383532160127!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!4m19!3e0!4m5!1s0x88de06ef0ba04fe1%3A0x387686b6146acca3!2s3533+Carambola+Cir%2C+Melbourne%2C+FL+32940!3m2!1d28.234479999999998!2d-80.75313109999999!4m5!1s0x89dde9a4a6faef3d%3A0xac1113996de4a061!2sNew+Baltimore+Travel+Plaza%2C+127+New+York+State+Thruway%2C+Hannacroix%2C+NY+12087!3m2!1d42.427956699999996!2d-73.80554479999999!4m5!1s0x4caee844875d1295%3A0xd2bc1dd5aaaf365d!2s7+Manor+Ln%2C+Sullivan%2C+ME+04664!3m2!1d44.523030999999996!2d-68.2087797!5e0!3m2!1sen!2sus!4v1491911621310" width="600" height="450" frameborder="0" style="border:0" allowfullscreen></iframe>
 */
/*
 * https://mapstogpx.com/load.php
 * ?d=default
 * &lang=en
 * &elev=off
 * &tmode=off
 * &pttype=route
 * &o=json
 * &cmt=on
 * &desc=on
 * &descasname=off
 * &w=on
 * &dtstr=20170411_083945
 * &gdata=3533%2BCarambola%2BCir%2C%2BMelbourne%2C%2BFL%2B32940%2FNew%2BBaltimore%2BTravel%2BPlaza%2C%2B127%2BNew%2BYork%2BState%2BThruway%2C%2BHannacroix%2C%2BNY%2B12087%2F7%2BManor%2BLn%2C%2BSullivan%2C%2BME%2B04664%2F%4036.1938353%2C-83.9469478%2C5z%2Fam%3Dt%2Fdata%3D!3m1!4b1!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1%3A0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-77.2875753!2d37.4965621!3s0x89b11b7c8ac52005%3A0x8afb4d10805f3f31!1m5!1m1!1s0x89dde9a4a6faef3d%3A0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295%3A0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0%3Fhl%3Den-US			
 */
			PlacesSearchResponse placeResponse = PlacesApi.nearbySearchQuery(context, new LatLng(42.426431, -73.805126) )
					.radius(1000)
					.awaitIgnoreError();
			
			//final String poi = "NEW BALTIMORE SERVICE PLAZA";
			
			for (PlacesSearchResult placeResult : placeResponse.results) {
				System.out.println( placeResult.name + " | " + placeResult.vicinity + " | " + placeResult.formattedAddress );
				
				PlaceDetails details = new PlaceDetailsRequest(context)
						.placeId(placeResult.placeId).awaitIgnoreError();
				System.out.println("  " + details.formattedAddress );
				System.out.println("  " + details.geometry.location );
//				if (details.name.toUpperCase().contains(poi)) {
//					System.out.println( details.name + " " + details.formattedAddress );
//				}
			}
//		    DirectionsApiRequest request = DirectionsApi.newRequest(context);
//		    DirectionsResult result = request
//		            .origin("3533 Carambola Circle, Melbourne, FL, USA")
//		            .destination("7 Manor Lane, Sullivan, ME, USA")
//		            //.waypoints( new LatLng(42.426431, -73.805126) ) //		
//		            .waypoints( "via:42.426431,-73.805126" )
//		            .mode(TravelMode.DRIVING)
//		            .await();
//		   System.out.println( request );
//
//		    System.out.println(result.geocodedWaypoints);
//		    System.out.println(result.geocodedWaypoints.length);
//		    System.out.println( result.geocodedWaypoints[0].geocoderStatus);
//		    System.out.println( result.geocodedWaypoints[1].geocoderStatus);
//		    System.out.println( result.geocodedWaypoints[1].types[0]);
//		    for(DirectionsRoute route : result.routes) {
//		    	for (DirectionsLeg leg : route.legs) {
//		    		System.out.println(leg.duration);
//		    		for (DirectionsStep step : leg.steps) {
//		    			//System.out.println(step);
//		    			//System.out.println(step.endLocation);
//		    			System.out.println(step.htmlInstructions);
//		    			System.out.println(step.duration);
//		    		}
//		    	}
////		    	List<LatLng> pathList = route.overviewPolyline.decodePath();
////		    	List<SnappedPoint> points = snapToRoads( context, pathList );
////		    	for (SnappedPoint point : points ) {
////		    		System.out.println( point);
////		    		break;
////		    	}
////		    	for (LatLng point : path) {
////		    		System.out.println(point);
////		    		results = GeocodingApi.reverseGeocode(context, point).await();
////		    		for (GeocodingResult where : results) {
////		    			System.out.println(where.formattedAddress);
////		    		}
////		    	}
//		    }
//		    
		    System.out.println( interceptor.getRequestUrl() );
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
