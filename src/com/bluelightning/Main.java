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
import com.bluelightning.WebBrowser.UiHandler;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.ControlPanel;
import com.bluelightning.map.ControlPanel.MarkerKinds;
import com.bluelightning.map.POIMarker;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.POISet.POIResult;
import com.bluelightning.poi.SamsClubPOI;
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
	protected EnumMap<ControlPanel.MarkerKinds, POISet> poiMap = new EnumMap<>(ControlPanel.MarkerKinds.class);
	protected EnumMap<ControlPanel.MarkerKinds, ArrayList<POISet.POIResult>> nearbyMap = new EnumMap<>(ControlPanel.MarkerKinds.class);
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
					}
					break;
					
				case "ControlPanel.Waypoints":
					nearby.clear();
					nearby.addAll(waypoints);  // always add stops along route
					for (ControlPanel.MarkerKinds key : poiMap.keySet()) {
						if (controlPanel.getMarkerStatus(key) && ! nearbyMap.containsKey(key)) {
							POISet pset = poiMap.get(key);
							if (pset != null) {
								controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
								nearbyMap.put(key, pset.getPointsOfInterestAlongRoute(route, controlPanel.getMarkerSearchRadius(key) ));
								controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							}
						}						
					}
					for (Entry<MarkerKinds, ArrayList<POIResult>> entry : nearbyMap.entrySet()) {
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

		POISet pset = WalmartPOI.factory(); 
		poiMap.put(ControlPanel.MarkerKinds.WALMARTS, pset);
		pset = SamsClubPOI.factory();
		poiMap.put(ControlPanel.MarkerKinds.SAMSCLUBS, pset);
		//TruckStopPOI.factory(); // POIBase.factory("POI/Costco_USA_Canada.csv");

		Events.eventBus.register(new UiHandler() );
		Events.eventBus.register(new POIClickHandler() );
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

}
