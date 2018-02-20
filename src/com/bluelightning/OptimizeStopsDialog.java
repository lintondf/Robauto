package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.apache.commons.io.IOUtils;

import com.bluelightning.OptimizeStopsDialog.DriverAssignments.Assignment;
import com.bluelightning.OptimizeStopsDialog.LegData;
import com.bluelightning.OptimizeStopsDialog.RoadDirectionData;
import com.bluelightning.OptimizeStopsDialog.StopData;
import com.bluelightning.json.HereRoute;
import com.bluelightning.json.Route;
import com.bluelightning.map.ButtonWaypoint;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.RestAreaPOI;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;


public class OptimizeStopsDialog extends JDialog {
	
	public static class LegData {
		public String startLabel;
		public String endLabel;
		public Double distance;
		public Double trafficTime;
		
		public String toString() {
			return String.format("%5.1f %5s; from %s to %s", 
					distance*Here2.METERS_TO_MILES, Here2.toPeriod(trafficTime), startLabel, endLabel);
		}
	}
	
	public static class RoadDirectionData {
		public String road;
		public String direction;
		
		public String toString() {
			return String.format("%s %s", road, direction );
		}
	}
	
	public static class StopData {
		public Boolean  use;
		public Double   distance;
		public Double   trafficTime;
		public String   road;
		public String   direction;
		public String   name;
		public Double[] driveTimes;
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (use != null) {
				sb.append( use.toString() ); sb.append(",");
			}
			sb.append(String.format("%5.1f, %5s,", distance*Here2.METERS_TO_MILES, Here2.toPeriod(trafficTime)));
			sb.append(road); sb.append(",");
			sb.append(direction); sb.append(",");
			sb.append(name); sb.append(",");
			if (driveTimes != null) {
				sb.append('[');
				for (Double d : driveTimes) {
					if (d != null)
						sb.append(Here2.toPeriod(d.doubleValue()));
					sb.append(",");
				}
				sb.append(']');
			}
			return sb.toString();
		}
	}
	
	
	public static class DriverAssignments implements Comparable<DriverAssignments> {
		
		public static class Assignment {
			List<StopData>   stops;
			List<Double>     driveTimes;
		}
		
		double        driveImbalance;
		double[]      totalDriveTimes;
		Assignment[]  assignments;
		double        score;
		
		public DriverAssignments( int nDrivers ) {
			totalDriveTimes = new double[nDrivers];
			assignments = new Assignment[nDrivers];
			for (int i = 0; i < nDrivers; i++) {
				assignments[i] = new Assignment();
				assignments[i].stops = new ArrayList<>();
				assignments[i].driveTimes = new ArrayList<>();
			}
		}
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append( String.format("%.1f, ", score) );
			sb.append(String.format("%s, ", Here2.toPeriod(driveImbalance)));
			sb.append('[');
			for (double d : totalDriveTimes) {
				sb.append(String.format("%s,", Here2.toPeriod(d)));
			}
			sb.append(']');
			sb.append("  (");
			for (Assignment a : assignments) {
				sb.append('[');
				for (double d : a.driveTimes) {
					sb.append(String.format("%s,", Here2.toPeriod(d)));
				}
				sb.append(']');
			}
			sb.append(")");
			return sb.toString();
		}

		@Override
		public int compareTo(DriverAssignments that) {
			driveImbalance = Math.abs( totalDriveTimes[0] - totalDriveTimes[1] ); 
			return (int) (score - that.score);
		}
		
	}

	private final JPanel contentPanel = new JPanel();
	private JTable roadsTable;
	private JTable legTable;
	private JTable stopsTable;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		HereRoute hereRoute = null;
		try {
			String json = IOUtils.toString(new FileInputStream("route.json"), "UTF-8");
			hereRoute = (HereRoute) Here2.gson.fromJson(json, HereRoute.class);
			if (hereRoute == null || hereRoute.getResponse() == null)
				throw new NullPointerException();
		} catch (Exception x) {
			x.printStackTrace();
			return;
		}
		System.out.println(hereRoute.getResponse().getMetaInfo());
		Set<Route> routes = hereRoute.getResponse().getRoute();
		Route route = routes.iterator().next();

		EnumMap<Main.MarkerKinds, POISet> poiMap = new EnumMap<>(Main.MarkerKinds.class);
		EnumMap<Main.MarkerKinds, ArrayList<POISet.POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
		ArrayList<ButtonWaypoint> nearby = new ArrayList<>();
		
		POISet pset = RestAreaPOI.factory();
		poiMap.put(Main.MarkerKinds.RESTAREAS, pset);
		nearbyMap.put(Main.MarkerKinds.RESTAREAS, pset.getPointsOfInterestAlongRoute(route, 5e3 ));
		
		OptimizeStops optimizeStops = new OptimizeStops( route, nearbyMap );
		
		List<LegData> legDataList = optimizeStops.getUiLegData();
		legDataList.forEach(System.out::println);
		
		List<RoadDirectionData> roadDirectionDataList = optimizeStops.getUiRoadData(0);
		roadDirectionDataList.forEach(System.out::println);
		
		List<StopData> stopDataList = optimizeStops.getUiStopData(2, 0, false, roadDirectionDataList);
		stopDataList.forEach(System.out::println);
		
		Permutations perm = new Permutations( stopDataList.size() );
		ArrayList<Integer[]> unique = perm.monotonic();
		Set<DriverAssignments> driverAssignments = new TreeSet<>();
		for (Integer[] elements : unique) {
			driverAssignments.add( generateDriverAssignments(2, legDataList.get(0), stopDataList, elements) );
		}
		Iterator<DriverAssignments> it = driverAssignments.iterator();
		
		String css = "";;
		try {
			css = IOUtils.toString(new FileInputStream("themes/style.css"));
		} catch (Exception x) {}

		String html = toHtml(2, legDataList.get(0), it.next(), css );
		try {
			PrintWriter out = new PrintWriter("report.html");
			out.println(html);
			out.close();
		} catch (Exception x) {
			x.printStackTrace();
		}
		
		try {
			OptimizeStopsDialog dialog = new OptimizeStopsDialog(html );
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected static String toHtml( int nDrivers, LegData legData, DriverAssignments driverAssignments, String css ) {
		Report report = new Report();
		report.depart("", legData.startLabel, 0.0);
		
		List<Iterator<Double>> driveTimes = new ArrayList<Iterator<Double>>( Arrays.asList(
				driverAssignments.assignments[0].driveTimes.iterator(),
				driverAssignments.assignments[1].driveTimes.iterator() ) );
		List<Iterator<StopData>>  stops = new ArrayList<Iterator<StopData>>( Arrays.asList(
				driverAssignments.assignments[0].stops.iterator(),
				driverAssignments.assignments[1].stops.iterator() ) );
		
		int driver = 0;
		double lastDistance = 0.0;
		while (stops.get(driver).hasNext()) {
			StopData stopData = stops.get(driver).next();
			Double   driveTime = driveTimes.get(driver).next();
			int hours = (int) (driveTime / 3600.0);
			int minutes = ((int) (driveTime / 60.0)) % 60;
			double stepDistance = stopData.distance - lastDistance;
			lastDistance = stopData.distance;
			report.drive(driver, hours, minutes, stepDistance*Here2.METERS_TO_MILES);
			report.stop(stopData.name, stopData.road, 0);
			driver = (driver + 1) % nDrivers;
		}
		
		report.stop("",	legData.endLabel, 0.0);
		report.arrive(0.0);
		return report.toHtml(css);		
	}
	
	protected static double scoreTime( double time ) {
		//https://en.wikipedia.org/wiki/Logistic_function
		final double MIN_TIME = 45;
		final double MAX_TIME = 120;
		final double L = 1000;
		final double k = 0.5;
		
		time /= 60;  // to minutes
		// =$B$4/(1+EXP(-$C$4*(A5-$B$2)))
		double upper = L / (1.0 + Math.exp(-k*(time - MAX_TIME))); 
		// =$B$4/(1+EXP(-$C$4*($B$1-A5)))
		double lower = L / (1.0 + Math.exp(-k*(MIN_TIME - time))); 
		return 1 + upper + lower;
	}

	protected static DriverAssignments generateDriverAssignments(int nDrivers, LegData legData, List<StopData> stopDataList,
			Integer[] elements) {
		ArrayList<StopData> sublist = new ArrayList<>();
		for (Integer i : elements) {
			sublist.add( stopDataList.get(i.intValue()));
		}
		StopData legEnd = new StopData();
		legEnd.distance = legData.distance;
		legEnd.trafficTime = legData.trafficTime;
		sublist.add(legEnd);
		DriverAssignments driverAssignments = new DriverAssignments(nDrivers);
		int driver = 0;
		double lastTime = 0.0;
		double score = 0.0;
		for (StopData stopData : sublist) {
			driverAssignments.assignments[driver].stops.add( stopData );
			double dTime = stopData.trafficTime - lastTime;
			driverAssignments.assignments[driver].driveTimes.add( dTime );
			driverAssignments.totalDriveTimes[driver] += dTime;
			score += scoreTime( dTime );
			lastTime = stopData.trafficTime;
			driver = (driver + 1) % nDrivers;
		}
		driverAssignments.driveImbalance = Math.abs(driverAssignments.totalDriveTimes[1] - driverAssignments.totalDriveTimes[0] );
		driverAssignments.score = score + driverAssignments.driveImbalance;
		//System.out.println(driverAssignments);
		return driverAssignments;
	}

	/**
	 * Create the dialog.
	 */
	public OptimizeStopsDialog(String html) {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				JSplitPane inputSplitPane = new JSplitPane();
				tabbedPane.addTab("Input", null, inputSplitPane, null);
				inputSplitPane.setResizeWeight(0.5);
				inputSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
				{
					JPanel upperPanel = new JPanel();
					inputSplitPane.setLeftComponent(upperPanel);
					upperPanel.setLayout(new BorderLayout(0, 0));
					{
						legTable = new JTable();
						legTable.setFillsViewportHeight(true);
						upperPanel.add(legTable);
					}
					{
						roadsTable = new JTable();
						roadsTable.setFillsViewportHeight(true);
						upperPanel.add(roadsTable);
					}
				}
				{
					JPanel lowerPanel = new JPanel();
					inputSplitPane.setRightComponent(lowerPanel);
					lowerPanel.setLayout(new BorderLayout(0, 0));
					{
						JScrollPane scrollPane = new JScrollPane();
						lowerPanel.add(scrollPane, BorderLayout.WEST);
					}
					{
						stopsTable = new JTable();
						lowerPanel.add(stopsTable, BorderLayout.CENTER);
					}
				}
			}
			{
				JPanel outputPanel = new JPanel();
				tabbedPane.addTab("Output", null, outputPanel, null);
				outputPanel.setLayout(new BorderLayout(0, 0));
				{
					JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
					outputPanel.add(tabbedPane_1, BorderLayout.NORTH);
					{
						JPanel panel = new JPanel();
						tabbedPane_1.addTab("Case 1", null, panel, null);
						tabbedPane_1.setEnabledAt(0, true);
						panel.setLayout(new BorderLayout(0, 0));
						{
							JTextPane textPane = new JTextPane();
							textPane.setContentType("text/html");
							textPane.setText(html);
							// create a document, set it on the jeditorpane, then add the html
							panel.add(textPane);
						}
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
