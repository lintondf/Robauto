package com.bluelightning.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingConstants;

public class HereRouteDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField fromPlusCode;
	private JTextField fromLatLon;
	private JTextField fromAddress;
	private JTextField toPlusCode;
	private JTextField toLatLon;
	private JTextField toAddress;
	private JButton okButton;
	private JButton cancelButton;

	/**
	 * Create the dialog.
	 */
	public HereRouteDialog() {
		setTitle("Here.com Routing");
		setBounds(100, 100, 450, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		{
			JPanel fromPanel = new JPanel();
			fromPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "From:", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
			contentPanel.add(fromPanel);
			GridBagLayout gbl_fromPanel = new GridBagLayout();
			gbl_fromPanel.columnWidths = new int[]{141, 51, 86, 0};
			gbl_fromPanel.rowHeights = new int[]{20, 0, 0};
			gbl_fromPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_fromPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			fromPanel.setLayout(gbl_fromPanel);
			{
				JLabel lblPlusCode = new JLabel("Plus Code:");
				GridBagConstraints gbc_lblPlusCode = new GridBagConstraints();
				gbc_lblPlusCode.anchor = GridBagConstraints.EAST;
				gbc_lblPlusCode.insets = new Insets(5, 5, 5, 5);
				gbc_lblPlusCode.gridx = 0;
				gbc_lblPlusCode.gridy = 0;
				fromPanel.add(lblPlusCode, gbc_lblPlusCode);
			}
			{
				fromPlusCode = new JTextField();
				GridBagConstraints gbc_fromPlusCode = new GridBagConstraints();
				gbc_fromPlusCode.insets = new Insets(2, 2, 5, 2);
				gbc_fromPlusCode.gridwidth = 4;
				gbc_fromPlusCode.fill = GridBagConstraints.HORIZONTAL;
				gbc_fromPlusCode.anchor = GridBagConstraints.NORTHWEST;
				gbc_fromPlusCode.gridx = 1;
				gbc_fromPlusCode.gridy = 0;
				fromPanel.add(fromPlusCode, gbc_fromPlusCode);
				fromPlusCode.setColumns(10);
			}
			{
				JLabel lblLatlon = new JLabel("Lat,Lon:");
				GridBagConstraints gbc_lblLatlon = new GridBagConstraints();
				gbc_lblLatlon.anchor = GridBagConstraints.EAST;
				gbc_lblLatlon.insets = new Insets(5, 5, 5, 5);
				gbc_lblLatlon.gridx = 0;
				gbc_lblLatlon.gridy = 1;
				fromPanel.add(lblLatlon, gbc_lblLatlon);
			}
			{
				fromLatLon = new JTextField();
				GridBagConstraints gbc_fromLatLon = new GridBagConstraints();
				gbc_fromLatLon.gridwidth = 4;
				gbc_fromLatLon.insets = new Insets(5, 5, 5, 5);
				gbc_fromLatLon.fill = GridBagConstraints.HORIZONTAL;
				gbc_fromLatLon.gridx = 1;
				gbc_fromLatLon.gridy = 1;
				fromPanel.add(fromLatLon, gbc_fromLatLon);
				fromLatLon.setColumns(10);
			}
			{
				JLabel lblLatlon = new JLabel("Address:");
				GridBagConstraints gbc_lblLatlon = new GridBagConstraints();
				gbc_lblLatlon.anchor = GridBagConstraints.EAST;
				gbc_lblLatlon.insets = new Insets(5, 5, 5, 5);
				gbc_lblLatlon.gridx = 0;
				gbc_lblLatlon.gridy = 2;
				fromPanel.add(lblLatlon, gbc_lblLatlon);
			}
			{
				fromAddress = new JTextField();
				GridBagConstraints gbc_fromAddress = new GridBagConstraints();
				gbc_fromAddress.gridwidth = 4;
				gbc_fromAddress.insets = new Insets(5, 5, 5, 5);
				gbc_fromAddress.fill = GridBagConstraints.HORIZONTAL;
				gbc_fromAddress.gridx = 1;
				gbc_fromAddress.gridy = 2;
				fromPanel.add(fromAddress, gbc_fromAddress);
				fromAddress.setColumns(20);
			}
		}
		{
			JPanel toPanel = new JPanel();
			toPanel.setBorder(new TitledBorder(null, "To:", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(toPanel);
			GridBagLayout gbl_toPanel = new GridBagLayout();
			gbl_toPanel.columnWidths = new int[]{141, 51, 86, 0};
			gbl_toPanel.rowHeights = new int[]{20, 0, 0};
			gbl_toPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
			gbl_toPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			toPanel.setLayout(gbl_toPanel);
			{
				JLabel lblPlusCode = new JLabel("Plus Code:");
				GridBagConstraints gbc_lblPlusCode = new GridBagConstraints();
				gbc_lblPlusCode.anchor = GridBagConstraints.EAST;
				gbc_lblPlusCode.insets = new Insets(5, 5, 5, 5);
				gbc_lblPlusCode.gridx = 0;
				gbc_lblPlusCode.gridy = 0;
				toPanel.add(lblPlusCode, gbc_lblPlusCode);
			}
			{
				toPlusCode = new JTextField();
				GridBagConstraints gbc_fromPlusCode = new GridBagConstraints();
				gbc_fromPlusCode.insets = new Insets(2, 2, 5, 2);
				gbc_fromPlusCode.gridwidth = 4;
				gbc_fromPlusCode.fill = GridBagConstraints.HORIZONTAL;
				gbc_fromPlusCode.anchor = GridBagConstraints.NORTHWEST;
				gbc_fromPlusCode.gridx = 1;
				gbc_fromPlusCode.gridy = 0;
				toPanel.add(toPlusCode, gbc_fromPlusCode);
				toPlusCode.setColumns(10);
			}
			{
				JLabel lblLatlon = new JLabel("Lat,Lon:");
				GridBagConstraints gbc_lblLatlon = new GridBagConstraints();
				gbc_lblLatlon.anchor = GridBagConstraints.EAST;
				gbc_lblLatlon.insets = new Insets(5, 5, 5, 5);
				gbc_lblLatlon.gridx = 0;
				gbc_lblLatlon.gridy = 1;
				toPanel.add(lblLatlon, gbc_lblLatlon);
			}
			{
				toLatLon = new JTextField();
				GridBagConstraints gbc_fromLatLon = new GridBagConstraints();
				gbc_fromLatLon.gridwidth = 4;
				gbc_fromLatLon.insets = new Insets(5, 5, 5, 5);
				gbc_fromLatLon.fill = GridBagConstraints.HORIZONTAL;
				gbc_fromLatLon.gridx = 1;
				gbc_fromLatLon.gridy = 1;
				toPanel.add(toLatLon, gbc_fromLatLon);
				toLatLon.setColumns(10);
			}
			{
				JLabel lblLatlon = new JLabel("Address:");
				GridBagConstraints gbc_lblLatlon = new GridBagConstraints();
				gbc_lblLatlon.anchor = GridBagConstraints.EAST;
				gbc_lblLatlon.insets = new Insets(5, 5, 5, 5);
				gbc_lblLatlon.gridx = 0;
				gbc_lblLatlon.gridy = 2;
				toPanel.add(lblLatlon, gbc_lblLatlon);
			}
			{
				toAddress = new JTextField();
				GridBagConstraints gbc_toAddress = new GridBagConstraints();
				gbc_toAddress.gridwidth = 4;
				gbc_toAddress.insets = new Insets(5, 5, 5, 5);
				gbc_toAddress.fill = GridBagConstraints.HORIZONTAL;
				gbc_toAddress.gridx = 1;
				gbc_toAddress.gridy = 2;
				toPanel.add(toAddress, gbc_toAddress);
				toAddress.setColumns(20);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	/**
	 * @return the fromPlusCode
	 */
	public JTextField getFromPlusCode() {
		return fromPlusCode;
	}

	/**
	 * @return the fromLatLon
	 */
	public JTextField getFromLatLon() {
		return fromLatLon;
	}

	/**
	 * @return the toPlusCode
	 */
	public JTextField getToPlusCode() {
		return toPlusCode;
	}

	/**
	 * @return the toLatLon
	 */
	public JTextField getToLatLon() {
		return toLatLon;
	}

	/**
	 * @return the okButton
	 */
	public JButton getOkButton() {
		return okButton;
	}

	/**
	 * @return the cancelButton
	 */
	public JButton getCancelButton() {
		return cancelButton;
	}

	/**
	 * @return the fromAddress
	 */
	public JTextField getFromAddress() {
		return fromAddress;
	}

	/**
	 * @return the toAddress
	 */
	public JTextField getToAddress() {
		return toAddress;
	}

}
