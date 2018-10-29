package seedu.addressbook.storage.jaxb;

import seedu.addressbook.common.Utils;
import seedu.addressbook.data.exception.IllegalValueException;
import seedu.addressbook.data.place.*;
import seedu.addressbook.data.tag.Tag;
import seedu.addressbook.data.tag.UniqueTagList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB-friendly adapted place data holder class.
 */
public class AdaptedPlace {

    private static class AdaptedContactDetail {
        @XmlValue
        public String value;
//        @XmlAttribute(required = true)
//        public boolean isPrivate;
    }

    @XmlElement(required = true)
    private String name;
    @XmlElement(required = true)
    private AdaptedContactDetail address;
    @XmlElement(required = true)
    private AdaptedContactDetail latitude;
    @XmlElement(required = true)
    private AdaptedContactDetail longitude;

    @XmlElement
    private List<AdaptedTag> tagged = new ArrayList<>();

    /**
     * No-arg constructor for JAXB use.
     */
    public AdaptedPlace() {}


    /**
     * Converts a given Place into this class for JAXB use.
     *
     * @param source future changes to this will not affect the created Adaptedplace
     */
    public AdaptedPlace(ReadOnlyPlace source) {
        name = source.getName().fullName;

        latitude = new AdaptedContactDetail();
        latitude.value = source.getLatitude().toString();

        longitude = new AdaptedContactDetail();
        longitude.value = source.getLongitude().toString();

        address = new AdaptedContactDetail();
        address.value = source.getAddress().value;

        tagged = new ArrayList<>();
        for (Tag tag : source.getTags()) {
            tagged.add(new AdaptedTag(tag));
        }
    }

    /**
     * Returns true if any required field is missing.
     *
     * JAXB does not enforce (required = true) without a given XML schema.
     * Since we do most of our validation using the data class constructors, the only extra logic we need
     * is to ensure that every xml element in the document is present. JAXB sets missing elements as null,
     * so we check for that.
     */
    public boolean isAnyRequiredFieldMissing() {
        for (AdaptedTag tag : tagged) {
            if (tag.isAnyRequiredFieldMissing()) {
                return true;
            }
        }
        // second call only happens if phone/email/address are all not null
        return Utils.isAnyNull(name, latitude, longitude, address)
                || Utils.isAnyNull(latitude.value, longitude.value, address.value);
    }

    /**
     * Converts this jaxb-friendly adapted place object into the place object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted place
     */
    public Place toModelType() throws IllegalValueException {
        final List<Tag> placeTags = new ArrayList<>();
        for (AdaptedTag tag : tagged) {
            placeTags.add(tag.toModelType());
        }
        final Name name = new Name(this.name);
        final Double latitude = Double.parseDouble(this.latitude.value);
        final Double longitude = Double.parseDouble(this.longitude.value);
        final Address address = new Address(this.address.value);
        final UniqueTagList tags = new UniqueTagList(placeTags);
        return new Place(name, latitude, longitude, address, tags);
    }
}
