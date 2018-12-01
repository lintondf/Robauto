/**
 * 
 */
package com.bluelightning.gui;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.slf4j.LoggerFactory;

import com.bluelightning.CoPilot13Format;
import com.bluelightning.Here2;
import com.bluelightning.ManeuverMetrics;
import com.bluelightning.RobautoMain;
import com.bluelightning.TravelMode;
import com.bluelightning.data.FuelStop;
import com.bluelightning.data.TripPlan;
import com.bluelightning.poi.POIBase;
import com.bluelightning.poi.SamsClubPOI;
import com.bluelightning.poi.TruckStopPOI;

import seedu.addressbook.data.place.VisitedPlace;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author NOOK
 *
 */
public class FuelPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList<FuelStop> fuelStops;
	double timeToHere = 0.0;
	double distanceToHere = 0.0;
	int firstAhead = 0;
	JTable table;
	MyTableModel model;
	Font font;
	Font smallerFont;

	public FuelPanel() {
	}

	public FuelPanel(ArrayList<FuelStop> fuelStops, Font font) {
		this.fuelStops = fuelStops;
		this.font = font;
		this.smallerFont = font.deriveFont(0.5f * font.getSize());

		fuelStops.forEach(System.out::println);
		setLayout(new GridLayout(1, 0));

		model = new MyTableModel(fuelStops);
		table = new JTable(model);
		table.setFont(font);
		table.setPreferredScrollableViewportSize(new Dimension(500, 70));
		table.setFillsViewportHeight(true);

		TableColumn col = table.getColumnModel().getColumn(0);
		col.setCellRenderer(new RightCellRenderer());
		col = table.getColumnModel().getColumn(1);
		col.setCellRenderer(new CenteredCellRenderer());
		col = table.getColumnModel().getColumn(3);
		col.setCellRenderer(new SmallerCellRenderer());

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		ListSelectionModel selectionModel = table.getSelectionModel();

		selectionModel.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				int i = firstAhead + e.getLastIndex();
				if (i >= 0 && i < fuelStops.size()) {
					FuelStop stop = fuelStops.get(i);
					launchCoPilot( stop );
				}
			}

		});

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumnAdjuster tca = new TableColumnAdjuster(table);
		tca.setDynamicAdjustment(true);
		tca.adjustColumns();

		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		// Add the scroll pane to the panel.
		add(scrollPane);
	}

	public void update(ManeuverMetrics.ClosestManeuver currentManeuver, List<ManeuverMetrics> maneuverMetrics) {
		// int firstAhead = 0; /////!!!!!!!!!!!!!!!!!!
		if (model == null || table == null || fuelStops == null)
			return;
		int iNow = maneuverMetrics.indexOf(currentManeuver.metrics);
		double nowSpeed = currentManeuver.metrics.maneuver.getLength()
				/ currentManeuver.metrics.maneuver.getTravelTime();
		if (iNow >= 0) {
			// planned time and distances
			distanceToHere = currentManeuver.distanceFromStart + currentManeuver.metrics.totalDistance;
			timeToHere = currentManeuver.distanceFromStart / nowSpeed + currentManeuver.metrics.totalTime;
			if (fuelStops != null) {
				for (firstAhead = 0; firstAhead < fuelStops.size(); firstAhead++) {
					if (distanceToHere < fuelStops.get(firstAhead).distanceFromStart)
						break;
				}
			}
			// System.out.printf("%d of %d %10.1f, %10.0f\n", firstAhead,
			// fuelStops.size(), distanceToHere, timeToHere );
			model.fireTableDataChanged();
		}
	}

	class MyTableModel extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		ArrayList<FuelStop> fuelStops;

		private String[] columnNames = { "Distance", "Time", "Name", "Address" };

		public MyTableModel(ArrayList<FuelStop> fuelStops) {
			this.fuelStops = fuelStops;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return fuelStops.size() - firstAhead;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			FuelStop stop = fuelStops.get(row + firstAhead);
			switch (col) {
			case 0:
				return String.format("%.1f", (stop.distanceFromStart - distanceToHere) * Here2.METERS_TO_MILES);
			case 1:
				return Here2.toPeriod(stop.timeFromStart - timeToHere);
			case 2:
				String shortName = stop.name.trim();
				return shortName.substring(0, Math.min(16, shortName.length()));
			case 3:
				String shortAddress = stop.address.trim();
				return shortAddress.substring(0, Math.min(64, shortAddress.length()));
			}
			return null;
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

	}

	class CenteredCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int rowIndex, int vColIndex) {
			setText(value.toString());
			this.setHorizontalAlignment(SwingConstants.CENTER);
			return this;
		}
	}

	class RightCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int rowIndex, int vColIndex) {
			setText(value.toString());
			this.setHorizontalAlignment(SwingConstants.RIGHT);
			return this;
		}
	}

	class SmallerCellRenderer extends JLabel implements TableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int rowIndex, int vColIndex) {
			setText(value.toString());
			setFont(smallerFont);
			return this;
		}
	}
	
	private void launchCoPilot(FuelStop stop) {
		TruckStopPOI poi = new TruckStopPOI();
		poi.setName( stop.name );
		poi.setLatitude( stop.latitude );
		poi.setLongitude( stop.longitude );
		poi.setAddress( stop.address );
		CoPilot13Format format = new CoPilot13Format();
		String ipath = "gpstrip.trp.base";
		String opath =  "\\\\Surfacepro3\\NA\\save\\gpstrip.trp";
		try {
			List<VisitedPlace> positions = format.read( new BufferedReader(new InputStreamReader( new FileInputStream(ipath), CoPilot13Format.UTF16LE_ENCODING)));//, );
			positions.remove(1);
			positions.add( new VisitedPlace( poi ) );
		    PrintWriter stream = new PrintWriter(opath, CoPilot13Format.UTF16LE_ENCODING);
			format.write("MyRoute", positions, stream, 0, positions.size() );
			stream.close();
			stream = new PrintWriter( System.out );
			format.write("MyRoute", positions, stream, 0, positions.size() );
			stream.close();			
		} catch (Exception x ) {
			x.printStackTrace();
		}
		
	}
	
	
	private void launchMaps( double latitude, double longitude, String name ) {
		Runtime rt = Runtime.getRuntime();
		String cmd = String.format("C:\\Windows\\System32\\cmd.exe /c start ms-drive-to:\"?destination.latitude=%.6f&destination.longitude=%.6f&destination.name=%s\"", latitude, longitude, name );
		try {
			rt.exec(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		TravelMode.initLookAndFeel(60, 48);
		// Create and set up the window.
		JFrame frame = new JFrame("TableDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		TripPlan tripPlan = TripPlan.load(new File("MD2FL_2018.robauto"), frame);
		// Create and set up the content pane.
		FuelPanel newContentPane = new FuelPanel(tripPlan.getFuelStops(), frame.getFont());
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		Runtime rt = Runtime.getRuntime();
		try {
			rt.exec("C:\\Windows\\System32\\cmd.exe /c start ms-drive-to:\"?destination.latitude=47.680504&destination.longitude=-122.328262&destination.name=Green Lake\"");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// // configured via resources/logback.xml
		// RobautoMain.logger =
		// LoggerFactory.getLogger("com.bluelightning.FuelPanel");
		//
		// // Schedule a job for the event-dispatching thread:
		// // creating and showing this application's GUI.
		// javax.swing.SwingUtilities.invokeLater(new Runnable() {
		// public void run() {
		// createAndShowGUI();
		// }
		// });
	}
}
