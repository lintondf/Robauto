package com.bluelightning.gui;

import javax.swing.JPanel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import com.bluelightning.Events;
import com.bluelightning.PlannerMode;

import javax.swing.border.LineBorder;

import java.awt.Color;

import javax.swing.JCheckBox;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.awt.GridLayout;

import javax.swing.JComboBox;

public class MainControlPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected EnumMap<PlannerMode.MarkerKinds, Boolean> markerStatus = new EnumMap<PlannerMode.MarkerKinds, Boolean>(PlannerMode.MarkerKinds.class);
	
	public boolean getMarkerStatus( PlannerMode.MarkerKinds kind ) {
		Boolean tf = markerStatus.get(kind);
		return (tf == null) ? false : tf;
	}
	
	public double getMarkerSearchRadius( PlannerMode.MarkerKinds kind ) { // meters
		return 5.0e3; // TODO from GUI
	}

	/**
	 * Create the panel.
	 */
	public MainControlPanel() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {40, 0};
		gridBagLayout.rowHeights = new int[] {20, 0, 0, 0, 20, 20, 20};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Show", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		panel.setLayout(new GridLayout(6, 1, 0, 0));
		
		JCheckBox chckbxWalmarts = new JCheckBox("Walmarts");
		chckbxWalmarts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( PlannerMode.MarkerKinds.WALMARTS, selected );
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxWalmarts);
		
		JCheckBox chckbxSamsClubs = new JCheckBox("Sam's Clubs");
		chckbxSamsClubs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( PlannerMode.MarkerKinds.SAMSCLUBS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxSamsClubs);
		
		JCheckBox chckbxCostcos = new JCheckBox("Costcos");
		chckbxCostcos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( PlannerMode.MarkerKinds.COSTCOS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxCostcos);
		
		JCheckBox chckbxMurphy = new JCheckBox("Murphy");
		chckbxMurphy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( PlannerMode.MarkerKinds.MURPHY, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxMurphy);
		
		JCheckBox chckbxTruckStops = new JCheckBox("Truck Stops");
		chckbxTruckStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( PlannerMode.MarkerKinds.TRUCKSTOPS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxTruckStops);
		
//		JCheckBox chckbxCabelas = new JCheckBox("Cabelas");
//		chckbxTruckStops.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				boolean selected = ((JCheckBox) event.getSource()).isSelected();
//				markerStatus.put( Main.MarkerKinds.CABELAS, selected);
//				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
//			}
//		});
//		panel.add(chckbxCabelas);
		
		JCheckBox chckbxRestAreas = new JCheckBox("RestAreas");
		chckbxRestAreas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( PlannerMode.MarkerKinds.RESTAREAS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxRestAreas);
		
//		JButton bthStopsToBasecamp = new JButton("Stops to Basecamp");
//		GridBagConstraints gbc_bthStopsToBasecamp = new GridBagConstraints();
//		gbc_bthStopsToBasecamp.fill = GridBagConstraints.HORIZONTAL;
//		gbc_bthStopsToBasecamp.insets = new Insets(0, 0, 5, 0);
//		gbc_bthStopsToBasecamp.gridx = 0;
//		gbc_bthStopsToBasecamp.gridy = 1;
//		bthStopsToBasecamp.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent event) {
//				Events.eventBus.post( new Events.UiEvent("ControlPanel.StopsToBasecamp", event));
//			}
//		});
//		add(bthStopsToBasecamp, gbc_bthStopsToBasecamp);
		
		JButton bthPlanRoute = new JButton("Open...");
		GridBagConstraints gbc_bthPlanRoute = new GridBagConstraints();
		gbc_bthPlanRoute.fill = GridBagConstraints.HORIZONTAL;
		gbc_bthPlanRoute.insets = new Insets(0, 0, 5, 0);
		gbc_bthPlanRoute.gridx = 0;
		gbc_bthPlanRoute.gridy = 2;
		bthPlanRoute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.RouteOpen", event));
			}
		});
		add(bthPlanRoute, gbc_bthPlanRoute);
		
		JButton btnOptimizeStops = new JButton("Optimize Stops");
		GridBagConstraints gbc_btnOptimizeStops = new GridBagConstraints();
		gbc_btnOptimizeStops.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOptimizeStops.insets = new Insets(0, 0, 5, 0);
		gbc_btnOptimizeStops.gridx = 0;
		gbc_btnOptimizeStops.gridy = 3;
		btnOptimizeStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Optimize", event));
			}
		});
		add(btnOptimizeStops, gbc_btnOptimizeStops);
		
		JButton btnFirebug = new JButton("Firebug");
		GridBagConstraints gbc_btnFirebug = new GridBagConstraints();
		gbc_btnFirebug.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFirebug.gridx = 0;
		gbc_btnFirebug.gridy = 7;
		btnFirebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.FireBug", event));
			}
		});
		add(btnFirebug, gbc_btnFirebug);
		
		JButton btnFinalizeRoute = new JButton("Finalize Route");
		GridBagConstraints gbc_btnFinalizeRoute = new GridBagConstraints();
		gbc_btnFinalizeRoute.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFinalizeRoute.insets = new Insets(0, 0, 5, 0);
		gbc_btnFinalizeRoute.gridx = 0;
		gbc_btnFinalizeRoute.gridy = 4;
		add(btnFinalizeRoute, gbc_btnFinalizeRoute);
		btnFinalizeRoute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Finalize", event));
			}
		});
		
		String[] actions = {"Normal", "Clear Route", "Clear Places"};
		JComboBox<?> clearActionsBox = new JComboBox<Object>(actions);
		GridBagConstraints gbc_clearActionsBox = new GridBagConstraints();
		gbc_clearActionsBox.insets = new Insets(0, 0, 5, 0);
		gbc_clearActionsBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_clearActionsBox.gridx = 0;
		gbc_clearActionsBox.gridy = 5;
		add(clearActionsBox, gbc_clearActionsBox);
		clearActionsBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.ClearActions", event));
			}
		});
		
		
		JButton btnTravelMode = new JButton("Set Starting Fuel");
		GridBagConstraints gbc_btnCopilotOutput = new GridBagConstraints();
		gbc_btnCopilotOutput.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnCopilotOutput.insets = new Insets(0, 0, 5, 0);
		gbc_btnCopilotOutput.gridx = 0;
		gbc_btnCopilotOutput.gridy = 6;
		add(btnTravelMode, gbc_btnCopilotOutput);
		btnTravelMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.SetFuel", event));
			}
		});
		

	}

}
