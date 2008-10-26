package de.fu_berlin.inf.dpp.net;

public class MUCForbiddenException extends Exception {

    public static final String FORBIDDEN_ERROR_MESSAGE = "forbidden(403) Owner privileges required";

    /**
	 * 
	 */
    private static final long serialVersionUID = -2264079295534627973L;

    public MUCForbiddenException(String message) {
	super(message);
    }
}
