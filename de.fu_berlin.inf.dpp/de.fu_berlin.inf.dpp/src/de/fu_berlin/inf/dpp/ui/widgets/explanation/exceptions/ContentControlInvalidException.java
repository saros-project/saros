package de.fu_berlin.inf.dpp.ui.widgets.explanation.exceptions;

public class ContentControlInvalidException extends RuntimeException {
    private static final long serialVersionUID = 4000855428432271516L;

    public ContentControlInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}
