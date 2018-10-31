/**
 * 
 */
package com.bluelightning;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

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

import seedu.addressbook.data.place.VisitedPlace;

import com.bluelightning.json.Leg;
import com.bluelightning.json.Route;
import com.bluelightning.map.RoutePainter;
import com.bluelightning.map.StopMarker;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.ButtonWaypointOverlayPainter;


/**
 * @author NOOK
 *
 */
public class Map {
	
	protected static final double[] zoomMetersPerPixel = {
			156,412,
			78,206,
			39,103,
			19,551,
			9,776,
			4,888,
			2,444,
			1,222,
			610.984,
			305.492,
			152.746,
			76.373,
			38.187,
			19.093,
			9.547,
			4.773,
			2.387,
			1.193,
			0.596,
			0.298
	};
	protected JXMapViewer mapViewer;
	protected WaypointPainter<DefaultWaypoint> waypointPainter;
	protected RoutePainter routePainter;
	protected List<Painter<JXMapViewer>> painters;
	protected WaypointPainter<ButtonWaypoint> markerPainter;
	protected List<ButtonWaypoint> currentMarkers = new ArrayList<>();

	public JXMapViewer getMapViewer() {
		return mapViewer;
	}

	/**
	 * @param args
	 */
//	public static void main(String[] args) {
//		Here2.main(args);
//	}
	
	public Map() {
		mapViewer = new JXMapViewer();
		// Create a TileFactoryInfo for OpenStreetMap
		TileFactoryInfo info = new OSMTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(info);
		tileFactory.setThreadPoolSize(8);
		// Setup local file cache
		File cacheDir = new File(System.getProperty("user.home") + File.separator + ".jxmapviewer2");
		LocalResponseCache.installResponseCache(info.getBaseURL(), cacheDir, false);

		mapViewer.setTileFactory(tileFactory);

		// Add interactions
		MouseInputListener mia = new PanMouseInputListener(mapViewer);
		mapViewer.addMouseListener(mia);
		mapViewer.addMouseMotionListener(mia);
		mapViewer.addMouseListener(new CenterMapListener(mapViewer));
		mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCenter(mapViewer));
		mapViewer.addKeyListener(new PanKeyListener(mapViewer));
		mapViewer.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
					java.awt.Point p = e.getPoint();
					GeoPosition geo = mapViewer.convertPointToGeoPosition(p);
					RobautoMain.logger.debug("X:" + geo.getLatitude() + ",Y:" + geo.getLongitude());
					RobautoMain.logger.debug( Here2.geocodeReverseLookup( geo ) );
				}
			}
		});
//		mapViewer.addPropertyChangeListener("zoom", new PropertyChangeListener() {
//
//			@Override
//			public void propertyChange(PropertyChangeEvent event) {
//				Integer zoom = (Integer) event.getNewValue();
//				System.out.println(zoom + " " + zoomMetersPerPixel[zoom] *  mapViewer.getBounds().getWidth() );
//			}
//			
//		});
		mapViewer.setCenterPosition( new GeoPosition(28, -81));
		mapViewer.setZoom(10);
	}
	
	public void clearRoute() {
		// remove the old swing markers
		for (ButtonWaypoint w : currentMarkers) {
			mapViewer.remove(w);
		}
		currentMarkers.clear();
		markerPainter = new ButtonWaypointOverlayPainter();
		markerPainter.setWaypoints( new HashSet<ButtonWaypoint>());
		
        painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(markerPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		mapViewer.setOverlayPainter(painter);
		
		mapViewer.setCenterPosition( new GeoPosition(28, -81));
		mapViewer.setZoom(10);
	}
	
	public void show(ArrayList<List<GeoPosition>> tracks, ArrayList<ButtonWaypoint> waylist) {
		java.awt.Color[] colors = {java.awt.Color.RED, java.awt.Color.BLUE};
		int iColor = 0;
		
		markerPainter = new ButtonWaypointOverlayPainter();
		markerPainter.setWaypoints( new HashSet<ButtonWaypoint>(waylist));
		
        painters = new ArrayList<Painter<JXMapViewer>>();
        for (List<GeoPosition> track : tracks ) {
        	routePainter = new RoutePainter(track);
        	routePainter.setColor( colors[iColor++] );
			painters.add(routePainter);
        }
		// Set the focus
		mapViewer.zoomToBestFit(new HashSet<GeoPosition>(tracks.get(0)), 0.9);	
		painters.add(markerPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		mapViewer.setOverlayPainter(painter);
		
		// Add the JButtons to the map viewer
		for (ButtonWaypoint w : waylist) {
			mapViewer.add(w);
			currentMarkers.add(w);
		}
	}
	
	
	public List<ButtonWaypoint> showRoute( Route route) {
		List<GeoPosition> track = route.getShape();
		routePainter = new RoutePainter(track);
		// Set the focus
		mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.9);	
		
		// Create waypoints from the geo-positions
		ArrayList<ButtonWaypoint> waylist = new ArrayList<>();
		int kind = StopMarker.ORIGIN;
		String text = "";
		for (Leg leg : route.getLeg()) {
			text = leg.getStart().getUserLabel();
			waylist.add(new StopMarker(kind, text, new LatLon(leg.getStart().getMappedPosition())));
			kind = StopMarker.OVERNIGHT;
			text = leg.getEnd().getUserLabel();
		}
		waylist.add(new StopMarker(StopMarker.TERMINUS, text, track.get(track.size() - 1)));
		
		//waylist.forEach(System.out::println);

		markerPainter = new ButtonWaypointOverlayPainter();
		markerPainter.setWaypoints( new HashSet<ButtonWaypoint>(waylist));
		
        painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(routePainter);
		painters.add(markerPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		mapViewer.setOverlayPainter(painter);
		
		// Add the JButtons to the map viewer
		for (ButtonWaypoint w : waylist) {
			mapViewer.add(w);
			currentMarkers.add(w);
		}
		
		return waylist;
	}
	
	
	public void updateWaypoints(List<ButtonWaypoint> waylist) {
		// remove the old swing markers
		for (ButtonWaypoint w : currentMarkers) {
			mapViewer.remove(w);
		}
		markerPainter.setWaypoints( new HashSet<ButtonWaypoint>(waylist));
		currentMarkers.clear();
		// Add the JButtons to the map viewer
		for (ButtonWaypoint w : waylist) {
			mapViewer.add(w);
			currentMarkers.add(w);
		}
		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		mapViewer.setOverlayPainter(painter);
		mapViewer.validate();
	}
	
	public static final int ALL_DAYS = -1;
	
	protected ButtonWaypoint  youAreHere = null;

	public List<ButtonWaypoint> showRoute(ArrayList<Route> days, ArrayList<Integer> markers, ArrayList<ArrayList<VisitedPlace>> allPlaces, int whichDay) {
		ArrayList<VisitedPlace> flatPlaces = new ArrayList<>();
		for (ArrayList<VisitedPlace> dayPlaces : allPlaces) {
			flatPlaces.addAll(dayPlaces);
		}
		RobautoMain.logger.info(String.format("showRoute %d %d, %d", allPlaces.size(), flatPlaces.size(), markers.size() ));
		for (int i = 0; i < flatPlaces.size(); i++) {
			System.out.printf("showRoute %d %s\n", markers.get(i), flatPlaces.get(i) );
		}
		
		ArrayList<GeoPosition> track = new ArrayList<>(); 
		for (int i = 0; i < days.size(); i++) {
			if (whichDay == ALL_DAYS || i == whichDay) {
				track.addAll( days.get(i).getShape() );
			}
		}
		routePainter = new RoutePainter(track);
		
		// Create waypoints from the geo-positions
		ArrayList<ButtonWaypoint> waylist = new ArrayList<>();
		if (youAreHere != null)
			waylist.add(youAreHere);
		
		for (int i = 0; i < flatPlaces.size(); i++) {
			VisitedPlace place = flatPlaces.get(i);
			if (whichDay == ALL_DAYS || place.getVisitOrder() == (whichDay+1)) {
				StopMarker marker = new StopMarker( markers.get(i), place.toString(), new LatLon(place.getLatitude(), place.getLongitude()) );
				waylist.add(marker);
			}
		}
//		String text = "";
//		for (int i = 0; i < days.size(); i++) {
//			Route route = days.get(i);
//			for (Leg leg : route.getLeg()) {
//				text = leg.getStart().getUserLabel();
//				if (whichDay == ALL_DAYS || i == whichDay) {
//					waylist.add(new StopMarker(iMarker.next(), text, new LatLon(leg.getStart().getMappedPosition())));
//				} else {
//					iMarker.next();
//				}
//				text = leg.getEnd().getUserLabel();
//			}
//		}
//		waylist.add(new StopMarker(iMarker.next(), text, track.get(track.size() - 1)));

		markerPainter = new ButtonWaypointOverlayPainter();
		HashSet<ButtonWaypoint> buttonWaypointSet = new HashSet<>(waylist);
		markerPainter.setWaypoints( buttonWaypointSet );
		
        painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(routePainter);
		painters.add(markerPainter);

		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
		mapViewer.setOverlayPainter(painter);
		
		// Add the JButtons to the map viewer
		for (ButtonWaypoint w : waylist) {
			mapViewer.add(w);
			currentMarkers.add(w);
		}
		
		// Set the focus
		mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.9);	
		mapViewer.calculateZoomFrom( new HashSet<GeoPosition>(track) );
		return waylist;
	}

	public ButtonWaypoint getYouAreHere() {
		return youAreHere;
	}

	public void setYouAreHere(ButtonWaypoint youAreHere) {
		this.youAreHere = youAreHere;
	}
	
	public void moveYouAreHere( GeoPosition where) {
		if (youAreHere != null) {
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					youAreHere.setPosition(where);
					mapViewer.validate();			
				}
			});
		}
	}

}
