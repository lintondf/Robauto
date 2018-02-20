package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.io.IOUtils;

import com.bluelightning.json.HereRoute;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POISet;
import com.bluelightning.poi.RestAreaPOI;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;


public class OptimizeStopsDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
	
	public static class RoadDirectionData implements Comparable<RoadDirectionData> {
		public String road;
		public String direction;
		
		public String toString() {
			return String.format("%s %s", road, direction );
		}

		@Override
		public int compareTo(RoadDirectionData that) {
			return road.compareTo(that.road);
		}
	}
	
	public static class StopData {
		public Boolean  use;
		public Double   distance;
		public Double   trafficTime;
		public String   road;
		public String   state;
		public String   mileMarker;
		public String   direction;
		public String   name;
		public Double[] driveTimes;
		
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (use != null) {
				sb.append( use.toString() ); sb.append(",");
			}
			sb.append(String.format("%5.1f, %5s,", distance*Here2.METERS_TO_MILES, Here2.toPeriod(trafficTime)));
			sb.append(road);
			sb.append('/');
			sb.append(state);
			sb.append(":");
			sb.append(mileMarker);
			sb.append(",");
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

	protected final OptimizeStops optimizeStops;
	private final JPanel contentPanel = new JPanel();
	private JTable roadsTable;
	private JTable legTable;
	private JTable stopsTable;
	protected JTextPane textPane;
	protected JTabbedPane outputTabbedPane;
	protected LegTableModel legTableModel;
	protected RoadTableModel roadTableModel;
	protected StopsTableModel stopsTableModel;
	protected JButton optimizeButton;
	protected JButton chooseButton;
	protected JButton cancelButton;
	
	protected class OptimizeActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			System.out.println(event);
			switch (event.getActionCommand()) {
			case "Cancel":
				OptimizeStopsDialog.this.dispose();
				break;
			default:
				break;
			}
		}
		
	}
	
	protected class OptimizeLegSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (! event.getValueIsAdjusting()) {
				System.out.println(event);
				int which = event.getLastIndex();
				List<RoadDirectionData> roadDirectionDataList = optimizeStops.getUiRoadData(which);
				roadDirectionDataList.forEach(System.out::println);
				List<StopData> stopDataList = optimizeStops.getUiStopData(2, which, false, roadDirectionDataList);
				stopDataList.forEach(System.out::println);
				
				setRoadDirectionTable(roadDirectionDataList);
				setStopTable(stopDataList);				
			}
		}
		
	}

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
			
		try {
			OptimizeStopsDialog dialog = new OptimizeStopsDialog(optimizeStops);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);

			dialog.setLegTable( legDataList );
			dialog.setRoadDirectionTable(roadDirectionDataList);
			dialog.setStopTable(stopDataList);
			
			dialog.addListeners(
					dialog.new OptimizeActionListener(),
					dialog.new OptimizeLegSelectionListener()
					);
			
			for (int i = 0; it.hasNext() && i < 5; i++) {
				String html = toHtml(2, legDataList.get(i), it.next() );
				if (i==0) try {
					PrintWriter out = new PrintWriter("report.html");
					out.println(html);
					out.close();
				} catch (Exception x) {
					x.printStackTrace();
				}				
				JPanel panel = new JPanel();
				dialog.getOutputTabbedPane().addTab(String.format("Case %d", 1+i), null, panel, null);
				dialog.getOutputTabbedPane().setEnabledAt(i, true);
				panel.setLayout(new BorderLayout(0, 0));
				{
					JTextPane textPane = new JTextPane();
					textPane.setContentType("text/html");
					textPane.setText(html);
					panel.add(textPane);
				}
			} // for i
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void addListeners(OptimizeActionListener optimizeActionListener,
			OptimizeLegSelectionListener optimizeLegSelectionListener) {
		
		cancelButton.addActionListener(optimizeActionListener);
		optimizeButton.addActionListener(optimizeActionListener);
		chooseButton.addActionListener(optimizeActionListener);
		
		legTable.getSelectionModel().addListSelectionListener(optimizeLegSelectionListener);
	}

	protected static String toHtml( int nDrivers, LegData legData, DriverAssignments driverAssignments ) {
		Report report = new Report();
		report.depart(legData.startLabel, "", 0.0);
		
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
		report.arrive(0.0, legData.endLabel, "");
		return report.toHtml();		
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

	protected static class LegTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Start", "End", "Length (mi)", "Time (hr:mm)"};
		
		protected List<LegData> data = null;
		
		
		public LegTableModel() {
		}

		@Override
		public int getColumnCount() {
			return names.length;
		}

		@Override
		public int getRowCount() {
			if (data == null)
				return 0;
			return data.size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (data == null || iRow >= data.size())
				return null;
			LegData legData = data.get(iRow);
			switch (iCol) {
			case 0: 
				return legData.startLabel;
			case 1:
				return legData.endLabel;
			case 2:
				return String.format("%5.1f", legData.distance*Here2.METERS_TO_MILES);
			case 3:
				return Here2.toPeriod(legData.trafficTime);
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int iCol) {
			return String.class;
		}

		@Override
		public String getColumnName(int iCol) {
			return names[iCol];
		}

		public List<LegData> getData() {
			return data;
		}

		public void setData(List<LegData> data) {
			this.data = data;
			fireTableStructureChanged();
		}
		
	}
	
	
	protected static class RoadTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Name", "Direction"};
		
		protected List<RoadDirectionData> data = null;
		
		
		public RoadTableModel() {
		}

		@Override
		public int getColumnCount() {
			return names.length;
		}

		@Override
		public int getRowCount() {
			if (data == null)
				return 0;
			return data.size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (data == null || iRow >= data.size())
				return null;
			RoadDirectionData roadData = data.get(iRow);
			switch (iCol) {
			case 0: 
				return roadData.road;
			case 1:
				return roadData.direction;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int iCol) {
			return String.class;
		}

		@Override
		public String getColumnName(int iCol) {
			return names[iCol];
		}

		public List<RoadDirectionData> getData() {
			return data;
		}

		public void setData(List<RoadDirectionData> data) {
			this.data = data;
			fireTableStructureChanged();
		}
		
	}
	
	
	protected static class StopsTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Use", "Distance", "Time", "Road", "Direction", "Name"};
		protected static final double[] widths = {0.05, 0.1, 0.1, 0.25, 0.10, 0.40};
		protected static final boolean[] centered = {true, true, true, true, true, false};
		
		protected List<StopData> data = null;
		
		
		public StopsTableModel() {
		}

		@Override
		public int getColumnCount() {
			return names.length;
		}

		@Override
		public int getRowCount() {
			if (data == null)
				return 0;
			return data.size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (data == null || iRow >= data.size())
				return null;
			StopData stopData = data.get(iRow);
			switch (iCol) {
			case 0: 
				return stopData.use;
			case 1:
				return String.format("%5.1f", stopData.distance*Here2.METERS_TO_MILES);
			case 2:
				return Here2.toPeriod(stopData.trafficTime);
			case 3:
				return String.format("%s %s %s", stopData.road, stopData.state, stopData.mileMarker); 
			case 4:
				return stopData.direction;
			case 5:
				return stopData.name;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int iCol) {
			if (iCol == 0)
				return Boolean.class;
			return String.class;
		}

		@Override
		public String getColumnName(int iCol) {
			return names[iCol];
		}

		public List<StopData> getData() {
			return data;
		}

		public void setData(List<StopData> data) {
			this.data = data;
			fireTableStructureChanged();
		}
		
		public void resizeColumns( JTable table, double totalWidth) {
			TableColumn column = null;
			for (int i = 0; i < getColumnCount(); i++) {
			    column = table.getColumnModel().getColumn(i);
			    int width = (int) (widths[i]*totalWidth);
		        column.setPreferredWidth(width);
			}			
		}

		public void layoutColumns(JTable stopsTable) {
			// TODO make this work
			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );
			for (int i = 0; i < centered.length; i++) {
				if (centered[i])
					stopsTable.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
			}
		}
		
	}
	
	
	public void setLegTable( List<LegData> legDataList ) {
		legTableModel.setData(legDataList);
		if (! legDataList.isEmpty()) {
		}
	}
	
	public void setRoadDirectionTable( List<RoadDirectionData> roadDataList ) {
		roadTableModel.setData(roadDataList);
	}

	public void setStopTable( List<StopData> stopDataList ) {
		stopsTableModel.setData(stopDataList);
	}

	/**
	 * Create the dialog.
	 * @param optimizeStops 
	 */
	public OptimizeStopsDialog(OptimizeStops optimizeStops) {
		this.optimizeStops = optimizeStops;
		legTableModel = new LegTableModel();
		roadTableModel = new RoadTableModel();
		stopsTableModel = new StopsTableModel();
		setBounds(100, 100, 900, 600);
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
					upperPanel.setLayout(new GridLayout(0, 2, 0, 0));
					inputSplitPane.setLeftComponent(upperPanel);
					{
						legTable = new JTable(legTableModel);
						legTable.setBorder(new LineBorder(Color.BLACK, 1));
						legTable.setFillsViewportHeight(true);
						legTable.setVisible(true);
						JScrollPane scrollPane = new JScrollPane(legTable);
						upperPanel.add(scrollPane);
					}
					{
						roadsTable = new JTable(roadTableModel);
						roadsTable.setBorder(new LineBorder(Color.BLACK, 1));
						roadsTable.setFillsViewportHeight(true);
						roadsTable.setVisible(true);
						JScrollPane scrollPane = new JScrollPane(roadsTable);
						upperPanel.add(scrollPane);
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
						stopsTable = new JTable(stopsTableModel);
						stopsTable.setBorder(new LineBorder(Color.BLACK, 1));
						stopsTable.setFillsViewportHeight(true);
						stopsTable.setVisible(true);
						JScrollPane scrollPane = new JScrollPane(stopsTable);
						lowerPanel.add(scrollPane, BorderLayout.CENTER);
					}
				}
			}
			{
				JPanel outputPanel = new JPanel();
				tabbedPane.addTab("Output", null, outputPanel, null);
				outputPanel.setLayout(new BorderLayout(0, 0));
				{
					outputTabbedPane = new JTabbedPane(JTabbedPane.TOP);
					outputPanel.add(outputTabbedPane, BorderLayout.NORTH);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				optimizeButton = new JButton("Optimize");
				optimizeButton.setActionCommand("Optimize");
				buttonPane.add(optimizeButton);
//				getRootPane().setDefaultButton(okButton);
			}
			{
				chooseButton = new JButton("Choose");
				chooseButton.setActionCommand("Choose");
				buttonPane.add(chooseButton);
				getRootPane().setDefaultButton(chooseButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		stopsTableModel.layoutColumns( stopsTable );
		stopsTable.addComponentListener( new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent arg0) {
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentResized(ComponentEvent event) {
				Dimension dim = event.getComponent().getSize();
				stopsTableModel.resizeColumns(stopsTable, dim.getWidth());
			}

			@Override
			public void componentShown(ComponentEvent event) {
				componentResized(event);
			}
			
		});
		
	}

	public JTable getRoadsTable() {
		return roadsTable;
	}

	public JTable getStopsTable() {
		return stopsTable;
	}

	public JTabbedPane getOutputTabbedPane() {
		return outputTabbedPane;
	}

}
