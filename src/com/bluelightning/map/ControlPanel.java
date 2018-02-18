package com.bluelightning.map;

import javax.swing.JPanel;

import com.bluelightning.Events;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.awt.event.ActionEvent;

public class ControlPanel extends JPanel {

	public static enum MarkerKinds {
		WALMARTS, SAMSCLUBS, COSTCOS, TRUCKSTOPS, RESTAREAS
	};
	protected JCheckBox chckbxWalmarts;
	protected JCheckBox chckbxSamsClubs;
	protected JCheckBox chckbxCostco;
	protected JCheckBox chckbxTruckStops;
	protected JCheckBox chckbxRestAreas;
	
	protected EnumMap<MarkerKinds, Boolean> markerStatus = new EnumMap<MarkerKinds, Boolean>(MarkerKinds.class);
	
	public boolean getMarkerStatus( MarkerKinds kind ) {
		Boolean tf = markerStatus.get(kind);
		return (tf == null) ? false : tf;
	}
	
	public double getMarkerSearchRadius( MarkerKinds kind ) { // meters
		return 5.0e3; // TODO from GUI
	}

	/**
	 * Create the panel.
	 */
	public ControlPanel() {
		setLayout(null);
		
		chckbxWalmarts = new JCheckBox("Walmarts");
		chckbxWalmarts.setBounds(22, 22, 97, 23);
		chckbxWalmarts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( MarkerKinds.WALMARTS, selected );
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		add(chckbxWalmarts);
		
		chckbxSamsClubs = new JCheckBox("Sam's Clubs");
		chckbxSamsClubs.setBounds(22, 51, 97, 23);
		chckbxSamsClubs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( MarkerKinds.SAMSCLUBS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		add(chckbxSamsClubs);
		
		chckbxCostco = new JCheckBox("Costcos");
		chckbxCostco.setBounds(22, 77, 97, 23);
		chckbxCostco.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( MarkerKinds.COSTCOS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		add(chckbxCostco);
		
		chckbxTruckStops = new JCheckBox("Truck Stops");
		chckbxTruckStops.setBounds(22, 103, 97, 23);
		chckbxTruckStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( MarkerKinds.TRUCKSTOPS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		add(chckbxTruckStops);
		
		chckbxRestAreas = new JCheckBox("Rest Areas");
		chckbxRestAreas.setBounds(22, 129, 97, 23);
		chckbxRestAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( MarkerKinds.RESTAREAS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		add(chckbxRestAreas);
		
		JButton btnRoute = new JButton("Route");
		btnRoute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Route", event));
			}
		});
		btnRoute.setBounds(10, 168, 89, 23);
		add(btnRoute);
		
		JButton btnFirebug = new JButton("FireBug");
		btnFirebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.FireBug", event));
			}
		});
		btnFirebug.setBounds(10, 266, 89, 23);
		add(btnFirebug);
	}
	
}
