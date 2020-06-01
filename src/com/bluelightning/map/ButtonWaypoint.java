/**
 * 
 */
package com.bluelightning.map;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JButton;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

/**
 * @author NOOK
 *
 */
public class ButtonWaypoint extends JButton implements Waypoint {
	
	private static final long serialVersionUID = 1L;
	GeoPosition geoPosition;
	protected static Dimension preferredSize = new Dimension(48,48);
	/**
	 * @param preferredSize the preferredSize to set
	 */
	public static void setDefaultPreferredSize(Dimension preferredSize) {
		ButtonWaypoint.preferredSize = preferredSize;
	}

	public String name;
	
	protected Dimension size = preferredSize;
	
	void extractName(String userLabel ) {
		name = userLabel.trim();
		if (name.charAt(0) == '-' || Character.isDigit(name.charAt(0))) {
			String[] fields = name.replaceAll("  ",  " ").split(" ");
			if (fields.length >= 4) {
				name = "";
				for (int i = 3; i < fields.length; i++) {
					name += fields[i] + " ";
				}
				name = name.trim();
			}
		}
		//System.out.println(userLabel.trim() + " => " + name);
	}
	
	public ButtonWaypoint(String label, Icon icon, GeoPosition geoPosition) {
		super(icon);
		extractName(label);
		init(geoPosition);
	}
	

	public ButtonWaypoint(String label, GeoPosition geoPosition) {
		super(label);
		extractName(label);
		init(geoPosition);
	}
	
	private void init(GeoPosition geoPosition) {
		this.geoPosition = geoPosition;
		this.setSize((int) preferredSize.getWidth(), (int) preferredSize.getHeight());
		this.setPreferredSize(preferredSize);
		this.setOpaque(false);
		this.setContentAreaFilled(false);
		this.setBorderPainted(false);
		this.setVisible(true);
	}
	
	public String toString() {
		return String.format("%10.6f, %10.6f", this.geoPosition.getLatitude(), this.geoPosition.getLongitude() );
	}
	
	public String getName() {
		return name;
	}
	

	@Override
	public GeoPosition getPosition() {
		return geoPosition;
	}


	/**
	 * Set a new GeoPosition for this Waypoint
	 * @param coordinate a new position
	 */
	public void setPosition(GeoPosition coordinate)
	{
		GeoPosition old = getPosition();
		this.geoPosition = coordinate;
		firePropertyChange("position", old, getPosition());
	}
	
	public static Dimension getImageSize() {
		return preferredSize;
	}

}
