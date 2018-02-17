package seedu.addressbook.data;

import seedu.addressbook.data.place.*;
import seedu.addressbook.data.place.UniquePlaceList.*;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.data.tag.UniqueTagList;
import seedu.addressbook.data.tag.UniqueTagList.*;

import java.util.*;

/**
 * Represents the entire address book. Contains the data of the address book.
 *
 * Guarantees:
 *  - Every tag found in every place will also be found in the tag list.
 *  - The tags in each place point to tag objects in the master list. (== equality)
 */
public class AddressBook {

    private final UniquePlaceList allPlaces;
    private final UniqueTagList allTags; // can contain tags not attached to any place

    public static AddressBook empty() {
        return new AddressBook();
    }

    /**
     * Creates an empty address book.
     */
    public AddressBook() {
        allPlaces = new UniquePlaceList();
        allTags = new UniqueTagList();
    }

    /**
     * Constructs an address book with the given data.
     * Also updates the tag list with any missing tags found in any place.
     *
     * @param places external changes to this will not affect this address book
     * @param tags external changes to this will not affect this address book
     */
    public AddressBook(UniquePlaceList places, UniqueTagList tags) {
        this.allPlaces = new UniquePlaceList(places);
        this.allTags = new UniqueTagList(tags);
        for (Place p : allPlaces) {
            syncTagsWithMasterList(p);
        }
    }

    /**
     * Ensures that every tag in this place:
     *  - exists in the master list {@link #allTags}
     *  - points to a Tag object in the master list
     */
    private void syncTagsWithMasterList(Place place) {
        final UniqueTagList placeTags = place.getTags();
        allTags.mergeFrom(placeTags);

        // Create map with values = tag object references in the master list
        final Map<Tag, Tag> masterTagObjects = new HashMap<>();
        for (Tag tag : allTags) {
            masterTagObjects.put(tag, tag);
        }

        // Rebuild the list of place tags using references from the master list
        final Set<Tag> commonTagReferences = new HashSet<>();
        for (Tag tag : placeTags) {
            commonTagReferences.add(masterTagObjects.get(tag));
        }
        place.setTags(new UniqueTagList(commonTagReferences));
    }

    /**
     * Adds a place to the address book.
     * Also checks the new place's tags and updates {@link #allTags} with any new tags found,
     * and updates the Tag objects in the place to point to those in {@link #allTags}.
     *
     * @throws DuplicatePlaceException if an equivalent place already exists.
     */
    public void add(Place toAdd) throws DuplicatePlaceException {
        syncTagsWithMasterList(toAdd);
        allPlaces.add(toAdd);
    }
    
    public void update( Place toUpdate )  throws PlaceNotFoundException {
        allPlaces.update(toUpdate);
    }


    /**
     * Checks if an equivalent place exists in the address book.
     */
    public boolean contains(ReadOnlyPlace key) {
        return allPlaces.contains(key);
    }

    /**
     * Removes the equivalent place from the address book.
     *
     * @throws PlaceNotFoundException if no such place could be found.
     */
    public void remove(ReadOnlyPlace toRemove) throws PlaceNotFoundException {
        allPlaces.remove(toRemove);
    }

    /**
     * Clears all places and tags from the address book.
     */
    public void clear() {
        allPlaces.clear();
        allTags.clear();
    }

    /**
     * Defensively copied UniqueplaceList of all places in the address book at the time of the call.
     */
    public UniquePlaceList getAllPlaces() {
        return new UniquePlaceList(allPlaces);
    }

    /**
     * Defensively copied UniqueTagList of all tags in the address book at the time of the call.
     */
    public UniqueTagList getAllTags() {
        return new UniqueTagList(allTags);
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddressBook // instanceof handles nulls
                && this.allPlaces.equals(((AddressBook) other).allPlaces)
                && this.allTags.equals(((AddressBook) other).allTags));
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(allPlaces, allTags);
    }
}
