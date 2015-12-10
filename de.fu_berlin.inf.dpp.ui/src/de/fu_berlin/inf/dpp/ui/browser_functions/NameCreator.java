package de.fu_berlin.inf.dpp.ui.browser_functions;

/**
 * In order to distinguish BrowserFunctions and ui.frontend Javascript functions
 * this Class is responsible for creating the name of the actual injected
 * Javascript methode.
 */
public class NameCreator {

    private NameCreator() {
        // Hide implicit constructor
    }

    // Current convention is that every BF has an prefix that signals that this
    // function will invoke Javacode.
    private final static String PREFIX = "__java_";

    /**
     * This will return the name of an BF that follows the naming convention
     * 
     * @param plainname
     *            the name of the BrowserFunction
     * @return the actual injected name of the BrowserFunction
     */
    protected static String getConventionName(String plainname) {
        return PREFIX + plainname;
    }

}
