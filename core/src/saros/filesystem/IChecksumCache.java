package de.fu_berlin.inf.dpp.filesystem;

/**
 * An interface that can be used to access checksums. So they do not need to be recalculated as long
 * as they are not invalid.
 *
 * @author Stefan Rossbach
 */
public interface IChecksumCache {

  /**
   * Returns the checksum for the given file.
   *
   * @param file the file to lookup
   * @return the checksum or <code>null</code> if no checksum for this file exists or the checksum
   *     has become invalid
   */
  public abstract Long getChecksum(IFile file);

  /**
   * Adds or update a checksum in the cache for the given file.
   *
   * @param file file to add/update
   * @param checksum the checksum to add
   * @return <code>true</code> if the former checksum was invalid, <code>false</code> otherwise
   */
  public abstract boolean addChecksum(IFile file, long checksum);
}
