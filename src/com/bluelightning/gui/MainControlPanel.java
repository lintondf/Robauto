package com.bluelightning.gui;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import com.bluelightning.Events;
import com.bluelightning.Main;

import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JCheckBox;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EnumMap;
import java.awt.GridLayout;

public class MainControlPanel extends JPanel {

	protected EnumMap<Main.MarkerKinds, Boolean> markerStatus = new EnumMap<Main.MarkerKinds, Boolean>(Main.MarkerKinds.class);
	
	public boolean getMarkerStatus( Main.MarkerKinds kind ) {
		Boolean tf = markerStatus.get(kind);
		return (tf == null) ? false : tf;
	}
	
	public double getMarkerSearchRadius( Main.MarkerKinds kind ) { // meters
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
				markerStatus.put( Main.MarkerKinds.WALMARTS, selected );
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxWalmarts);
		
		JCheckBox chckbxSamsClubs = new JCheckBox("Sam's Clubs");
		chckbxSamsClubs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( Main.MarkerKinds.SAMSCLUBS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxSamsClubs);
		
		JCheckBox chckbxCostcos = new JCheckBox("Costcos");
		chckbxCostcos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( Main.MarkerKinds.COSTCOS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxCostcos);
		
		JCheckBox chckbxTruckStops = new JCheckBox("Truck Stops");
		chckbxTruckStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				boolean selected = ((JCheckBox) event.getSource()).isSelected();
				markerStatus.put( Main.MarkerKinds.TRUCKSTOPS, selected);
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
				markerStatus.put( Main.MarkerKinds.RESTAREAS, selected);
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Waypoints", event));
			}
		});
		panel.add(chckbxRestAreas);
		
		JButton bthPlanRoute = new JButton("Plan Route");
		GridBagConstraints gbc_bthPlanRoute = new GridBagConstraints();
		gbc_bthPlanRoute.fill = GridBagConstraints.HORIZONTAL;
		gbc_bthPlanRoute.insets = new Insets(0, 0, 5, 0);
		gbc_bthPlanRoute.gridx = 0;
		gbc_bthPlanRoute.gridy = 1;
		bthPlanRoute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Route", event));
			}
		});
		add(bthPlanRoute, gbc_bthPlanRoute);
		
		JButton btnOptimizeStops = new JButton("Optimize Stops");
		GridBagConstraints gbc_btnOptimizeStops = new GridBagConstraints();
		gbc_btnOptimizeStops.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOptimizeStops.insets = new Insets(0, 0, 5, 0);
		gbc_btnOptimizeStops.gridx = 0;
		gbc_btnOptimizeStops.gridy = 2;
		btnOptimizeStops.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.Optimize", event));
			}
		});
		add(btnOptimizeStops, gbc_btnOptimizeStops);
		
		JButton btnFirebug = new JButton("Firebug");
		GridBagConstraints gbc_btnFirebug = new GridBagConstraints();
		gbc_btnFirebug.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFirebug.insets = new Insets(0, 0, 5, 0);
		gbc_btnFirebug.gridx = 0;
		gbc_btnFirebug.gridy = 3;
		btnFirebug.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("ControlPanel.FireBug", event));
			}
		});
		add(btnFirebug, gbc_btnFirebug);

	}

}
