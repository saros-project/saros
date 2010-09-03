package de.fu_berlin.inf.dpp.project;

import java.io.IOException;
import java.util.ArrayList;
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

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSProjectInformation;

/**
 * Visits the resource changes in a shared project.
 */
class ProjectDeltaVisitor implements IResourceDeltaVisitor {

    /**
     * TODO
     */
    protected SharedResourcesManager sharedResourcesManager;
    protected SharedProject sharedProject;

    /**
     * TODO
     * 
     * @param sharedResourcesManager
     * @param sharedProject
     */
    ProjectDeltaVisitor(SharedResourcesManager sharedResourcesManager,
        SharedProject sharedProject) {
        this.sharedResourcesManager = sharedResourcesManager;
        this.sharedProject = sharedProject;
    }

    /** Stores activities to be sent due to one change event. */
    protected List<IResourceActivity> pendingActivities = new ArrayList<IResourceActivity>();

    /**
     * Orders activities in buffer.<br>
     * We want to execute any moves before deleting, and after creating folders.
     * 
     * haferburg: Sorting is not necessary, because they are already sorted
     * enough (activity on parent comes before activity on child). All we need
     * to do is make sure that folders are created first and deleted last. The
     * sorting stuff was introduced with 1742 (1688).
     */
    protected List<IResourceActivity> getOrderedActivities() {
        List<IResourceActivity> fileActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> folderCreateActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> folderRemoveActivities = new ArrayList<IResourceActivity>();
        List<IResourceActivity> otherActivities = new ArrayList<IResourceActivity>();

        /**
         * Split all pendingActivities.
         */
        for (IResourceActivity activity : pendingActivities) {
            FolderActivity.Type tFolder;

            if (activity instanceof FileActivity) {
                fileActivities.add(activity);
            } else if (activity instanceof FolderActivity) {
                tFolder = ((FolderActivity) activity).getType();
                if (tFolder == FolderActivity.Type.Created)
                    folderCreateActivities.add(activity);
                else if (tFolder == FolderActivity.Type.Removed)
                    folderRemoveActivities.add(activity);
            } else {
                otherActivities.add(activity);
            }
        }

        // Add activities to the result.
        List<IResourceActivity> result = folderCreateActivities;
        result.addAll(fileActivities);
        result.addAll(folderRemoveActivities);
        result.addAll(otherActivities);

        return result;
    }

    public boolean visit(IResourceDelta delta) {
        IResource resource = delta.getResource();
        if (resource.isDerived()) {
            return false;
        }

        boolean isProject = resource instanceof IProject;
        if (isProject) {
            IProject project = (IProject) resource;
            if (!project.isOpen())
                return false;

            VCSAdapter vcs = sharedProject.getVCSAdapter();
            if (vcs != null) {
                VCSProjectInformation test = vcs.getProjectInformation(project);
                String url = test.repositoryURL + test.projectPath;

                String oldUrl = sharedProject.vcsUrl;

                if (!url.equals(oldUrl)) {
                    // Switch
                    SPath spath = new SPath(resource);
                    User user = getUser();
                    addActivity(VCSActivity.switch_(user, spath, url, "HEAD"));
                    sharedProject.vcsUrl = url;
                    return false;
                }
            }
        }

        if (resource instanceof IFile) {
            handleFileDelta(delta);
            return true;
        } else if (resource instanceof IFolder || isProject) {
            return handleFolderDelta(delta);
        }

        assert false;
        return true;
    }

    protected boolean handleFolderDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();

        int kind = delta.getKind();
        switch (kind) {
        case IResourceDelta.ADDED:
            addActivity(new FolderActivity(getUser(),
                FolderActivity.Type.Created, new SPath(resource)));
            return true;
        case IResourceDelta.REMOVED:
            addActivity(new FolderActivity(getUser(),
                FolderActivity.Type.Removed, new SPath(resource)));
            // We don't want visit the children if this folder was removed. The
            // only interesting case is that a file was moved out of this or a
            // child folder, but we're still going to visit the move target.
            return false;
        default:
            return kind != IResourceDelta.NO_CHANGE;
        }
    }

    protected User getUser() {
        return sharedResourcesManager.sarosSession.getLocalUser();
    }

    protected void handleFileDelta(IResourceDelta delta) {
        IResource resource = delta.getResource();
        int kind = delta.getKind();

        switch (kind) {
        case IResourceDelta.CHANGED:
            if (!isContentChange(delta))
                return;

            addCreatedUnlessOpen(resource);
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

                if (sharedResourcesManager.sarosSession.isShared(newProject)) {
                    if (sharedResourcesManager.sarosSession
                        .isShared(oldProject)) {
                        // Moving inside the shared project
                        try {
                            addActivity(FileActivity.moved(
                                getUser(),
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
                    addActivity(FileActivity.removed(getUser(), new SPath(
                        resource), Purpose.ACTIVITY));
                    return;
                }
            }

            // usual files adding procedure

            addCreatedUnlessOpen(resource);
            return;

        case IResourceDelta.REMOVED:
            if (isMoved(delta)) {

                // REMOVED deltas have MovedTo set
                IPath newPath = delta.getMovedToPath();

                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IProject newProject = root.getProject(newPath.segment(0));

                if (sharedResourcesManager.sarosSession.isShared(newProject)) {
                    // Ignore "REMOVED" while moving into shared project
                    return;
                }
                // else moving file away from shared project, need to tell
                // others to delete! Fall-through...
            }

            addActivity(FileActivity.removed(getUser(), new SPath(resource),
                Purpose.ACTIVITY));
            return;

        default:
            return;
        }
    }

    protected void addActivity(IResourceActivity activity) {
        pendingActivities.add(activity);
    }

    /**
     * Adds a FileActivity.created if the file is not currently in any open
     * editor. We ignore opened files because otherwise we might send CHANGED
     * events for files that are also handled by the editor manager.
     * 
     * @param resource
     */
    private void addCreatedUnlessOpen(IResource resource) {
        SPath spath = new SPath(resource);
        if (sharedResourcesManager.editorManager.isOpened(spath)) {
            return;
        }

        SharedResourcesManager.log.debug("Resource " + resource.getName()
            + " changed");
        User user = getUser();
        try {
            addActivity(FileActivity.created(user, spath, Purpose.ACTIVITY));
        } catch (IOException e) {
            SharedResourcesManager.log.warn(
                "Resource could not be read for sending to peers:"
                    + resource.getLocation(), e);
            return;
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

    /**
     * Fires the ordered activities. To be run before change event ends.
     */
    // TODO Move to SRM :(
    public void finish() {
        for (IActivity activityDataObject : getOrderedActivities())
            fireActivity(activityDataObject);
    }

    // TODO Move to SRM :(
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
