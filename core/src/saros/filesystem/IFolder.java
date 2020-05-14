package saros.filesystem;

import java.io.IOException;

/**
 * Represents a handle for a folder in the (virtual) file system.
 *
 * <p>The referenced container do not necessarily have to exist in the local filesystem.
 *
 * @see IContainer
 */
// TODO generalize 'create()' for files and folders, move to IResource, and merge with IContainer
public interface IFolder extends IContainer {

  /**
   * Creates the folder in the local filesystem.
   *
   * @throws IOException if the folder creation failed or the resource already exists
   */
  void create() throws IOException;

  default Type getType() {
    return Type.FOLDER;
  }
}
