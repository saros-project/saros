package de.fu_berlin.inf.dpp.util;

import java.net.URL;

/**
 * TODO: This class may well be removed as soon as bjoern's browser component is
 * integrated.
 */
public class BrowserUtils {

    /**
     * Gets a URL for a given resource using the class loader of this util
     * class.
     * 
     * @param name
     *            name of the resource
     * @return URL of the resource or <code>null</code> if the resource was not
     *         found
     */
    public static URL getResourceURL(final String name) {
        return BrowserUtils.class.getClassLoader().getResource(name);
    }
}
