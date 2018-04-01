package com.bluelightning.gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JSplitPane;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

public class TravelActivePanel extends JPanel {

	protected JPanel leftPanel;
	protected JProgressBar progressBar;
	protected JTextPane textPane;
	private JPanel componentsPanel;

	/**
	 * Create the panel.
	 */
	public TravelActivePanel() {
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);
		
		leftPanel = new JPanel();
		splitPane.setLeftComponent(leftPanel);
		leftPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel rightPanel = new JPanel();
		splitPane.setRightComponent(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));
		
		progressBar = new JProgressBar();
		rightPanel.add(progressBar, BorderLayout.SOUTH);
		
		textPane = new JTextPane();
		rightPanel.add(textPane, BorderLayout.CENTER);
		
		componentsPanel = new JPanel();
		rightPanel.add(componentsPanel, BorderLayout.NORTH);

	}

	public JPanel getLeftPanel() {
		return leftPanel;
	}

	public void setLeftPanel(JPanel leftPanel) {
		this.leftPanel = leftPanel;
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public JTextPane getTextPane() {
		return textPane;
	}

	public void setTextPane(JTextPane textArea) {
		this.textPane = textArea;
	}

	public JPanel getComponentsPanel() {
		return componentsPanel;
	}

	public void setComponentsPanel(JPanel componentsPanel) {
		this.componentsPanel = componentsPanel;
	}

}
