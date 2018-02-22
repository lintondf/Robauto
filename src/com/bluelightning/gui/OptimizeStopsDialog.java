package com.bluelightning.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.jxmapviewer.viewer.GeoPosition;

import com.bluelightning.Events;
import com.bluelightning.Here2;
import com.bluelightning.Main;
import com.bluelightning.OptimizeStops;
import com.bluelightning.Permutations;
import com.bluelightning.Report;
import com.bluelightning.Events.AddManualStopEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.OptimizeStops.LegSummary;
import com.bluelightning.gui.OptimizeStopsDialog.StopData;
import com.bluelightning.json.HereRoute;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;
import com.google.common.eventbus.Subscribe;
import com.bluelightning.poi.RestAreaPOI;
import javax.swing.JTabbedPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Color;
import java.awt.Component;
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
		public Double   totalDistance;
		public String   road;
		public String   state;
		public String   mileMarker;
		public String   direction;
		public String   name;
		public Double[] driveTimes;
		
//		public StopData() {
//		}
//
		public StopData(LegData legData) {
			this.use = null;
			this.direction = "";
			this.road = "";
			this.state = "";
			this.mileMarker = "";
			this.distance = legData.distance;
			this.trafficTime = legData.trafficTime;	
			this.totalDistance = this.distance;
		}

		public StopData(POIResult result) {
			this.use = true;
			this.distance = result.legProgress.distance;
			this.trafficTime = result.legProgress.trafficTime;
			this.totalDistance = result.totalProgress.distance;
			if (result.poi instanceof RestAreaPOI) {
				RestAreaPOI restArea = (RestAreaPOI) result.poi;
				this.direction = restArea.getDirection();
				this.road = restArea.getHighway();
				this.state = restArea.getState();
				this.mileMarker = restArea.getMileMarker();
				this.name = restArea.getName();
			} else {
				this.direction = "";
				this.road = result.poi.getAddress();
				this.state = "";
				this.mileMarker = "";
				this.name = result.poi.getName();
			}
		}


		public StopData(LegSummary summary) {
			this.use = null;
			this.direction = "";
			this.road = "ARRIVE";
			this.state = "";
			this.mileMarker = "";
			this.distance = summary.leg.getLength();
			this.trafficTime = summary.leg.getTrafficTime();
			this.totalDistance = summary.finish.distance;
			this.name = "";
		}

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
	protected JTabbedPane choicesTabbedPane;
	protected LegTableModel legTableModel;
	protected RoadTableModel roadTableModel;
	protected StopsTableModel stopsTableModel;
	protected JButton optimizeButton;
	protected JButton chooseButton;
	protected JButton cancelButton;
	protected JButton addBeforeButton;
	protected JButton addAfterButton;
	protected CallbackHandler handler;
	protected int currentLeg = -1;
	protected ArrayList< ArrayList<StopData> > legStopData = new ArrayList<>();   // holds potentially modified leg stop data
	protected JTabbedPane outputTabbedPane;
	protected ArrayList<DriverAssignments> driverAssignments;
	
	public class CallbackHandler {
		
		int iLeg;
		boolean addBefore;
		int iRow;
		
		public CallbackHandler( int iLeg, boolean addBefore, int iRow ) {
			this.iLeg = iLeg;
			this.addBefore = addBefore;
			this.iRow = iRow;
		}
		
		@Subscribe
		protected void handle( AddManualStopEvent event ) {
			System.out.println( addBefore + " " + iRow + " " + event.result.toReport() );
			Events.eventBus.unregister(this); // one shot
			handler = null;
			SwingUtilities.invokeLater( new Runnable() {
				@Override
				public void run() {
					StopData data = new StopData(event.result);
					ArrayList<StopData> dataList = stopsTableModel.getData();
					if (addBefore) {
						dataList.add(iRow, data);
					} else {
						dataList.add(iRow+1, data);
					}
					stopsTableModel.setData(dataList);
				}
			});
		}
	}
	
	protected void addAfter() {
		int selected = stopsTable.getSelectedRow();
		if (selected < 0)
			return;
		if (selected < stopsTableModel.getRowCount()-1) { // cant add after last row
			handler = new CallbackHandler( currentLeg, false, selected);
			double distance0 = stopsTableModel.getData().get(selected).totalDistance;
			double distance1 = stopsTableModel.getData().get(selected+1).totalDistance;
			add( handler, distance0, distance1 );
		}
	}
	
	protected void addBefore() {
		int selected = stopsTable.getSelectedRow();
		if (selected < 0)
			return;
		double distance0 = 0;
		double distance1 = stopsTableModel.getData().get(selected).totalDistance;
		if (selected > 0) {
			distance0 = stopsTableModel.getData().get(selected-1).totalDistance;
		}
		handler = new CallbackHandler( currentLeg, true, selected);
		add( handler, distance0, distance1 );
	}
	
	protected void add(CallbackHandler handler, double distance0, double distance1) {
			ArrayList<POIResult> segmentPOI = optimizeStops.getRouteSegmentPOI(distance0, distance1);
			AddManualStopDialog addDialog = new AddManualStopDialog();
			addDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			addDialog.setData(segmentPOI);
			addDialog.setVisible(true);
			Events.eventBus.register( handler );
	}
	
	protected void optimizeStops() {
		while (choicesTabbedPane.getTabCount() > 0) {
			choicesTabbedPane.removeTabAt(0);
		}
		ArrayList<StopData> stopDataList = stopsTableModel.getData();
		Permutations perm = new Permutations( stopDataList.size() );
		ArrayList<Integer[]> unique = perm.monotonic();
		driverAssignments = new ArrayList<>();
		LegData legData = legTableModel.getData().get(currentLeg);
		for (Integer[] elements : unique) {
			driverAssignments.add( generateDriverAssignments(2, legData, stopDataList, elements) );
		}
		Collections.sort(driverAssignments);
		Iterator<DriverAssignments> it = driverAssignments.iterator();
		
		for (int i = 0; it.hasNext() && i < 5; i++) {
			String html = toHtml(2, legData, it.next() );
			JPanel panel = new JPanel();
			getOutputTabbedPane().addTab(String.format("Case %d", 1+i), null, panel, null);
			getOutputTabbedPane().setEnabledAt(i, true);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JTextPane textPane = new JTextPane();
				textPane.setContentType("text/html");
				textPane.setText(html);
				panel.add(textPane);
			}
		} // for i
	}
	
	protected void commitStops() {
		for (ArrayList<StopData> stopList : legStopData ) {
			stopList.forEach( System.out::println );
		}
	}
	
	protected void chooseCurrentTab() {
		// TODO not this way; build output tab from saved data for all legs; need StopData constructors
		int selected = choicesTabbedPane.getSelectedIndex();
		if (selected >= 0) {
			System.out.println( driverAssignments.get(selected) );
			JPanel pane = new JPanel();
			outputTabbedPane.addTab(String.format("Leg %d", currentLeg), pane);
		}
	}
	
	protected class OptimizeActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			System.out.println(event);
			switch (event.getActionCommand()) {
			case "Cancel":
				if (handler != null) {
					Events.eventBus.unregister(handler);
					handler = null;
				}
				OptimizeStopsDialog.this.dispose();
				break;
			case "Add Before":
				addBefore();
				break;
			case "Add After":
				addAfter();
				break;
			case "Optimize":
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						optimizeStops();
					}
				});
				break;
			case "Choose":
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						chooseCurrentTab();
						//commitStops();
					}
				});
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
				if (currentLeg >= 0) {
					legStopData.set(currentLeg, getStopTable() );
				}
				currentLeg = event.getLastIndex();
				List<RoadDirectionData> roadDirectionDataList = optimizeStops.getUiRoadData(currentLeg);
				roadDirectionDataList.forEach(System.out::println);
				setRoadDirectionTable(roadDirectionDataList);
				if (legStopData.get(currentLeg) == null) {
					ArrayList<StopData> stopDataList = optimizeStops.getUiStopData(2, currentLeg, false, roadDirectionDataList);
					stopDataList.forEach(System.out::println);
					setStopTable(stopDataList);
				} else {
					setStopTable( legStopData.get(currentLeg) );
				}
			}
		}
		
	}
	
	protected class OptimizeStopSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (! event.getValueIsAdjusting()) {
				System.out.println(event);
				int which = event.getLastIndex();
			}
		}
		
	}

	private void addListeners(OptimizeActionListener optimizeActionListener,
			OptimizeLegSelectionListener optimizeLegSelectionListener) {
		
		cancelButton.addActionListener(optimizeActionListener);
		optimizeButton.addActionListener(optimizeActionListener);
		chooseButton.addActionListener(optimizeActionListener);
		addAfterButton.addActionListener(optimizeActionListener);
		addBeforeButton.addActionListener(optimizeActionListener);
		
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
		StopData legStop = new StopData(legData); 
		sublist.add(legStop);
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
		
		protected ArrayList<StopData> data = null;
		
		
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

		public ArrayList<StopData> getData() {
			return data;
		}

		public void setData(ArrayList<StopData> data) {
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
		for (int i = 0; i < legDataList.size(); i++) {
			legStopData.add(null);
		}
	}
	
	public void setRoadDirectionTable( List<RoadDirectionData> roadDataList ) {
		roadTableModel.setData(roadDataList);
	}

	public void setStopTable( ArrayList<StopData> stopDataList ) {
		stopsTableModel.setData(stopDataList);
	}
	
	public ArrayList<StopData> getStopTable() {
		return stopsTableModel.getData();
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
				JPanel choicesPanel = new JPanel();
				tabbedPane.addTab("Choices", null, choicesPanel, null);
				choicesPanel.setLayout(new BorderLayout(0, 0));
				{
					choicesTabbedPane = new JTabbedPane(JTabbedPane.TOP);
					choicesPanel.add(choicesTabbedPane, BorderLayout.NORTH);
					choicesTabbedPane.addChangeListener( new ChangeListener() {
						@Override
						public void stateChanged(ChangeEvent event) {
							System.out.println(choicesTabbedPane.getSelectedIndex());
						}
					});
				}
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
				addBeforeButton = new JButton("Add Before");
				addBeforeButton.setActionCommand("Add Before");
				buttonPane.add(addBeforeButton);
			}
			{
				addAfterButton = new JButton("Add After");
				addAfterButton.setActionCommand("Add After");
				buttonPane.add(addAfterButton);
			}
			{
				optimizeButton = new JButton("Optimize");
				optimizeButton.setActionCommand("Optimize");
				buttonPane.add(optimizeButton);
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
		return choicesTabbedPane;
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
		EnumMap<Main.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
		Main.loadPOIMap( poiMap );
		poiMap.forEach((kind, set) -> {
			nearbyMap.put(kind, set.getPointsOfInterestAlongRoute(route, 5e3 ));
		} );
		
		
		OptimizeStops optimizeStops = new OptimizeStops( route, poiMap, nearbyMap );
		
		List<LegData> legDataList = optimizeStops.getUiLegData();
		legDataList.forEach(System.out::println);
		
		List<RoadDirectionData> roadDirectionDataList = optimizeStops.getUiRoadData(0);
		roadDirectionDataList.forEach(System.out::println);
		
		ArrayList<StopData> stopDataList = optimizeStops.getUiStopData(2, 0, false, roadDirectionDataList);
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
				String html = toHtml(2, legDataList.get(0), it.next() );
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
	
}
