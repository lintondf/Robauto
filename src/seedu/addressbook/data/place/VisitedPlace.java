package seedu.addressbook.data.place;

public class VisitedPlace extends Place implements Comparable<VisitedPlace> {
	
	Integer visitOrder;
	boolean passThru;
	boolean overnight;
	boolean driverSwitch;
	double  fuel;            // amount to purchase in gallons; zero if none

	public VisitedPlace(ReadOnlyPlace source) {
		super(source);
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

}
