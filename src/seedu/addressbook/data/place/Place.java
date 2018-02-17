package seedu.addressbook.data.place;

import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.data.tag.UniqueTagList;
import seedu.addressbook.data.tag.UniqueTagList.DuplicateTagException;

import java.util.List;
import java.util.Objects;

/**
 * Represents a Place in the address book.
 * Guarantees: details are present and not null, field values are validated.
 */
public class Place implements ReadOnlyPlace {

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
    /**
     * Assumption: Every field must be present and not null.
     */
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

}
