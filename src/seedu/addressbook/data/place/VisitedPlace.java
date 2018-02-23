package seedu.addressbook.data.place;

import java.io.Serializable;

import com.bluelightning.poi.POI;

import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.tag.UniqueTagList;

public class VisitedPlace extends Place implements Comparable<VisitedPlace>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	Integer visitOrder;
	boolean passThru;
	boolean overnight;
	boolean driverSwitch;
	double  fuel;            // amount to purchase in gallons; zero if none
	
	public VisitedPlace() {
		
	}

	public VisitedPlace(ReadOnlyPlace source) {
		super(source);
	}
	
	public VisitedPlace( POI poi ) throws IllegalValueException {
		super(new Name(poi.getName()), poi.getLatitude(), poi.getLongitude(), new Address(poi.getAddress()), poi.getTags() );
	}
	
	public String toGeo() {
		//geo!37.7914050,-122.3987030;;My Home
		String label = String.format("%s %s", getName().fullName, getAddress().value );
		return String.format("geo!%f,%f;;%s", getLatitude(), getLongitude(), label);
	}
	

	@Override
	public int compareTo(VisitedPlace that) {
		return visitOrder.compareTo(that.visitOrder);
	}

	public boolean isPassThru() {
		return passThru;
	}

	public void setPassThru(boolean passThru) {
		this.passThru = passThru;
	}

	public boolean isOvernight() {
		return overnight;
	}

	public void setOvernight(boolean overnight) {
		this.overnight = overnight;
	}

	public boolean isDriverSwitch() {
		return driverSwitch;
	}

	public void setDriverSwitch(boolean driverSwitch) {
		this.driverSwitch = driverSwitch;
	}

	public double getFuel() {
		return fuel;
	}

	public void setFuel(double fuel) {
		this.fuel = fuel;
	}

	public Integer getVisitOrder() {
		return visitOrder;
	}

	public void setVisitOrder(Integer visitOrder) {
		this.visitOrder = visitOrder;
	}

}
