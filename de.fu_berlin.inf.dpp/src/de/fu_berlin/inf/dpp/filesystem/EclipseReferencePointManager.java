package de.fu_berlin.inf.dpp.filesystem;

import java.util.HashMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;

/**
 * The EclipseReferencePointManager maps an {@link IReferencePoint} reference point to {@link
 * org.eclipse.core.resources.IProject} project
 */
public class EclipseReferencePointManager {

  HashMap<IReferencePoint, org.eclipse.core.resources.IProject> referencePointToProjectMapper;

  public EclipseReferencePointManager() {
    referencePointToProjectMapper =
        new HashMap<IReferencePoint, org.eclipse.core.resources.IProject>();
  }

  /**
   * Creates and returns the {@link IReferencePoint} reference point of given {@link IResource}
   * resource. The reference point points on the resource's project full path.
   *
   * @param resource
   * @return the reference point of given resourcen
   */
  public static IReferencePoint create(IResource resource) {
    if (resource == null) return null;

    IProject project = resource.getProject();
    de.fu_berlin.inf.dpp.filesystem.IPath path =
        ResourceAdapterFactory.create(project.getFullPath());

    return new ReferencePointImpl(path);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link
   * org.eclipse.core.resources.IProject} project. The reference point will be created from the
   * project's relative path.
   *
   * @param project
   */
  public synchronized void put(org.eclipse.core.resources.IProject project) {
    IReferencePoint referencePoint = create(project);

    put(referencePoint, project);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link
   * org.eclipse.core.resources.IProject} project
   *
   * @param referencePoint the key of the pair
   * @param project the value of the pair
   */
  public synchronized void put(
      IReferencePoint referencePoint, org.eclipse.core.resources.IProject project) {
    if (!referencePointToProjectMapper.containsKey(referencePoint)) {
      referencePointToProjectMapper.put(referencePoint, project);
    }
  }

  /**
   * Returns the {@link org.eclipse.core.resources.IProject} given by the {@link IReferencePoint}
   *
   * @param referencePoint the key for which the IProject should be returned
   * @return the IProject given by referencePoint
   */
  public synchronized org.eclipse.core.resources.IProject get(IReferencePoint referencePoint) {
    return referencePointToProjectMapper.get(referencePoint);
  }

  /**
   * Returns the {@link IResource} resource in combination of the {@link IReferencePoint} reference
   * point and the {@link IPath} relative path from the reference point to the resource, or null if
   * the resource does not exist
   *
   * @param referencePoint The reference point, on which the resource belongs to
   * @param referencePointRelativePath the relative path from the reference point to the resource
   * @return the resource of the reference point from referencePointRelativePath or null if resource
   *     does not exist.
   */
  public synchronized IResource getResource(
      IReferencePoint referencePoint, IPath referencePointRelativePath) {
    if (referencePoint == null) throw new NullPointerException("Reference point is null");

    if (referencePointRelativePath == null) throw new NullPointerException("Path is null");

    org.eclipse.core.resources.IProject project = get(referencePoint);

    if (project == null)
      throw new NullPointerException(
          "For reference point " + referencePoint + " does'nt exist a project.");

    return project.findMember(referencePointRelativePath);
  }

  /**
   * Returns a handle to the file identified by the given path and reference point
   *
   * @param referencePoint The reference point, on which the file belongs to
   * @param referencePointRelativePath the relative path from the reference point to the file
   * @return a handle to the file
   */
  public synchronized IFile getFile(
      IReferencePoint referencePoint, IPath referencePointRelativePath) {
    if (referencePoint == null) throw new NullPointerException("Reference point is null");

    if (referencePointRelativePath == null) throw new NullPointerException("Path is null");

    org.eclipse.core.resources.IProject project = get(referencePoint);

    if (project == null)
      throw new NullPointerException(
          "For reference point " + referencePoint + " does'nt exist a project.");

    return project.getFile(referencePointRelativePath);
  }

  /**
   * Returns a handle to the file identified by the given path and reference point
   *
   * @param referencePoint the relative path from the reference point to the folder
   * @param referencePointRelativePath
   * @return a handle to the folder
   */
  public synchronized IFolder getFolder(
      IReferencePoint referencePoint, IPath referencePointRelativePath) {
    if (referencePoint == null) throw new NullPointerException("Reference point is null");

    if (referencePointRelativePath == null) throw new NullPointerException("Path is null");

    org.eclipse.core.resources.IProject project = get(referencePoint);

    if (project == null)
      throw new NullPointerException(
          "For reference point " + referencePoint + " does'nt exist a project.");

    return project.getFolder(referencePointRelativePath);
  }
}
