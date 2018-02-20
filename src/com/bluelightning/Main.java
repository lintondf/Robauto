package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Label;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.Events.POIClickEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.WebBrowser.UiHandler;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.ControlPanel;
import com.bluelightning.map.POIMarker;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.RestAreaPOI;
import com.bluelightning.poi.POISet.POIResult;
import com.bluelightning.poi.SamsClubPOI;
import com.bluelightning.poi.TruckStopPOI;
import com.bluelightning.poi.WalmartPOI;
import com.google.common.eventbus.Subscribe;

// http://forecast.weather.gov/MapClick.php?lat=28.23&lon=-80.7&FcstType=digitalDWML

public class Main {
	
	protected List<ButtonWaypoint> waypoints;
	protected Map         map;
	protected JXMapViewer mapViewer;
	protected RoutePanel  routePanel;
	protected ControlPanel controlPanel;
	protected Route route;
	protected EnumMap<Main.MarkerKinds, POISet> poiMap = new EnumMap<>(Main.MarkerKinds.class);
	protected EnumMap<Main.MarkerKinds, ArrayList<POISet.POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
	protected ArrayList<ButtonWaypoint> nearby = new ArrayList<>();
	protected MainPanel mainPanel;
	protected WebBrowser browserCanvas;
	
	public class POIClickHandler {
		@Subscribe
		protected void handle( POIClickEvent event ) {
			System.out.println( event.toString() );
			browserCanvas.moveTo( event.poi.getLatitude(), event.poi.getLongitude() );
			int index = mainPanel.getRightTabbedPane().indexOfTab("AllStays");
			mainPanel.getRightTabbedPane().setSelectedIndex(index);
		}		
	}
	
	public void insureNearbyMapLoaded( Route route, Main.MarkerKinds key, POISet pset ) {
		nearbyMap.put(key, pset.getPointsOfInterestAlongRoute(route, controlPanel.getMarkerSearchRadius(key) ));
	}
	
	public class UiHandler {
			@Subscribe
			protected void handle( UiEvent event ) {
				System.out.println(event.source + " " + event.awtEvent );
				switch (event.source) {
				case "ControlPanel.Route":
					route = Here2.computeRoute();
					if (route != null) {
						waypoints = map.showRoute(route);
						int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
						mainPanel.getRightTabbedPane().setSelectedIndex(index);
//						long start = System.nanoTime();
						insureNearbyMapLoaded( route, Main.MarkerKinds.RESTAREAS, poiMap.get(Main.MarkerKinds.RESTAREAS));
//						long finish = System.nanoTime();
//						double secs = 1.0e9 * (double) (finish - start);
//						System.out.println( secs + " seconds");
						OptimizeStops optimizeStops = new OptimizeStops(route, poiMap, nearbyMap);
					}
					break;
					
				case "ControlPanel.Waypoints":
					nearby.clear();
					nearby.addAll(waypoints);  // always add stops along route
					for (Main.MarkerKinds key : poiMap.keySet()) {
						if (controlPanel.getMarkerStatus(key) && ! nearbyMap.containsKey(key)) {
							POISet pset = poiMap.get(key);
							if (pset != null) {
								controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								insureNearbyMapLoaded( route, key, pset);
								controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							}
						}						
					}
					for (Entry<Main.MarkerKinds, ArrayList<POIResult>> entry : nearbyMap.entrySet()) {
						if (controlPanel.getMarkerStatus(entry.getKey())) {
							System.out.println("Adding " + entry.getValue().size() + " " + entry.getKey().toString());
							nearby.addAll( POIMarker.factory( entry.getValue() ));
						}
					}
					System.out.println(nearby.size() + " total markers");
					map.updateWaypoints(nearby);
					break;
				default:
					break;
				}
			}
		}

	public static enum MarkerKinds {
		WALMARTS, SAMSCLUBS, COSTCOS, TRUCKSTOPS, RESTAREAS
	}
	
	// static support test use
	public static void loadPOIMap(EnumMap<Main.MarkerKinds, POISet> poiMap) {
		POISet pset = WalmartPOI.factory(); 
		poiMap.put(Main.MarkerKinds.WALMARTS, pset);
		pset = SamsClubPOI.factory();
		poiMap.put(Main.MarkerKinds.SAMSCLUBS, pset);
		pset = RestAreaPOI.factory();
		poiMap.put(Main.MarkerKinds.RESTAREAS, pset);
		pset = TruckStopPOI.factory(); 
		poiMap.put(Main.MarkerKinds.TRUCKSTOPS, pset);
		//TODO Costco, Cabelas		
	}

	public Main() {
		// Display the viewer in a JFrame
		JFrame frame = new JFrame("RobAuto RV Trip Planner");
		mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout() );
		routePanel = new RoutePanel();
		mainPanel.getRightTabbedPane().addTab("Route", null, routePanel, null);
		map = new Map();
		mapViewer = map.getMapViewer();
		mainPanel.getRightTabbedPane().addTab("Map", null, mapViewer, null);
		browserCanvas = WebBrowser.factory(mainPanel);
		controlPanel = new ControlPanel();
		mainPanel.getLeftPanel().add( controlPanel );
		frame.setContentPane(mainPanel);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);
		
		// load base POI sets
		loadPOIMap( poiMap );
		
		// Bind event handlers

		Events.eventBus.register(new UiHandler() );
		Events.eventBus.register(new POIClickHandler() );
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

	public EnumMap<Main.MarkerKinds, ArrayList<POISet.POIResult>> getNearbyMap() {
		return nearbyMap;
	}

}
