package de.fu_berlin.inf.dpp.server.filesystem;

import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IWorkspace;
import de.fu_berlin.inf.dpp.filesystem.ReferencePointImpl;
import java.io.File;
import java.util.HashMap;

public class ServerReferencePointManager {
  HashMap<IReferencePoint, File> referencePointToFileMapper;

  public ServerReferencePointManager() {
    referencePointToFileMapper = new HashMap<IReferencePoint, File>();
  }

  public static IReferencePoint create(IWorkspace workspace) {
    IPath serverWorkspacePath = workspace.getLocation();

    return new ReferencePointImpl(serverWorkspacePath);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link File} directory
   *
   * @param referencePoint the key of the pair
   * @param directory the value of the pair
   */
  public synchronized void put(IReferencePoint referencePoint, File directory) {
    if (!referencePointToFileMapper.containsKey(referencePoint)) {
      referencePointToFileMapper.put(referencePoint, directory);
    }
  }

  /**
   * Returns the {@link File} directory given by the {@link IReferencePoint}
   *
   * @param referencePoint the key for which the directory should be returned
   * @return the directory given by referencePoint
   */
  public synchronized File get(IReferencePoint referencePoint) {

    return referencePointToFileMapper.get(referencePoint);
  }

  /**
   * Returns the {@link File} resource in combination of the {@link IReferencePoint} reference point
   * and the {@link IPath} relative path from the reference point to the resource.
   *
   * @param referencePoint The reference point, on which the resource belongs to
   * @param referencePointRelativePath the relative path from the reference point to the resource
   * @return the resource of the reference point from referencePointRelativePath
   */
  public synchronized File getResource(
      IReferencePoint referencePoint, IPath referencePointRelativePath) {
    if (referencePoint == null) throw new NullPointerException("Reference point is null");

    if (referencePointRelativePath == null) throw new NullPointerException("Path is null");

    File directory = get(referencePoint);

    if (directory == null)
      throw new NullPointerException(
          "For reference point " + referencePoint + " does'nt exist a directoy.");

    return findVirtualFile(directory, referencePointRelativePath);
  }

  private File findVirtualFile(final File directory, IPath path) {

    File file = new File(directory.getPath().concat(path.toString()));

    if (!file.exists()) return null;

    return file;
  }
}
