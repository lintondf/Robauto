package robauto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.RoadsApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.SnappedPoint;
import com.google.maps.model.TransitMode;
import com.google.maps.model.TransitRoutingPreference;
import com.google.maps.model.TravelMode;

public class Main {
	
	static final String apiKey = "AIzaSyCA9cRJXpWLwxM4IccF9o3BCp2varIqm24";
	
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
		GeoApiContext context = new GeoApiContext().setApiKey(apiKey);  
				//.setEnterpriseCredentials(clientID, clientSecret);
		GeocodingResult[] results;
		try {
//			results = GeocodingApi.geocode(context,
//			    "1600 Amphitheatre Parkway Mountain View, CA 94043").await();
//			System.out.println(results[0].formattedAddress);

		    DirectionsApiRequest request = DirectionsApi.newRequest(context);
		    DirectionsResult result = request
		            .origin("3533 Carambola Circle, Melbourne, FL, USA")
		            .destination("7 Manor Lane, Sullivan, ME, USA")
		            .mode(TravelMode.DRIVING)
		            .await();

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
		} catch (ApiException | InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
