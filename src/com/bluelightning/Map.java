/**
 * 
 */
package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JPanel;
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

import com.bluelightning.json.Leg;
import com.bluelightning.json.Route;
import com.bluelightning.map.ControlPanel;
import com.bluelightning.map.POIMarker;
import com.bluelightning.map.RoutePainter;
import com.bluelightning.map.SwingMarker;
import com.bluelightning.map.POIMarkerOverlayPainter;
import com.bluelightning.poi.POISet.POIResult;


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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Here2.main(args);
	}
	
	public static JXMapViewer factory() {
		JXMapViewer mapViewer = new JXMapViewer();

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
					System.out.println("X:" + geo.getLatitude() + ",Y:" + geo.getLongitude());
				}
			}
		});
		mapViewer.addPropertyChangeListener("zoom", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Integer zoom = (Integer) event.getNewValue();
				System.out.println(zoom + " " + zoomMetersPerPixel[zoom] *  mapViewer.getBounds().getWidth() );
			}
			
		});
		mapViewer.setCenterPosition( new GeoPosition(28, -81));
		mapViewer.setZoom(10);
		return mapViewer;
	}

	public static void showMap(List<GeoPosition> track, Route route, List<POIMarker> nearby) {
		JXMapViewer mapViewer = new JXMapViewer();

		// Display the viewer in a JFrame
		JFrame frame = new JFrame("RobAuto RV Trip Planner");
		MainPanel mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout() );
		mainPanel.getRightTabbedPane().addTab("Planner", null, mapViewer, null);
		WebBrowser browserCanvas = WebBrowser.factory(mainPanel);
		mainPanel.getLeftPanel().add( new ControlPanel() );
		frame.setContentPane(mainPanel);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);

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
					System.out.println("X:" + geo.getLatitude() + ",Y:" + geo.getLongitude());
				}
			}
		});
		mapViewer.addPropertyChangeListener("zoom", new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				Integer zoom = (Integer) event.getNewValue();
				System.out.println(zoom + " " + zoomMetersPerPixel[zoom] *  mapViewer.getBounds().getWidth() );
			}
			
		});
		mapViewer.setCenterPosition( new GeoPosition(28, -81));
		mapViewer.setZoom(10);

//		RoutePainter routePainter = new RoutePainter(track);
//
//		// Set the focus
//		mapViewer.zoomToBestFit(new HashSet<GeoPosition>(track), 0.9);
//
//		// Create waypoints from the geo-positions
//		ArrayList<DefaultWaypoint> waylist = new ArrayList<>();
//		for (Leg leg : route.getLeg()) {
//			waylist.add(new DefaultWaypoint(new LatLon(leg.getStart().getMappedPosition())));
//		}
//		waylist.add(new DefaultWaypoint(track.get(track.size() - 1)));
//		Set<DefaultWaypoint> waypoints = new HashSet<DefaultWaypoint>(waylist);
//
//		// Create a waypoint painter that takes all the waypoints
//		WaypointPainter<DefaultWaypoint> waypointPainter = new WaypointPainter<DefaultWaypoint>();
//		waypointPainter.setWaypoints(waypoints);
//
//		Set<POIMarker> markers = new HashSet<POIMarker>(nearby);
//
//		// Set the overlay painter
//		WaypointPainter<POIMarker> markerPainter = new POIMarkerOverlayPainter();
//		markerPainter.setWaypoints(markers);
//
//		// Create a compound painter that uses both the route-painter and the
//		// waypoint-painter
//		List<Painter<JXMapViewer>> painters = new ArrayList<Painter<JXMapViewer>>();
//		painters.add(routePainter);
//		painters.add(waypointPainter);
//		painters.add(markerPainter);
//
//		CompoundPainter<JXMapViewer> painter = new CompoundPainter<JXMapViewer>(painters);
//		mapViewer.setOverlayPainter(painter);
//		
//		// Add the JButtons to the map viewer
//		for (POIMarker w : nearby) {
//			mapViewer.add(w);
//		}
	}

}
