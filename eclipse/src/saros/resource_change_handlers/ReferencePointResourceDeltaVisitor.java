package saros.resource_change_handlers;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.IPath;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IResourceActivity;
import saros.editor.EditorManager;
import saros.filesystem.IFolder;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.session.ISarosSession;
import saros.session.User;
import saros.util.FileUtils;

/** Visits the resource changes in a shared reference point. */
final class ReferencePointResourceDeltaVisitor implements IResourceDeltaVisitor {

  private static final Logger log = Logger.getLogger(ReferencePointResourceDeltaVisitor.class);

  private static final int CAPACITY_THRESHOLD = 128;

  private final EditorManager editorManager;

  private final User user;

  private final ISarosSession session;

  /** The reference point whose resources the delta visitor is iterating. */
  private final IReferencePoint referencePoint;

  private final IContainer referencePointDelegate;

  private final List<IResourceActivity<? extends saros.filesystem.IResource>> resourceActivities;

  public ReferencePointResourceDeltaVisitor(
      ISarosSession session, EditorManager editorManager, IReferencePoint referencePoint) {
    this.session = session;
    this.editorManager = editorManager;
    this.referencePoint = referencePoint;
    this.referencePointDelegate = ResourceConverter.getDelegate(referencePoint);
    this.user = session.getLocalUser();

    this.resourceActivities = new ArrayList<>(CAPACITY_THRESHOLD);
  }

  public List<IResourceActivity<? extends saros.filesystem.IResource>> getActivities() {
    return sort(resourceActivities);
  }

  @Override
  public boolean visit(IResourceDelta delta) {
    IResource resource = delta.getResource();

    if (resource.isDerived()) return false;

    /*
     * TODO Refactor this, we don't need to make a distinction here.
     * Resource is resource. It's just Saros that insists on having separate
     * activities for files and folders.
     */
    if (isFile(resource)) {
      handleFileDelta(delta);
      return true;
    } else if (isFolder(resource)) {
      return handleFolderDelta(delta);
    }

    return true;
  }

  private boolean handleFolderDelta(IResourceDelta delta) {
    IResource resource = delta.getResource();
    int kind = delta.getKind();

    switch (kind) {
      case IResourceDelta.ADDED:
        generateCreated(resource);
        return true;

      case IResourceDelta.REMOVED:
        generateRemoved(resource);
        return true;

      default:
        return kind != IResourceDelta.NO_CHANGE;
    }
  }

  private void handleFileDelta(IResourceDelta delta) {
    IResource resource = delta.getResource();
    int kind = delta.getKind();

    boolean contentChange = isContentChange(delta);

    /*
     * TODO Generate better move deltas, e.g we share multiple reference points and
     *  a file is moved between them. Currently we generate a delete and create activity.
     */

    switch (kind) {
      case IResourceDelta.CHANGED:
        if (contentChange) generateContentChanged(resource);

        return;

      case IResourceDelta.ADDED:

        // Was this file moved or renamed?
        if (isMovedFrom(delta)) {

          // Adds have getMovedFrom set:
          IPath oldFullPath = delta.getMovedFromPath();

          if (isSameReferencePoint(oldFullPath)) {
            // Moving inside this reference point
            generateMoved(resource, oldFullPath, contentChange);
            return;
          }

          /*
           * else moving a file into (or between) the shared reference point(s), treat like an add!
           * Fall-through ...
           */
        }

        generateCreated(resource);

        return;

      case IResourceDelta.REMOVED:
        if (isMoved(delta)) {

          // REMOVED deltas have MovedTo set
          IPath newPath = delta.getMovedToPath();

          // Ignore "REMOVED" while moving in shared reference point
          if (isSameReferencePoint(newPath)) return;

          /*
           * else moving file out of (or between) shared reference point(s), treat like a delete!
           * Fall-through...
           */
        }

        generateRemoved(resource);
        return;

      default:
        return;
    }
  }

  private void generateCreated(IResource resource) {

    saros.filesystem.IResource wrappedResource =
        ResourceConverter.convertToResource(referencePoint, resource);

    if (wrappedResource == null) {
      log.error(
          "Could not create resource creation activity for "
              + resource
              + " as no Saros resource object could be obtained - used reference point: "
              + referencePoint);

      return;

    } else if (!session.isShared(wrappedResource)) {
      log.debug("Ignoring resource creation of ignored resource " + wrappedResource);

      return;
    }

    if (isFile(resource)) {
      IFile file = resource.getAdapter(IFile.class);

      byte[] content = FileUtils.getLocalFileContent(file);
      String charset = FileUtils.getLocalFileCharset(file);

      if (content == null || charset == null) {
        logResourceReadError(resource);
        return;
      }

      addActivity(
          new FileActivity(
              user,
              Type.CREATED,
              Purpose.ACTIVITY,
              (saros.filesystem.IFile) wrappedResource,
              null,
              content,
              charset));

    } else if (isFolder(resource)) {

      addActivity(new FolderCreatedActivity(user, (IFolder) wrappedResource));

    } else {
      assert false : "cannot handle resource of IResource#getType() = " + resource.getType();
    }
  }

  private void generateMoved(IResource resource, IPath oldFullPath, boolean contentChange) {

    byte[] content = null;
    String charset = null;

    assert resource.getType() == IResource.FILE;

    IFile file = resource.getAdapter(IFile.class);

    if (contentChange) {
      content = FileUtils.getLocalFileContent(file);
      charset = FileUtils.getLocalFileCharset(file);

      if (content == null || charset == null) {
        logResourceReadError(resource);
        return;
      }
    }

    saros.filesystem.IFile newFile = ResourceConverter.convertToFile(referencePoint, file);

    if (newFile == null) {
      log.error(
          "Could not create file move activity for "
              + file
              + " as no Saros file object could be obtained - used reference point: "
              + referencePoint);

      return;
    }

    IPath referencePointRelativePath = getReferencePointRelativePath(oldFullPath);

    if (referencePointRelativePath.equals(oldFullPath)) {
      log.error(
          "Could not create file move activity for "
              + file
              + " as no reference-point-relative path could be calculated for the old location "
              + oldFullPath
              + " - used reference point path: "
              + referencePointRelativePath);

      return;
    }

    saros.filesystem.IFile oldFile =
        referencePoint.getFile(ResourceConverter.toPath(referencePointRelativePath));

    boolean newFileIsShared = session.isShared(newFile);
    boolean oldFileWasShared = session.isShared(oldFile);

    if (newFileIsShared && oldFileWasShared) {
      addActivity(
          new FileActivity(user, Type.MOVED, Purpose.ACTIVITY, newFile, oldFile, content, charset));

    } else if (newFileIsShared) {
      addActivity(
          new FileActivity(user, Type.CREATED, Purpose.ACTIVITY, newFile, null, content, charset));

    } else if (oldFileWasShared) {
      addActivity(
          new FileActivity(user, Type.REMOVED, Purpose.ACTIVITY, null, oldFile, null, null));

    } else {
      log.debug(
          "Ignoring file move with both ignored source and target file - old file: "
              + oldFile
              + ", new file: "
              + newFile);
    }
  }

  private void generateRemoved(IResource resource) {

    saros.filesystem.IResource removedResource =
        ResourceConverter.convertToResource(referencePoint, resource);

    if (removedResource == null) {
      log.error(
          "Could not create resource deletion activity for "
              + resource
              + " as no Saros resource object could be obtained - used reference point: "
              + referencePoint);

      return;

    } else if (!session.isShared(removedResource)) {
      log.debug("Ignoring resource deletion of ignored resource " + removedResource);

      return;
    }

    if (resource instanceof IFile) {
      saros.filesystem.IFile removedFile = (saros.filesystem.IFile) removedResource;

      editorManager.closeEditor(removedFile, false);

      addActivity(
          new FileActivity(user, Type.REMOVED, Purpose.ACTIVITY, removedFile, null, null, null));

    } else {
      IFolder removedFolder = (IFolder) removedResource;

      addActivity(new FolderDeletedActivity(user, removedFolder));
    }
  }

  /**
   * Adds a FileActivity.created if the file is currently not managed by the EditorManager. We
   * ignore managed files because otherwise we might send CHANGED events for files that are also
   * handled by the editor manager.
   *
   * <p>We also ignore files that are not part of the current sharing.
   *
   * @param resource
   */
  private void generateContentChanged(IResource resource) {

    assert resource.getType() == IResource.FILE;

    IFile file = resource.getAdapter(IFile.class);

    saros.filesystem.IFile wrappedFile = ResourceConverter.convertToFile(referencePoint, file);

    if (wrappedFile == null) {
      log.error(
          "Could not create content change activity for "
              + file
              + " as no Saros file object could be obtained - used reference point: "
              + referencePoint);

      return;

    } else if (!session.isShared(wrappedFile)) {
      log.debug("Ignoring content change of ignored file " + wrappedFile);

      return;
    }

    if (!session.isShared(wrappedFile)) return;

    if (editorManager.isOpened(wrappedFile) || editorManager.isManaged(file)) return;

    byte[] content = FileUtils.getLocalFileContent(file);
    String charset = FileUtils.getLocalFileCharset(file);

    if (content == null || charset == null) {
      logResourceReadError(resource);

    } else {
      addActivity(
          new FileActivity(
              user, Type.CREATED, Purpose.ACTIVITY, wrappedFile, null, content, charset));
    }
  }

  private void addActivity(IResourceActivity<? extends saros.filesystem.IResource> activity) {
    resourceActivities.add(activity);
  }

  // Utility methods

  /**
   * Returns whether the given full resource path points to a child resource of the reference point.
   *
   * @param fullResourcePath the full resource path to check
   * @return whether the given full resource path points to a child resource of the reference point
   */
  private boolean isSameReferencePoint(IPath fullResourcePath) {
    return referencePointDelegate.getFullPath().isPrefixOf(fullResourcePath);
  }

  /**
   * Returns the reference-point-relative path representing the given full path.
   *
   * @param fullResourcePath the full resource path to relativize
   * @return the reference-point-relative path representing the given full path or the given full
   *     path if no such path could be constructed
   */
  private IPath getReferencePointRelativePath(IPath fullResourcePath) {
    IPath referencePointPath = referencePointDelegate.getFullPath();

    return fullResourcePath.makeRelativeTo(referencePointPath);
  }

  private static boolean isMoved(IResourceDelta delta) {
    return (isMovedFrom(delta) || isMovedTo(delta));
  }

  private static boolean isMovedFrom(IResourceDelta delta) {
    return ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0);
  }

  private static boolean isMovedTo(IResourceDelta delta) {
    return ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0);
  }

  private static boolean isContentChange(IResourceDelta delta) {
    return ((delta.getFlags() & IResourceDelta.CONTENT) != 0);
  }

  private static boolean isFolder(IResource resource) {
    return resource.getType() == IResource.FOLDER;
  }

  private static boolean isFile(IResource resource) {
    return resource.getType() == IResource.FILE;
  }

  /**
   * Sorts the given resource activities, ensuring that folders are always created before files and
   * files are always deleted before folders.
   */
  private static List<IResourceActivity<? extends saros.filesystem.IResource>> sort(
      final List<IResourceActivity<? extends saros.filesystem.IResource>> resourceActivities) {
    /*
     * haferburg: Sorting is not necessary, because activities are already
     * sorted enough (activity on parent comes before activity on child).
     * All we need to do is make sure that folders are created first and
     * deleted last. The sorting stuff was introduced with 1742 (1688).
     */
    List<IResourceActivity<? extends saros.filesystem.IResource>> fileActivities =
        new ArrayList<>();
    List<IResourceActivity<? extends saros.filesystem.IResource>> folderCreateActivities =
        new ArrayList<>();
    List<IResourceActivity<? extends saros.filesystem.IResource>> folderRemoveActivities =
        new ArrayList<>();
    List<IResourceActivity<? extends saros.filesystem.IResource>> otherActivities =
        new ArrayList<>();

    // Split all collectedActivities.
    for (IResourceActivity<? extends saros.filesystem.IResource> activity : resourceActivities) {
      if (activity instanceof FileActivity) {
        fileActivities.add(activity);
      } else if (activity instanceof FolderCreatedActivity) {
        folderCreateActivities.add(activity);
      } else if (activity instanceof FolderDeletedActivity) {
        folderRemoveActivities.add(activity);
      } else {
        otherActivities.add(activity);
      }
    }

    // Add activities to the result.
    List<IResourceActivity<? extends saros.filesystem.IResource>> result = new ArrayList<>();
    result.addAll(folderCreateActivities);
    result.addAll(fileActivities);
    result.addAll(folderRemoveActivities);
    result.addAll(otherActivities);

    return result;
  }

  private static void logResourceReadError(IResource resource) {
    log.error("could not read contents of file: " + resource.getFullPath());
  }
}
