package de.fu_berlin.inf.dpp.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Listens for resource changes in shared project and fires activityDataObjects.
 */
class ProjectDeltaVisitor implements IResourceDeltaVisitor {

    /**
     * 
     */
    private SharedResourcesManager sharedResourcesManager;

    /**
     * @param sharedResourcesManager
     */
    ProjectDeltaVisitor(SharedResourcesManager sharedResourcesManager) {
        this.sharedResourcesManager = sharedResourcesManager;
    }

    // stores activityDataObjects that happen while one change event
    protected List<IResourceActivity> activitiesBuffer = new ArrayList<IResourceActivity>();

    /**
     * Compares the number of segments of the IPaths of the given Activities.
     * The longer path is 'bigger'.
     */
    protected class PathLengthComparator implements
        Comparator<IResourceActivity> {

        public int compare(IResourceActivity a1, IResourceActivity a2) {
            int a1sc = a1.getPath().getProjectRelativePath().segmentCount();
            int a2sc = a2.getPath().getProjectRelativePath().segmentCount();

            return a1sc - a2sc;
        }

    }

    /**
     * Compares the number of segments of the IPaths of the given Activities.
     * The shorter path is 'bigger'.
     */
    protected class ReversePathLengthComparator implements
        Comparator<IResourceActivity> {

        PathLengthComparator pathLengthComparator = new PathLengthComparator();

        public int compare(IResourceActivity a1, IResourceActivity a2) {
            return -pathLengthComparator.compare(a1, a2);
        }
    }

    /**
     * Orders activityDataObjects in the buffer and fires them. To be run before
     * change event ends.
     */
    public void finish() {
        for (IActivity activityDataObject : getOrderedActivities())
            fireActivity(activityDataObject);
    }

    /**
     * Orders activityDataObjects in buffer. Most importantly we want to execute
     * any file moves before deleting a folder.
     */
    // why do we need these sorting shenanigans?
    protected List<IResourceActivity> getOrderedActivities() {
        // TODO Use a comparator which includes all this as a sorting rule

        List<IResourceActivity> fileCreateActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> fileMoveActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> fileRemoveActivities = new ArrayList<IResourceActivity>();

        List<IResourceActivity> folderCreateActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> folderRemoveActivities = new ArrayList<IResourceActivity>();

        List<IResourceActivity> orderedList;

        /**
         * split all activityDataObjects in activityDataObjects buffer in groups
         * ({File,Folder} * {Create, Move, Delete})
         */
        for (IResourceActivity activity : activitiesBuffer) {
            FolderActivity.Type tFolder;
            FileActivity.Type tFile;

            if (activity instanceof FileActivity) {
                tFile = ((FileActivity) activity).getType();
                if (tFile == FileActivity.Type.Created)
                    fileCreateActivities.add(activity);
                else if (tFile == FileActivity.Type.Moved)
                    fileMoveActivities.add(activity);
                else if (tFile == FileActivity.Type.Removed)
                    fileRemoveActivities.add(activity);

            }
            if (activity instanceof FolderActivity) {
                tFolder = ((FolderActivity) activity).getType();
                if (tFolder == FolderActivity.Type.Created)
                    folderCreateActivities.add(activity);
                else if (tFolder == FolderActivity.Type.Removed)
                    folderRemoveActivities.add(activity);

            }
        }

        PathLengthComparator pathLengthComparator = new PathLengthComparator();
        // ReversePathLengthComparator iplc = new ReversePathLengthComparator();

        // Sorts Activities by their length
        Collections.sort(fileCreateActivities, pathLengthComparator);
        Collections.sort(fileMoveActivities, pathLengthComparator);
        // Collections.sort(fileRemoveActivities, iplc);
        Collections.sort(folderCreateActivities, pathLengthComparator);
        // Collections.sort(folderRemoveActivities, iplc);

        orderedList = new ArrayList<IResourceActivity>();

        // add activityDataObjects to the result
        orderedList.addAll(folderCreateActivities);
        orderedList.addAll(fileMoveActivities);
        orderedList.addAll(fileCreateActivities);
        orderedList.addAll(fileRemoveActivities);
        orderedList.addAll(folderRemoveActivities);

        return orderedList;
    }

    public boolean visit(IResourceDelta delta) {

        assert this.sharedResourcesManager.sarosSession != null;

        ISarosSession sarosSession = sharedResourcesManager.sarosSession;
        if (!sarosSession.isDriver()) {
            return false;
        }

        IResource resource = delta.getResource();
        if (resource instanceof IProject)
            return sarosSession.isShared((IProject) resource);

        if (resource.isDerived()) {
            return false;
        }

        if (resource instanceof IFile) {
            handleFileDelta(delta);
            return true;
        } else if (resource instanceof IFolder)
            return handleFolderDelta(delta);

        assert false;
        return true;
    }

    protected boolean handleFolderDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();
        int kind = delta.getKind();
        switch (kind) {
        case IResourceDelta.ADDED:
            addActivity(new FolderActivity(
                this.sharedResourcesManager.sarosSession.getLocalUser(),
                FolderActivity.Type.Created, new SPath(resource)));
            return true;
        case IResourceDelta.REMOVED:
            addActivity(new FolderActivity(
                this.sharedResourcesManager.sarosSession.getLocalUser(),
                FolderActivity.Type.Removed, new SPath(resource)));
            // We don't want visit the children if this folder was removed. The
            // only interesting case is that a child file was moved out of this
            // folder, but then we're still going to visit the move target.
            return false;
        default:
            return kind != IResourceDelta.NO_CHANGE;
        }
    }

    protected void handleFileDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();
        int kind = delta.getKind();

        switch (kind) {
        case IResourceDelta.CHANGED:
            if (!isContentChange(delta))
                return;

            addActivity(createdUnlessOpen(resource));
            return;

        case IResourceDelta.ADDED:

            // is this an "ADD" while moving/renaming a file?
            if (isMovedFrom(delta)) {

                IPath newPath = resource.getFullPath();
                // Adds have getMovedFrom set:
                IPath oldPath = delta.getMovedFromPath();

                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IProject newProject = root.getProject(newPath.segment(0));
                IProject oldProject = root.getProject(oldPath.segment(0));

                if (this.sharedResourcesManager.sarosSession
                    .isShared(newProject)) {
                    if (this.sharedResourcesManager.sarosSession
                        .isShared(oldProject)) {
                        // Moving inside the shared project
                        try {
                            addActivity(FileActivity.moved(
                                this.sharedResourcesManager.sarosSession
                                    .getLocalUser(),
                                new SPath(newProject, newPath
                                    .removeFirstSegments(1)),
                                new SPath(oldProject, oldPath
                                    .removeFirstSegments(1)),
                                isContentChange(delta)));
                            return;
                        } catch (IOException e) {
                            SharedResourcesManager.log
                                .warn("Resource could not be read for"
                                    + " sending to peers:"
                                    + resource.getLocation());
                        }
                    } else {
                        // Moving a file into the shared project
                        // -> Treat like an add!

                        // Fall-through
                    }
                } else {
                    // Moving away!
                    addActivity(FileActivity
                        .removed(this.sharedResourcesManager.sarosSession
                            .getLocalUser(), new SPath(resource),
                            Purpose.ACTIVITY));
                    return;
                }
            }

            // usual files adding procedure

            addActivity(createdUnlessOpen(resource));
            return;

        case IResourceDelta.REMOVED:
            if (isMoved(delta)) {

                // REMOVED deltas have MovedTo set
                IPath newPath = delta.getMovedToPath();

                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IProject newProject = root.getProject(newPath.segment(0));

                if (this.sharedResourcesManager.sarosSession
                    .isShared(newProject)) {
                    // Ignore "REMOVED" while moving into shared project
                    return;
                }
                // else moving file away from shared project, need to tell
                // others to delete! Fall-through...
            }

            addActivity(FileActivity.removed(
                this.sharedResourcesManager.sarosSession.getLocalUser(),
                new SPath(resource), Purpose.ACTIVITY));
            return;

        default:
            return;
        }
    }

    protected void addActivity(IResourceActivity activity) {
        activitiesBuffer.add(activity);
    }

    /**
     * Returns a new FileActivity.created if the file is not currently in any
     * open editor. We ignore opened files because otherwise we might send
     * CHANGED events for files that are also handled by the editor manager.<br>
     * If an error occurs while reading the file, this method returns null.
     * 
     * @param resource
     * @return
     */
    private IResourceActivity createdUnlessOpen(IResource resource) {
        SPath spath = new SPath(resource);
        if (this.sharedResourcesManager.editorManager.isOpened(spath)) {
            return null;
        }

        SharedResourcesManager.log.debug("Resource " + resource.getName()
            + " changed");
        try {
            return FileActivity.created(
                this.sharedResourcesManager.sarosSession.getLocalUser(), spath,
                Purpose.ACTIVITY);
        } catch (IOException e) {
            SharedResourcesManager.log.warn(
                "Resource could not be read for sending to peers:"
                    + resource.getLocation(), e);
            return null;
        }
    }

    /**
     * It analyzes the given IResourceDelta and returns true if the
     * "Moved"-flags are set.
     * */
    protected boolean isMoved(IResourceDelta delta) {
        return (isMovedFrom(delta) || isMovedTo(delta));
    }

    protected boolean isMovedFrom(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0);
    }

    protected boolean isMovedTo(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0);
    }

    /**
     * Indicates content change of a resource while checking its delta.
     */
    protected boolean isContentChange(IResourceDelta delta) {
        return ((delta.getFlags() & IResourceDelta.CONTENT) != 0);

    }

    protected void fireActivity(final IActivity activityDataObject) {
        Util.runSafeSWTSync(SharedResourcesManager.log, new Runnable() {
            public void run() {
                for (IActivityListener listener : ProjectDeltaVisitor.this.sharedResourcesManager.listeners) {
                    listener.activityCreated(activityDataObject);
                }
            }
        });
    }
}