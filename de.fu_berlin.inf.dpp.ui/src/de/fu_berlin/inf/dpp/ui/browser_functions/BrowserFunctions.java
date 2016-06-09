package de.fu_berlin.inf.dpp.ui.browser_functions;

import java.util.Collections;
import java.util.List;

/**
 * This class holds all currently available {@link BrowserFunction browser
 * functions}.
 */
/*
 * FIXME the only purpose of this class is to avoid cyclic dependency errors
 * which arise when the list of browser functions would be added as dependency
 * in the BrowserCreator class.
 */
public class BrowserFunctions {

    private static List<TypedJavascriptFunction> browserfunctions = Collections
        .emptyList();

    public BrowserFunctions(List<TypedJavascriptFunction> browserfunctions) {
        BrowserFunctions.browserfunctions = browserfunctions;
    }

    public static List<TypedJavascriptFunction> getAll() {
        return browserfunctions;
    }
}
