package com.bluelightning.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import com.bluelightning.Events;
import com.bluelightning.Here2;
import com.bluelightning.Main;
import com.bluelightning.data.TripPlan.StopData;
import com.bluelightning.Events.AddManualStopEvent;
import com.bluelightning.poi.POIResult;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class AddManualStopDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JTable choiceTable;
	protected ChoiceTableModel choiceTableModel;

	protected static class ChoiceTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		protected static final String[] names = {"Distance", "Time", "Name", "Address"};
		protected static final double[] widths = {0.10, 0.10, 0.40, 0.40};
		protected static final boolean[] centered = {true, true, true, true};
		
		protected ArrayList<POIResult> data = null;
		
		
		public ChoiceTableModel() {
		}

		@Override
		public int getColumnCount() {
			return names.length;
		}

		@Override
		public int getRowCount() {
			if (data == null)
				return 0;
			return data.size();
		}

		@Override
		public Object getValueAt(int iRow, int iCol) {
			if (data == null || iRow >= data.size())
				return null;
			POIResult result = data.get(iRow);
			switch (iCol) {
			case 0: 
				return String.format("%5.1f", result.legProgress.distance*Here2.METERS_TO_MILES);
			case 1:
				return Here2.toPeriod(result.legProgress.trafficTime);
			case 2:
				return result.poi.getName(); 
			case 3:
				return result.poi.getAddress();
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int iCol) {
			return String.class;
		}

		@Override
		public String getColumnName(int iCol) {
			return names[iCol];
		}

		public ArrayList<POIResult> getData() {
			return data;
		}

		public void setData(ArrayList<POIResult> data) {
			this.data = data;
			fireTableStructureChanged();
		}
		
		public void resizeColumns( JTable table, double totalWidth) {
			TableColumn column = null;
			for (int i = 0; i < getColumnCount(); i++) {
			    column = table.getColumnModel().getColumn(i);
			    int width = (int) (widths[i]*totalWidth);
		        column.setPreferredWidth(width);
			}			
		}

		public void layoutColumns(JTable stopsTable) {
			// TODO make this work
			DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
			centerRenderer.setHorizontalAlignment( SwingConstants.CENTER );
			for (int i = 0; i < centered.length; i++) {
				if (centered[i])
					stopsTable.getColumnModel().getColumn(i).setCellRenderer( centerRenderer );
			}
		}
		
	}
	
	public void setData(ArrayList<POIResult> data) {
		choiceTableModel.setData(data);
	}
	
	/**
	 * Create the dialog.
	 */
	public AddManualStopDialog() {
		choiceTableModel = new ChoiceTableModel();
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			choiceTable = new JTable(choiceTableModel);
			JScrollPane scrollPane = new JScrollPane(choiceTable);
			contentPanel.add(scrollPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				okButton.addActionListener( new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {
						if (choiceTable.getSelectedRow() >= 0) {
							POIResult result = choiceTableModel.getData().get(choiceTable.getSelectedRow());
							Events.eventBus.post( new Events.AddManualStopEvent(result) );
							Main.logger.info("dispose() on AMSD OK");
							AddManualStopDialog.this.dispose();
						}
					}
					
				});
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				cancelButton.addActionListener( new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent event) {
						Main.logger.info("dispose() on AMSD Cancel");
						AddManualStopDialog.this.dispose();
					}
					
				});
			}
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AddManualStopDialog dialog = new AddManualStopDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
