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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.VCSActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSAdapterFactory;

/**
 * This manager is responsible for handling all resource changes that aren't
 * handled by the EditorManager, that is for changes that aren't done by
 * entering text in an text editor. It creates and executes file and folder
 * activities.
 * 
 * @author rdjemili
 * 
 */
@Component(module = "core")
public class SharedResourcesManager implements IResourceChangeListener,
    IActivityProvider, Disposable {
    /*
     * haferburg: We're really only interested in
     * IResourceChangeEvent.POST_CHANGE events. I don't know why other events
     * were tracked, so I removed them.
     * 
     * We're definitely not interested in PRE_REFRESH, refreshes are only
     * interesting when they result in an actual change, in which case we will
     * receive a POST_CHANGE event anyways.
     * 
     * We also don't need PRE_CLOSE, since we'll get a POST_CHANGE anyways and
     * also have to test project.isOpen().
     * 
     * TODO We might want to add PRE_DELETE if the user deletes our shared
     * project though.
     */
    static final int INTERESTING_EVENTS = IResourceChangeEvent.POST_CHANGE;

    static Logger log = Logger
        .getLogger(SharedResourcesManager.class.getName());

    /**
     * While paused the SharedResourcesManager doesn't fire activities
     */
    protected boolean pause = false;

    protected ISarosSession sarosSession;

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

    public ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            sarosSession = newSarosSession;
            sarosSession.addActivityProvider(SharedResourcesManager.this);
            ResourcesPlugin.getWorkspace().addResourceChangeListener(
                SharedResourcesManager.this, INTERESTING_EVENTS);
        }

        @Override
        public void sessionEnded(ISarosSession oldSarosSession) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                SharedResourcesManager.this);

            assert sarosSession == oldSarosSession;
            sarosSession.removeActivityProvider(SharedResourcesManager.this);
            sarosSession = null;
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
            logPauseWarning(event);
            return;
        }

        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            try {
                // Creations, deletions, modifications of files and folders.
                handlePostChange(event);
            } catch (Exception e) {
                log.error("Couldn't handle resource change.", e);
            }
        } else {
            log.error("Unhandled event type in in SharedResourcesManager: "
                + event);
        }
    }

    protected void handlePostChange(IResourceChangeEvent event)
        throws CoreException {
        assert sarosSession != null;

        if (!sarosSession.isDriver()) {
            return;
        }

        IResourceDelta delta = event.getDelta();
        log.trace(".resourceChanged() - Delta will be processed");
        if (delta == null) {
            log.error("Unexpected empty delta in " + "SharedResourcesManager: "
                + event);
            return;
        }
        assert delta.getResource() instanceof IWorkspaceRoot;

        // Iterate over all projects.
        IResourceDelta[] projectDeltas = delta.getAffectedChildren();
        for (IResourceDelta projectDelta : projectDeltas) {
            IResource resource = projectDelta.getResource();
            assert resource instanceof IProject;
            IProject project = (IProject) resource;
            if (!sarosSession.isShared(project))
                continue;

            SharedProject sharedProject = sarosSession
                .getSharedProject(project);
            boolean isProjectOpen = project.isOpen();
            if (sharedProject.isOpen() != isProjectOpen) {
                sharedProject.setOpen(isProjectOpen);
                if (isProjectOpen) {
                    // Since the project was just opened, we're going to get
                    // a notification that each file in the project was just
                    // added.
                    // TODO: Check if any of the files actually were
                    // modified, but skip all others.
                    continue;
                } else {
                    // The project was just closed.
                    // TODO: What do we do here?
                }
            }
            if (!isProjectOpen)
                continue;
            ProjectDeltaVisitor visitor = new ProjectDeltaVisitor(this,
                sharedProject);
            projectDelta.accept(visitor);
            visitor.finish();
        }
    }

    /*
     * TODO This warning is misleading! The consistency recovery process might
     * cause IResourceChangeEvents (which do not need to be replicated)
     */
    protected void logPauseWarning(IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {

            IResourceDelta delta = event.getDelta();
            if (delta == null) {
                log.error("Resource changed while paused"
                    + " but unexpected empty delta in "
                    + "SharedResourcesManager: " + event);
                return;
            }

            ToStringResourceDeltaVisitor visitor = new ToStringResourceDeltaVisitor();
            try {
                delta.accept(visitor);
            } catch (CoreException e) {
                log.error("ToStringResourceDelta visitor crashed", e);
                return;
            }
            log.warn("Resource changed while paused:\n" + visitor.toString());
        } else {
            log.error("Unexpected event type in in logPauseWarning: " + event);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void exec(IActivity activity) {

        if (!(activity instanceof FileActivity
            || activity instanceof FolderActivity || activity instanceof VCSActivity))
            return;

        try {
            fileReplacementInProgressObservable.startReplacement();

            if (activity instanceof FileActivity) {
                exec((FileActivity) activity);
            } else if (activity instanceof FolderActivity) {
                exec((FolderActivity) activity);
            } else if (activity instanceof VCSActivity) {
                exec((VCSActivity) activity);
            }

        } catch (CoreException e) {
            log.error("Failed to execute resource activity.", e);
        } finally {
            fileReplacementInProgressObservable.replacementDone();
        }
    }

    protected void exec(VCSActivity activity) {
        IProject project = activity.getPath().getProject();
        IPath path = activity.getPath().getProjectRelativePath();
        String url = activity.getURL();
        String revision = activity.getRevision();
        VCSAdapter vcs = VCSAdapterFactory.getAdapter(project);
        if (vcs == null) {
            log.error("Could not execute VCS activity.");
            return;
        }
        if (activity.getType() == VCSActivity.Type.Switch) {
            IResource resource = null;
            {
                try {
                    resource = project.getFolder(path);
                } catch (Exception e) {
                    log.debug("", e);
                }
                if (resource == null) {
                    try {
                        resource = project.getFile(path);
                    } catch (Exception e) {
                        log.debug("", e);
                    }
                }
                if (resource == null) {
                    // TODO find a safer way...
                    resource = project;

                }
            }
            vcs.switch_(resource, url, revision, null);
            activity.getRevision();
        } else {
            log.error("VCS activity type not implemented yet.");
        }
    }

    protected void exec(FileActivity activity) throws CoreException {

        if (this.sarosSession == null) {
            log.warn("Project has ended for FileActivity " + activity);
            return;
        }

        SPath path = activity.getPath();
        IFile file = path.getFile();

        if (activity.isRecovery()) {
            log.info("Received consistency file: " + activity);

            if (log.isInfoEnabled() && (activity.getContents() != null)) {
                Util.logDiff(log, activity.getSource().getJID(), path,
                    activity.getContents(), file);
            }
        }

        // Create or remove file
        if (activity.getType() == FileActivity.Type.Created) {
            // TODO should be reported to the user
            SubMonitor monitor = SubMonitor.convert(new NullProgressMonitor());
            try {
                FileUtil.writeFile(
                    new ByteArrayInputStream(activity.getContents()), file,
                    monitor);
            } catch (Exception e) {
                log.error("Could not write file: " + file);
            }
        } else if (activity.getType() == FileActivity.Type.Removed) {
            FileUtil.delete(file);
        } else if (activity.getType() == FileActivity.Type.Moved) {

            IPath newFilePath = activity.getPath().getFile().getFullPath();

            IResource oldResource = activity.getOldPath().getFile();

            if (oldResource == null) {
                log.error(".exec Old File is not availible while moving "
                    + activity.getOldPath());
            } else
                FileUtil.move(newFilePath, oldResource);

            // while moving content of the file changed
            if (activity.getContents() != null) {

                SubMonitor monitor = SubMonitor
                    .convert(new NullProgressMonitor());
                try {
                    FileUtil.writeFile(
                        new ByteArrayInputStream(activity.getContents()), file,
                        monitor);
                } catch (Exception e) {
                    log.error("Could not write file: " + file);
                }
            }
        }

        if (activity.isRecovery()) {

            // The file contents has been replaced, now reset Jupiter
            this.sarosSession.getConcurrentDocumentClient().reset(path);

            this.consistencyWatchdogClient.performCheck(path);
        }
    }

    protected void exec(FolderActivity activity) throws CoreException {

        SPath path = activity.getPath();

        IFolder folder = path.getProject().getFolder(
            path.getProjectRelativePath());

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
