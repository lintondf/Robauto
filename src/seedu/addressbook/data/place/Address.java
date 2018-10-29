package seedu.addressbook.data.place;

import java.io.Serializable;

import seedu.addressbook.data.exception.IllegalValueException;

/**
 * Represents a Person's address in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidAddress(String)}
 */
public class Address implements Serializable {

	private static final long serialVersionUID = -10030140998752416L;
	public static final String EXAMPLE = "123, some street";
    public static final String MESSAGE_ADDRESS_CONSTRAINTS = "Person addresses can be in any format";
    public static final String ADDRESS_VALIDATION_REGEX = ".+";

    public String value;
    private boolean isPrivate;

    /**
     * Validates given address.
     *
     * @throws IllegalValueException if given address string is invalid.
     */
    public Address(String address) throws IllegalValueException {
        this.isPrivate = false;
//        if (!isValidAddress(address)) {
//            throw new IllegalValueException(MESSAGE_ADDRESS_CONSTRAINTS);
//        }
        this.value = address;
    }

    public Address() {
		value = "";
	}
    
    public String getStreetAddress() {
    	if (value == null || value.isEmpty())
    		return "";
    	String[] fields = value.split(",");
    	if (fields.length < 1)
    		return "";
    	return fields[0].trim();
    }

    public String getCity() {
    	if (value == null || value.isEmpty())
    		return "";
    	String[] fields = value.split(",");
    	if (fields.length < 2)
    		return "";
    	return fields[1].trim();
    }

    public String getState() {
    	if (value == null || value.isEmpty())
    		return "";
    	String[] fields = value.split(",");
    	if (fields.length < 3)
    		return "";
    	return fields[2].trim();
    }

	/**
     * Returns true if a given string is a valid person email.
     */
    public static boolean isValidAddress(String test) {
        return test.matches(ADDRESS_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Address // instanceof handles nulls
                && this.value.equals(((Address) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public boolean isPrivate() {
        return isPrivate;
    }
    
//    private void readObject(java.io.ObjectInputStream in)
//    		 throws IOException, ClassNotFoundException {
//        int version = in.readInt();
//        this.value = (String)in.readObject();
//        this.isPrivate = (Boolean)in.readObject();
//    }
}