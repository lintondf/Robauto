/**
 * 
 */
package seedu.addressbook.commands;

import seedu.addressbook.common.Messages;
import seedu.addressbook.data.place.Place;
import seedu.addressbook.data.place.UniquePlaceList.PlaceNotFoundException;


/**
 * @author NOOK
 *
 */
public class UpdateCommand extends Command {
	
    public static final String COMMAND_WORD = "update";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ":\n" 
            + " update a person record";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Updated Person: %1$s";
    
    private Place updatedPlace;
	
    public UpdateCommand( Place updatedPlace ) {
    	this.updatedPlace = updatedPlace;
    }
    
    @Override
    public CommandResult execute() {
        try {
            addressBook.update(updatedPlace);
            return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, updatedPlace));

        } catch (PlaceNotFoundException pnfe) {
            return new CommandResult(Messages.MESSAGE_PERSON_NOT_IN_ADDRESSBOOK);
        }
    }
   
    
}
