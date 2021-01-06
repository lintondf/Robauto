package com.bluelightning;

import java.util.ArrayList;

import seedu.addressbook.data.place.VisitedPlace;

public interface TripSource {
	
	public ArrayList<Day> getDays();
	public ArrayList<VisitedPlace> getPlaces();
}
