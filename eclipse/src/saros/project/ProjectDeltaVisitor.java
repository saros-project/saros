package saros.project;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import saros.activities.FileActivity;
import saros.activities.FileActivity.Purpose;
import saros.activities.FileActivity.Type;
import saros.activities.FolderCreatedActivity;
import saros.activities.FolderDeletedActivity;
import saros.activities.IResourceActivity;
import saros.activities.SPath;
import saros.editor.EditorManager;
import saros.filesystem.IFolder;
import saros.filesystem.ResourceAdapterFactory;
import saros.session.ISarosSession;
import saros.session.User;
import saros.util.FileUtils;

/**
 * Visits the resource changes in a shared project.
 *
 * <p><b>Note:</b> The visitor has to be reset in order to be reused.
 */
final class ProjectDeltaVisitor implements IResourceDeltaVisitor {

  private static final Logger log = Logger.getLogger(ProjectDeltaVisitor.class);

  private static final int CAPACITY_THRESHOLD = 128;

  private final EditorManager editorManager;

  private final User user;

  private final ISarosSession session;

  private List<IResourceActivity> resourceActivities =
      new ArrayList<IResourceActivity>(CAPACITY_THRESHOLD);

  public ProjectDeltaVisitor(ISarosSession session, EditorManager editorManager) {
    this.session = session;
    this.editorManager = editorManager;
    this.user = session.getLocalUser();
  }

  public List<IResourceActivity> getActivities() {
    return sort(resourceActivities);
  }

  public void reset() {
    if (resourceActivities.size() > CAPACITY_THRESHOLD) {
      resourceActivities = new ArrayList<IResourceActivity>(CAPACITY_THRESHOLD);
    } else {
      resourceActivities.clear();
    }
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

    IProject project = resource.getProject();
    boolean contentChange = isContentChange(delta);

    /*
     * TODO Generate better move deltas, e.g we share multiple projects and
     * a file is moved between those projects. Currently we generate a
     * delete and create activity.
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
          IProject oldProject = getProject(oldFullPath);

          if (project.equals(oldProject)) {
            // Moving inside this project
            generateMoved(resource, oldFullPath, oldProject, contentChange);
            return;
          }

          /*
           * else moving a file into the shared project , treat like an
           * add! Fall-through ...
           */
        }

        generateCreated(resource);

        return;

      case IResourceDelta.REMOVED:
        if (isMoved(delta)) {

          // REMOVED deltas have MovedTo set
          IPath newPath = delta.getMovedToPath();
          IProject newProject = ProjectDeltaVisitor.getProject(newPath);

          // Ignore "REMOVED" while moving into shared project
          if (project.equals(newProject)) return;

          /*
           * else moving file away from shared project, need to tell
           * others to delete! Fall-through...
           */
        }

        generateRemoved(resource);
        return;

      default:
        return;
    }
  }

  private void generateCreated(IResource resource) {

    saros.filesystem.IResource wrappedResource = ResourceAdapterFactory.create(resource);

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

      addActivity(new FolderCreatedActivity(user, new SPath((IFolder) wrappedResource)));

    } else {
      assert false : "cannot handle resource of IResource#getType() = " + resource.getType();
    }
  }

  private void generateMoved(
      IResource resource, IPath oldFullPath, IProject oldProject, boolean contentChange) {

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

    saros.filesystem.IFile newFile = ResourceAdapterFactory.create(file);

    saros.filesystem.IFile oldFile =
        ResourceAdapterFactory.create(oldProject)
            .getFile(ResourceAdapterFactory.create(oldFullPath.removeFirstSegments(1)));

    addActivity(
        new FileActivity(user, Type.MOVED, Purpose.ACTIVITY, newFile, oldFile, content, charset));
  }

  private void generateRemoved(IResource resource) {

    saros.filesystem.IResource removedResource = ResourceAdapterFactory.create(resource);

    if (resource instanceof IFile) {
      saros.filesystem.IFile removedFile = (saros.filesystem.IFile) removedResource;

      editorManager.closeEditor(new SPath(removedFile), false);

      addActivity(
          new FileActivity(user, Type.REMOVED, Purpose.ACTIVITY, removedFile, null, null, null));

    } else {
      IFolder removedFolder = (IFolder) removedResource;

      addActivity(new FolderDeletedActivity(user, new SPath(removedFolder)));
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

    saros.filesystem.IFile wrappedFile = ResourceAdapterFactory.create(file);

    if (!session.isShared(wrappedFile)) return;

    if (editorManager.isOpened(new SPath(wrappedFile)) || editorManager.isManaged(file)) return;

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

  private void addActivity(IResourceActivity activity) {
    resourceActivities.add(activity);
  }

  // Utility methods

  private static IProject getProject(IPath path) {
    assert path != null && path.segmentCount() > 0;
    return ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
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
  private static List<IResourceActivity> sort(final List<IResourceActivity> resourceActivities) {
    /*
     * haferburg: Sorting is not necessary, because activities are already
     * sorted enough (activity on parent comes before activity on child).
     * All we need to do is make sure that folders are created first and
     * deleted last. The sorting stuff was introduced with 1742 (1688).
     */
    List<IResourceActivity> fileActivities = new ArrayList<IResourceActivity>();
    List<IResourceActivity> folderCreateActivities = new ArrayList<IResourceActivity>();
    List<IResourceActivity> folderRemoveActivities = new ArrayList<IResourceActivity>();
    List<IResourceActivity> otherActivities = new ArrayList<IResourceActivity>();

    // Split all collectedActivities.
    for (IResourceActivity activity : resourceActivities) {
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
    List<IResourceActivity> result = new ArrayList<IResourceActivity>();
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
