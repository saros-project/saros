package de.fu_berlin.inf.dpp.util;

import java.io.IOException;

/**
 * An IOException which provides a convenience constructor taking a cause.
 * 
 * This class is basically used, because IOException in Java 1.5 does not have
 * this particular constructor.
 */
public class CausedIOException extends IOException {

    private static final long serialVersionUID = -1388201497639968452L;

    public CausedIOException(String description, Exception exeption) {
        super(description);
        initCause(exeption);
    }

    public CausedIOException(Exception exception) {
        super();
        initCause(exception);
    }
}
