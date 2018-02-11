package com.bluelightning;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import org.jxmapviewer.JXMapViewer;

import com.bluelightning.map.ControlPanel;

public class Main {
	
	JXMapViewer mapViewer;
	
	public Main() {
		// Display the viewer in a JFrame
		JFrame frame = new JFrame("RobAuto RV Trip Planner");
		MainPanel mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout() );
		mapViewer = Map.factory();
		mainPanel.getRightTabbedPane().addTab("Planner", null, mapViewer, null);
		WebBrowser browserCanvas = WebBrowser.factory(mainPanel);
		mainPanel.getLeftPanel().add( new ControlPanel() );
		frame.setContentPane(mainPanel);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

}
