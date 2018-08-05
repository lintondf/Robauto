package com.bluelightning.gui;

import javax.swing.JButton;
import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JTextField;

public class TravelSpeedPanel extends JPanel {
	private JTextField expectedSpeed;
	private JTextField averageSpeed;
	private JTextField currentSpeed;

	/**
	 * Create the panel.
	 */
	public TravelSpeedPanel() {
		this.setMaximumSize( new Dimension( 2000, 25 ));
		this.setMinimumSize( new Dimension( 100, 25 ));
		this.setPreferredSize( new Dimension( 100, 25 ));
		this.setSize(100, 25);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{25};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Speed [mph]");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.weightx = 1.0;
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		currentSpeed = new JTextField();
		GridBagConstraints gbc_currentSpeed = new GridBagConstraints();
		gbc_currentSpeed.weightx = 1.0;
		gbc_currentSpeed.insets = new Insets(0, 0, 5, 5);
		gbc_currentSpeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_currentSpeed.gridx = 1;
		gbc_currentSpeed.gridy = 0;
		add(currentSpeed, gbc_currentSpeed);
		currentSpeed.setColumns(10);
		
		averageSpeed = new JTextField();
		GridBagConstraints gbc_averageSpeed = new GridBagConstraints();
		gbc_averageSpeed.insets = new Insets(0, 0, 5, 5);
		gbc_averageSpeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_averageSpeed.gridx = 2;
		gbc_averageSpeed.gridy = 0;
		add(averageSpeed, gbc_averageSpeed);
		averageSpeed.setColumns(10);
		
		expectedSpeed = new JTextField();
		GridBagConstraints gbc_expectedSpeed = new GridBagConstraints();
		gbc_expectedSpeed.weightx = 1.0;
		gbc_expectedSpeed.insets = new Insets(0, 0, 5, 0);
		gbc_expectedSpeed.fill = GridBagConstraints.HORIZONTAL;
		gbc_expectedSpeed.gridx = 3;
		gbc_expectedSpeed.gridy = 0;
		add(expectedSpeed, gbc_expectedSpeed);
		expectedSpeed.setColumns(10);
		this.validate();
	}

}
