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

import com.bluelightning.DouglasPeuckerReducer;
import com.bluelightning.Events;
import com.bluelightning.GeodeticPosition;
import com.bluelightning.Here2;
import com.bluelightning.Main;
import com.bluelightning.OptimizeStops;
import com.bluelightning.Permutations;
import com.bluelightning.Report;
import com.bluelightning.data.TripPlan;
import com.bluelightning.data.TripPlan.LegData;
import com.bluelightning.data.TripPlan.StopData;
import com.bluelightning.data.TripPlan.TripLeg;
import com.bluelightning.Events.AddManualStopEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.Main.MarkerKinds;
import com.bluelightning.json.HereRoute;
import com.bluelightning.json.Route;
import com.bluelightning.poi.POIBase;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

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
	protected JButton addBeforeButton;
	protected JButton addAfterButton;
	protected CallbackHandler handler;
	protected int currentLeg = 0;

	protected JTextPane outputTextPane;

	protected ArrayList<TripPlan.DriverAssignments> presentedChoices;
	
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
					TripPlan.StopData data = new TripPlan.StopData(event.result);
					ArrayList<TripPlan.StopData> dataList = stopsTableModel.getData();
					if (addBefore) {
						dataList.add(iRow, data);
					} else {
						dataList.add(iRow+1, data);
					}
					stopsTableModel.setData(dataList);
					generateLegStopChoices();
					OptimizeStopsDialog.this.optimizeStops.getTripPlan().getTripLeg(currentLeg).stopDataList = dataList;
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
		} else {
			distance0 = stopsTableModel.getData().get(selected).totalDistance - stopsTableModel.getData().get(selected).distance;
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
//		ArrayList<TripLeg> tripLegs = optimizeStops.getTripPlan().getTripLegs();
//		Iterator<LegData> it = legTableModel.getData().iterator();
//		Report report = new Report();
//		for (TripLeg leg : tripLegs) {
//			leg.legData = it.next();
//			report.add( TripPlan.N_DRIVERS, leg.legData, leg.driverAssignments );
//		}
		Report report = optimizeStops.getTripPlan().getTripReport();
		String html = report.toHtml();
		Events.eventBus.post( new Events.StopsCommitEvent( html ) );
	}
	
	protected void chooseCurrentTab() {
		int selected = choicesTabbedPane.getSelectedIndex();
		if (selected >= 0) {
			optimizeStops.getTripPlan().getTripLeg(currentLeg).driverAssignments = presentedChoices.get(selected);
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
				Main.logger.info("dispose() on OSD Cancel");
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
				if (handler != null) {
					Events.eventBus.unregister(handler);
					handler = null;
				}
				Main.logger.info("dispose() on OSD Commit");
				OptimizeStopsDialog.this.dispose();
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
				Main.logger.info("OSD/OLSL VC " + currentLeg);
				if (currentLeg >= 0) {
					optimizeStops.getTripPlan().getTripLeg(currentLeg).stopDataList = getStopTable();
				}
				currentLeg = event.getLastIndex();
				setCurrentLeg( optimizeStops.getTripPlan().getTripLeg(currentLeg) );
				Main.logger.info("OSD/OLSL VC A");
				generateLegStopChoices(currentLeg);
				Main.logger.info("OSD/OLSL VC exiting");
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
	
	public class OptimizeRoadModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent event) {
			if (event.getColumn() == 1) {
				TripLeg leg = optimizeStops.getTripPlan().getTripLeg(currentLeg);
				optimizeStops.getTripPlan().updateRoadDirectionData(leg, roadTableModel.getData());
				setCurrentLeg(leg);
				generateLegStopChoices(currentLeg);
			}
		}
		
	}

	public void addListeners(OptimizeActionListener optimizeActionListener,
			OptimizeLegSelectionListener optimizeLegSelectionListener,
			OptimizeRoadModelListener optimizeRoadModelListener) {
		
		commitButton.addActionListener(optimizeActionListener);
		chooseButton.addActionListener(optimizeActionListener);
		addAfterButton.addActionListener(optimizeActionListener);
		addBeforeButton.addActionListener(optimizeActionListener);
		
		legTable.getSelectionModel().addListSelectionListener(optimizeLegSelectionListener);
		this.roadsTable.getModel().addTableModelListener(optimizeRoadModelListener);
	}

	protected static class LegTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Start", "End", "Length (mi)", "Time (hr:mm)"};
		
		protected List<TripPlan.LegData> data = null;
		
		
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
			TripPlan.LegData legData = data.get(iRow);
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

		public List<TripPlan.LegData> getData() {
			return data;
		}

		public void setData(List<TripPlan.LegData> data) {
			this.data = data;
			fireTableStructureChanged();
		}
		
	}
	
	
	protected static class RoadTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Name", "Direction"};
		
		protected List<TripPlan.RoadDirectionData> data = null;
		
		
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
			TripPlan.RoadDirectionData roadData = data.get(iRow);
			switch (iCol) {
			case 0: 
				return roadData.road;
			case 1:
				return roadData.direction;
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (data == null || rowIndex < 0 || rowIndex >= data.size() || columnIndex != 1)
				return;
			TripPlan.RoadDirectionData roadData = data.get(rowIndex);
			roadData.direction = (String) aValue;
			data.set(rowIndex, roadData);
			fireTableCellUpdated(rowIndex, columnIndex);
		}
		
		@Override
		public Class<?> getColumnClass(int iCol) {
			return String.class;
		}

		@Override
		public String getColumnName(int iCol) {
			return names[iCol];
		}

		public List<TripPlan.RoadDirectionData> getData() {
			return data;
		}

		public void setData(List<TripPlan.RoadDirectionData> data) {
			this.data = data;
			fireTableStructureChanged();
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 1;
		}

	}
	
	
	protected static class StopsTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Use", "Refuel", "Fuel?", "Distance", "Time", "Road", "Dir.", "Name"};
		protected static final double[] widths = {0.05, 0.05, 0.05, 0.05, 0.08, 0.07, 0.20, 0.10, 0.35};
		protected static final boolean[] centered = {true, true, true, true, true, true, true, false};
		
		protected ArrayList<TripPlan.StopData> data = null;
		
		
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
			TripPlan.StopData stopData = data.get(iRow);
			switch (iCol) {
			case 0: 
				return stopData.use;
			case 1:
				return stopData.refuel;
			case 2:
				return stopData.fuelAvailable;
			case 3:
				return String.format("%5.1f", stopData.distance*Here2.METERS_TO_MILES);
			case 4:
				return Here2.toPeriod(stopData.trafficTime);
			case 5:
				return String.format("%s %s %s", stopData.road, stopData.state, stopData.mileMarker); 
			case 6:
				return stopData.direction;
			case 7:
				return stopData.name;
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int iCol) {
			if (iCol <= 1)
				return Boolean.class;
			return String.class;
		}

		@Override
		public String getColumnName(int iCol) {
			return names[iCol];
		}

		public ArrayList<TripPlan.StopData> getData() {
			return data;
		}

		public void setData(ArrayList<TripPlan.StopData> data) {
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
	
	public String updateTripData() {
		legTableModel.setData(optimizeStops.getTripPlan().getTripLegData());
		Report report = optimizeStops.getTripPlan().getTripReport();
		outputTextPane.setContentType("text/html");
		String html = report.toHtml();
		outputTextPane.setText(html);
		return html;
	}
	
	public void setCurrentLeg( TripLeg legData ) {
		roadTableModel.setData(legData.roadDirectionDataList);
		stopsTableModel.setData(legData.stopDataList);
	}
	
	public ArrayList<TripPlan.StopData> getStopTable() {
		return stopsTableModel.getData();
	}

	/**
	 * Create the dialog.
	 * @param optimizeStops 
	 */
	public OptimizeStopsDialog(OptimizeStops optimizeStops) {
		this.setTitle("Robauto - Optimize Stops");
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
				commitButton = new JButton("Done");
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
	
	public void generateLegStopChoices() {
		generateLegStopChoices( currentLeg );
	}
	
	
	protected ArrayList<TripPlan.StopData> limitTotalStops( ArrayList<TripPlan.StopData> stopDataList ) {
		if (stopDataList.size() <= 16)
			return stopDataList;
		ArrayList<TripPlan.StopData> reduced = new ArrayList<>(stopDataList);
		while (reduced.size() > 16) {
			double leastValuablePeers = 1e12;
			int    leastValuable = -1;
			for (int i = 1; i < reduced.size()-1; i++) {
				TripPlan.StopData before = reduced.get(i-1);
				TripPlan.StopData current = reduced.get(i);
				TripPlan.StopData after = reduced.get(i+1);
				double value = Math.abs(current.trafficTime - before.trafficTime) + Math.abs(current.trafficTime - after.trafficTime);
				if (value < leastValuablePeers) {
					leastValuablePeers = value;
					leastValuable = i;
				}
			}
			if (leastValuable == -1)
				break;
			reduced.remove(leastValuable);
		}
		return reduced;
	}
	
	public void generateLegStopChoices(int iLeg) {
		Main.logger.info("Generating leg stop choices...");
		while (getChoicesTabbedPane().getComponentCount() > 0) {
			getChoicesTabbedPane().removeTabAt(0);
		}
		// generate all possible permutations of driver assignments
		// do not permute stopping at the arrival point (last in
		// stopDataList)
		optimizeStops.getTripPlan().getTripLeg(iLeg).stopDataList.forEach(System.out::println);
		optimizeStops.getTripPlan().getTripLeg(iLeg).roadDirectionDataList.forEach(System.out::println);
		ArrayList<TripPlan.StopData> reduced = limitTotalStops(optimizeStops.getTripPlan().getTripLeg(iLeg).stopDataList);
		reduced.forEach(System.out::println);
		
		Permutations perm = new Permutations(reduced.size() - 1);
		ArrayList<Integer[]> unique = perm.monotonic();
		Main.logger.info(String.format("  %d unique", unique.size()));
		Set<TripPlan.DriverAssignments> driverAssignmentsSet = new TreeSet<>();
		for (Integer[] elements : unique) {
			driverAssignmentsSet.add(TripPlan.generateDriverAssignments(TripPlan.N_DRIVERS,
					optimizeStops.getTripPlan().getTripLeg(iLeg).legData, 
					optimizeStops.getTripPlan().getTripLeg(iLeg).stopDataList,
					elements));
		}
		Main.logger.info(String.format("  %d assignments", driverAssignmentsSet.size()));
		Iterator<TripPlan.DriverAssignments> it = driverAssignmentsSet.iterator();
		
		presentedChoices = new ArrayList<>();

		for (int i = 0; it.hasNext() && i < 5; i++) {
			TripPlan.DriverAssignments choiceDriverAssignments = it.next();
			presentedChoices.add(choiceDriverAssignments);
			String html = OptimizeStops.toHtml(2, optimizeStops.getTripPlan().getTripLeg(iLeg).legData, choiceDriverAssignments);
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
		Main.logger.info("  reported");
	}
	


}
