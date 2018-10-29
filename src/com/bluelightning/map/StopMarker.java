/**
 * 
 */
package com.bluelightning.map;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.jxmapviewer.viewer.GeoPosition;

/**
 * @author NOOK
 *
 */
public class StopMarker extends ButtonWaypoint {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int ORIGIN    = 0;
	public static final int DRIVERS   = 1;
	public static final int FUEL      = 2;
	public static final int OVERNIGHT = 4;
	public static final int TERMINUS  = 7;
	
	
//	public enum Kind { ORIGIN, FUEL, DRIVERS, OVERNIGHT, TERMINUS };
//	
//	protected static  EnumMap<Kind, Image>  images = new EnumMap<Kind, Image>(Kind.class);
	
	/*
	 * 000 - ORIGIN
	 * 001 - Drivers
	 * 010 - Fuel
	 * 011 - Drivers+Fuel
	 * 100 - Overnight
	 * 101 - INVALID
	 * 110 - Fuel + Overnight
	 * 111 - TERMINUS
	 */
	
	protected static  ArrayList<Image> images = new ArrayList<>();
	
	static {
		Dimension size = ButtonWaypoint.getImageSize();
		Image image = null;
		for (int i = ORIGIN; i <= TERMINUS; i++)
			images.add(image);
		try {
			image = ImageIO.read(new File("images/starting_waypoint.png"))
					.getScaledInstance(8, 8, Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.set( ORIGIN, image);
		try {
			image = ImageIO.read(new File("images/Stop-base.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		for (int i = ORIGIN+1; i < TERMINUS; i++)
			images.set( i, image);
		try {
			image = ImageIO.read(new File("images/Stop-D.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.set( DRIVERS, image);
		try {
			image = ImageIO.read(new File("images/Stop-DF.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.set( DRIVERS + FUEL, image);
		try {
			image = ImageIO.read(new File("images/Stop-F.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.set( FUEL, image);
		try {
			image = ImageIO.read(new File("images/Stop-FO.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.set( FUEL + OVERNIGHT, image);
		try {
			image = ImageIO.read(new File("images/Stop-O.png"))
					.getScaledInstance((int)size.getWidth(), (int)size.getHeight(), Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.set( OVERNIGHT, image);
		try {
			image = ImageIO.read(new File("images/ending_waypoint.png"))
					.getScaledInstance(8, 8, Image.SCALE_SMOOTH);
		} catch (Exception x) {}
		images.set( TERMINUS, image);
	}
	
	protected String text;
	protected int    kind;
	protected int    dayNumber = 0;

	public StopMarker(int kind, String text, GeoPosition geoPosition) {
		super( new ImageIcon(images.get(kind)), 
			  geoPosition);
		this.kind = kind;
		this.text = text;
		this.setToolTipText(text);
		this.addMouseListener(new StopMarkerMouseListener());
		//RobautoMain.logger.debug(String.format("StopMarker %o %s", kind, geoPosition.toString() ) );
	}
	
	public StopMarker(int kind, String name, String text, GeoPosition geoPosition) {
		super( new ImageIcon(images.get(kind)), 
			  geoPosition);
		this.kind = kind;
		this.setName(name);
		this.text = text;
		this.setToolTipText(text);
		this.addMouseListener(new StopMarkerMouseListener());
		//RobautoMain.logger.debug(String.format("StopMarker %o %s", kind, geoPosition.toString() ) );
	}
	
	public String toString() {
		return String.format("%d/%d, %s %s %s", kind, dayNumber, this.getName(), text, super.toString() ).replaceAll("\n", "; ");
	}

	public void setKind( int kind ) {
		this.kind = kind;
		this.setIcon( new ImageIcon(images.get(kind)) );
	}
	
	/**
	 * @return the dayNumber
	 */
	public int getDayNumber() {
		return dayNumber;
	}

	/**
	 * @param dayNumber the dayNumber to set
	 */
	public void setDayNumber(int dayNumber) {
		this.dayNumber = dayNumber;
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
