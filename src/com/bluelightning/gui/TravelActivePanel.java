package com.bluelightning.gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JSplitPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIDefaults;
import javax.swing.text.DefaultCaret;

public class TravelActivePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected JPanel leftPanel;
	protected JProgressBar progressBar;
	protected JTextPane textPane;
	protected TravelSpeedPanel componentsPanel;
	protected JSplitPane splitPane;
	protected JScrollPane scroll;
	
	protected JPanel mapPanel;
	protected JPanel nextPanel;
	protected JTextPane nextText;

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
		progressBar.setVisible(false);

		textPane = new JTextPane();
		scroll = new JScrollPane(textPane);
		rightPanel.add(scroll, BorderLayout.CENTER);
		DefaultCaret caret = (DefaultCaret) textPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		JSplitPane leftSplit = new JSplitPane();
		leftSplit.setOrientation( JSplitPane.VERTICAL_SPLIT );
		leftSplit.setResizeWeight(0.7);
		leftPanel.add(leftSplit, BorderLayout.CENTER);
		
        mapPanel = new JPanel();
        leftSplit.setLeftComponent(mapPanel);
        mapPanel.setLayout(new BorderLayout(0, 0));
        
        nextPanel = new JPanel();
        leftSplit.setRightComponent(nextPanel);
        nextPanel.setLayout(new BorderLayout(0, 0));
        
        nextText = new JTextPane();
        nextPanel.add( nextText, BorderLayout.CENTER);
        
        nextText.setEnabled(false);
        Color fgColor = Color.BLACK;
        Color bgColor = Color.CYAN;
        UIDefaults defaults = new UIDefaults();
        defaults.put("TextPane[Disabled].backgroundPainter", bgColor);
        //defaults.put("TextPane[Disabled].textForeground", Color.BLACK );
        //defaults.put("TextPane.disabledText", Color.BLACK );
        //defaults.put("TextPane.foreground", Color.BLACK );
        //defaults.put("TextPane[Enabled].textForeground", Color.BLACK );
        nextText.putClientProperty("Nimbus.Overrides", defaults);
        //nextText.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
        nextText.setBackground(bgColor);
        nextText.setForeground( fgColor );
        nextText.setSelectedTextColor(fgColor);
        nextText.setDisabledTextColor(fgColor);
	}

	public JPanel getMapPanel() {
		return mapPanel;
	}
	
	public JTextPane getNextTextPane() {
		return nextText;
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
