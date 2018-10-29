package seedu.addressbook.data.place;

import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.data.tag.UniqueTagList;
import seedu.addressbook.data.tag.UniqueTagList.DuplicateTagException;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.bluelightning.Here2;
import com.bluelightning.LatLon;

/**
 * Represents a Place in the address book.
 * Guarantees: details are present and not null, field values are validated.
 */
public class Place implements ReadOnlyPlace, Serializable, Comparable<Place> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Name name;
    private Double latitude;
    private Double longitude;
    private Address address;

    private final UniqueTagList tags;
    
    public static Double UNKNOWN = new Double(0);
    
    public static Place factory( String name, String address, UniqueTagList tags ) {
    	try {
			return new Place(new Name(name), UNKNOWN, UNKNOWN, new Address(address), tags);
		} catch (IllegalValueException e) {
			return null;
		}
    }

    
    public static Place factory( String name, String address, List<Tag> tags ) {
    	try {
			return factory( name, address, new UniqueTagList(tags) );
		} catch (DuplicateTagException e) {
			return null;
		}
    }
    
    
    public static Place factory( String name, String address ) {
    	return factory( name, address, new UniqueTagList());
    }
    
    
    public Place() {
        this.name = new Name();
        this.latitude = new Double(0);
        this.longitude = new Double(0);
        this.address = new Address();
        this.tags = new UniqueTagList();   	
    }

    
    public Place(Name name, Double latitude, Double longitude, Address address, UniqueTagList tags) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.tags = new UniqueTagList(tags); // protect internal tags from changes in the arg list
    }

    /**
     * Copy constructor.
     */
    public Place(ReadOnlyPlace source) {
        this(source.getName(), source.getLatitude(), source.getLongitude(), source.getAddress(), source.getTags());
    }
    
    public LatLon geocode() {
    	LatLon where = Here2.geocodeLookup(address.toString());
    	if (where != null) {
    		this.latitude = where.getLatitude();
    		this.longitude = where.getLongitude();
    	}
    	return where;
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public Double getLatitude() {
        return latitude;
    }

    @Override
    public Double getLongitude() {
        return longitude;
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public UniqueTagList getTags() {
        return new UniqueTagList(tags);
    }

    /**
     * Replaces this person's tags with the tags in the argument tag list.
     */
    public void setTags(UniqueTagList replacement) {
        tags.setTags(replacement);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof ReadOnlyPlace // instanceof handles nulls
                && this.isSameStateAs((ReadOnlyPlace) other));
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, latitude, longitude, address, tags);
    }

    @Override
    public String toString() {
        return getAsTextShowAll();
    }


	public void setName(Name name) {
		this.name = name;
	}


	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}


	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}


	public void setAddress(Address address) {
		this.address = address;
	}


	@Override
	public int compareTo(Place o) {
		return this.name.toString().compareTo(o.name.toString());
	}

}
