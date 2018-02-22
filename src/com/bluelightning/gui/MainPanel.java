package com.bluelightning.gui;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import java.awt.BorderLayout;
import javax.swing.JLayeredPane;
import javax.swing.JTextArea;

public class MainPanel extends JPanel {

	protected JSplitPane upperLowerPane;
	protected JSplitPane leftRightPane;
	protected JPanel leftPanel;
	protected JTabbedPane rightTabbedPane;
	protected JPanel lowerPanel;
	protected JTextArea lowerTextArea;

	public JSplitPane getUpperLowerPane() {
		return upperLowerPane;
	}

	public void setUpperLowerPane(JSplitPane upperLowerPane) {
		this.upperLowerPane = upperLowerPane;
	}

	public JSplitPane getLeftRightPane() {
		return leftRightPane;
	}

	public void setLeftRightPane(JSplitPane leftRightPane) {
		this.leftRightPane = leftRightPane;
	}

	public JPanel getLeftPanel() {
		return leftPanel;
	}

	public void setLeftPanel(JPanel leftPanel) {
		this.leftPanel = leftPanel;
	}

	public JTabbedPane getRightTabbedPane() {
		return rightTabbedPane;
	}

	public void setRightTabbedPane(JTabbedPane rightTabbedPane) {
		this.rightTabbedPane = rightTabbedPane;
	}

	public JPanel getLowerPanel() {
		return lowerPanel;
	}

	public void setLowerPanel(JPanel lowerPanel) {
		this.lowerPanel = lowerPanel;
	}

	public JTextArea getLowerTextArea() {
		return lowerTextArea;
	}

	public void setLowerTextArea(JTextArea lowerTextArea) {
		this.lowerTextArea = lowerTextArea;
	}

	/**
	 * Create the panel.
	 */
	public MainPanel() {
		setLayout(new BorderLayout(0, 0));
		
		upperLowerPane = new JSplitPane();
		upperLowerPane.setResizeWeight(0.75);
		upperLowerPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(upperLowerPane);
		
		leftRightPane = new JSplitPane();
		leftRightPane.setResizeWeight(0.25);
		upperLowerPane.setLeftComponent(leftRightPane);
		
		leftPanel = new JPanel();
		leftRightPane.setLeftComponent(leftPanel);
		
		rightTabbedPane = new JTabbedPane();
		leftRightPane.setRightComponent(rightTabbedPane);
		
		lowerPanel = new JPanel();
		upperLowerPane.setRightComponent(lowerPanel);
		lowerPanel.setLayout(new BorderLayout(0, 0));
		
		lowerTextArea = new JTextArea();
		lowerTextArea.setText("Log Messages");
		lowerPanel.add(lowerTextArea);

	}

}
