package com.bluelightning.map;

import javax.swing.JPanel;

import com.bluelightning.Events;

import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ControlPanel extends JPanel {

	/**
	 * Create the panel.
	 */
	public ControlPanel() {
		setLayout(null);
		
		JCheckBox chckbxWalmarts = new JCheckBox("Walmarts");
		chckbxWalmarts.setBounds(22, 22, 97, 23);
		add(chckbxWalmarts);
		
		JCheckBox chckbxSamsClubs = new JCheckBox("Sams Clubs");
		chckbxSamsClubs.setBounds(22, 51, 97, 23);
		add(chckbxSamsClubs);
		
		JCheckBox chckbxCostco = new JCheckBox("Costco");
		chckbxCostco.setBounds(22, 77, 97, 23);
		add(chckbxCostco);
		
		JCheckBox chckbxTruckStops = new JCheckBox("Truck Stops");
		chckbxTruckStops.setBounds(22, 103, 97, 23);
		add(chckbxTruckStops);
		
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
		btnFirebug.setBounds(10, 202, 89, 23);
		add(btnFirebug);

	}
}
