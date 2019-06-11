package saros.filesystem;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import saros.activities.SPath;

/**
 * The EclipseReferencePointManager maps an {@link IReferencePoint} reference point to {@link
 * IProject} project
 */
public class EclipseReferencePointManager {

  ConcurrentHashMap<IReferencePoint, IProject> referencePointToProjectMapper;

  public EclipseReferencePointManager() {
    referencePointToProjectMapper = new ConcurrentHashMap<>();
  }

  /**
   * Creates and returns the {@link IReferencePoint} reference point of given {@link IResource}
   * resource, or null if the given {@link IResource} resource is null. The reference point points
   * on the resource's project full path.
   *
   * @param resource for which the reference point should be created
   * @return the reference point of given resource
   */
  public static IReferencePoint create(IResource resource) {
    if (resource == null) return null;

    IProject project = resource.getProject();
    saros.filesystem.IPath path = ResourceAdapterFactory.create(project.getFullPath());

    return new ReferencePointImpl(path);
  }

  /**
   * Inserts a pair of {@link IReferencePoint} reference point and {@link IProject} project. The
   * reference point will be created from the project's relative path by
   * {#putIfAbsent(IReferencePoint referencePoint, IProject project) putIfAbsent} .
   *
   * @param project the {@link IProject} project for which a reference point is created and are
   *     inserted.
   * @exception IllegalArgumentException if {@link IProject} project is null
   */
  public void putIfAbsent(IProject project) {
    checkArgument(project, "Project is null");

    IReferencePoint referencePoint = create(project);

    putIfAbsent(referencePoint, project);
  }

  /**
   * Insert a pair of {@link IReferencePoint} reference point and {@link IProject} project
   *
   * @param referencePoint the key of the pair
   * @param project the value of the pair
   * @exception IllegalArgumentException if {@link IProject} project or {@link IReferencePoint}
   *     reference point is null
   */
  public void putIfAbsent(IReferencePoint referencePoint, IProject project) {
    checkArgument(referencePoint, "Reference point is null");
    checkArgument(project, "Project is null");

    referencePointToProjectMapper.putIfAbsent(referencePoint, project);
  }

  /**
   * Returns the {@link IProject} project given by the {@link IReferencePoint} reference point
   *
   * @param referencePoint the key for which the IProject should be returned
   * @return the IProject given by referencePoint
   * @exception IllegalArgumentException if {@link IReferencePoint} reference point is null
   */
  public IProject getProject(IReferencePoint referencePoint) {
    checkArgument(referencePoint, "Reference point is null");

    return referencePointToProjectMapper.get(referencePoint);
  }

  /**
   * Returns the {@link IResource} resource in combination of the {@link IReferencePoint} reference
   * point and the {@link IPath} relative path from the {@link IReferencePoint} reference point to
   * the {@link IResource} resource, or null if the resource does not exist
   *
   * @param referencePoint The {@link IReferencePoint} reference point, on which the {@link
   *     IResource} resource belongs to
   * @param referencePointRelativePath the {@link IPath} relative path from the {@link
   *     IReferencePoint} reference point to the {@link IResource} resource
   * @return the resource of the {@link IReferencePoint} reference point from {@link IPath}
   *     referencePointRelativePath or null if resource does not exist.
   * @exception IllegalArgumentException if {@link IReferencePoint} reference point or {@link IPath}
   *     referencePointRelativePath is null
   * @exception NullPointerException if the given {@link IReferencePoint} reference point does't
   *     exist an {@link IProject} project
   */
  public IResource findMember(IReferencePoint referencePoint, IPath referencePointRelativePath) {
    checkArgument(referencePoint, "Reference point is null");
    checkArgument(referencePointRelativePath, "ReferencePointsRelativePath is null");

    IProject project = Objects.requireNonNull(getProject(referencePoint));

    return project.findMember(referencePointRelativePath);
  }

  /**
   * Returns the {@link IResource} resource represented by this {@link SPath} SPath, or null if the
   * resource does not exist
   *
   * @param path the {@link SPath} SPath which represents the {@link IResource} resource
   * @return the resource of the {@link IReferencePoint} reference point from {@link IPath}
   *     referencePointRelativePath or null if resource does not exist.
   * @exception IllegalArgumentException if {@link SPath} path is null
   * @exception NullPointerException if the given {@link SPath} path does contain an {@link
   *     IReferencePoint} reference point
   * @exception NullPointerException if the given {@link SPath} path does contain an {@link IPath}
   *     referencePointRelativePath
   */
  public IResource findMember(SPath path) {
    checkArgument(path, "SPath is null");

    IReferencePoint referencePoint = Objects.requireNonNull(path.getReferencePoint());
    saros.filesystem.IPath referencePointRelativePath =
        Objects.requireNonNull(path.getProjectRelativePath());

    IPath eclipsePath = ((EclipsePathImpl) referencePointRelativePath).getDelegate();

    return findMember(referencePoint, eclipsePath);
  }

  /**
   * Returns a handle to the {@link IFile} file identified by the given {@link IPath} path and
   * {@link IReferencePoint} reference point
   *
   * @param referencePoint The {@link IReferencePoint} reference point, on which the {@link IFile}
   *     file belongs to
   * @param referencePointRelativePath the relative {@link IPath} path from the {@link
   *     IReferencePoint} reference point to the {@link IFile} file
   * @return a handle to the {@link IFile} file
   * @exception IllegalArgumentException if {@link IReferencePoint} reference point or {@link IPath}
   *     referencePointRelativePath is null
   * @exception NullPointerException if the given {@link IReferencePoint} reference point does't
   *     exist an {@link IProject} project
   */
  public IFile getFile(IReferencePoint referencePoint, IPath referencePointRelativePath) {
    checkArgument(referencePoint, "Reference point is null");
    checkArgument(referencePointRelativePath, "ReferencePointsRelativePath is null");

    IProject project = Objects.requireNonNull(getProject(referencePoint));

    return project.getFile(referencePointRelativePath);
  }

  /**
   * Returns a handle to the {@link IFile} file represented by this {@link SPath} SPath
   *
   * @param path the {@link SPath} SPath which represents the {@link IFile} file
   * @return a handle to the {@link IFile} file
   * @exception IllegalArgumentException if {@link SPath} path is null
   * @exception NullPointerException if the given {@link SPath} path does contain an {@link
   *     IReferencePoint} reference point
   * @exception NullPointerException if the given {@link SPath} path does contain an {@link IPath}
   *     referencePointRelativePath
   */
  public IFile getFile(SPath path) {
    checkArgument(path, "SPath is null");

    IReferencePoint referencePoint = Objects.requireNonNull(path.getReferencePoint());
    saros.filesystem.IPath referencePointRelativePath =
        Objects.requireNonNull(path.getProjectRelativePath());

    IPath eclipsePath = ((EclipsePathImpl) referencePointRelativePath).getDelegate();

    return getFile(referencePoint, eclipsePath);
  }

  /**
   * Returns a handle to the {@link IFolder} folder identified by the given {@link IPath} path and
   * {@link IReferencePoint} reference point
   *
   * @param referencePoint The {@link IReferencePoint} reference point, on which the {@link IFolder}
   *     folder belongs to
   * @param referencePointRelativePath the {@link IPath} relative path from the {@link
   *     IReferencePoint} reference point to the {@link IFolder} folder
   * @return a handle to the {@link IFolder} folder
   * @exception IllegalArgumentException if {@link IReferencePoint} reference point or {@link IPath}
   *     referencePointRelativePath is null
   * @exception NullPointerException if the given {@link IReferencePoint} reference point does't
   *     exist an {@link IProject} project
   */
  public IFolder getFolder(IReferencePoint referencePoint, IPath referencePointRelativePath) {
    checkArgument(referencePoint, "Reference point is null");
    checkArgument(referencePointRelativePath, "ReferencePointsRelativePath is null");

    IProject project = Objects.requireNonNull(getProject(referencePoint));

    return project.getFolder(referencePointRelativePath);
  }

  /**
   * Returns a handle to the {@link IFolder} folder represented by this {@link SPath} SPath
   *
   * @param path the {@link SPath} SPath which represents the {@link IFolder} folder
   * @return a handle to the {@link IFolder} folder
   * @exception IllegalArgumentException if {@link SPath} path is null
   * @exception NullPointerException if the given {@link SPath} path does contain an {@link
   *     IReferencePoint} reference point
   * @exception NullPointerException if the given {@link SPath} path does contain an {@link IPath}
   *     referencePointRelativePath
   */
  public IFolder getFolder(SPath path) {
    checkArgument(path, "SPath is null");

    IReferencePoint referencePoint = Objects.requireNonNull(path.getReferencePoint());
    saros.filesystem.IPath referencePointRelativePath =
        Objects.requireNonNull(path.getProjectRelativePath());

    IPath eclipsePath = ((EclipsePathImpl) referencePointRelativePath).getDelegate();

    return getFolder(referencePoint, eclipsePath);
  }

  /**
   * Check, if the given argument is null
   *
   * @param argument which is checked for null
   * @param <T> Type of argument parameter (like {@link IReferencePoint reference point})
   * @param message message for the IllegalArgumentException
   * @exception IllegalArgumentException if given argument is null
   */
  private <T> void checkArgument(T argument, String message) {
    if (argument == null) throw new IllegalArgumentException(message);
  }
}
