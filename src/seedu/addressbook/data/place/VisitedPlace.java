package seedu.addressbook.data.place;

import java.io.Serializable;

import com.bluelightning.data.TripPlan;
import com.bluelightning.poi.POI;
import com.bluelightning.poi.POIBase;

import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.tag.UniqueTagList;

public class VisitedPlace extends Place implements Comparable<VisitedPlace>, Serializable {
	
	private static final long serialVersionUID = 1L;
	
	Integer visitOrder;
	boolean passThru;
	boolean overnight;
	boolean driverSwitch;
	POI.FuelAvailable fuelAvailable;
	double  fuel;            // amount to purchase in gallons; zero if none
	
	public VisitedPlace() {
		super();
		visitOrder = new Integer(1);
	}

	public VisitedPlace(ReadOnlyPlace source) {
		super(source);
		visitOrder = new Integer(1);
	}
	
	public VisitedPlace( POI poi ) throws IllegalValueException {
		super(new Name(poi.getName()), 
			  poi.getLatitude(), 
			  poi.getLongitude(), 
			  new Address(poi.getAddress()), 
			  poi.getTags() );
		fuelAvailable = poi.getFuelAvailable();
		visitOrder = new Integer(1);
	}
	
	public VisitedPlace( TripPlan.StopData stop )  throws IllegalValueException {
		super (new Name(stop.name),
			   stop.getLatitude(),
			   stop.getLongitude(),
			   new Address(stop.getAddress()),
			   POIBase.toFuelTags( POIBase.fromFuelString(stop.fuelAvailable)) );
		fuelAvailable = POIBase.fromFuelString(stop.fuelAvailable);
		visitOrder = new Integer(1);
	}
	
	public String toString() {
		String label = String.format("%s/%s", getName().fullName, getAddress().value );
		return String.format("%10.6f, %10.6f  %s", getLatitude(), getLongitude(), label);
	}
	
	public String toGeo() {
		return toGeo(passThru);
	}
	
	public String toGeo(Boolean passThru) {
		//geo!37.7914050,-122.3987030;;My Home
		String label = String.format("%s/%s", getName().fullName, getAddress().value );
		return String.format("geo!%s!%f,%f;;%s", (passThru)? "passThrough" : "stopOver", getLatitude(), getLongitude(), label);
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
		return (visitOrder == null) ? new Integer(1) : visitOrder;
	}

	public void setVisitOrder(Integer visitOrder) {
		this.visitOrder = visitOrder;
	}

	public POI.FuelAvailable getFuelAvailable() {
		if (fuelAvailable == null)
			fuelAvailable = new POI.FuelAvailable();
		return fuelAvailable;
	}

	public void setFuelAvailable(POI.FuelAvailable fuelAvailable) {
		this.fuelAvailable = fuelAvailable;
	}

}
