package seedu.addressbook.data.place;

import seedu.addressbook.common.Utils;
import seedu.addressbook.data.exception.DuplicateDataException;

import java.util.*;

/**
 * A list of places. Does not allow null elements or duplicates.
 *
 * @see Place#equals(Object)
 * @see Utils#elementsAreUnique(Collection)
 */
public class UniquePlaceList implements Iterable<Place> {

    /**
     * Signals that an operation would have violated the 'no duplicates' property of the list.
     */
    public static class DuplicatePlaceException extends DuplicateDataException {
        protected DuplicatePlaceException() {
            super("Operation would result in duplicate places");
        }
    }

    /**
     * Signals that an operation targeting a specified place in the list would fail because
     * there is no such matching place in the list.
     */
    public static class PlaceNotFoundException extends Exception {}

    private final List<Place> internalList = new ArrayList<>();

    /**
     * Constructs empty place list.
     */
    public UniquePlaceList() {}

    /**
     * Constructs a place list with the given places.
     */
    public UniquePlaceList(Place... places) throws DuplicatePlaceException {
        final List<Place> initialTags = Arrays.asList(places);
        if (!Utils.elementsAreUnique(initialTags)) {
            throw new DuplicatePlaceException();
        }
        internalList.addAll(initialTags);
    }

    /**
     * Constructs a list from the items in the given collection.
     * @param places a collection of places
     * @throws DuplicatePlaceException if the {@code places} contains duplicate places
     */
    public UniquePlaceList(Collection<Place> places) throws DuplicatePlaceException {
        if (!Utils.elementsAreUnique(places)) {
            throw new DuplicatePlaceException();
        }
        internalList.addAll(places);
    }

    /**
     * Constructs a shallow copy of the list.
     */
    public UniquePlaceList(UniquePlaceList source) {
        internalList.addAll(source.internalList);
    }

    /**
     * Unmodifiable java List view with elements cast as immutable {@link ReadOnlyPlace}s.
     * For use with other methods/libraries.
     * Any changes to the internal list/elements are immediately visible in the returned list.
     */
    public List<ReadOnlyPlace> immutableListView() {
        return Collections.unmodifiableList(internalList);
    }


    /**
     * Checks if the list contains an equivalent place as the given argument.
     */
    public boolean contains(ReadOnlyPlace toCheck) {
        return internalList.contains(toCheck);
    }

    /**
     * Adds a place to the list.
     *
     * @throws DuplicatePlaceException if the place to add is a duplicate of an existing place in the list.
     */
    public void add(Place toAdd) throws DuplicatePlaceException {
        if (contains(toAdd)) {
            throw new DuplicatePlaceException();
        }
        internalList.add(toAdd);
    }

    /**
     * Removes the equivalent place from the list.
     *
     * @throws PlaceNotFoundException if no such place could be found in the list.
     */
    public void remove(ReadOnlyPlace toRemove) throws PlaceNotFoundException {
        final boolean placeFoundAndDeleted = internalList.remove(toRemove);
        if (!placeFoundAndDeleted) {
            throw new PlaceNotFoundException();
        }
    }

    
    /**
     * Replace a place in the list.
     *
     * @throws PlaceNotFoundException if no such place could be found in the list.
     */
    public void update( Place toReplace ) throws PlaceNotFoundException {
        final boolean placeFoundAndDeleted = internalList.remove(toReplace);
        if (!placeFoundAndDeleted) {
            throw new PlaceNotFoundException();
        }
        internalList.add( toReplace );
    }
    
    
    /**
     * Clears all places in list.
     */
    public void clear() {
        internalList.clear();
    }

    @Override
    public Iterator<Place> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniquePlaceList // instanceof handles nulls
                && this.internalList.equals(
                        ((UniquePlaceList) other).internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

	public void sort() {
		Collections.sort(internalList);
	}

}
