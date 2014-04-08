package de.fu_berlin.inf.dpp.filesystem;

/**
 * An interface that can be used to access checksums. So they do not need to be
 * recalculated as long as they are not invalid.
 * 
 * @NOTE as we have currently no abstraction between the business logic and
 *       Eclipse the unique identifier must match same layout as created by
 *       {@link FileContentNotifierBridge} class !
 * @author Stefan Rossbach
 */
public interface IChecksumCache {

    /**
     * Returns the checksum for the given unique identifier.
     * 
     * @param path
     *            a unique identifier
     * @return the checksum or <code>null</code> if no checksum for this
     *         identifier exists or the checksum has become invalid
     */
    public abstract Long getChecksum(String path);

    /**
     * Adds or update a checksum in the cache.
     * 
     * @param path
     *            a unique identifier
     * @param checksum
     *            the checksum to add
     * @return <code>true</code> if the former checksum was invalid,
     *         <code>false</code> otherwise
     */
    public abstract boolean addChecksum(String path, long checksum);

}