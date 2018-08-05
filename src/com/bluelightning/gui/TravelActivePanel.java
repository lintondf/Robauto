package com.bluelightning.gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;

public class TravelActivePanel extends JPanel {

	protected JPanel leftPanel;
	protected JProgressBar progressBar;
	protected JTextPane textPane;
	protected TravelSpeedPanel componentsPanel;
	protected JSplitPane splitPane;
	protected JScrollPane scroll;

	/**
	 * Create the panel.
	 */
	public TravelActivePanel() {
		setLayout(new BorderLayout(0, 0));
		
		splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		
		add(splitPane, BorderLayout.CENTER);
		
		leftPanel = new JPanel();
		splitPane.setLeftComponent(leftPanel);
		leftPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel rightPanel = new JPanel();
		splitPane.setRightComponent(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));
		
		progressBar = new JProgressBar();
		rightPanel.add(progressBar, BorderLayout.SOUTH);
		
//		componentsPanel = new TravelSpeedPanel();
//		rightPanel.add(componentsPanel, BorderLayout.NORTH);

		textPane = new JTextPane();
		scroll = new JScrollPane(textPane);
		rightPanel.add(scroll, BorderLayout.CENTER);
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

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

	public TravelSpeedPanel getComponentsPanel() {
		return componentsPanel;
	}

	public void setComponentsPanel(TravelSpeedPanel componentsPanel) {
		this.componentsPanel = componentsPanel;
	}

	public JSplitPane getSplitPane() {
		return splitPane;
	}

	public JScrollPane getScroll() {
		return scroll;
	}

}
