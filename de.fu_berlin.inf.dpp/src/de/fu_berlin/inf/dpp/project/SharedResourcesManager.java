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
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.util.BlockingProgressMonitor;
import de.fu_berlin.inf.dpp.util.FileUtil;

/**
 * This manager is responsible for handling all resource changes that aren't
 * handled by the EditorManager, that is for changes that aren't done by
 * entering text in an text editor. It creates and executes file activities and
 * folder activities.
 * 
 * @author rdjemili
 * 
 * @component The single instance of this class per application is created by
 *            PicoContainer in the central plug-in class {@link Saros}
 */
public class SharedResourcesManager implements IResourceChangeListener,
    IActivityProvider {

    private static Logger log = Logger.getLogger(SharedResourcesManager.class
        .getName());

    /**
     * Should be set to <code>true</code> while executing resource changes to
     * avoid an infinite resource event loop.
     */
    private boolean replicationInProgess = false;

    private ISharedProject sharedProject;

    private final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();

    private final List<IActivityListener> listeners = new LinkedList<IActivityListener>();

    /**
     * Listens for resource changes in shared project and fires activities.
     */
    private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

        public boolean visit(IResourceDelta delta) {

            assert SharedResourcesManager.this.sharedProject != null;

            if (!SharedResourcesManager.this.sharedProject.isDriver()) {
                return false;
            }

            IResource resource = delta.getResource();
            if (resource.getProject() == null) {
                return true;
            }

            if (resource.getProject() != SharedResourcesManager.this.sharedProject
                .getProject()) {
                return false;
            }

            if (resource.isDerived()) {
                return false;
            }

            IPath path = delta.getProjectRelativePath();
            int kind = delta.getKind();

            IActivity activity = null;
            if (resource instanceof IFile) {
                activity = handleFileDelta(path, kind);

            } else if (resource instanceof IFolder) {
                activity = handleFolderDelta(path, kind);
            }

            if (activity != null) {
                // TODO A delete activity is triggered twice
                // log.debug("File Activity Fired: " + activity + " for Delta: "
                // + delta, new StackTrace());
                fireActivity(activity);
            }

            return delta.getKind() != IResourceDelta.NO_CHANGE;
        }

        private IActivity handleFolderDelta(IPath path, int kind) {
            switch (kind) {
            case IResourceDelta.ADDED:
                return new FolderActivity(saros.getMyJID().toString(),
                    FolderActivity.Type.Created, path);

            case IResourceDelta.REMOVED:
                return new FolderActivity(saros.getMyJID().toString(),
                    FolderActivity.Type.Removed, path);

            default:
                return null;
            }
        }

        private IActivity handleFileDelta(IPath path, int kind) {
            switch (kind) {
            case IResourceDelta.CHANGED:

                /*
                 * FIXME If a CHANGED event happens and it was not triggered by
                 * the user or us saving the file, then we must think about what
                 * we want to do
                 */
                return null;

            case IResourceDelta.ADDED:
                // ignore opened files because otherwise we might send CHANGED
                // events for files that are also handled by the editor manager.
                if (editorManager.isOpened(path)) {
                    // TODO Think about if this is needed...
                    return null;
                }
                return new FileActivity(saros.getMyJID().toString(),
                    FileActivity.Type.Created, path);

            case IResourceDelta.REMOVED:
                return new FileActivity(saros.getMyJID().toString(),
                    FileActivity.Type.Removed, path);

            default:
                return null;
            }
        }

        private void fireActivity(IActivity activity) {
            for (IActivityListener listener : SharedResourcesManager.this.listeners) {
                listener.activityCreated(activity);
            }
        }
    }

    @Inject
    protected Saros saros;

    @Inject
    protected EditorManager editorManager;

    protected ISessionManager sessionManager;

    public SharedResourcesManager(ISessionManager sessionManager) {
        this.sessionManager = sessionManager;
        sessionManager.addSessionListener(sessionListener);
    }

    public ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISharedProject project) {
            sharedProject = project;
            sharedProject.getActivityManager().addProvider(
                SharedResourcesManager.this);
            ResourcesPlugin.getWorkspace().addResourceChangeListener(
                SharedResourcesManager.this);
        }

        @Override
        public void sessionEnded(ISharedProject project) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                SharedResourcesManager.this);

            assert sharedProject == project;
            sharedProject.getActivityManager().removeProvider(
                SharedResourcesManager.this);
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

        if (replicationInProgess)
            return;

        try {

            switch (event.getType()) {

            case IResourceChangeEvent.PRE_BUILD:
            case IResourceChangeEvent.POST_BUILD:
            case IResourceChangeEvent.POST_CHANGE:

                IResourceDelta delta = event.getDelta();
                if (delta != null)
                    delta.accept(this.visitor);
                else
                    log
                        .error("Unexpected empty delta in SharedResourcesManager: "
                            + event);
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

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.IActivityProvider
     */
    public void exec(IActivity activity) {
        try {
            this.replicationInProgess = true;

            if (activity instanceof FileActivity) {
                exec((FileActivity) activity);
            } else if (activity instanceof FolderActivity) {
                exec((FolderActivity) activity);
            }

        } catch (CoreException e) {
            log.error("Failed to execute resource activity.", e);

        } finally {
            this.replicationInProgess = false;
        }
    }

    private void exec(FileActivity activity) throws CoreException {
        IProject project = this.sharedProject.getProject();
        IFile file = project.getFile(activity.getPath());

        if (activity.getType() == FileActivity.Type.Created) {
            FileUtil.writeFile(activity.getContents(), file);
        } else if (activity.getType() == FileActivity.Type.Removed) {
            FileUtil.deleteFile(file);
        }
    }

    private void exec(FolderActivity activity) throws CoreException {
        IProject project = this.sharedProject.getProject();
        IFolder folder = project.getFolder(activity.getPath());

        BlockingProgressMonitor monitor = new BlockingProgressMonitor();
        if (activity.getType() == FolderActivity.Type.Created) {
            folder.create(true, true, monitor);
            try {
                monitor.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (monitor.isCanceled()) {
                log.warn("Creating folder failed: " + folder);
            }
        } else if (activity.getType() == FolderActivity.Type.Removed) {
            folder.delete(true, monitor);
            try {
                monitor.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (monitor.isCanceled()) {
                log.warn("Deleting folder failed: " + folder);
            }
        }

    }
}
