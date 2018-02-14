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
	
	GeoPosition geoPosition;
	protected static final Dimension size = new Dimension(18,18);
	
	public ButtonWaypoint(Icon icon, GeoPosition geoPosition) {
		super(icon);
		init(geoPosition);
	}
	

	public ButtonWaypoint(String label, GeoPosition geoPosition) {
		super(label);
		init(geoPosition);
	}
	
	private void init(GeoPosition geoPosition) {
		this.geoPosition = geoPosition;
		this.setSize((int) size.getWidth(), (int) size.getHeight());
		this.setPreferredSize(size);
		this.setOpaque(false);
		this.setBorderPainted(false);
		this.setVisible(true);
	}
	

	@Override
	public GeoPosition getPosition() {
		return geoPosition;
	}


	public static Dimension getImageSize() {
		return size;
	}

}
