/**
 * 
 */
package com.bluelightning.map;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.EnumMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author NOOK
 *
 */
public class StopMarker extends ButtonWaypoint {
	
	public enum Kind { ORIGIN, FUEL, DRIVERS, OVERNIGHT, TERMINUS };
	
	protected static  EnumMap<Kind, Image>  images = new EnumMap<Kind, Image>(Kind.class);

	static {
		Dimension size = ButtonWaypoint.getImageSize();
		Image image = null;
		try {
			image = ImageIO.read(new File("images/starting_waypoint.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.put( Kind.ORIGIN, image);
		try {
			image = ImageIO.read(new File("images/fuel_waypoint.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.put( Kind.FUEL, image);
		try {
			image = ImageIO.read(new File("images/drivers_waypoint.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.put( Kind.DRIVERS, image);
		try {
			image = ImageIO.read(new File("images/overnight_waypoint.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.put( Kind.OVERNIGHT, image);
		try {
			image = ImageIO.read(new File("images/ending_waypoint.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.put( Kind.TERMINUS, image);
//		images.forEach( (k,i) -> {
//			System.out.println(k + " " + i);
//		});
	}
	
	protected String text;

	public StopMarker(Kind kind, String text, GeoPosition geoPosition) {
		super( new ImageIcon(images.get(kind)), 
			  geoPosition);
		this.text = text;
		this.setToolTipText(text);
		this.addMouseListener(new StopMarkerMouseListener());
	}
	
	private class StopMarkerMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			JOptionPane.showMessageDialog(StopMarker.this, "You clicked on " + text);
			//StopMarker.this.setVisible(false);
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
