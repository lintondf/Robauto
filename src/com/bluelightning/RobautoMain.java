package com.bluelightning;

import java.awt.EventQueue;

import org.slf4j.Logger;

import com.bluelightning.data.TripPlan;

public class RobautoMain {
	
	static PlannerMode plannerMode;
	static TravelMode  travelMode;
	public static TripPlan tripPlan;
	public static Logger logger;
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					plannerMode = new PlannerMode();
					plannerMode.setVisible(true);
					travelMode = new TravelMode();
					travelMode.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

}
