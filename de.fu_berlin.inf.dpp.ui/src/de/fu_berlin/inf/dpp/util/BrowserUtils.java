package de.fu_berlin.inf.dpp.util;

/**
 * TODO: This class may well be removed as soon as bjoern's browser
 * component is integrated.
 */
public class BrowserUtils {

    /**
     * Gets a url for a file inside the resource folder.
     * This does not work for jar files.
     *
     * @param filename relative path of the file inside the resource folder
     * @return file url as string
     */
    public static String getUrlForClasspathFile(String filename) {
        return BrowserUtils.class
            .getResource(filename).toString();
    }
}
