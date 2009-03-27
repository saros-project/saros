package de.fu_berlin.inf.dpp.util;

import java.io.IOException;

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
