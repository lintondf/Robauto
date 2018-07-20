/**
 * 
 */
package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBElement;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.json.Leg;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.StopMarker;

import slash.navigation.gpx.GpxUtil;
import slash.navigation.gpx.binding11.ExtensionsType;
import slash.navigation.gpx.binding11.GpxType;
import slash.navigation.gpx.binding11.RteType;
import slash.navigation.gpx.binding11.TrkType;
import slash.navigation.gpx.binding11.WptType;
import slash.navigation.gpx.garmin3.AutoroutePointT;
import slash.navigation.gpx.garmin3.RoutePointExtensionT;

/**
 * @author lintondf 1. Use Basecamp to plan day-by-day route. Export to GPX 2.
 *         In Robauto plan driver and fuel stops.
 */
public class Garmin extends JPanel {

	// min_lon,min_lat,max_lon,max_lat.
	// https://api.openstreetmap.org/api/0.6/map?bbox=-68.20,44.00,-68.00,44.20
	protected static JFrame frame;
	protected Map map;
	protected JXMapViewer mapViewer;
	protected static GeodeticCalculator geoCalc = new GeodeticCalculator();
	protected static Ellipsoid wgs84 = Ellipsoid.WGS84;

	protected int dups = 0;
	protected int nearby = 0;

	public static class TrackPoint extends LatLon {
		public double distanceToHere;

		public TrackPoint(LatLon where) {
			super(where);
			this.distanceToHere = 0.0;
		}
		
		public String toString() {
			return super.toString() + " " + 0.001*distanceToHere;
		}
	}

	protected void add(LatLon point) {
		TrackPoint tp = new TrackPoint(point);
		if (track.isEmpty()) {
			track.add(tp);
			trackPoints.add(tp);
		} else {
			TrackPoint last = (TrackPoint) track.get(track.size() - 1);
			if (last.getLatitude() == point.getLatitude()
					&& last.getLongitude() == point.getLongitude()) {
				dups++;
				return;
			}
			GeodeticCurve curve = geoCalc.calculateGeodeticCurve(wgs84,
					new GlobalCoordinates(last), new GlobalCoordinates(point));
			tp.distanceToHere = last.distanceToHere
					+ curve.getEllipsoidalDistance();
			track.add(tp);
			trackPoints.add(tp);
		}
	}
	
	public ArrayList<ButtonWaypoint> waylist;
	public List<GeoPosition> track = new ArrayList<>();
	public List<TrackPoint> trackPoints = new ArrayList<>();

	public Garmin(String path) {
		try {
			FileReader fir = new FileReader(new File(path));
			GpxType gpx = GpxUtil.unmarshal11(fir);
			List<RteType> rtes = gpx.getRte();
			
			waylist = new ArrayList<>();
			int kind = StopMarker.ORIGIN;
			String text = "";

			for (RteType rte : rtes) {
				System.out.println("RTE: " + rte.getName());
				List<WptType> rtepts = rte.getRtept();
				for (WptType rtept : rtepts) {
					System.out.println("RTEPT: " + rtept.getName() + " "
							+ rtept.getCmt() + " " + rtept.getLat() + ","
							+ rtept.getLon());
					LatLon where = new LatLon(rtept.getLat().doubleValue(),
							rtept.getLon().doubleValue());
					add(where);
					text = rtept.getCmt();
					waylist.add(new StopMarker(kind, text, where));
					kind = StopMarker.OVERNIGHT;
					ExtensionsType extensions = rtept.getExtensions();
					List<Object> any = extensions.getAny();
					for (Object ext : any) {
						if (ext instanceof JAXBElement) {
							JAXBElement element = (JAXBElement) ext;
							System.out.println("JAXBElement: "
									+ element.getName() + " : "
									+ element.getValue());
							switch (element.getName().toString()) {
							case "{http://www.garmin.com/xmlschemas/GpxExtensions/v3}RoutePointExtension":
								RoutePointExtensionT rpext = (RoutePointExtensionT) element
										.getValue();
								System.out.println("AutoroutePointT #"
										+ rpext.getRpt().size());
								List<AutoroutePointT> points = rpext.getRpt();
								for (AutoroutePointT point : points) {
									where = new LatLon(point.getLat()
											.doubleValue(), point.getLon()
											.doubleValue());
									add(where);
								}
								break;
							default:
								break;
							}
						} else {
							System.out.println("UNKNOWN: " + ext);
						}
					}
				}
			}
		} catch (Exception x) {
			x.printStackTrace();
		}

	}

	public Garmin() {
		setLayout(new BorderLayout());
		map = new com.bluelightning.Map();
		mapViewer = map.getMapViewer();
		this.add(mapViewer, BorderLayout.CENTER);
		String path = "/Users/lintondf/basecamp.GPX";
		try {
			FileReader fir = new FileReader(new File(path));
			GpxType gpx = GpxUtil.unmarshal11(fir);
			System.out.println(gpx);
			// List<WptType> wpts = gpx.getWpt();
			// for (WptType wpt : wpts) {
			// System.out.println("WPT: " + wpt.getName() + " " + wpt.getCmt());
			// }
			List<RteType> rtes = gpx.getRte();
			waylist = new ArrayList<>();
			int kind = StopMarker.ORIGIN;
			String text = "";

			for (RteType rte : rtes) {
				if (rte.getName().startsWith("Maine Home to")) {
					System.out.println("RTE: " + rte.getName());
					List<WptType> rtepts = rte.getRtept();
					for (WptType rtept : rtepts) {
						System.out.println("RTEPT: " + rtept.getName() + " "
								+ rtept.getCmt() + " " + rtept.getLat() + ","
								+ rtept.getLon());
						LatLon where = new LatLon(rtept.getLat().doubleValue(),
								rtept.getLon().doubleValue());
						add(where);
						text = rtept.getCmt();
						waylist.add(new StopMarker(kind, text, where));
						kind = StopMarker.OVERNIGHT;
						ExtensionsType extensions = rtept.getExtensions();
						List<Object> any = extensions.getAny();
						for (Object ext : any) {
							if (ext instanceof JAXBElement) {
								JAXBElement element = (JAXBElement) ext;
								System.out.println("JAXBElement: "
										+ element.getName() + " : "
										+ element.getValue());
								switch (element.getName().toString()) {
								case "{http://www.garmin.com/xmlschemas/GpxExtensions/v3}RoutePointExtension":
									RoutePointExtensionT rpext = (RoutePointExtensionT) element
											.getValue();
									System.out.println("AutoroutePointT #"
											+ rpext.getRpt().size());
									List<AutoroutePointT> points = rpext
											.getRpt();
									for (AutoroutePointT point : points) {
										where = new LatLon(point.getLat()
												.doubleValue(), point.getLon()
												.doubleValue());
										add(where);
									}
									break;
								default:
									break;
								}
							} else {
								System.out.println("UNKNOWN: " + ext);
							}
						}
					}
				}
			}
			if (!track.isEmpty()) {
				// validateTrack( track );
				waylist.add(new StopMarker(StopMarker.TERMINUS, text, track
						.get(track.size() - 1)));
				System.out.println(track.size() + " track points; " + dups
						+ " " + nearby);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						ArrayList< List<GeoPosition> > tracks = new ArrayList<>();
						tracks.add(track);
						map.show(tracks, waylist);
					}
				});
			}
			// List<TrkType> trks = gpx.getTrk();
			// for (TrkType trk : trks) {
			// System.out.println( "TRK: " + trk.getName() );
			// }
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	private void validateTrack(List<GeoPosition> track) {
		for (GeoPosition where : track) {
			if (where.getLatitude() < 30.0 || where.getLatitude() > 50.0
					|| where.getLongitude() < -80.0
					|| where.getLongitude() > -60) {
				System.out.println(where);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frame = new JFrame();
					frame.setTitle("Robauto - Garmin");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					Image icon = ImageIO.read(
							new File("images/icon-travel.jpg"))
							.getScaledInstance((int) 64, (int) 64,
									Image.SCALE_SMOOTH);
					frame.setIconImage(icon);

					Garmin garmin = new Garmin();

					frame.setContentPane(garmin);
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					SwingUtilities.updateComponentTreeUI(frame);
					frame.pack();
					frame.setVisible(true);
					frame.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosing(WindowEvent e) {
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
