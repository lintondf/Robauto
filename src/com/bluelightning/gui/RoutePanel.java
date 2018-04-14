package com.bluelightning.gui;

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

import com.bluelightning.data.TripPlan;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIBase;

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
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;

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

	public static class WaypointsModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Day", "Via","Name","Address","Latitude","Longitude","Fuel"};
		protected static final double[] widths = {0.05,  0.05, 0.30,    0.35,   0.10,     0.10,        0.05};
		protected static final boolean[] centered = {true, true, false, false, false, false, true};
		
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
				return Integer.toString( place.getVisitOrder() );
			case 1:
				return place.isPassThru();
			case 2: 
				return place.getName().fullName;
			case 3:
				return place.getAddress().value;
			case 4:
				return String.format("%12.6f", place.getLatitude() );
			case 5:
				return String.format("%12.6f", place.getLongitude() );
			case 6:
				return POIBase.toFuelString( place.getFuelAvailable());
			}
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (placesList == null || rowIndex < 0 || rowIndex >= placesList.size() || columnIndex > 1)
				return;
			VisitedPlace place = placesList.get(rowIndex);
			switch (columnIndex) {
			case 0:
				place.setVisitOrder( Integer.parseInt( (String) aValue ) );
				break;
			case 1:
				place.setPassThru( (Boolean) aValue );
				break;
			}
			fireTableCellUpdated(rowIndex, columnIndex);
		}

		@Override
		public Class<?> getColumnClass(int iCol) {
			if (iCol == 1)
				return Boolean.class;
			return String.class;
		}

		@Override
		public String getColumnName(int iCol) {
			return names[iCol];
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column < 2;
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
			for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) { 
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
			TableColumn dayColumn = stopsTable.getColumnModel().getColumn(0);
			JComboBox comboBox = new JComboBox();
			comboBox.addItem("1");
			comboBox.addItem("2");
			comboBox.addItem("3");
			comboBox.addItem("4");
			comboBox.addItem("5");
			comboBox.addItem("6");
			dayColumn.setCellEditor(new DefaultCellEditor(comboBox));
		}
		
	}
	
	

	/**
	 * Create the panel.
	 */
	public RoutePanel(ActionListener listener) {
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
				waypointsModel.layoutColumns(waypointsTable);
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
		gbl_buttonPanel.columnWidths = new int[] {25};
		gbl_buttonPanel.rowHeights = new int[] {20, 20, 0, 0, 0, 0};
		gbl_buttonPanel.columnWeights = new double[]{0.0};
		gbl_buttonPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		gbc_btnAddAfter.insets = new Insets(0, 0, 5, 5);
		gbc_btnAddAfter.gridx = 0;
		gbc_btnAddAfter.gridy = 3;
		buttonPanel.add(btnAddAfter, gbc_btnAddAfter);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(listener);
		btnRemove.setActionCommand("RoutePanel.Remove");
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnRemove.insets = new Insets(0, 0, 0, 5);
		gbc_btnRemove.gridx = 0;
		gbc_btnRemove.gridy = 4;
		buttonPanel.add(btnRemove, gbc_btnRemove);
		
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
