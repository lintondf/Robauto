/**
 * 
 */
package com.bluelightning.map;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;

import com.bluelightning.Events;
import com.bluelightning.LatLon;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIResult;

/**
 * @author NOOK
 *
 */
public class POIMarker extends ButtonWaypoint {

	POIResult result;
	
	public POIMarker(POIResult result) {
		super( new ImageIcon(result.poi.getImage()), 
				new GeoPosition(result.poi.getLatitude(), result.poi.getLongitude() ));
		this.result = result;
		setToolTipText(result.toReport());
		setSize((int) ButtonWaypoint.preferredSize.getWidth(), (int) ButtonWaypoint.preferredSize.getHeight());
		setPreferredSize(ButtonWaypoint.preferredSize);
		addMouseListener(new POIMarkerMouseListener());
		setVisible(true);
	}
	
	public POI getPOI() {
		return result.poi;
	}
	
	public POIResult getPOIResult() {
		return result;
	}

	public static List<POIMarker> factory( List<POIResult> in) {
		ArrayList<POIMarker> out = new ArrayList<>();
		in.forEach( r -> { 
			out.add(new POIMarker(r)); 
		});
		return out;
	}

	@Override
	public GeoPosition getPosition() {
		return geoPosition;
	}

	private class POIMarkerMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
//			JOptionPane.showMessageDialog(POIMarker.this, "You clicked on " + result.toReport(), "POI Marker", JOptionPane.PLAIN_MESSAGE);
			Events.eventBus.post( new Events.POIClickEvent(result.poi));
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
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
