package de.fu_berlin.inf.dpp.project;

import static java.text.MessageFormat.format;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.picocontainer.Startable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderCreatedActivity;
import de.fu_berlin.inf.dpp.activities.FolderDeletedActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IFileSystemModificationActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.filesystem.EclipseFileImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipseFolderImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipsePathImpl;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.observables.FileReplacementInProgressObservable;
import de.fu_berlin.inf.dpp.session.AbstractActivityConsumer;
import de.fu_berlin.inf.dpp.session.AbstractActivityProducer;
import de.fu_berlin.inf.dpp.session.AbstractSessionListener;
import de.fu_berlin.inf.dpp.session.IActivityConsumer;
import de.fu_berlin.inf.dpp.session.IActivityConsumer.Priority;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISessionListener;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.FileUtils;

/**
 * This manager is responsible for handling all resource changes that aren't
 * handled by the EditorManager, that is for changes that aren't done by
 * entering text in a text editor. It produces and consumes file and folder
 * activities.
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
    private static final int INTERESTING_EVENTS = IResourceChangeEvent.POST_CHANGE;

    private static final Logger log = Logger
        .getLogger(SharedResourcesManager.class);

    /**
     * If the StopManager has paused the project, the SharedResourcesManager
     * doesn't react to resource changes.
     */
    private boolean pause = false;

    private final ISarosSession sarosSession;

    private final StopManager stopManager;

    /**
     * Should return <code>true</code> while executing resource changes to avoid
     * an infinite resource event loop.
     */
    @Inject
    private FileReplacementInProgressObservable fileReplacementInProgressObservable;

    @Inject
    private EditorManager editorManager;

    /** map that holds the current open or closed state for every shared project */
    private final Map<IProject, Boolean> projectStates = new HashMap<IProject, Boolean>();

    private final ISessionListener sessionListener = new AbstractSessionListener() {

        @Override
        public void projectAdded(
            de.fu_berlin.inf.dpp.filesystem.IProject project) {
            synchronized (projectStates) {
                IProject eclipseProject = (IProject) ResourceAdapterFactory
                    .convertBack(project);
                projectStates.put(eclipseProject, eclipseProject.isOpen());
            }
        }

        @Override
        public void projectRemoved(
            de.fu_berlin.inf.dpp.filesystem.IProject project) {
            synchronized (projectStates) {
                IProject eclipseProject = (IProject) ResourceAdapterFactory
                    .convertBack(project);
                projectStates.remove(eclipseProject);
            }
        }

    };

    private final Blockable stopManagerListener = new Blockable() {
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
        sarosSession.addListener(sessionListener);
        sarosSession.addActivityProducer(this);
        sarosSession.addActivityConsumer(consumer, Priority.ACTIVE);
        stopManager.addBlockable(stopManagerListener);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
            INTERESTING_EVENTS);
    }

    @Override
    public void stop() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        stopManager.removeBlockable(stopManagerListener);
        sarosSession.removeActivityProducer(this);
        sarosSession.removeActivityConsumer(consumer);
        sarosSession.removeListener(sessionListener);
    }

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

        if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
            // Creations, deletions, modifications of files and folders.
            handlePostChange(event);
        } else {
            log.error("Unhandled event type in in SharedResourcesManager: "
                + event);
        }
    }

    private void handlePostChange(IResourceChangeEvent event) {

        if (!sarosSession.hasWriteAccess()) {
            return;
        }

        IResourceDelta delta = event.getDelta();

        if (log.isTraceEnabled()) {
            IJobManager jobManager = Job.getJobManager();
            Job currentJob = jobManager.currentJob();
            log.trace("received resource change event caused by job  ='"
                + currentJob == null ? "N/A" : currentJob.getName() + "'");
        }

        if (delta == null) {
            log.error("unexpected empty delta in resource change event: "
                + event);
            return;
        }

        if (log.isTraceEnabled())
            log.trace("received resource delta contains:\n"
                + deltaToString(delta));

        assert delta.getResource() instanceof IWorkspaceRoot;

        boolean postpone = false;

        IResourceDelta[] projectDeltas = delta.getAffectedChildren();

        for (IResourceDelta projectDelta : projectDeltas) {

            assert projectDelta.getResource() instanceof IProject;

            IProject project = (IProject) projectDelta.getResource();

            if (!sarosSession.isShared(ResourceAdapterFactory.create(project)))
                continue;

            if (!checkOpenClosed(project)) {

                if (log.isDebugEnabled())
                    log.debug("ignoring delta changes for project " + project
                        + " as it was only opened");

                continue;
            }

            ProjectDeltaVisitor visitor = new ProjectDeltaVisitor(
                editorManager, sarosSession);

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
                log.warn(format("ProjectDeltaVisitor of project {0} "
                    + "failed for some reason.", project.getName()), e);
            }

            if (visitor.postponeSending()) {
                postpone = true;
            }

            log.trace("Adding new activities " + visitor.pendingActivities);
            pendingActivities.enterAll(visitor.pendingActivities);
        }
        if (!postpone) {
            fireActivities();
        } else if (!pendingActivities.isEmpty()) {
            log.debug("Postponing sending the activities");
        }
    }

    private boolean checkOpenClosed(IProject project) {

        boolean newProjectState = project.isOpen();

        Boolean oldProjectState;

        synchronized (projectStates) {
            oldProjectState = projectStates.get(project);

            if (oldProjectState == null)
                return false;

            projectStates.put(project, newProjectState);
        }

        boolean stateChanged = newProjectState != oldProjectState;

        /*
         * Since the project was just opened, we would get a notification that
         * each file in the project was just added, so we're simply going to
         * ignore this delta. Any resources that were modified externally would
         * be out-of-sync anyways, so when the user refreshes them we'll get
         * notified.
         */

        if (stateChanged && /* open */newProjectState)
            return false;

        /*
         * TODO report file events in a closed project? Can this happen anyways
         * ?
         */

        return newProjectState;
    }

    /**
     * Fires the ordered activities. To be run before change event ends.
     */
    private void fireActivities() {
        if (pendingActivities.isEmpty())
            return;

        final List<IResourceActivity> orderedActivities = pendingActivities
            .retrieveAll();

        if (log.isTraceEnabled())
            log.trace("Sending activities " + orderedActivities.toString());

        for (final IActivity activity : orderedActivities)
            fireActivity(activity);
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
    private void logPauseWarning(IResourceChangeEvent event) {
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

    private String deltaToString(IResourceDelta delta) {
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
            if (!(activity instanceof IFileSystemModificationActivity))
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
        public void receive(FolderCreatedActivity activity) {

            SPath path = activity.getPath();

            IFolder folder = ((EclipseFolderImpl) path.getProject().getFolder(
                path.getProjectRelativePath())).getDelegate();

            try {
                FileUtils.create(folder);
            } catch (CoreException e) {
                log.error("Failed to execute activity: " + activity, e);
            }
        }

        @Override
        public void receive(FolderDeletedActivity activity) {

            SPath path = activity.getPath();

            IFolder folder = ((EclipseFolderImpl) path.getProject().getFolder(
                path.getProjectRelativePath())).getDelegate();

            try {
                if (folder.exists())
                    FileUtils.delete(folder);

            } catch (CoreException e) {
                log.error("Failed to execute activity: " + activity, e);
            }
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
            editorManager.openEditor(path, true);
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
        byte[] newContent = activity.getContent();
        byte[] actualContent = null;

        if (file.exists())
            actualContent = FileUtils.getLocalFileContent(file);

        if (!Arrays.equals(newContent, actualContent)) {
            FileUtils.writeFile(new ByteArrayInputStream(newContent), file);
        } else {
            log.debug("FileActivity " + activity + " dropped (same content)");
        }

        if (encoding != null)
            updateFileEncoding(encoding, file);
    }
}
