package de.fu_berlin.inf.dpp.filesystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Factory to create adapters from Eclipse {@link org.eclipse.core.resources.IResource resources} to
 * Saros Core {@linkplain de.fu_berlin.inf.dpp.filesystem.IResource resources}.
 */
public class ResourceAdapterFactory {

  public static IPath create(org.eclipse.core.runtime.IPath path) {

    if (path == null) return null;

    return new EclipsePathImpl(path);
  }

  public static IProject create(org.eclipse.core.resources.IProject project) {
    return (IProject) adapt(project);
  }

  public static IFile create(org.eclipse.core.resources.IFile file) {
    return (IFile) adapt(file);
  }

  public static IFolder create(org.eclipse.core.resources.IFolder folder) {
    return (IFolder) adapt(folder);
  }

  public static IResource create(org.eclipse.core.resources.IResource resource) {
    return adapt(resource);
  }

  /**
   * Converts a collection of Eclipse resources to Saros Core file system resources. The elements
   * contained in the returned list have the same order as returned by the iterator of the
   * collection.
   *
   * @param resources collection of Eclipse resources
   * @return list which will the contain the converted resources or <code>null</code> if resources
   *     was <code>null</code>
   */
  public static List<IResource> convertTo(
      Collection<? extends org.eclipse.core.resources.IResource> resources) {
    if (resources == null) return null;

    List<IResource> out = new ArrayList<IResource>(resources.size());
    convertTo(resources, out);
    return out;
  }

  /**
   * Converts a Saros Core file system resource to a Eclipse resource.
   *
   * @param resource a Saros Core resource
   * @return the corresponding Eclipse {@linkplain org.eclipse.core.resources.IResource resource} or
   *     <code>null</code> if resource is <code>null</code>
   */
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
   */
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
   */
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
   */
  public static void convertBack(
      Collection<? extends IResource> in,
      Collection<? super org.eclipse.core.resources.IResource> out) {

    for (IResource resource : in) out.add(((EclipseResourceImpl) resource).getDelegate());
  }

  private static IResource adapt(org.eclipse.core.resources.IResource resource) {
    if (resource == null) return null;

    switch (resource.getType()) {
      case IResource.FILE:
        return new EclipseFileImpl(
            (org.eclipse.core.resources.IFile)
                resource.getAdapter(org.eclipse.core.resources.IFile.class));
      case IResource.FOLDER:
        return new EclipseFolderImpl(
            (org.eclipse.core.resources.IFolder)
                resource.getAdapter(org.eclipse.core.resources.IFolder.class));
      case IResource.PROJECT:
        return new EclipseProjectImpl(
            (org.eclipse.core.resources.IProject)
                resource.getAdapter(org.eclipse.core.resources.IProject.class));
      case IResource.ROOT:
        return new EclipseWorkspaceRootImpl(
            (org.eclipse.core.resources.IWorkspaceRoot)
                resource.getAdapter(org.eclipse.core.resources.IWorkspaceRoot.class));
      default:
        return new EclipseResourceImpl(resource);
    }
  }
}
