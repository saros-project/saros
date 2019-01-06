package de.fu_berlin.inf.dpp.activities;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IFolder;
import de.fu_berlin.inf.dpp.filesystem.IPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.misc.xstream.SPathConverter;
import org.apache.commons.lang3.ObjectUtils;

/**
 * A <i>SPath</i> points to a {@link IResource resource} in a {@link IProject project}. The specific
 * resource does not need to exist, neither during the marshaling nor during unmarshaling.
 *
 * <p><i>SPath</i> objects can be marshaled and unmarshaled.
 *
 * @see SPathConverter
 */
@XStreamAlias("SPath")
public class SPath {

  /**
   * @JTourBusStop 4, Some Basics:
   *
   * <p>Individual IProjects use IPaths to identify their resources. However, because Saros needs to
   * keep track of resources across multiple projects, it encapsulates IPaths in an SPath that
   * includes additional identifying information.
   */

  /** The local IProject in which the resource is contained which this SPath represents */
  private final IProject project;

  /** The local IReferencePoint in which the resource is contained which this SPath represents */
  private final IReferencePoint referencePoint;

  /** The project relative path of the resource this SPath represents. */
  private final IPath projectRelativePath;

  /**
   * Default constructor, initializing this SPath as a reference to the resource identified by the
   * given path in the given project.
   *
   * @param path
   * @throws IllegalArgumentException if project is <code>null</code><br>
   * @throws IllegalArgumentException if the path is <code>null</code> or is not relative
   */
  public SPath(IProject project, IPath path) {
    if (project == null) throw new IllegalArgumentException("project is null");

    if (path == null) throw new IllegalArgumentException("path is null");

    if (path.isAbsolute()) throw new IllegalArgumentException("path is absolute: " + path);

    this.project = project;
    this.projectRelativePath = path;
    this.referencePoint = project.getReferencePoint();
  }

  /** Convenience constructor, which retrieves path and project from the given resource */
  public SPath(IResource resource) {
    this(resource.getProject(), resource.getProjectRelativePath());
  }

  /**
   * Returns the project relative path of the resource represented by this SPath.
   *
   * @return project relative path of the resource
   */
  public IPath getProjectRelativePath() {
    return projectRelativePath;
  }

  /**
   * Returns a handle for an IFile represented by this SPath.
   *
   * @return the IFile contained in the associated IProject for the given project relative path
   */
  public IFile getFile() {
    return project.getFile(projectRelativePath);
  }

  /**
   * Returns the IResource represented by this SPath.
   *
   * <p><b>Note:</b> This operation might perform disk I/O.
   *
   * @return the resource represented by this SPath or <code>null</code> if such or resource does
   *     not exist
   */
  public IResource getResource() {
    return project.findMember(projectRelativePath);
  }

  /** Returns the reference point in which the referenced resource is located. */
  public IReferencePoint getReferencePoint() {
    return referencePoint;
  }

  /** Convenience method for getting the full path of the file identified by this SPath. */
  public IPath getFullPath() {
    final IPath fullProjectPath = project.getFullPath();
    return fullProjectPath.append(projectRelativePath);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ObjectUtils.hashCode(project);
    result = prime * result + ObjectUtils.hashCode(projectRelativePath);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;

    if (obj == null) return false;

    if (!(obj instanceof SPath)) return false;

    SPath other = (SPath) obj;

    return ObjectUtils.equals(project, other.project)
        && ObjectUtils.equals(projectRelativePath, other.projectRelativePath);
  }

  @Override
  public String toString() {
    return "SPath [project=" + project + ", projectRelativePath=" + projectRelativePath + "]";
  }
}
