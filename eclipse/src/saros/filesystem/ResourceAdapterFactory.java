package saros.filesystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Factory to create adapters from Eclipse {@link org.eclipse.core.resources.IResource resources} to
 * Saros Core {@linkplain saros.filesystem.IResource resources}.
 *
 * @deprecated use {@link ResourceConverter} instead
 */
@Deprecated
public class ResourceAdapterFactory {

  /**
   * @deprecated use {@link ResourceConverter#convertToPath(org.eclipse.core.runtime.IPath)} instead
   */
  @Deprecated
  public static IPath create(org.eclipse.core.runtime.IPath path) {

    if (path == null) return null;

    return new EclipsePathImpl(path);
  }

  /** @deprecated should not be needed anymore */
  @Deprecated
  public static IReferencePoint create(org.eclipse.core.resources.IProject project) {
    return (IReferencePoint) adapt(project);
  }

  /**
   * @deprecated use {@link ResourceConverter#convertToFile(Set, org.eclipse.core.resources.IFile)}
   *     or {@link ResourceConverter#convertToFile(IReferencePoint,
   *     org.eclipse.core.resources.IFile)} instead
   */
  @Deprecated
  public static IFile create(org.eclipse.core.resources.IFile file) {
    return (IFile) adapt(file);
  }

  /**
   * @deprecated use {@link ResourceConverter#convertToContainer(Set,
   *     org.eclipse.core.resources.IContainer)} or {@link
   *     ResourceConverter#convertToContainer(IReferencePoint,
   *     org.eclipse.core.resources.IContainer)} instead
   */
  @Deprecated
  public static IFolder create(org.eclipse.core.resources.IFolder folder) {
    return (IFolder) adapt(folder);
  }

  /**
   * @deprecated use {@link ResourceConverter#convertToResource(Set,
   *     org.eclipse.core.resources.IResource)} or {@link
   *     ResourceConverter#convertToResource(IReferencePoint, org.eclipse.core.resources.IResource)}
   *     instead
   */
  @Deprecated
  public static IResource create(org.eclipse.core.resources.IResource resource) {
    return adapt(resource);
  }

  /**
   * Converts a Saros Core file system resource to a Eclipse resource.
   *
   * @param resource a Saros Core resource
   * @return the corresponding Eclipse {@linkplain org.eclipse.core.resources.IResource resource} or
   *     <code>null</code> if resource is <code>null</code>
   * @deprecated use {@link ResourceConverter#getDelegate(IResource)}
   */
  @Deprecated
  public static org.eclipse.core.resources.IResource convertBack(IResource resource) {

    if (resource == null) return null;

    return ((EclipseResourceImpl) resource).getDelegate();
  }

  /**
   * Converts a collection of Saros Core file system resources to Eclipse resources.The elements
   * contained in the returned list have the same order as returned by the iterator of the
   * collection.
   *
   * @param resources collection of Saros Core resources
   * @return list which will the contain the converted resources or <code>null</code> if resources
   *     was <code>null</code>
   * @deprecated no longer supported; pull loop outside and use {@link
   *     ResourceConverter#getDelegate(IResource)}
   */
  @Deprecated
  public static List<org.eclipse.core.resources.IResource> convertBack(
      Collection<? extends IResource> resources) {
    if (resources == null) return null;

    List<org.eclipse.core.resources.IResource> out =
        new ArrayList<org.eclipse.core.resources.IResource>(resources.size());
    convertBack(resources, out);
    return out;
  }

  /**
   * Converts a collection of Eclipse resources to Saros Core file system resources.
   *
   * @param in collection of Eclipse resources
   * @param out collection which will the contain the converted resources
   * @deprecated no longer supported; pull loop outside and use {@link
   *     ResourceConverter#convertToResource(Set, org.eclipse.core.resources.IResource)} or {@link
   *     ResourceConverter#convertToResource(IReferencePoint, org.eclipse.core.resources.IResource)}
   */
  @Deprecated
  public static void convertTo(
      Collection<? extends org.eclipse.core.resources.IResource> in,
      Collection<? super IResource> out) {

    for (org.eclipse.core.resources.IResource resource : in) out.add(adapt(resource));
  }

  /**
   * Converts a collection of Saros Core file system resources to Eclipse resources.
   *
   * @param in collection of Saros Core file system resources
   * @param out collection which will the contain the converted resources
   * @deprecated no longer supported; pull loop outside and use {@link
   *     ResourceConverter#getDelegate(IResource)}
   */
  @Deprecated
  public static void convertBack(
      Collection<? extends IResource> in,
      Collection<? super org.eclipse.core.resources.IResource> out) {

    for (IResource resource : in) out.add(((EclipseResourceImpl) resource).getDelegate());
  }

  private static IResource adapt(org.eclipse.core.resources.IResource resource) {
    if (resource == null) return null;

    switch (resource.getType()) {
      case org.eclipse.core.resources.IResource.FILE:
        return new EclipseFileImpl(resource.getAdapter(org.eclipse.core.resources.IFile.class));

      case org.eclipse.core.resources.IResource.FOLDER:
        return new EclipseFolderImpl(resource.getAdapter(org.eclipse.core.resources.IFolder.class));

      case org.eclipse.core.resources.IResource.PROJECT:
        return new EclipseProjectImpl(
            resource.getAdapter(org.eclipse.core.resources.IProject.class));

      case org.eclipse.core.resources.IResource.ROOT:
        return new EclipseContainerImpl(
            resource.getAdapter(org.eclipse.core.resources.IWorkspaceRoot.class));

      default:
        return new EclipseResourceImpl(resource);
    }
  }
}
