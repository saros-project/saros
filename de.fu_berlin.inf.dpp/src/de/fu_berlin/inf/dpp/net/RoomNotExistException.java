package de.fu_berlin.inf.dpp.net;

/**
 * 
 * An Exception that is thrown when an error occurs performing an room operation
 * with non existing room.
 * 
 * @author rieger
 * 
 */
public class RoomNotExistException extends Exception {

    public static final String MUC_ERROR_MESSAGE = "item-not-found(404) Conference room does not exist";

    /**
	 * 
	 */
    private static final long serialVersionUID = -7470837131463504604L;

    public RoomNotExistException(String message) {
	super(message);
    }
}
