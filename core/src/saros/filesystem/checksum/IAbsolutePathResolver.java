package saros.filesystem.checksum;

import saros.filesystem.IFile;

/**
 * Interface representing a helper class that can be user to resolve absolute paths for {@link
 * IFile} objects.
 *
 * <p><b>NOTE:</b> This interface should only be used for checksum cache related activities. There
 * generally shouldn't be a need to resolve absolute paths in the Saros core logic, so think
 * carefully before using this interface for other purposes.
 */
public interface IAbsolutePathResolver {

  /**
   * Returns the absolute path for the given IFile.
   *
   * @param file the file to get the absolute path for
   * @return the absolute path for the given IFile or <code>null</code> if no such path could be
   *     constructed
   */
  String getAbsolutePath(IFile file);
}
