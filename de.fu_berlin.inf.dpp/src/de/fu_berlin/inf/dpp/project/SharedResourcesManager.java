/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
 * (c) Riad Djemili - 2006
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 1, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package de.fu_berlin.inf.dpp.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity.Purpose;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * This manager is responsible for handling all resource changes that aren't
 * handled by the EditorManager, that is for changes that aren't done by
 * entering text in an text editor. It creates and executes file activities and
 * folder activities.
 * 
 * @author rdjemili
 * 
 */
@Component(module = "core")
public class SharedResourcesManager implements IResourceChangeListener,
    IActivityProvider, Disposable {

    private static Logger log = Logger.getLogger(SharedResourcesManager.class
        .getName());

    /**
     * While paused the SharedResourcesManager doesn't fire activities
     */
    protected boolean pause = false;

    protected ISharedProject sharedProject;

    protected List<IActivityListener> listeners = new LinkedList<IActivityListener>();

    protected StopManager stopManager;

    /**
     * Should return <code>true</code> while executing resource changes to avoid
     * an infinite resource event loop.
     */
    @Inject
    protected FileReplacementInProgressObservable fileReplacementInProgressObservable;

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected ConsistencyWatchdogClient consistencyWatchdogClient;

    protected ISessionManager sessionManager;

    public SharedResourcesManager(ISessionManager sessionManager,
        StopManager stopManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.addSessionListener(sessionListener);
        this.stopManager = stopManager;
        this.stopManager.addBlockable(stopManagerListener);
    }

    protected Blockable stopManagerListener = new Blockable() {
        public void unblock() {
            SharedResourcesManager.this.pause = false;
        }

        public void block() {
            SharedResourcesManager.this.pause = true;
        }
    };

    /**
     * Listens for resource changes in shared project and fires activities.
     */
    protected class ResourceDeltaVisitor implements IResourceDeltaVisitor {

        // stores activities that happen while one change event
        protected List<IResourceActivity> activitiesBuffer = new ArrayList<IResourceActivity>();

        /**
         * Compares the length (in number segments) of IPaths of given
         * Activities. Longer path is 'bigger'.
         * */
        protected class PathLengthComparator implements
            Comparator<IResourceActivity> {

            public int compare(IResourceActivity a1, IResourceActivity a2) {
                int a1sc = a1.getPath().segmentCount();
                int a2sc = a2.getPath().segmentCount();

                return a1sc - a2sc;
            }

        }

        /**
         * Compares the length (in number segments) of IPaths of given
         * Activities. Shorter path is 'bigger'.
         * */
        protected class ReversePathLengthComparator implements
            Comparator<IResourceActivity> {

            PathLengthComparator pathLengthComparator = new PathLengthComparator();

            public int compare(IResourceActivity a1, IResourceActivity a2) {
                return -pathLengthComparator.compare(a1, a2);
            }
        }

        /**
         * To be run before change event ends. It orders activities in buffer
         * and fires them.
         */
        public void finish() {
            for (IActivity activity : getOrderedActivities())
                fireActivity(activity);
        }

        /**
         * Orders activities in buffer.
         */
        protected List<IResourceActivity> getOrderedActivities() {
            // TODO Use a comparator which includes all this as a sorting rule

            List<IResourceActivity> fileCreateActivities = new ArrayList<IResourceActivity>();
            List<IResourceActivity> fileMoveActivities = new ArrayList<IResourceActivity>();
            List<IResourceActivity> fileRemoveActivities = new ArrayList<IResourceActivity>();

            List<IResourceActivity> folderCreateActivities = new ArrayList<IResourceActivity>();
            List<IResourceActivity> folderRemoveActivities = new ArrayList<IResourceActivity>();

            List<IResourceActivity> orderedList;

            /**
             * split all activities in activities buffer in groups
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
            ReversePathLengthComparator iplc = new ReversePathLengthComparator();

            // Sorts Activities by their length
            Collections.sort(fileCreateActivities, pathLengthComparator);
            Collections.sort(fileMoveActivities, pathLengthComparator);
            Collections.sort(fileRemoveActivities, iplc);
            Collections.sort(folderCreateActivities, pathLengthComparator);
            Collections.sort(folderRemoveActivities, iplc);

            orderedList = new ArrayList<IResourceActivity>();

            // add activities to the result
            orderedList.addAll(folderCreateActivities);
            orderedList.addAll(fileMoveActivities);
            orderedList.addAll(fileCreateActivities);
            orderedList.addAll(fileRemoveActivities);
            orderedList.addAll(folderRemoveActivities);

            return orderedList;
        }

        public boolean visit(IResourceDelta delta) {

            assert sharedProject != null;

            if (!sharedProject.isDriver()) {
                return false;
            }

            IResource resource = delta.getResource();
            if (resource.getProject() == null) {
                return true;
            }

            if (resource.getProject() != sharedProject.getProject()) {
                return false;
            }

            if (resource.isDerived()) {
                return false;
            }

            IResourceActivity activity = null;

            if (resource instanceof IFile)
                activity = handleFileDelta(delta);
            else if (resource instanceof IFolder)
                activity = handleFolderDelta(delta);

            if (activity != null) {

                // TODO A delete activity is triggered twice
                activitiesBuffer.add(activity);
            }

            return delta.getKind() != IResourceDelta.NO_CHANGE;
        }

        protected IResourceActivity handleFolderDelta(IResourceDelta delta) {
            IResource resource = delta.getResource();
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:

                return new FolderActivity(saros.getMyJID(),
                    FolderActivity.Type.Created, resource
                        .getProjectRelativePath());

            case IResourceDelta.REMOVED:
                if (isMoved(delta))
                    return null;
                return new FolderActivity(saros.getMyJID(),
                    FolderActivity.Type.Removed, resource
                        .getProjectRelativePath());

            default:
                return null;
            }
        }

        protected IResourceActivity handleFileDelta(IResourceDelta delta) {
            IResource resource = delta.getResource();
            int kind = delta.getKind();

            switch (kind) {
            case IResourceDelta.CHANGED:
                log.debug("Resource " + resource.getName() + " changed");

                /*
                 * FIXME If a CHANGED event happens and it was not triggered by
                 * the user or us saving the file, then we must think about what
                 * we want to do
                 */
                return null;

            case IResourceDelta.ADDED:

                // is this an "ADD" while moving/renaming a file?
                if (isMovedFrom(delta)) {

                    JID jid = saros.getMyJID();

                    IPath newPath = resource.getFullPath().makeRelative();
                    IPath oldPath = delta.getMovedFromPath().makeRelative();

                    newPath = newPath.removeFirstSegments(1);
                    oldPath = oldPath.removeFirstSegments(1);

                    try {
                        return FileActivity.moved(sharedProject.getProject(),
                            jid, newPath, oldPath, isContentChange(delta));
                    } catch (IOException e) {
                        log
                            .warn("Resource could not be read for sending to peers:"
                                + resource.getLocation());
                    }
                }

                // usual files adding procedure

                // ignore opened files because otherwise we might send
                // CHANGED
                // events for files that are also handled by the editor
                // manager.
                if (editorManager.isOpened(resource.getProjectRelativePath())) {
                    // TODO Think about if this is needed...
                    return null;
                }
                try {

                    return FileActivity.created(sharedProject.getProject(),
                        saros.getMyJID(), resource.getFullPath().makeRelative()
                            .removeFirstSegments(1), Purpose.ACTIVITY);
                } catch (IOException e) {
                    log.warn("Resource could not be read for sending to peers:"
                        + resource.getLocation(), e);
                }
                return null;

            case IResourceDelta.REMOVED:
                if (isMoved(delta)) // Ignore "REMOVED" while moving
                    return null;
                return FileActivity.removed(saros.getMyJID(), resource
                    .getProjectRelativePath(), Purpose.ACTIVITY);

            default:
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

        protected void fireActivity(final IActivity activity) {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    for (IActivityListener listener : listeners) {
                        listener.activityCreated(activity);
                    }
                }
            });
        }
    }

    public ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            sharedProject.addActivityProvider(SharedResourcesManager.this);
            ResourcesPlugin.getWorkspace().addResourceChangeListener(
                SharedResourcesManager.this);
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                SharedResourcesManager.this);

            assert sharedProject == project;
            sharedProject.removeActivityProvider(SharedResourcesManager.this);
            sharedProject = null;
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void addActivityListener(IActivityListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void removeActivityListener(IActivityListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * This method is called from Eclipse when changes to resource are detected
     */
    public void resourceChanged(IResourceChangeEvent event) {

        if (fileReplacementInProgressObservable.isReplacementInProgress())
            return;

        /*
         * If the StopManager has paused the project do not react to resource
         * changes
         */
        if (pause) {
            /*
             * TODO This warning is misleading! The consistency recovery process
             * might cause IResourceChangeEvents (which do not need to be
             * replicated)
             */
            if (event.getResource() != null)
                log.warn("Resource changed while paused: "
                    + event.getResource().getProjectRelativePath(),
                    new StackTrace());
            else
                log.warn("Resource changed while paused", new StackTrace());
            return;
        }

        try {

            switch (event.getType()) {

            case IResourceChangeEvent.PRE_BUILD:
            case IResourceChangeEvent.POST_BUILD:
            case IResourceChangeEvent.POST_CHANGE:

                IResourceDelta delta = event.getDelta();
                log.trace(".resourceChanged() - Delta will be processed");
                if (delta != null) {
                    ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
                    delta.accept(visitor);
                    visitor.finish();
                } else
                    log.error("Unexpected empty delta in "
                        + "SharedResourcesManager: " + event);
                break;
            case IResourceChangeEvent.PRE_CLOSE:
            case IResourceChangeEvent.PRE_DELETE:
            case IResourceChangeEvent.PRE_REFRESH:

                // TODO We should handle these as well (at least if the user
                // deletes / refreshes our shared project)
                break;

            default:
                // Because additional events might be added in the future
                log.error("Unhandled case in in SharedResourcesManager: "
                    + event);
            }

        } catch (Exception e) {
            log.error("Couldn't handle resource change.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void exec(IActivity activity) {

        if (!(activity instanceof FileActivity || activity instanceof FolderActivity))
            return;

        try {
            fileReplacementInProgressObservable.startReplacement();

            if (activity instanceof FileActivity) {
                exec((FileActivity) activity);
            } else if (activity instanceof FolderActivity) {
                exec((FolderActivity) activity);
            }

        } catch (CoreException e) {
            log.error("Failed to execute resource activity.", e);
        } finally {
            fileReplacementInProgressObservable.replacementDone();
        }
    }

    protected void exec(FileActivity activity) throws CoreException {

        if (this.sharedProject == null) {
            log.warn("Project has ended for FileActivity " + activity);
            return;
        }

        IProject project = this.sharedProject.getProject();

        IPath path = activity.getPath();
        IFile file = sharedProject.getProject().getFile(path);

        if (activity.isRecovery()) {
            log.info("Received consistency file: " + activity);

            if (log.isInfoEnabled() && (activity.getContents() != null)) {
                Util.logDiff(log, activity.getSource(), path, activity
                    .getContents(), file);
            }
        }

        // Create or remove file
        if (activity.getType() == FileActivity.Type.Created) {
            // TODO should be reported to the user
            SubMonitor monitor = SubMonitor.convert(new NullProgressMonitor());
            try {
                FileUtil.writeFile(new ByteArrayInputStream(activity
                    .getContents()), file, monitor);
            } catch (Exception e) {
                log.error("Could not write file: " + file);
            }
        } else if (activity.getType() == FileActivity.Type.Removed) {
            FileUtil.delete(file);
        } else if (activity.getType() == FileActivity.Type.Moved) {

            IPath newFilePath = activity.getPath();
            IResource fileOldResource = project.findMember(activity
                .getOldPath());
            IResource nfpR = project.getFile(newFilePath);
            newFilePath = nfpR.getFullPath();

            if (fileOldResource == null) {
                log.error(".exec Old File is not availible while moving "
                    + activity.getOldPath().toOSString());
            } else
                FileUtil.move(newFilePath, fileOldResource);

            // while moving content of the file changed
            if (activity.getContents() != null) {

                IFile fileResource = (IFile) project.findMember(newFilePath
                    .removeFirstSegments(1));

                fileResource.setContents(new ByteArrayInputStream(activity
                    .getContents()), true, true, null);
            }

        }

        if (activity.isRecovery()) {

            // The file contents has been replaced, now reset Jupiter
            this.sharedProject.getConcurrentDocumentClient().reset(path);

            this.consistencyWatchdogClient.performCheck(path);
        }
    }

    protected void exec(FolderActivity activity) throws CoreException {
        IFolder folder = this.sharedProject.getProject().getFolder(
            activity.getPath());

        if (activity.getType() == FolderActivity.Type.Created) {
            FileUtil.create(folder);
        } else if (activity.getType() == FolderActivity.Type.Removed) {
            try {
                FileUtil.delete(folder);
            } catch (CoreException e) {
                log.warn("Removing folder failed: " + folder);
            }
        }

    }

    public void dispose() {
        stopManager.removeBlockable(stopManagerListener);
    }
}
