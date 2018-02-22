package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.Events.AddManualStopEvent;
import com.bluelightning.Events.AddWaypointEvent;
import com.bluelightning.Events.POIClickEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.WebBrowser.UiHandler;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.ControlPanel;
import com.bluelightning.map.POIMarker;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.RestAreaPOI;
import com.bluelightning.poi.SamsClubPOI;
import com.bluelightning.poi.TruckStopPOI;
import com.bluelightning.poi.WalmartPOI;
import com.google.common.eventbus.Subscribe;

import seedu.addressbook.data.AddressBook;
import seedu.addressbook.logic.Logic;

// http://forecast.weather.gov/MapClick.php?lat=28.23&lon=-80.7&FcstType=digitalDWML

public class Main {

	protected List<ButtonWaypoint> waypoints;
	protected Map map;
	protected JXMapViewer mapViewer;
	protected RoutePanel routePanel;
	protected ControlPanel controlPanel;
	protected Route route;
	protected EnumMap<Main.MarkerKinds, POISet> poiMap = new EnumMap<>(Main.MarkerKinds.class);
	protected EnumMap<Main.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
	protected ArrayList<ButtonWaypoint> nearby = new ArrayList<>();
	protected MainPanel mainPanel;
	protected WebBrowser browserCanvas;
	protected Logic controller;
	protected AddressBook addressBook;

	public class POIClickHandler {
		@Subscribe
		protected void handle(POIClickEvent event) {
			System.out.println(event.toString());
			browserCanvas.moveTo(event.poi.getLatitude(), event.poi.getLongitude());
			int index = mainPanel.getRightTabbedPane().indexOfTab("AllStays");
			mainPanel.getRightTabbedPane().setSelectedIndex(index);
		}
	}

	public void insureNearbyMapLoaded(Route route, Main.MarkerKinds key, POISet pset) {
		nearbyMap.put(key, pset.getPointsOfInterestAlongRoute(route, controlPanel.getMarkerSearchRadius(key)));
	}

	public class UiHandler {
		@Subscribe
		protected void handle(UiEvent event) {
			System.out.println(event.source + " " + event.awtEvent);
			switch (event.source) {
			case "RoutePanel.AddAfter":
				routeAddAfter();
				break;
			case "RoutePanel.AddBefore":
				routeAddBefore();
				break;
			case "RoutePanel.MoveDown":
				routeMoveDown();
				break;
			case "RoutePanel.MoveUp":
				routeMoveUp();
				break;
			case "ControlPanel.Route":
				route = Here2.computeRoute();
				if (route != null) {
					waypoints = map.showRoute(route);
					int index = mainPanel.getRightTabbedPane().indexOfTab("Map");
					mainPanel.getRightTabbedPane().setSelectedIndex(index);
					insureNearbyMapLoaded(route, Main.MarkerKinds.RESTAREAS, poiMap.get(Main.MarkerKinds.RESTAREAS));
					OptimizeStops optimizeStops = new OptimizeStops(route, poiMap, nearbyMap);
				}
				break;

			case "ControlPanel.Waypoints":
				nearby.clear();
				nearby.addAll(waypoints); // always add stops along route
				for (Main.MarkerKinds key : poiMap.keySet()) {
					if (controlPanel.getMarkerStatus(key) && !nearbyMap.containsKey(key)) {
						POISet pset = poiMap.get(key);
						if (pset != null) {
							controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							insureNearbyMapLoaded(route, key, pset);
							controlPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
						}
					}
				}
				for (Entry<Main.MarkerKinds, ArrayList<POIResult>> entry : nearbyMap.entrySet()) {
					if (controlPanel.getMarkerStatus(entry.getKey())) {
						System.out.println("Adding " + entry.getValue().size() + " " + entry.getKey().toString());
						nearby.addAll(POIMarker.factory(entry.getValue()));
					}
				}
				System.out.println(nearby.size() + " total markers");
				map.updateWaypoints(nearby);
				break;
			default:
				break;
			}
		}
		
		private int selectedWaypointRow = -1;
		private CallbackHandler handler = null;
		
		private class CallbackHandler {
			@Subscribe
			protected void handle( AddWaypointEvent event ) {
				System.out.println( event + " " + event.place );
				Events.eventBus.unregister(this); // one shot
				handler = null;
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						
					}
				} );
			}			
		}

		private void routeAddAfter() {
			selectedWaypointRow = routePanel.getWaypointTable().getSelectedRow();
			if (selectedWaypointRow >= 0) {
				Events.eventBus.register( new CallbackHandler() );
				AddAddressDialog dialog = new AddAddressDialog(controller, addressBook);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		}

		private void routeAddBefore() {
		}

		private void routeMoveDown() {
		}

		private void routeMoveUp() {
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
		// TODO Costco, Cabelas
	}

	public Main() {
		// Display the viewer in a JFrame
		JFrame frame = new JFrame("RobAuto RV Trip Planner");
		mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout());
		routePanel = new RoutePanel(new Events.EventActionListener());
		mainPanel.getRightTabbedPane().addTab("Route", null, routePanel, null);
		map = new Map();
		mapViewer = map.getMapViewer();
		mainPanel.getRightTabbedPane().addTab("Map", null, mapViewer, null);
		browserCanvas = WebBrowser.factory(mainPanel);
		controlPanel = new ControlPanel();
		mainPanel.getLeftPanel().add(controlPanel);
		frame.setContentPane(mainPanel);
		// frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);

		// load base POI sets
		loadPOIMap(poiMap);

		// Bind event handlers

		Events.eventBus.register(new UiHandler());
		Events.eventBus.register(new POIClickHandler());
		
		try {
			controller = new Logic();
			controller.getStorage().load();
			addressBook = controller.getAddressBook();
		} catch (Exception x) {		
			x.printStackTrace();
		}
	}

	public EnumMap<Main.MarkerKinds, ArrayList<POIResult>> getNearbyMap() {
		return nearbyMap;
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

}
