package com.bluelightning.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import com.bluelightning.OptimizeStops.StopData;
import com.bluelightning.Permutations;
import com.bluelightning.Report;
import com.bluelightning.data.TripPlan;
import com.bluelightning.data.TripPlan.TripLeg;
import com.bluelightning.Events.AddManualStopEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.json.HereRoute;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POIResult;
import com.bluelightning.poi.POISet;
import com.google.common.eventbus.Subscribe;

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
import javax.swing.ScrollPaneConstants;


public class OptimizeStopsDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected final OptimizeStops optimizeStops;
	protected final JPanel contentPanel = new JPanel();
	protected JTable roadsTable;
	protected JTable legTable;
	protected JTable stopsTable;
	protected JTextPane textPane;
	protected JTabbedPane choicesTabbedPane;
	protected LegTableModel legTableModel;
	protected RoadTableModel roadTableModel;
	protected StopsTableModel stopsTableModel;
	protected JButton commitButton;
	protected JButton chooseButton;
	protected JButton cancelButton;
	protected JButton addBeforeButton;
	protected JButton addAfterButton;
	protected CallbackHandler handler;
	protected int currentLeg = 0;

	protected JTextPane outputTextPane;

	protected ArrayList<OptimizeStops.DriverAssignments> presentedChoices;
	
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
					OptimizeStops.StopData data = new OptimizeStops.StopData(event.result);
					ArrayList<OptimizeStops.StopData> dataList = stopsTableModel.getData();
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
			startAddDialog( handler, distance0, distance1 );
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
		startAddDialog( handler, distance0, distance1 );
	}
	
	protected void startAddDialog(CallbackHandler handler, double distance0, double distance1) {
			ArrayList<POIResult> segmentPOI = optimizeStops.getRouteSegmentPOI(distance0, distance1);
			AddManualStopDialog addDialog = new AddManualStopDialog();
			addDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			addDialog.setData(segmentPOI);
			addDialog.setVisible(true);
			Events.eventBus.register( handler );
	}
	
	
	protected void commitSelectedChoice() {
//		for (ArrayList<OptimizeStops.StopData> stopList : legStopData ) {
//			stopList.forEach( System.out::println );
//		}
	}
	
	protected void chooseCurrentTab() {
		// TODO not this way; build output tab from saved data for all legs; need StopData constructors
		int selected = choicesTabbedPane.getSelectedIndex();
		if (selected >= 0) {
			optimizeStops.getTripPlan().getTripLegs().get(currentLeg).driverAssignments = presentedChoices.get(selected);
			updateTripData();
		}
	}
	
	public class OptimizeActionListener implements ActionListener {

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
			case "Choose":
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						chooseCurrentTab();
					}
				});
				break;
			case "Commit":
				SwingUtilities.invokeLater( new Runnable() {
					@Override
					public void run() {
						commitSelectedChoice();
					}
				});
				break;
			default:
				break;
			}
		}

	}
	
	public class OptimizeLegSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent event) {
			if (! event.getValueIsAdjusting()) {
				System.out.println(event);
				if (currentLeg >= 0) {
					optimizeStops.getTripPlan().getTripLegs().get(currentLeg).stopDataList = getStopTable();
				}
				currentLeg = event.getLastIndex();
				setCurrentLeg( optimizeStops.getTripPlan().getTripLegs().get(currentLeg) );
				generateLegStopChoices(currentLeg);
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

	public void addListeners(OptimizeActionListener optimizeActionListener,
			OptimizeLegSelectionListener optimizeLegSelectionListener) {
		
		cancelButton.addActionListener(optimizeActionListener);
		commitButton.addActionListener(optimizeActionListener);
		chooseButton.addActionListener(optimizeActionListener);
		addAfterButton.addActionListener(optimizeActionListener);
		addBeforeButton.addActionListener(optimizeActionListener);
		
		legTable.getSelectionModel().addListSelectionListener(optimizeLegSelectionListener);
	}

	protected static class LegTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Start", "End", "Length (mi)", "Time (hr:mm)"};
		
		protected List<OptimizeStops.LegData> data = null;
		
		
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
			OptimizeStops.LegData legData = data.get(iRow);
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

		public List<OptimizeStops.LegData> getData() {
			return data;
		}

		public void setData(List<OptimizeStops.LegData> data) {
			this.data = data;
			fireTableStructureChanged();
		}
		
	}
	
	
	protected static class RoadTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Name", "Direction"};
		
		protected List<OptimizeStops.RoadDirectionData> data = null;
		
		
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
			OptimizeStops.RoadDirectionData roadData = data.get(iRow);
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

		public List<OptimizeStops.RoadDirectionData> getData() {
			return data;
		}

		public void setData(List<OptimizeStops.RoadDirectionData> data) {
			this.data = data;
			fireTableStructureChanged();
		}
		
	}
	
	
	protected static class StopsTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Use", "Distance", "Time", "Road", "Direction", "Name"};
		protected static final double[] widths = {0.05, 0.1, 0.1, 0.25, 0.10, 0.40};
		protected static final boolean[] centered = {true, true, true, true, true, false};
		
		protected ArrayList<OptimizeStops.StopData> data = null;
		
		
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
			OptimizeStops.StopData stopData = data.get(iRow);
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

		public ArrayList<OptimizeStops.StopData> getData() {
			return data;
		}

		public void setData(ArrayList<OptimizeStops.StopData> data) {
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
	
	
	public void updateTripData() {
		ArrayList<TripLeg> tripLegs = optimizeStops.getTripPlan().getTripLegs();
		ArrayList<OptimizeStops.LegData> legDataList = new ArrayList<>();
		Report report = new Report();
		for (TripLeg leg : tripLegs) {
			legDataList.add( leg.legData );
			report.add( TripPlan.N_DRIVERS, leg.legData, leg.driverAssignments );
		}
		legTableModel.setData(legDataList);
		outputTextPane.setContentType("text/html");
		outputTextPane.setText(report.toHtml());

	}
	
	public void setCurrentLeg( TripLeg legData ) {
		roadTableModel.setData(legData.roadDirectionDataList);
		stopsTableModel.setData(legData.stopDataList);
	}
	
//	private void setRoadDirectionTable( List<RoadDirectionData> roadDataList ) {
//		roadTableModel.setData(roadDataList);
//	}
//
//	private void setStopTable( ArrayList<StopData> stopDataList ) {
//		stopsTableModel.setData(stopDataList);
//	}
	
	public ArrayList<OptimizeStops.StopData> getStopTable() {
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
					choicesPanel.add(choicesTabbedPane); //, BorderLayout.NORTH);
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
					outputTextPane = new JTextPane();
					outputTextPane.setContentType("text/html");
					outputTextPane.setEditable(false);
					JScrollPane scroll = new JScrollPane(outputTextPane);
					scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
					scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
					outputPanel.add(scroll);//, BorderLayout.NORTH);
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
			{
				commitButton = new JButton("Commit");
				commitButton.setActionCommand("Commit");
				buttonPane.add(commitButton);
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

	public JTabbedPane getChoicesTabbedPane() {
		return choicesTabbedPane;
	}
	
	public void generateLegStopChoices(int iLeg) {
		while (getChoicesTabbedPane().getComponentCount() > 0) {
			getChoicesTabbedPane().removeTabAt(0);
		}
		// generate all possible permutations of driver assignments
		// do not permute stopping at the arrival point (last in
		// stopDataList)
		Permutations perm = new Permutations(optimizeStops.getTripPlan().getTripLegs().get(iLeg).stopDataList.size() - 1);
		ArrayList<Integer[]> unique = perm.monotonic();
		Set<OptimizeStops.DriverAssignments> driverAssignmentsSet = new TreeSet<>();
		for (Integer[] elements : unique) {
			driverAssignmentsSet.add(OptimizeStops.generateDriverAssignments(TripPlan.N_DRIVERS,
					optimizeStops.getTripPlan().getTripLegs().get(iLeg).legData, 
					optimizeStops.getTripPlan().getTripLegs().get(iLeg).stopDataList,
					elements));
		}
		Iterator<OptimizeStops.DriverAssignments> it = driverAssignmentsSet.iterator();
		
		presentedChoices = new ArrayList<>();

		for (int i = 0; it.hasNext() && i < 5; i++) {
			OptimizeStops.DriverAssignments choiceDriverAssignments = it.next();
			presentedChoices.add(choiceDriverAssignments);
			String html = OptimizeStops.toHtml(2, optimizeStops.getTripPlan().getTripLegs().get(iLeg).legData, choiceDriverAssignments);
			JPanel panel = new JPanel();
			getChoicesTabbedPane().addTab(String.format("Case %d", 1 + i), null, panel, null);
			getChoicesTabbedPane().setEnabledAt(i, true);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JTextPane textPane = new JTextPane();
				textPane.setContentType("text/html");
				textPane.setText(html);
				panel.add(textPane);
			}
		} // for i
	}
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
//		HereRoute hereRoute = null;
//		try {
//			String json = IOUtils.toString(new FileInputStream("route.json"), "UTF-8");
//			hereRoute = (HereRoute) Here2.gson.fromJson(json, HereRoute.class);
//			if (hereRoute == null || hereRoute.getResponse() == null)
//				throw new NullPointerException();
//		} catch (Exception x) {
//			x.printStackTrace();
//			return;
//		}
//		System.out.println(hereRoute.getResponse().getMetaInfo());
//		Set<Route> routes = hereRoute.getResponse().getRoute();
//		Route route = routes.iterator().next();
//
//		EnumMap<Main.MarkerKinds, POISet> poiMap = new EnumMap<>(Main.MarkerKinds.class);
//		EnumMap<Main.MarkerKinds, ArrayList<POIResult>> nearbyMap = new EnumMap<>(Main.MarkerKinds.class);
//		Main.loadPOIMap( poiMap );
//		poiMap.forEach((kind, set) -> {
//			nearbyMap.put(kind, set.getPointsOfInterestAlongRoute(route, 5e3 ));
//		} );
//		
//		
//		OptimizeStops optimizeStops = new OptimizeStops( route, poiMap, nearbyMap );
//		
//		List<LegData> legDataList = optimizeStops.getUiLegData();
//		legDataList.forEach(System.out::println);
//		
//		List<RoadDirectionData> roadDirectionDataList = optimizeStops.getUiRoadData(0);
//		roadDirectionDataList.forEach(System.out::println);
//		
//		ArrayList<StopData> stopDataList = optimizeStops.getUiStopData(2, 0, false, roadDirectionDataList);
//		stopDataList.forEach(System.out::println);
//		
//		Permutations perm = new Permutations( stopDataList.size() );
//		ArrayList<Integer[]> unique = perm.monotonic();
//		Set<DriverAssignments> driverAssignments = new TreeSet<>();
//		for (Integer[] elements : unique) {
//			driverAssignments.add( OptimizeStops.generateDriverAssignments(2, legDataList.get(0), stopDataList, elements) );
//		}
//		Iterator<DriverAssignments> it = driverAssignments.iterator();
//			
//		try {
//			OptimizeStopsDialog dialog = new OptimizeStopsDialog(optimizeStops);
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.setVisible(true);
//
//			dialog.setLegData( legDataList );
//			dialog.setRoadDirectionTable(roadDirectionDataList);
//			dialog.setStopTable(stopDataList);
//			
//			dialog.addListeners(
//					dialog.new OptimizeActionListener(),
//					dialog.new OptimizeLegSelectionListener()
//					);
//			
//			for (int i = 0; it.hasNext() && i < 5; i++) {
//				String html = toHtml(2, legDataList.get(0), it.next() );
//				if (i==0) try {
//					PrintWriter out = new PrintWriter("report.html");
//					out.println(html);
//					out.close();
//				} catch (Exception x) {
//					x.printStackTrace();
//				}				
//				JPanel panel = new JPanel();
//				dialog.getOutputTabbedPane().addTab(String.format("Case %d", 1+i), null, panel, null);
//				dialog.getOutputTabbedPane().setEnabledAt(i, true);
//				panel.setLayout(new BorderLayout(0, 0));
//				{
//					JTextPane textPane = new JTextPane();
//					textPane.setContentType("text/html");
//					textPane.setText(html);
//					panel.add(textPane);
//				}
//			} // for i
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

}
