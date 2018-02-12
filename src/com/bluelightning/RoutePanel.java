package com.bluelightning;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.GridBagLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import javax.swing.SpringLayout;
import javax.swing.table.TableColumn;
import javax.swing.JTextField;
import javax.swing.JTable;

public class RoutePanel extends JPanel {

	protected DefaultListModel<String> listModel = new DefaultListModel<String>();
	private   JTable wayPointList;

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
		wayPointList = new JTable(data, columnNames);
		TableColumn column = null;
		for (int i = 0; i < columnWidths.length; i++) {
		    column = wayPointList.getColumnModel().getColumn(i);
	        column.setPreferredWidth(columnWidths[i]);
		}
		JScrollPane scrollPane = new JScrollPane(wayPointList);
		wayPointList.setFillsViewportHeight(true);
		SpringLayout springLayout = new SpringLayout();
		springLayout.putConstraint(SpringLayout.EAST, scrollPane, 424, SpringLayout.WEST, this);
		setLayout(springLayout);

		springLayout.putConstraint(SpringLayout.NORTH, scrollPane, 10, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.WEST, scrollPane, 23, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, scrollPane, -10, SpringLayout.SOUTH, this);
		add(scrollPane);
		
	}

	public JTable getWayPointList() {
		return wayPointList;
	}

	public static class MyListCellRenderer extends JLabel implements ListCellRenderer {

		
		public MyListCellRenderer() {
			setOpaque(true);
			setHorizontalAlignment(LEFT);
			setVerticalAlignment(CENTER);
		}

		/*
		 * This method finds the image and text corresponding to the selected
		 * value and returns the label, set up to display the text and image.
		 */
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
//			// Get the selected index. (The index param isn't
//			// always valid, so just use the value.)
//			int selectedIndex = ((Integer) value).intValue();
//
//			if (isSelected) {
//				setBackground(list.getSelectionBackground());
//				setForeground(list.getSelectionForeground());
//			} else {
//				setBackground(list.getBackground());
//				setForeground(list.getForeground());
//			}
//
			this.setSize((int)list.getSize().getWidth(), 40 );
			setText(String.format("<html>%s</html>", (String) value));
			setFont(list.getFont());

			return this;
		}

	}
}
