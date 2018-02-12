package com.bluelightning;

import java.awt.BorderLayout;
import java.awt.Label;

import javax.swing.JFrame;

import org.jxmapviewer.JXMapViewer;

import com.bluelightning.WebBrowser.UiHandler;
import com.bluelightning.map.ControlPanel;

// http://forecast.weather.gov/MapClick.php?lat=28.23&lon=-80.7&FcstType=digitalDWML

public class Main {
	
	JXMapViewer mapViewer;
	RoutePanel  routePanel;
	
	public Main() {
		// Display the viewer in a JFrame
		JFrame frame = new JFrame("RobAuto RV Trip Planner");
		MainPanel mainPanel = new MainPanel();
		mainPanel.getLeftPanel().setLayout(new BorderLayout() );
		routePanel = new RoutePanel();
		mainPanel.getRightTabbedPane().addTab("Route", null, routePanel, null);
		mapViewer = Map.factory();
		mainPanel.getRightTabbedPane().addTab("Map", null, mapViewer, null);
		WebBrowser browserCanvas = WebBrowser.factory(mainPanel);
		mainPanel.getLeftPanel().add( new ControlPanel() );
		frame.setContentPane(mainPanel);
		//frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		browserCanvas.initialize(frame);
		Events.eventBus.register(new Here2.UiHandler() );
	}

	public static void main(String[] args) {
		Main main = new Main();
	}

}
