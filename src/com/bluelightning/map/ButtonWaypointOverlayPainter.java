package com.bluelightning.map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointPainter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;

/**
 * "Paints" the Swing waypoints. In fact, just takes care of correct positioning
 * of the representing button.
 *
 * @author Daniel Stahr
 */
public class ButtonWaypointOverlayPainter extends WaypointPainter<ButtonWaypoint> {

	@Override
	protected void doPaint(Graphics2D g, JXMapViewer jxMapViewer, int width, int height) {
		for (ButtonWaypoint waypoint : getWaypoints()) {
			Point2D point = jxMapViewer.getTileFactory().geoToPixel(waypoint.getPosition(), jxMapViewer.getZoom());
			Rectangle rectangle = jxMapViewer.getViewportBounds();
			int buttonX = (int) (point.getX() - rectangle.getX());
			int buttonY = (int) (point.getY() - rectangle.getY());
			JButton button = waypoint;
			button.setLocation(buttonX - button.getWidth() / 2, buttonY - button.getHeight() / 2);
		}
	}
}