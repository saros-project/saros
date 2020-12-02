package saros.lsp.utils; // TODO: is from eclipse

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IFolder;
import saros.filesystem.IResource;
import saros.util.StackTrace;

/**
 * This class contains static utility methods for file handling.
 *
 * @author orieger/chjacob
 * @implNote Taken from the server implementation
 */
public class FileUtils { // TODO: still used?

  private static Logger LOG = Logger.getLogger(FileUtils.class);

  private FileUtils() {
    // no instantiation allowed
  }

  /**
   * Writes the data of the given input stream to the given file.
   *
   * @param input the input stream to write to the file
   * @param file the file to create/overwrite
   */
  public static void writeFile(final InputStream input, final IFile file) {
    try {
      if (file.exists()) {
        file.setContents(input);
      } else {
        mkdirs(file);
        file.create(input);
      }
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  /** Makes sure that the parent directories of the given {@linkplain IResource resource} exist. */
  public static void mkdirs(final IResource resource) {

    final List<IFolder> parents = new ArrayList<IFolder>();

    IContainer parent = resource.getParent();

    while (parent != null && parent.getType() == IResource.Type.FOLDER) {
      if (parent.exists()) break;

      parents.add((IFolder) parent);
      parent = parent.getParent();
    }

    Collections.reverse(parents);

    for (final IFolder folder : parents)
      try {
        folder.create();
      } catch (IOException e) {
        LOG.error(e);
      }
  }

  /**
   * Creates the given folder. All parent folder may be created on demand if necessary.
   *
   * @param folder the folder to create
   */
  public static void create(final IFolder folder) {
    if (folder.exists()) {
      LOG.warn("folder already exists: " + folder, new StackTrace());
      return;
    }

    mkdirs(folder);
    try {
      folder.create();
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  /**
   * Deletes the given resource and tries to store the existing resource to the Eclipse history
   * space.
   *
   * @param resource the resource to delete
   */
  public static void delete(final IResource resource) {
    if (!resource.exists()) {
      LOG.warn("file for deletion does not exist: " + resource, new StackTrace());
      return;
    }

    try {
      resource.delete();
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  /**
   * Retrieves the content of a local file
   *
   * @param localFile
   * @return Byte array of the file contents. Is <code>null</code> if the file does not exist or is
   *     out of sync, the reference points to no file, or the conversion to a byte array failed.
   */
  public static byte[] getLocalFileContent(IFile localFile) {
    InputStream in = null;
    byte[] content = null;
    try {
      in = localFile.getContents();
      content = IOUtils.toByteArray(in);
    } catch (IOException e) {
      LOG.warn(
          "could not convert file content to byte array (file: "
              + localFile.getReferencePointRelativePath()
              + ")");
    } finally {
      IOUtils.closeQuietly(in);
    }
    return content;
  }
}
