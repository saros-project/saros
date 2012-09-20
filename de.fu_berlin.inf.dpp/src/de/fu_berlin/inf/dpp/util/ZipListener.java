package de.fu_berlin.inf.dpp.util;

/**
 * Interface for reporting progress of Zip operations.
 * 
 * @author Stefan Rossbach
 */
public interface ZipListener {

    /**
     * Gets called when a new Zip entry is created.
     * 
     * @param filename
     *            the name of the file including its path that will be
     *            compressed now
     * @return <code>true</true> if the Zip progress should be aborted, <code>false</code>
     *         otherwise
     */
    public boolean update(String filename);

    /**
     * Gets called when a chunk of data has been deflated. This should be called
     * in a moderate amount (e.g after every x bytes).
     * 
     * @param totalRead
     *            the amount of bytes that has already been read
     * @param totalSize
     *            the total size in bytes that will be read when the operation
     *            has finished or <code>-1</code> if the size is unknown
     * @return <code>true</true> if the Zip progress should be aborted, <code>false</code>
     *         otherwise
     */
    public boolean update(long totalRead, long totalSize);
}
