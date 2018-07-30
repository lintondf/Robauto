/**
 * 
 */
package com.bluelightning;

import static slash.common.helpers.JAXBHelper.newContext;
import static slash.common.helpers.JAXBHelper.newMarshaller;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;

import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GeodeticCurve;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.w3c.dom.Element;

import com.bluelightning.json.Leg;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.map.StopMarker;
import com.bluelightning.poi.POI;

import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.place.Place;
import seedu.addressbook.data.place.VisitedPlace;
import seedu.addressbook.logic.Logic;
import slash.navigation.gpx.GpxUtil;
import slash.navigation.gpx.binding10.Gpx.Wpt;
import slash.navigation.gpx.binding11.ExtensionsType;
import slash.navigation.gpx.binding11.GpxType;
import slash.navigation.gpx.binding11.RteType;
import slash.navigation.gpx.binding11.TrkType;
import slash.navigation.gpx.binding11.WptType;
import slash.navigation.gpx.garmin3.AddressT;
import slash.navigation.gpx.garmin3.AutoroutePointT;
import slash.navigation.gpx.garmin3.DisplayModeT;
import slash.navigation.gpx.garmin3.ExtensionsT;
import slash.navigation.gpx.garmin3.RoutePointExtensionT;
import slash.navigation.gpx.garmin3.WaypointExtensionT;

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
		public double distancePriorToHere;
		public double distanceStartToHere;
		public double heading;

		public TrackPoint(LatLon where) {
			super(where);
			this.distanceStartToHere = 0.0;
			this.distancePriorToHere = 0.0;
			this.heading = 0.0;
		}
		
		public String toString() {
			return String.format("%s; %7.3f km; %5.0f deg; %6.3f km", super.toString(), 0.001*distanceStartToHere, heading, 0.001*this.distancePriorToHere );
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
			tp.distancePriorToHere = curve.getEllipsoidalDistance();
			tp.distanceStartToHere = last.distanceStartToHere
					+ curve.getEllipsoidalDistance();
			tp.heading = curve.getAzimuth();
			track.add(tp);
			trackPoints.add(tp);
		}
	}
	
	public ArrayList<ButtonWaypoint> waylist;
	public List<GeoPosition> track = new ArrayList<>();
	public List<TrackPoint> trackPoints = new ArrayList<>();
	
	static JAXBContext context11 =
        newContext(slash.navigation.gpx.binding11.ObjectFactory.class,
                slash.navigation.gpx.garmin3.ObjectFactory.class,
                slash.navigation.gpx.trackpoint1.ObjectFactory.class,
                slash.navigation.gpx.trackpoint2.ObjectFactory.class,
                slash.navigation.gpx.trip1.ObjectFactory.class);
	static Marshaller marshaller = newMarshaller(context11);
	
	/*
		boolean passThru;
	boolean overnight;
	boolean driverSwitch;
	POI.FuelAvailable fuelAvailable;
	double  fuel;            // amount to purchase in gallons; zero if none

          Overnight Drivers Refuel
             N         N      0       0 Flag, Blue
             Y         N      0       4 Flag, Red
             N         Y      0       2 Navaid, White
             Y         Y      0       6 Navaid, Red/White
             N         N     >0       1 Navaid, Green (inconceivable)
             Y         N     >0       5 Navaid, Red/Green
             N         Y     >0       3 Navaid, White/Green
             Y         Y     >0       7 Navaid, Red/Green (inconceivable)
	 */
	public static boolean writeToGPX( List<VisitedPlace> places, String path ) {
		final String[] syms = {
				"Flag, Blue",
				"Navaid, Green",
				"Navaid, White",
				"Navaid, White/Green",
				"Flag, Red",
				"Navaid, Red/Green",
				"Navaid, Red/White",
				"Navaid, Red/Green"
		};
		try {
			FileWriter fiw = new FileWriter( new File(path) );
			GpxType gpx = new GpxType();
			gpx.setVersion("1.1"); // REQUIRED

			for (VisitedPlace place : places) {
				WptType w = new WptType();
				w.setLat(new BigDecimal( place.getLatitude().doubleValue()) );
				w.setLon(new BigDecimal( place.getLongitude().doubleValue()) );
				w.setCmt( place.getAddress().toString() );
				String name = String.format("%s, %s, %s", place.getName(), place.getAddress().getCity(), place.getAddress().getState() );
				w.setName( name );
				w.setDesc( place.getName().toString() );
				int index = (place.getFuel() > 0.0) ? 1 : 0;
				index += (place.isDriverSwitch()) ? 2 : 0;
				index += (place.isOvernight()) ? 4 : 0;
				System.out.println( index + " " + syms[index]);
				w.setSym(syms[index]);
				w.setType("user");
//				ExtensionsType extensions = new ExtensionsType();
//				w.setExtensions(extensions);
				gpx.getWpt().add(w);
			}
			marshaller.marshal(new slash.navigation.gpx.binding11.ObjectFactory().createGpx(gpx), fiw);
			return true;
		} catch (Exception x) {
			x.printStackTrace();
			return false;
		}		
	}
	
	public static boolean writeToGPX( AddressBook addressBook, String path ) {
		try {
			FileWriter fiw = new FileWriter( new File(path) );
			GpxType gpx = new GpxType();
			gpx.setVersion("1.1"); // REQUIRED

			for (Place place : addressBook.getAllPlaces()) {
				WptType w = new WptType();
				w.setLat(new BigDecimal( place.getLatitude().doubleValue()) );
				w.setLon(new BigDecimal( place.getLongitude().doubleValue()) );
				w.setCmt( place.getAddress().toString() );
				String name = String.format("%s, %s, %s", place.getName(), place.getAddress().getCity(), place.getAddress().getState() );
				w.setName( name );
				w.setDesc( place.getName().toString() );
				w.setSym("Flag, Red");
				w.setType("user");
//				ExtensionsType extensions = new ExtensionsType();
//				w.setExtensions(extensions);
				gpx.getWpt().add(w);
			}
			marshaller.marshal(new slash.navigation.gpx.binding11.ObjectFactory().createGpx(gpx), fiw);
			return true;
		} catch (Exception x) {
			x.printStackTrace();
			return false;
		}
	}
	
	public List<WptType> wpts;

	public Garmin(String path) {
		try {
			FileReader fir = new FileReader(new File(path));
			GpxType gpx = GpxUtil.unmarshal11(fir);
			List<RteType> rtes = gpx.getRte();
			wpts = gpx.getWpt();
			
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
		Logic controller;
		AddressBook addressBook;

		try {
			controller = new Logic();
			controller.getStorage().load();
			addressBook = controller.getAddressBook();
			FileReader fir = new FileReader(new File("/Users/lintondf/wpt.GPX"));
			GpxType gpx = GpxUtil.unmarshal11(fir);

			writeToGPX( addressBook, "/Users/lintondf/addressBook.GPX");
		} catch (Exception x) {
			RobautoMain.logger.trace(x.getMessage());
		}

	}

}
