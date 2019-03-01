package saros.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * This class contains static utility methods for file handling.
 *
 * @author orieger/chjacob
 */
public class FileUtils {

  private static Logger LOG = Logger.getLogger(FileUtils.class);

  private FileUtils() {
    // no instantiation allowed
  }

  /**
   * Writes the data of the given input stream to the given file.
   *
   * @param input the input stream to write to the file
   * @param file the file to create/overwrite
   * @throws CoreException
   */
  public static void writeFile(final InputStream input, final IFile file) throws CoreException {
    if (file.exists()) {
      file.setContents(input, false, true, null);
    } else {
      mkdirs(file);
      file.create(input, false, null);
    }
  }

  /**
   * Makes sure that the parent directories of the given {@linkplain IResource resource} exist.
   *
   * @throws CoreException
   */
  public static void mkdirs(final IResource resource) throws CoreException {

    final List<IFolder> parents = new ArrayList<IFolder>();

    IContainer parent = resource.getParent();

    while (parent != null && parent.getType() == IResource.FOLDER) {
      if (parent.exists()) break;

      parents.add((IFolder) parent);
      parent = parent.getParent();
    }

    Collections.reverse(parents);

    for (final IFolder folder : parents) folder.create(false, true, null);
  }

  /**
   * Creates the given folder. All parent folder may be created on demand if necessary.
   *
   * @param folder the folder to create
   * @throws CoreException
   */
  public static void create(final IFolder folder) throws CoreException {
    if (folder.exists()) {
      LOG.warn("folder already exists: " + folder, new StackTrace());
      return;
    }

    mkdirs(folder);
    folder.create(false, true, null);
  }

  /**
   * Deletes the given resource and tries to store the existing resource to the Eclipse history
   * space.
   *
   * @param resource the resource to delete
   * @throws CoreException
   */
  public static void delete(final IResource resource) throws CoreException {
    if (!resource.exists()) {
      LOG.warn("file for deletion does not exist: " + resource, new StackTrace());
      return;
    }

    resource.delete(IResource.KEEP_HISTORY, null);
  }

  /**
   * Moves the given {@link IResource} to the place, that is pointed by the given {@link IPath}.
   *
   * <p>This method excepts both variables to be relative to the workspace.
   *
   * @param destination Destination of moving the given resource.
   * @param source Resource, that is going to be moved
   */
  public static void move(final IPath destination, final IResource source) throws CoreException {

    source.move(destination.makeAbsolute(), false, null);
  }

  /**
   * Calculates the total file count and size for all resources.
   *
   * @param resources collection containing the resources that file sizes and file count should be
   *     calculated
   * @param includeMembers <code>true</code> to include the members of resources that represents a
   *     {@linkplain IContainer container}
   * @param flags additional flags on how to process the members of containers
   * @return a pair containing the {@linkplain Pair#getLeft() file size} and {@linkplain
   *     Pair#getRight() file count} for the given resources
   */
  public static Pair<Long, Long> getFileCountAndSize(
      Collection<? extends IResource> resources, boolean includeMembers, int flags) {

    long totalFileSize = 0;
    long totalFileCount = 0;

    for (IResource resource : resources) {
      switch (resource.getType()) {
        case IResource.FILE:
          totalFileCount++;

          try {
            long filesize = EFS.getStore(resource.getLocationURI()).fetchInfo().getLength();

            totalFileSize += filesize;
          } catch (Exception e) {
            LOG.warn("failed to retrieve file size of file " + resource.getLocationURI(), e);
          }
          break;
        case IResource.PROJECT:
        case IResource.FOLDER:
          if (!includeMembers) break;

          try {
            IContainer container = resource.getAdapter(IContainer.class);

            Pair<Long, Long> subFileCountAndSize =
                FileUtils.getFileCountAndSize(
                    Arrays.asList(container.members(flags)), includeMembers, flags);

            totalFileSize += subFileCountAndSize.getLeft();
            totalFileCount += subFileCountAndSize.getRight();

          } catch (Exception e) {
            LOG.warn("failed to process container: " + resource, e);
          }
          break;
        default:
          break;
      }
    }

    return Pair.of(totalFileSize, totalFileCount);
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
    } catch (CoreException e) {
      LOG.warn("could not get content of file " + localFile.getFullPath());
    } catch (IOException e) {
      LOG.warn(
          "could not convert file content to byte array (file: " + localFile.getFullPath() + ")");
    } finally {
      IOUtils.closeQuietly(in);
    }
    return content;
  }
}
