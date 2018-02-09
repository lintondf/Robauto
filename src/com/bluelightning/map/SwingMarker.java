package com.bluelightning.map;

import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * A waypoint that is represented by a button on the map.
 *
 * @author Daniel Stahr
 */
public class SwingMarker extends DefaultWaypoint {
	
	private JButton button;
	private final String text;
	
	private static final Dimension size = new Dimension(18,18);
	
	public static Dimension getSize() {
		return size;
	}

	public SwingMarker(GeoPosition coord, String label, String tooltip )  {
		super(coord);
		this.text = label;
		try {
			Image img = ImageIO.read(new File("images/restarea.png")); //getClass().getResource("images/costco.png"));
			Icon icon = new ImageIcon(img);
			button = new JButton(icon);
		} catch (Exception x) {
			button = new JButton(label);
		}
		button.setToolTipText(tooltip);
		button.setSize((int) size.getWidth(), (int) size.getHeight());
		button.setPreferredSize(size);
		button.addMouseListener(new SwingWaypointMouseListener());
		button.setVisible(true);
	}

	public SwingMarker(Image img, GeoPosition coord, String label, String tooltip )  {
		super(coord);
		this.text = label;
		Icon icon = new ImageIcon(img);
		button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.setSize((int) size.getWidth(), (int) size.getHeight());
		button.setPreferredSize(size);
		button.addMouseListener(new SwingWaypointMouseListener());
		button.setVisible(true);
	}

	public JButton getButton() {
		return button;
	}

	private class SwingWaypointMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			JOptionPane.showMessageDialog(button, "You clicked on " + text);
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}
}