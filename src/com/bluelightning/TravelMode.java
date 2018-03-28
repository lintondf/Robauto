package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.bluelightning.Events.StopsCommitEvent;
import com.bluelightning.Events.UiEvent;
import com.bluelightning.PlannerMode.UiHandler;
import com.google.common.eventbus.Subscribe;

import javax.swing.JSplitPane;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import java.awt.Font;

public class TravelMode extends JPanel {

	protected JList listOfDays;
	protected JSplitPane splitPane;
	
	public class UiHandler {
		@Subscribe
		protected void handle(UiEvent event) {
			//System.out.println(event.source + " " + event.awtEvent);
			switch (event.source) {
			case "TravelControl.PlannerMode":
				Events.eventBus.post( new Events.UiEvent("PlannerMode", null));
				break;
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public TravelMode() {
		setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		this.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnPlannerMode = new JButton("Planner Mode");
		btnPlannerMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Events.eventBus.post( new Events.UiEvent("TravelControl.PlannerMode", event));
			}
		});
		buttonPanel.add(btnPlannerMode);
		
		JButton btnPriorDay = new JButton("Prior Day");
		buttonPanel.add(btnPriorDay);
		
		JButton btnNextDay = new JButton("Next Day");
		buttonPanel.add(btnNextDay);
		
		JButton btnSelectDay = new JButton("Select Day");
		buttonPanel.add(btnSelectDay);
		
		splitPane = new JSplitPane();
		this.add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		this.add(scrollPane, BorderLayout.NORTH);
		
		String[] days = {"Day 01 - Melbourne to Pooler", "Day 02 - Pooler to Banner Elk"};
		listOfDays = new JList(days);
		listOfDays.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(listOfDays);
		scrollPane.setVisible(true);
		splitPane.setVisible(false);
		
		SwingUtilities.updateComponentTreeUI(this);
		
		// Bind event handlers
		Events.eventBus.register(new UiHandler());
		
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TravelMode frame = new TravelMode();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
