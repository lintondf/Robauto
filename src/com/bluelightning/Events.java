package com.bluelightning;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ForkJoinPool;

import javax.swing.JFrame;

import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIResult;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import seedu.addressbook.data.place.ReadOnlyPlace;

public class Events {

	// System-wide event bus
	public static EventBus eventBus = new AsyncEventBus(new ForkJoinPool());
	
	public static class AddAddressStopEvent {
		public POI   poi;
		
		public AddAddressStopEvent( POI poi) {
			this.poi = poi;
		}
	}
	
	public static class AddManualStopEvent {
		public POIResult   result;
		
		public AddManualStopEvent( POIResult result) {
			this.result = result;
		}
	}
	
	public static class AddWaypointEvent {
		public ReadOnlyPlace place;

		public AddWaypointEvent( ReadOnlyPlace place) {
			this.place = place;
		}
	}
	
	public static class POIClickEvent {
		public POI   poi;
		
		public POIClickEvent( POI poi) {
			this.poi = poi;
		}
	}
	
	public static class StopsCommitEvent {
		public String html;
		
		public StopsCommitEvent( String html ) {
			this.html = html;
		}
	}
	
	public static class TripPlanUpdated {
		public TripPlanUpdated() {}
	}
	
	public static class UiEvent {
		public String source;
		public AWTEvent awtEvent;
		
		public UiEvent( String source, AWTEvent awtEvent ) {
			this.source = source;
			this.awtEvent = awtEvent;
		}
	}
	
	public static class EventActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			if (event.getActionCommand() != null && !event.getActionCommand().isEmpty()) {
				eventBus.post( new UiEvent(event.getActionCommand(), event) );
			}
		}
		
	}


}
