package com.bluelightning;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import seedu.addressbook.commands.AddCommand;
import seedu.addressbook.commands.CommandResult;
import seedu.addressbook.commands.ListCommand;
import seedu.addressbook.commands.ViewCommand;
import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.place.Address;
import seedu.addressbook.data.place.Name;
import seedu.addressbook.data.place.Place;
import seedu.addressbook.data.place.ReadOnlyPlace;
import seedu.addressbook.data.place.UniquePlaceList;
import seedu.addressbook.data.place.VisitedPlace;
import seedu.addressbook.data.tag.UniqueTagList;
import seedu.addressbook.logic.Logic;

import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.Button;
import java.awt.Dimension;

public class RoutePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable      waypointsTable;
	private WaypointsModel waypointsModel;

	protected static class WaypointsModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Via","Address","Name","Latitude","Longitude"};
		protected static final double[] widths = {0.10, 0.30,     0.40,   0.10,     0.10};
		protected static final boolean[] centered = {true, false, false, false, false};
		
		AddAddressDialog dialog;
		protected ArrayList<VisitedPlace> placesList;
		
		public WaypointsModel() {
			super();
		}

		@Override
		public int getColumnCount() {
			return names.length;
		}

		@Override
		public int getRowCount() {
			if (placesList == null)
				return 0;
			return placesList.size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (placesList == null || iRow >= getRowCount())
				return null;
			VisitedPlace place = placesList.get(iRow);
			switch (iCol) {
			case 0:
				return place.isPassThru();
			case 1: 
				return place.getName().fullName;
			case 2:
				return place.getAddress().value;
			case 3:
				return String.format("%12.6f", place.getLatitude() );
			case 4:
				return String.format("%12.6f", place.getLongitude() );
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

		public ArrayList<VisitedPlace> getData() {
			return placesList;
		}

		public void setData(ArrayList<VisitedPlace> data) {
			this.placesList = data;
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
	
	

	/**
	 * Create the panel.
	 */
	public RoutePanel(ActionListener listener) {
//		String[] pointAddresses = { "3533 Carambola Cir, Melbourne, FL", "15 Mill Creek Circle, Pooler, GA",
//				"125 Riverside Dr, Banner Elk, NC", "2350 So Pleasant Valley Rd, Winchester, VA 22601",
//				"10654 Breezewood Dr, Woodstock, MD 21163-1317", "1365 Boston Post Road, Milford, CT",
//				"836 Palmer Avenue, Falmouth, MA", "100 Cabelas Blvd, Scarborough, ME", "7 Manor Lane, Sullivan, ME" };
//		
//		String[] columnNames = {"Address","PassThru","Latitude","Longitude"};
//		Object[][] data = {
//				{"3533 Carambola Cir, Melbourne, FL", new Boolean(false), new Double(28), new Double(-81)}
//		};
//		int[] columnWidths = { 200, 10, 50, 50 };
//		waypointTable = new JTable(data, columnNames);
//		TableColumn column = null;
//		for (int i = 0; i < columnWidths.length; i++) {
//		    column = waypointTable.getColumnModel().getColumn(i);
//	        column.setPreferredWidth(columnWidths[i]);
//		}
		waypointsModel = new WaypointsModel();
		waypointsTable = new JTable(waypointsModel);
		JScrollPane scrollPane = new JScrollPane(waypointsTable);
		waypointsTable.setFillsViewportHeight(true);
		setLayout(new BorderLayout(0, 0));
		waypointsModel.layoutColumns(waypointsTable);
		waypointsTable.addComponentListener( new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent arg0) {
			}

			@Override
			public void componentMoved(ComponentEvent arg0) {
			}

			@Override
			public void componentResized(ComponentEvent event) {
				Dimension dim = event.getComponent().getSize();
				waypointsModel.resizeColumns(waypointsTable, dim.getWidth());
			}

			@Override
			public void componentShown(ComponentEvent event) {
				componentResized(event);
			}
			
		});
		
		//JSplitPane splitPane2 = new JSplitPane();
//		splitPane2.setResizeWeight(0.5);
//		splitPane2.setOrientation(JSplitPane.VERTICAL_SPLIT);
//		add(splitPane2);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.9);
		add(splitPane);
//		splitPane2.setLeftComponent(splitPane);
		
		JPanel buttonPanel = new JPanel();
		splitPane.setRightComponent(buttonPanel);
		GridBagLayout gbl_buttonPanel = new GridBagLayout();
		gbl_buttonPanel.columnWidths = new int[] {75, 0, 0};
		gbl_buttonPanel.rowHeights = new int[] {30, 30, 0, 0, 0};
		gbl_buttonPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_buttonPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		buttonPanel.setLayout(gbl_buttonPanel);
		
		JButton btnMoveUp = new JButton("Move Up");
		btnMoveUp.setActionCommand("RoutePanel.MoveUp");
		btnMoveUp.addActionListener(listener);
		GridBagConstraints gbc_btnMoveUp = new GridBagConstraints();
		gbc_btnMoveUp.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnMoveUp.insets = new Insets(0, 0, 5, 5);
		gbc_btnMoveUp.gridx = 0;
		gbc_btnMoveUp.gridy = 0;
		buttonPanel.add(btnMoveUp, gbc_btnMoveUp);
		
		JButton btnMoveDown = new JButton("Move Down");
		btnMoveDown.addActionListener(listener);
		btnMoveDown.setActionCommand("RoutePanel.MoveDown");
		GridBagConstraints gbc_btnMoveDown = new GridBagConstraints();
		gbc_btnMoveDown.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnMoveDown.insets = new Insets(0, 0, 5, 5);
		gbc_btnMoveDown.gridx = 0;
		gbc_btnMoveDown.gridy = 1;
		buttonPanel.add(btnMoveDown, gbc_btnMoveDown);
		
		JButton btnAddBefore = new JButton("Add Before");
		btnAddBefore.addActionListener(listener);
		btnAddBefore.setActionCommand("RoutePanel.AddBefore");
		GridBagConstraints gbc_btnAddBefore = new GridBagConstraints();
		gbc_btnAddBefore.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddBefore.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddBefore.gridx = 0;
		gbc_btnAddBefore.gridy = 2;
		buttonPanel.add(btnAddBefore, gbc_btnAddBefore);
		
		JButton btnAddAfter = new JButton("Add After");
		btnAddAfter.addActionListener(listener);
		btnAddAfter.setActionCommand("RoutePanel.AddAfter");
		GridBagConstraints gbc_btnAddAfter = new GridBagConstraints();
		gbc_btnAddAfter.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddAfter.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddAfter.gridx = 0;
		gbc_btnAddAfter.gridy = 3;
		buttonPanel.add(btnAddAfter, gbc_btnAddAfter);
		
		JPanel tablePanel = new JPanel();
		splitPane.setLeftComponent(tablePanel);
		tablePanel.setLayout(new BorderLayout(0, 0));
		
		tablePanel.add(scrollPane, BorderLayout.NORTH);
		
//		JPanel lowerPanel = new JPanel();
//		splitPane2.setRightComponent(lowerPanel);
//		lowerPanel.setLayout(new BorderLayout(0, 0));
//		
//		htmlPane = new JTextPane();
//		JScrollPane scrollPane_1 = new JScrollPane(htmlPane);
//		lowerPanel.add(scrollPane_1, BorderLayout.NORTH);
	}

	
	public WaypointsModel getWaypointsModel() {
		return waypointsModel;
	}
	
	
	public JTable getWaypointTable() {
		return waypointsTable;
	}

	public static void main(String[] args) {
		CommandResult result;
		try {
			Logic controller = new Logic();
			AddressBook addressBook = controller.getAddressBook();
			if (false) {
				Place place = Place.factory("Sams Club", "15 Mill Creek Circle, Pooler, GA");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Grandfather Campground", "125 Riverside Dr, Banner Elk, NC");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Walmart", "2350 So Pleasant Valley Rd, Winchester, VA 22601");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Megan Mudge", "10654 Breezewood Dr, Woodstock, MD 21163-1317");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Walmart", "1365 Boston Post Road, Milford, CT");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Sippewissett Campground", "836 Palmer Avenue, Falmouth, MA");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Cabelas", "100 Cabelas Blvd, Scarborough, ME");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Home Maine", "7 Manor Lane, Sullivan, ME 40664");
				place.geocode();
				addressBook.add(place);
				place = Place.factory("Home Florida", "3533 Carambola Cir, Melbourne, FL");
				place.geocode();
				addressBook.add(place);
				controller.getStorage().save(addressBook);
			}
			SortedSet<VisitedPlace> visiting = Collections.synchronizedSortedSet(new TreeSet<VisitedPlace>());
//			Place place = Place.factory("Maine Home", "7 Manor Lane, Sullivan, ME 40664");
//			addressBook.add(place);
//			UniquePlaceList placeList = addressBook.getAllPlaces();
//			for (ReadOnlyPlace p : placeList) {
//				System.out.println(p);
//			}
//			AddCommand addCommand = new AddCommand("Home",
//					0.0, 
//					0.0, 
//					"3533 Carambola Circle, Melbourne, FL 32940", 
//					new TreeSet<String>() );
//			System.out.println(addCommand);
//			result = addressBookController.execute(addCommand);
//			result = addressBookController.execute(new ListCommand());
//			Optional<List<? extends ReadOnlyPlace>> allPersons = result.getRelevantPersons();
//			System.out.println(result.feedbackToUser);
//			for (ReadOnlyPlace rop : allPersons.get()) {
//				System.out.println(rop);
//			}
//			result = addressBookController.execute( new ViewCommand(1) );
//			System.out.println(result.feedbackToUser);
//			allPersons = result.getRelevantPersons();
//			for (ReadOnlyPlace rop : allPersons.get()) {
//				System.out.println(rop);
//			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
