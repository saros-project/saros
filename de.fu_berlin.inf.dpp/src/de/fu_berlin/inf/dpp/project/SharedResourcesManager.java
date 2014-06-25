/*
 * DPP - Serious Distributed Pair Programming
 * (c) Freie Universität Berlin - Fachbereich Mathematik und Informatik - 2006
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

import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.Startable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.VCSActivity;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogClient;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipseFolderImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipsePathImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipseProjectImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipseResourceImpl;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.vcs.VCSAdapter;
import de.fu_berlin.inf.dpp.vcs.VCSResourceInfo;

/**
 * This manager is responsible for handling all resource changes that aren't
 * handled by the EditorManager, that is for changes that aren't done by
 * entering text in a text editor. It produces and consumes file, folder, and
 * VCS activities.
 * <p>
 * TODO Extract AbstractActivityProducer/Consumer functionality in another
 * classes ResourceActivityProducer/Consumer, rename to
 * SharedResourceChangeListener.
 */
/*
 * For a good introduction to Eclipse's resource change notification mechanisms
 * see
 * http://www.eclipse.org/articles/Article-Resource-deltas/resource-deltas.html
 */
@Component(module = "core")
public class SharedResourcesManager extends AbstractActivityProducer implements
    IResourceChangeListener, Startable {
    /** The {@link IResourceChangeEvent}s we're going to register for. */
    /*
     * haferburg: We're really only interested in
     * IResourceChangeEvent.POST_CHANGE events. I don't know why other events
     * were tracked, so I removed them.
     * 
     * We're definitely not interested in PRE_REFRESH, refreshes are only
     * interesting when they result in an actual change, in which case we will
     * receive a POST_CHANGE event anyways.
     * 
     * We also don't need PRE_CLOSE, since we'll also get a POST_CHANGE and
     * still have to test project.isOpen().
     * 
     * We might want to add PRE_DELETE if the user deletes our shared project
     * though.
     */
    static final int INTERESTING_EVENTS = IResourceChangeEvent.POST_CHANGE;

    private static final Logger log = Logger
        .getLogger(SharedResourcesManager.class);

    /**
     * If the StopManager has paused the project, the SharedResourcesManager
     * doesn't react to resource changes.
     */
    protected boolean pause = false;

    protected final ISarosSession sarosSession;

    protected final StopManager stopManager;

    private final Map<IProject, SharedProject> sharedProjects = Collections
        .synchronizedMap(new HashMap<IProject, SharedProject>());
    /**
     * Should return <code>true</code> while executing resource changes to avoid
     * an infinite resource event loop.
     */
    @Inject
    protected FileReplacementInProgressObservable fileReplacementInProgressObservable;

    @Inject
    protected EditorManager editorManager;

    @Inject
    protected ConsistencyWatchdogClient consistencyWatchdogClient;

    protected Blockable stopManagerListener = new Blockable() {
        @Override
        public void unblock() {
            SharedResourcesManager.this.pause = false;
        }

        @Override
        public void block() {
            SharedResourcesManager.this.pause = true;
        }
    };

    @Override
    public void start() {
        sarosSession.addActivityProducer(this);
        sarosSession.addActivityConsumer(consumer);
        stopManager.addBlockable(stopManagerListener);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
            INTERESTING_EVENTS);
    }

    @Override
    public void stop() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        sarosSession.removeActivityProducer(this);
        sarosSession.removeActivityConsumer(consumer);
        stopManager.removeBlockable(stopManagerListener);
    }

    private IJobChangeListener jobChangeListener = new JobChangeAdapter() {
        @Override
        public void done(IJobChangeEvent event) {
            Job job = event.getJob();
            log.trace("Job " + job.getName() + " done");
            job.removeJobChangeListener(jobChangeListener);
        }
    };

    private ResourceActivityFilter pendingActivities = new ResourceActivityFilter();

    public SharedResourcesManager(ISarosSession sarosSession,
        StopManager stopManager) {
        this.sarosSession = sarosSession;
        this.stopManager = stopManager;
    }

    /**
     * This method is called from Eclipse when changes to resource are detected
     */
    @Override
    public void resourceChanged(IResourceChangeEvent event) {

        if (fileReplacementInProgressObservable.isReplacementInProgress())
            return;

        if (pause) {
            logPauseWarning(event);
            return;
        }

        if (log.isTraceEnabled()) {
            IJobManager jobManager = Job.getJobManager();
            Job currentJob = jobManager.currentJob();
            if (currentJob != null) {
                currentJob.addJobChangeListener(jobChangeListener);
                log.trace("currentJob='" + currentJob.getName() + "'");
            }
        }
        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            // Creations, deletions, modifications of files and folders.
            handlePostChange(event);
        } else {
            log.error("Unhandled event type in in SharedResourcesManager: "
                + event);
        }
    }

    protected void handlePostChange(IResourceChangeEvent event) {

        if (!sarosSession.hasWriteAccess()) {
            return;
        }

        IResourceDelta delta = event.getDelta();
        log.trace(".resourceChanged() - Delta will be processed");
        if (delta == null) {
            log.error("Unexpected empty delta in " + "SharedResourcesManager: "
                + event);
            return;
        }

        if (log.isTraceEnabled())
            log.trace("handlePostChange\n" + deltaToString(delta));

        assert delta.getResource() instanceof IWorkspaceRoot;

        // Iterate over all projects.
        boolean postpone = false;
        final boolean useVersionControl = sarosSession.useVersionControl();
        IResourceDelta[] projectDeltas = delta.getAffectedChildren();
        for (IResourceDelta projectDelta : projectDeltas) {
            assert projectDelta.getResource() instanceof IProject;
            IProject project = (IProject) projectDelta.getResource();
            if (!sarosSession.isShared(ResourceAdapterFactory.create(project)))
                continue;

            if (!checkOpenClosed(project))
                continue;

            if (useVersionControl && !checkVCSConnection(project))
                continue;

            SharedProject sharedProject = sharedProjects.get(project);

            if (sharedProject == null)
                continue;

            VCSAdapter vcs = useVersionControl ? VCSAdapter.getAdapter(project)
                : null;
            ProjectDeltaVisitor visitor;
            if (vcs == null) {
                visitor = new ProjectDeltaVisitor(editorManager, sarosSession,
                    sharedProject);
            } else {
                visitor = vcs.getProjectDeltaVisitor(editorManager,
                    sarosSession, sharedProject);
            }

            try {
                /*
                 * There is some magic involved here. The ProjectDeltaVisitor
                 * will ignore changed files that are currently opened in an
                 * editor to prevent transmitting the whole file content of the
                 * modified file.
                 * 
                 * FIXME document this behavior in the ProjectDeltaVisitor !
                 */
                projectDelta.accept(visitor, IContainer.INCLUDE_HIDDEN);
            } catch (CoreException e) {
                // The Eclipse documentation doesn't specify when
                // CoreExceptions can occur.
                log.debug(format("ProjectDeltaVisitor of project {0} "
                    + "failed for some reason.", project.getName()), e);
            }
            if (visitor.postponeSending()) {
                postpone = true;
            }
            log.trace("Adding new activities " + visitor.pendingActivities);
            pendingActivities.enterAll(visitor.pendingActivities);

            // if (!postpone)
            // assert sharedProject.checkIntegrity();

            log.trace("sharedProject.resourceMap: \n"
                + sharedProject.resourceMap);
        }
        if (!postpone) {
            fireActivities();
        } else if (!pendingActivities.isEmpty()) {
            log.debug("Postponing sending the activities");
        }
    }

    protected boolean checkOpenClosed(IProject project) {

        SharedProject sharedProject = sharedProjects.get(project);

        if (sharedProject == null)
            return false;

        boolean isProjectOpen = project.isOpen();
        if (sharedProject.updateProjectIsOpen(isProjectOpen)) {
            if (isProjectOpen) {
                // Since the project was just opened, we would get
                // a notification that each file in the project was just
                // added, so we're simply going to ignore this delta. Any
                // resources that were modified externally would be
                // out-of-sync anyways, so when the user refreshes them
                // we'll get notified.
                return false;
            } else {
                // The project was just closed, what do we do here?
            }
        }
        if (!isProjectOpen)
            return false;
        return true;
    }

    /**
     * Returns false if the VCS changed.
     * 
     * @param project
     * @return
     */
    protected boolean checkVCSConnection(IProject project) {

        SharedProject sharedProject = sharedProjects.get(project);

        if (sharedProject == null)
            return true;

        VCSAdapter vcs = VCSAdapter.getAdapter(project);
        VCSAdapter oldVcs = sharedProject.getVCSAdapter();

        if (sharedProject.updateVcs(vcs)) {
            if (vcs == null) {
                // Disconnect
                boolean deleteContent = oldVcs == null
                    || !oldVcs.hasLocalCache(project);
                VCSActivity activity = VCSActivity.disconnect(sarosSession,
                    ResourceAdapterFactory.create(project), deleteContent);
                pendingActivities.enter(activity);
                sharedProject.updateRevision(null);
                sharedProject.updateVcsUrl(null);
            } else {
                // Connect
                VCSResourceInfo info = vcs.getResourceInfo(project);
                String repositoryString = vcs.getRepositoryString(project);
                if (repositoryString == null || info.getURL() == null) {
                    // HACK For some reason, Subclipse returns null values
                    // here. Pretend the vcs is still null and wait for the
                    // next time we get here.
                    sharedProject.updateVcs(null);
                    return false;
                }

                String directory = info.getURL().substring(
                    repositoryString.length());
                VCSActivity activity = VCSActivity.connect(sarosSession,
                    ResourceAdapterFactory.create(project), repositoryString,
                    directory, vcs.getProviderID(project));
                pendingActivities.enter(activity);
                sharedProject.updateVcsUrl(info.getURL());
                sharedProject.updateRevision(info.getRevision());

                log.debug("Connect to VCS");
            }
            return false;
        }

        return true;
    }

    /**
     * Fires the ordered activities. To be run before change event ends.
     */
    protected void fireActivities() {
        if (pendingActivities.isEmpty())
            return;
        final List<IResourceActivity> orderedActivities = pendingActivities
            .retrieveAll();
        log.trace("Sending activities " + orderedActivities.toString());
        for (final IActivity activity : orderedActivities) {
            /*
             * Make sure we only send a VCSActivity if VC is enabled for this
             * session.
             */
            if (sarosSession.useVersionControl()
                || !(activity instanceof VCSActivity)) {
                fireActivity(activity);
            } else {
                log.error("Tried to send VCSActivity with VC support disabled.");
            }
        }
    }

    /*
     * coezbek: This warning is misleading! The consistency recovery process
     * might cause IResourceChangeEvents (which do not need to be replicated)
     * [Added in branches/10.2.26.r2028, the commit message claims "Improved
     * logging of ResourceChanges while paused".]
     * 
     * haferburg: When is this even called? We don't get here while this class
     * executes any activity. We can only get here when pause is true, but not
     * fileReplacementInProgressObservable. Also, why add a misleading warning
     * in the first place??
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

            log.warn("Resource changed while paused:\n" + deltaToString(delta));
        } else {
            log.error("Unexpected event type in in logPauseWarning: " + event);
        }
    }

    protected String deltaToString(IResourceDelta delta) {
        ToStringResourceDeltaVisitor visitor = new ToStringResourceDeltaVisitor();
        try {
            delta.accept(visitor, IContainer.INCLUDE_PHANTOMS
                | IContainer.INCLUDE_HIDDEN
                | IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
        } catch (CoreException e) {
            log.error("ToStringResourceDelta visitor crashed", e);
            return "";
        }
        return visitor.toString();
    }

    private final IActivityConsumer consumer = new AbstractActivityConsumer() {
        @Override
        public void exec(IActivity activity) {
            if (!(activity instanceof FileActivity
                || activity instanceof FolderActivity || activity instanceof VCSActivity))
                return;

            /*
             * FIXME this will lockout everything. File changes made in the
             * meantime from another background job are not recognized. See
             * AddMultipleFilesTest STF test which fails randomly.
             */
            fileReplacementInProgressObservable.startReplacement();
            log.trace("execing " + activity);

            super.exec(activity);

            fileReplacementInProgressObservable.replacementDone();
            log.trace("done execing " + activity);
        }

        @Override
        public void receive(FileActivity activity) {
            try {
                handleFileActivity(activity);
            } catch (CoreException e) {
                log.error("Failed to execute activity: " + activity, e);
            }
        }

        @Override
        public void receive(FolderActivity activity) {
            try {
                handleFolderActivity(activity);
            } catch (CoreException e) {
                log.error("Failed to execute activity: " + activity, e);
            }
        }

        @Override
        public void receive(VCSActivity activity) {
            handleVCSActivity(activity);
        }
    };

    protected void handleFileActivity(FileActivity activity)
        throws CoreException {

        if (activity.isRecovery()) {
            handleFileRecovery(activity);
            return;
        }

        // TODO check if we should open / close existing editors here too
        switch (activity.getType()) {
        case CREATED:
            handleFileCreation(activity);
            break;
        case REMOVED:
            handleFileDeletion(activity);
            break;
        case MOVED:
            handleFileMove(activity);
            break;
        }
    }

    private void handleFileRecovery(FileActivity activity) throws CoreException {
        SPath path = activity.getPath();

        log.debug("performing recovery for file: "
            + activity.getPath().getFullPath());

        editorManager.saveLazy(path);

        boolean editorWasOpen = editorManager.isOpenEditor(path);

        if (editorWasOpen)
            editorManager.closeEditor(path);

        FileActivity.Type type = activity.getType();

        try {
            if (type == FileActivity.Type.CREATED)
                handleFileCreation(activity);
            else if (type == FileActivity.Type.REMOVED)
                handleFileDeletion(activity);
            else
                log.warn("performing recovery for type " + type
                    + " is not supported");
        } finally {
            /*
             * always reset Jupiter algorithm, because upon receiving that
             * activity, it was already reset on the host side
             */
            sarosSession.getConcurrentDocumentClient().reset(path);
        }

        if (editorWasOpen && type != FileActivity.Type.REMOVED)
            editorManager.openEditor(path);

        consistencyWatchdogClient.performCheck(path);
    }

    /**
     * Updates encoding of a file. A best effort is made to use the inherited
     * encoding if available. Does nothing if the file does not exist or the
     * encoding to set is <code>null</code>
     * 
     * @param encoding
     *            the encoding that should be used
     * @param file
     *            the file to update
     * @throws CoreException
     *             if setting the encoding failed
     */
    private void updateFileEncoding(final String encoding, final IFile file)
        throws CoreException {

        if (encoding == null)
            return;

        if (!file.exists())
            return;

        try {
            Charset.forName(encoding);
        } catch (Exception e) {
            log.warn("encoding " + encoding + " for file " + file
                + " is not available on this platform", e);
            return;
        }

        String projectEncoding = null;
        String fileEncoding = null;

        try {
            projectEncoding = file.getProject().getDefaultCharset();
        } catch (CoreException e) {
            log.warn(
                "could not determine project encoding for project "
                    + file.getProject(), e);
        }

        try {
            fileEncoding = file.getCharset();
        } catch (CoreException e) {
            log.warn("could not determine file encoding for file " + file, e);
        }

        if (encoding.equals(fileEncoding)) {
            log.debug("encoding does not need to be changed for file: " + file);
            return;
        }

        // use inherited encoding if possible
        if (encoding.equals(projectEncoding)) {
            log.debug("changing encoding for file " + file
                + " to use default project encoding: " + projectEncoding);
            file.setCharset(null, new NullProgressMonitor());
            return;
        }

        log.debug("changing encoding for file " + file + " to encoding: "
            + encoding);

        file.setCharset(encoding, new NullProgressMonitor());
    }

    private void handleFileMove(FileActivity activity) throws CoreException {
        IPath newFilePath = ((EclipsePathImpl) activity.getPath().getFile()
            .getFullPath()).getDelegate();

        IResource oldResource = ((EclipseFileImpl) activity.getOldPath()
            .getFile()).getDelegate();

        FileUtils.mkdirs(((EclipseFileImpl) activity.getPath().getFile())
            .getDelegate());
        FileUtils.move(newFilePath, oldResource);

        if (activity.getContent() == null)
            return;

        handleFileCreation(activity);
    }

    private void handleFileDeletion(FileActivity activity) throws CoreException {
        IFile file = ((EclipseFileImpl) activity.getPath().getFile())
            .getDelegate();

        if (file.exists())
            FileUtils.delete(file);
        else
            log.warn("could not delete file " + file
                + " because it does not exist");
    }

    private void handleFileCreation(FileActivity activity) throws CoreException {
        IFile file = ((EclipseFileImpl) activity.getPath().getFile())
            .getDelegate();

        final String encoding = activity.getEncoding();

        byte[] actualContent = FileUtils.getLocalFileContent(file);
        byte[] newContent = activity.getContent();

        if (!Arrays.equals(newContent, actualContent)) {
            FileUtils.writeFile(new ByteArrayInputStream(newContent), file,
                new NullProgressMonitor());
        } else {
            log.debug("FileActivity " + activity + " dropped (same content)");
        }

        if (encoding != null)
            updateFileEncoding(encoding, file);
    }

    protected void handleFolderActivity(FolderActivity activity)
        throws CoreException {

        SPath path = activity.getPath();

        IFolder folder = ((EclipseFolderImpl) path.getProject().getFolder(
            path.getProjectRelativePath())).getDelegate();

        if (activity.getType() == FolderActivity.Type.CREATED) {
            FileUtils.create(folder);
        } else if (activity.getType() == FolderActivity.Type.REMOVED) {
            try {
                if (folder.exists())
                    FileUtils.delete(folder);
            } catch (CoreException e) {
                log.warn("Removing folder failed: " + folder);
            }
        }
    }

    protected void handleVCSActivity(VCSActivity activity) {
        final VCSActivity.Type activityType = activity.getType();
        SPath path = activity.getPath();

        final IResource resource = ((EclipseResourceImpl) path.getResource())
            .getDelegate();

        final IProject project = ((EclipseProjectImpl) path.getProject())
            .getDelegate();

        final String url = activity.getURL();
        final String directory = activity.getDirectory();
        final String revision = activity.getParam1();

        // Connect is special since the project doesn't have a VCSAdapter
        // yet.
        final VCSAdapter vcs = activityType == VCSActivity.Type.CONNECT ? VCSAdapter
            .getAdapter(revision) : VCSAdapter.getAdapter(project);
        if (vcs == null) {
            log.warn("Could not execute VCS activity. Do you have the Subclipse plug-in installed?");
            if (activity.containedActivity.size() > 0) {
                log.trace("contained activities: "
                    + activity.containedActivity.toString());
            }
            for (IResourceActivity a : activity.containedActivity) {
                consumer.exec(a);
            }
            return;
        }

        try {
            // TODO Should these operations run in an IWorkspaceRunnable?
            Shell shell = SWTUtils.getShell();
            ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(
                shell);
            progressMonitorDialog.open();
            Shell pmdShell = progressMonitorDialog.getShell();
            pmdShell.setText("Saros running VCS operation");
            log.trace("about to call progressMonitorDialog.run");
            progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor progress)

                throws InvocationTargetException, InterruptedException {
                    log.trace("progressMonitorDialog.run started");
                    if (!SWTUtils.isSWT())
                        log.trace("not in SWT thread");
                    if (activityType == VCSActivity.Type.CONNECT) {
                        vcs.connect(project, url, directory, progress);
                    } else if (activityType == VCSActivity.Type.DISCONNECT) {
                        vcs.disconnect(project, revision != null, progress);
                    } else if (activityType == VCSActivity.Type.SWITCH) {
                        vcs.switch_(resource, url, revision, progress);
                    } else if (activityType == VCSActivity.Type.UPDATE) {
                        vcs.update(resource, revision, progress);
                    } else {
                        log.error("VCS activity type not implemented yet.");
                    }
                    log.trace("progressMonitorDialog.run done");
                }

            });
            pmdShell.dispose();
        } catch (InvocationTargetException e) {
            // TODO We can't get here, right?
            throw new IllegalStateException(e);
        } catch (InterruptedException e) {
            log.error("Code not designed to be interrupted!");
        }
    }

    // HACK
    public void projectAdded(de.fu_berlin.inf.dpp.filesystem.IProject project) {
        synchronized (sharedProjects) {
            IProject eclipseProject = ((EclipseProjectImpl) project)
                .getDelegate();
            sharedProjects.put(eclipseProject, new SharedProject(
                eclipseProject, sarosSession));
        }
    }

    // HACK
    public void projectRemoved(de.fu_berlin.inf.dpp.filesystem.IProject project) {
        synchronized (sharedProjects) {

            SharedProject sharedProject = sharedProjects
                .remove(((EclipseProjectImpl) project).getDelegate());
            if (sharedProject != null)
                sharedProject.delete();
        }
    }
}
