package com.bluelightning.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import com.bluelightning.Events;
import com.bluelightning.Here2;
import com.bluelightning.LatLon;
import com.bluelightning.Main;
import com.bluelightning.data.TripPlan.StopData;
import com.bluelightning.Events.AddWaypointEvent;

import seedu.addressbook.data.AddressBook;
import seedu.addressbook.data.place.Address;
import seedu.addressbook.data.place.Name;
import seedu.addressbook.data.place.Place;
import seedu.addressbook.data.place.ReadOnlyPlace;
import seedu.addressbook.data.place.UniquePlaceList;
import seedu.addressbook.logic.Logic;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class AddAddressDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable addressesTable;

	protected Logic       controller = null;
	protected AddressBook addressBook = null;
	protected AddressesModel addressesModel;
	private JTextField nameField;
	private JTextField addressField;
	
	protected static class AddressesModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Name", "Address", "Tags"};
		protected static final double[] widths = {0.30, 0.50, 0.20};
		protected static final boolean[] centered = {false, false, true};
		
		AddAddressDialog dialog;
		protected UniquePlaceList placesList;
		
		public AddressesModel(UniquePlaceList placesList) {
			super();
			this.placesList = placesList;
		}

		@Override
		public int getColumnCount() {
			return names.length;
		}

		@Override
		public int getRowCount() {
			if (placesList == null)
				return 0;
			return placesList.immutableListView().size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (placesList == null || iRow >= getRowCount())
				return null;
			ReadOnlyPlace place = placesList.immutableListView().get(iRow);
			switch (iCol) {
			case 0: 
				return place.getName().fullName;
			case 1:
				return place.getAddress().value;
			case 2:
				return place.getTags().toString();
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

		public UniquePlaceList getData() {
			return placesList;
		}

		public void setData(UniquePlaceList data) {
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
	
	protected class AddAddressActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			System.out.println(event);
			switch (event.getActionCommand()) {
			case "Cancel":
				Main.logger.info("dispose() on Cancel");
				AddAddressDialog.this.dispose();
				break;
			case "OK":
				ReadOnlyPlace place = null;
				String address = (String) addressesModel.getValueAt( addressesTable.getSelectedRow(), 1);
				List<ReadOnlyPlace> places = addressBook.getPlacesWithAddress(address);
				if (places != null && !places.isEmpty()) {
					place = places.get(0);
					Events.eventBus.post( new Events.AddWaypointEvent(place) );
				}
				Main.logger.info("dispose() on OK");
				AddAddressDialog.this.dispose();
				break;
			case "Create":
				try {
					Place p = new Place();
					p.setName( new Name( nameField.getText()) );
					p.setAddress( new Address( addressField.getText() ) );
					LatLon where = Here2.geocodeLookup(p.getAddress().value );
					if (where != null) {
						p.setLatitude( where.getLatitude() );
						p.setLongitude( where.getLongitude() );
						addressBook.add(p);
						controller.getStorage().save(addressBook);
						addressesModel.setData( addressBook.getAllPlaces() );
					}
				} catch (Exception x) {}
				break;
			}
		}
	}
	
	/**
	 * Create the dialog.
	 * @param addressBook 
	 * @param controller 
	 */
	public AddAddressDialog(Logic controller, AddressBook addressBook) {
		setModal(true);
		this.controller = controller;
		this.addressBook = addressBook;
		AddAddressActionListener listener = new AddAddressActionListener();
		addressesModel = new AddressesModel( addressBook.getAllPlaces() );
		setTitle("Select Waypoint to Add");
		setBounds(100, 100, 900, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			contentPanel.add(scrollPane);
			{
				addressesTable = new JTable(addressesModel);
				addressesTable.setFillsViewportHeight(true);
				addressesTable.setVisible(true);
				scrollPane.setViewportView(addressesTable);
			}
		}
		{
			JPanel lowerPane = new JPanel();
			lowerPane.setLayout(new BorderLayout(0, 0));
			getContentPane().add(lowerPane, BorderLayout.SOUTH);
			{
				JPanel addPane = new JPanel();
				addPane.setLayout(new FlowLayout(FlowLayout.LEFT));
				lowerPane.add(addPane, BorderLayout.CENTER);
				{
					JLabel lblNewLabel = new JLabel("New Name");
					addPane.add(lblNewLabel);
				}
				{
					nameField = new JTextField();
					addPane.add(nameField);
					nameField.setColumns(10);
				}
				{
					JLabel lblNewLabel_1 = new JLabel("Address");
					addPane.add(lblNewLabel_1);
				}
				{
					addressField = new JTextField();
					addPane.add(addressField);
					addressField.setColumns(20);
				}
				{
					JButton btnCreateButton = new JButton("Create");
					btnCreateButton.setActionCommand("Create");
					btnCreateButton.addActionListener(listener);
					addPane.add(btnCreateButton);
				}
			}
			{
				JPanel buttonPane = new JPanel();
				buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
				lowerPane.add(buttonPane, BorderLayout.SOUTH);
				{
					JButton okButton = new JButton("OK");
					okButton.setActionCommand("OK");
					okButton.addActionListener(listener);
					buttonPane.add(okButton);
					
				}
				{
					JButton cancelButton = new JButton("Cancel");
					cancelButton.setActionCommand("Cancel");
					cancelButton.addActionListener(listener);
					buttonPane.add(cancelButton);
				}
			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		try {
			Logic controller = new Logic();
			controller.getStorage().load();
			AddressBook addressBook = controller.getAddressBook();
			System.out.println( addressBook.getAllPlaces().immutableListView().size());
			AddAddressDialog dialog = new AddAddressDialog(controller, addressBook);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
