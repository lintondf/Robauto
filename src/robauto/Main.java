package robauto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PlaceDetailsRequest;
import com.google.maps.PlacesApi;
import com.google.maps.RoadsApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceDetails;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TransitRoutingPreference;
import com.google.maps.model.TravelMode;

public class Main {
	
	static final String apiKey = "AIzaSyCA9cRJXpWLwxM4IccF9o3BCp2varIqm24";
	
	@SuppressWarnings("deprecation")
	public static CloseableHttpClient createHttpClient() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
	    HttpClientBuilder b = HttpClientBuilder.create();
	 
	    // setup a Trust Strategy that allows all certificates.
	    //
	    SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
	        public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
	            return true;
	        }

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
	
	
	private static String mapToJson( String mapUrl ) {
		String base = "https://mapstogpx.com/load.php" + 
				"?d=default" + 
				"&lang=en" + 
				"&elev=off" + 
				"&tmode=off" + 
				"&pttype=route" + 
				"&o=json" + 
				"&cmt=on" + 
				"&desc=on" + 
				"&descasname=off" + 
				"&w=on" + 
				"&dtstr=20170411_083945" + 
				"&gdata=";
		mapUrl = mapUrl.replace("https://www.google.com/maps/dir/", "");
		try {
			URL url = new URL(base + URLEncoder.encode(mapUrl) );
			System.out.println(url);
//			url = new URL("https://mapstogpx.com/load.php?d=default&lang=en&elev=on&tmode=off&pttype=route&o=json&cmt=on&desc=on&descasname=off&w=on&dtstr=20170411_091225&gdata=3533%2BCarambola%2BCir%2C%2BMelbourne%2C%2BFL%2B32940%2FNew%2BBaltimore%2BTravel%2BPlaza%2C%2B127%2BNew%2BYork%2BState%2BThruway%2C%2BHannacroix%2C%2BNY%2B12087%2F7%2BManor%2BLn%2C%2BSullivan%2C%2BME%2B04664%2F%4036.1938353%2C-83.9469478%2C5z%2Fam%3Dt%2Fdata%3D!3m1!4b1!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1%3A0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-77.2875753!2d37.4965621!3s0x89b11b7c8ac52005%3A0x8afb4d10805f3f31!1m5!1m1!1s0x89dde9a4a6faef3d%3A0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295%3A0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0%3Fhl%3Den-US");
//			System.out.println(url);
			CloseableHttpClient httpClient = createHttpClient();
			
			HttpGet  conn = new HttpGet(url.toString());
			CloseableHttpResponse response = httpClient.execute(conn);
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == 200) {
				if ( response.getEntity().isStreaming() ) {
					StringBuffer sb = new StringBuffer();
					BufferedReader rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
					String line;
					while ((line = rd.readLine()) != null) {
					   sb.append(line);
					}
					rd.close();	
					JsonParser parser = new JsonParser();
					JsonElement element = parser.parse(sb.toString());

					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					String json = gson.toJson(element);
					return json;
				}
				System.err.println( response.getEntity().toString() );
			}
			System.err.println("updateUser Response: " + response);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private static List<SnappedPoint> snapToRoads(GeoApiContext context, List<LatLng> pathList ) throws ApiException, InterruptedException, IOException {
		List<SnappedPoint> path = new ArrayList<SnappedPoint>();
		int j = 0;
		LatLng[] empty = new LatLng[100];
		for (int i = 0; (i+100) < pathList.size(); i += 100) {
			j = i + 100;
			path.addAll( Arrays.asList(
					RoadsApi.snapToRoads(context, pathList.subList(i, j).toArray(empty) ).await() ) );
		}
		empty = new LatLng[pathList.size() - j];
		path.addAll( Arrays.asList(
				RoadsApi.snapToRoads(context, pathList.subList(j, pathList.size()).toArray(empty) ).await() ) );
		return path;
	}

	public static void main(String[] args) {
		String mapUrl = "https://www.google.com/maps/dir/3533+Carambola+Cir,+Melbourne,+FL+32940,+USA/Jekyll+Island+Campground,+Riverview+Drive,+Brunswick,+GA/Walmart+Supercenter,+1550+Skibo+Rd,+Fayetteville,+NC+28303/38.67118,-77.17452/Patapsco+Valley+State+Park,+8020+Baltimore+National+Pike,+Ellicott+City,+MD+21043/@35.1225427,-79.2581005,8.75z/am=t/data=!4m27!4m26!1m5!1m1!1s0x88de06ef0ba04fe1:0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!1m5!1m1!1s0x88e4dbc92d7c68d7:0x29d0308d10819d72!2m2!1d-81.4128!2d31.1072326!1m5!1m1!1s0x89ab6b142d902c5b:0xc299c532d93859dc!2m2!1d-78.957215!2d35.081892!1m0!1m5!1m1!1s0x89c81f7c74c6818d:0xbb46cc34aae2e03f!2m2!1d-76.7828076!2d39.2885596!3e0"; 
				//"https://www.google.com/maps/dir/3533+Carambola+Cir,+Melbourne,+FL+32940/New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087/7+Manor+Ln,+Sullivan,+ME+04664/@36.1938353,-83.9469478,5z/am=t/data=!3m1!4b1!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1:0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-77.2875753!2d37.4965621!3s0x89b11b7c8ac52005:0x8afb4d10805f3f31!1m5!1m1!1s0x89dde9a4a6faef3d:0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295:0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0?hl=en-US";
		String json = mapToJson( mapUrl );
		if (json != null) {
			//System.out.println(json);
			Gson gson = new Gson();
			Route route = gson.fromJson(json, Route.class );
			for (Route.Point point : route.points) {
				System.out.println( point.nextturn + " | " + point.dir );
			}
			return;
		}
		HttpInterceptorHandler interceptor = new HttpInterceptorHandler();
		GeoApiContext context = new GeoApiContext( interceptor ).setApiKey(apiKey);  
				//.setEnterpriseCredentials(clientID, clientSecret);
		GeocodingResult[] results;
		try {
//			results = GeocodingApi.geocode(context,
//			    "1600 Amphitheatre Parkway Mountain View, CA 94043").await();
//			System.out.println(results[0].formattedAddress);
//https://www.google.com/maps?ll=36.532674,-74.947547&z=4&t=m&hl=en-US&gl=US&mapclient=embed&saddr=3533+Carambola+Cir,+Melbourne,+FL+32940&daddr=New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087+to:7+Manor+Ln,+Sullivan,+ME+04664&dirflg=d
/*			
https://www.google.com/maps/dir
 * /3533+Carambola+Cir,+Melbourne,+FL+32940
 * /New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087
 * /7+Manor+Lane,+Sullivan,+ME+04664-3126,+USA
 * /@40.2305511,-76.0472951,13.75z
 * /data=!4m25!4m24!1m10!1m1!1s0x88de06ef0ba04fe1:0x387686b6146acca3!2m2!1d-80.7531311!2d28.23448!3m4!1m2!1d-76.036814!2d40.2489107!3s0x89c66da0509541c3:0x8e8c1db0911f4e88!1m5!1m1!1s0x89dde9a4a6faef3d:0xac1113996de4a061!2m2!1d-73.8055448!2d42.4279567!1m5!1m1!1s0x4caee844875d1295:0xd2bc1dd5aaaf365d!2m2!1d-68.2087797!2d44.523031!3e0
 * ?hl=en-US
*/
//https://www.google.com/maps/dir/3533+Carambola+Cir,+Melbourne,+FL+32940/New+Baltimore+Travel+Plaza,+127+New+York+State+Thruway,+Hannacroix,+NY+12087/7+Manor+Lane,+Sullivan,+ME+04664-3126,+USA/@34.7716243,-80.3462902,5z
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
			
			final String poi = "NEW BALTIMORE SERVICE PLAZA";
			
			for (PlacesSearchResult placeResult : placeResponse.results) {
				System.out.println( placeResult.placeId + " | " + placeResult.vicinity + " | " + placeResult.name );
				
//				PlaceDetails details = new PlaceDetailsRequest(context)
//						.placeId(placeResult.placeId).awaitIgnoreError();
//				for (String type : details.types) {
//					System.out.print( type + " " );
//				}
//				if (details.name.toUpperCase().contains(poi)) {
//					System.out.println( details.name + " " + details.formattedAddress );
//				}
			}
		    DirectionsApiRequest request = DirectionsApi.newRequest(context);
		    DirectionsResult result = request
		            .origin("3533 Carambola Circle, Melbourne, FL, USA")
		            .destination("7 Manor Lane, Sullivan, ME, USA")
		            //.waypoints( new LatLng(42.426431, -73.805126) ) //		
		            .waypoints( "via:42.426431,-73.805126" )
		            .mode(TravelMode.DRIVING)
		            .await();
		   System.out.println( request );

		    System.out.println(result.geocodedWaypoints);
		    System.out.println(result.geocodedWaypoints.length);
		    System.out.println( result.geocodedWaypoints[0].geocoderStatus);
		    System.out.println( result.geocodedWaypoints[1].geocoderStatus);
		    System.out.println( result.geocodedWaypoints[1].types[0]);
		    for(DirectionsRoute route : result.routes) {
		    	for (DirectionsLeg leg : route.legs) {
		    		System.out.println(leg.duration);
		    		for (DirectionsStep step : leg.steps) {
		    			//System.out.println(step);
		    			//System.out.println(step.endLocation);
		    			System.out.println(step.htmlInstructions);
		    			System.out.println(step.duration);
		    		}
		    	}
//		    	List<LatLng> pathList = route.overviewPolyline.decodePath();
//		    	List<SnappedPoint> points = snapToRoads( context, pathList );
//		    	for (SnappedPoint point : points ) {
//		    		System.out.println( point);
//		    		break;
//		    	}
//		    	for (LatLng point : path) {
//		    		System.out.println(point);
//		    		results = GeocodingApi.reverseGeocode(context, point).await();
//		    		for (GeocodingResult where : results) {
//		    			System.out.println(where.formattedAddress);
//		    		}
//		    	}
		    }
		    
		    System.out.println( interceptor.getRequestUrl() );
		    
		} catch (ApiException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
