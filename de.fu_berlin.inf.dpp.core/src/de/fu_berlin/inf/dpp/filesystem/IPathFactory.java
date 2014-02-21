package de.fu_berlin.inf.dpp.filesystem;

/**
 * An interface for implementing a factory that is able to convert {@link IPath
 * path} objects to their string representation and vice versa. Implementation
 * must throw the given {@link RuntimeException runtime exceptions} as declared
 * in the method signatures.
 */
public interface IPathFactory {

    /**
     * Converts a path to its string representation
     * 
     * @param path
     *            the path to convert
     * @return the string representation of the path
     * 
     * @throws NullPointerException
     *             if path is <code>null</code>
     * @throws IllegalArgumentException
     *             if the path is not relative (e.g is presents a full path like
     *             /etc/init.d/)
     */
    public String fromPath(IPath path);

    /**
     * Converts a string to a path object
     * 
     * @param name
     *            the name of the path to convert
     * @return a path object representing the path of the given name
     * 
     * @throws NullPointerException
     *             if name is <code>null</code>
     * @throws IllegalArgumentException
     *             if the resulting path object is not a relative path
     */
    public IPath fromString(String name);
}
