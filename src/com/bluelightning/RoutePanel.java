package com.bluelightning;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JTextPane;
import javax.swing.table.TableColumn;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTabbedPane;

public class RoutePanel extends JPanel {
	private JTable      waypointList;
	protected JTextPane htmlPane;


	/**
	 * Create the panel.
	 */
	public RoutePanel() {
		String[] pointAddresses = { "3533 Carambola Cir, Melbourne, FL", "15 Mill Creek Circle, Pooler, GA",
				"125 Riverside Dr, Banner Elk, NC", "2350 So Pleasant Valley Rd, Winchester, VA 22601",
				"10654 Breezewood Dr, Woodstock, MD 21163-1317", "1365 Boston Post Road, Milford, CT",
				"836 Palmer Avenue, Falmouth, MA", "100 Cabelas Blvd, Scarborough, ME", "7 Manor Lane, Sullivan, ME" };
		
		String[] columnNames = {"Address","PassThru","Latitude","Longitude"};
		Object[][] data = {
				{"3533 Carambola Cir, Melbourne, FL", new Boolean(false), new Double(28), new Double(-81)}
		};
		int[] columnWidths = { 200, 10, 50, 50 };
		waypointList = new JTable(data, columnNames);
		TableColumn column = null;
		for (int i = 0; i < columnWidths.length; i++) {
		    column = waypointList.getColumnModel().getColumn(i);
	        column.setPreferredWidth(columnWidths[i]);
		}
		JScrollPane scrollPane = new JScrollPane(waypointList);
		waypointList.setFillsViewportHeight(true);
		setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		add(splitPane);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.8);
		splitPane.setLeftComponent(splitPane_1);
		
		JPanel upperLeftPanel = new JPanel();
		splitPane_1.setRightComponent(upperLeftPanel);
		upperLeftPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnNewButton = new JButton("New button");
		upperLeftPanel.add(btnNewButton);
		
		JPanel upperRightPanel = new JPanel();
		splitPane_1.setLeftComponent(upperRightPanel);
		upperRightPanel.setLayout(new BorderLayout(0, 0));
		
		upperRightPanel.add(scrollPane, BorderLayout.NORTH);
		
		JPanel lowerPanel = new JPanel();
		splitPane.setRightComponent(lowerPanel);
		lowerPanel.setLayout(new BorderLayout(0, 0));
		
		htmlPane = new JTextPane();
		JScrollPane scrollPane_1 = new JScrollPane(htmlPane);
		lowerPanel.add(scrollPane_1, BorderLayout.NORTH);

	}

	
	public JTable getRouteTable() {
		return waypointList;
	}

	public JTextPane getHtmlPane() {
		return htmlPane;
	}

}
