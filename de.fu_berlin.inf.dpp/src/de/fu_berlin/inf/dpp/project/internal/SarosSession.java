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
package de.fu_berlin.inf.dpp.project.internal;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.joda.time.DateTime;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IProjectActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.management.TransformationResult;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.util.ArrayUtils;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.util.MappedList;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * TODO Review if SarosSession, ConcurrentDocumentManager, ActivitySequencer all
 * honor start() and stop() semantics.
 */
public class SarosSession implements ISarosSession, Disposable {

    private static final Logger log = Logger.getLogger(SarosSession.class);

    public static final int MAX_USERCOLORS = 5;

    /* Dependencies */
    @Inject
    protected Saros saros;

    @Inject
    protected SarosNet sarosNet;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected DataTransferManager transferManager;

    @Inject
    protected ProjectNegotiationObservable projectNegotiationObservable;

    @Inject
    protected EditorManager editorManager;

    protected ActivitySequencer activitySequencer;

    protected ConcurrentDocumentClient concurrentDocumentClient;

    protected ConcurrentDocumentServer concurrentDocumentServer;

    protected final CopyOnWriteArrayList<IActivityProvider> activityProviders = new CopyOnWriteArrayList<IActivityProvider>();

    protected StopManager stopManager = new StopManager(this);

    private MappedList<String, IActivityDataObject> queuedActivities = new MappedList<String, IActivityDataObject>();

    /* Instance fields */
    protected User localUser;

    protected ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    protected SharedProjectListenerDispatch listenerDispatch = new SharedProjectListenerDispatch();

    protected User host;

    protected FreeColors freeColors = null;

    protected DateTime sessionStart;

    protected SarosProjectMapper projectMapper = new SarosProjectMapper();

    protected boolean useVersionControl = true;

    protected BlockingQueue<List<IActivity>> pendingActivityLists = new LinkedBlockingQueue<List<IActivity>>();

    protected List<IResource> selectedResources = new ArrayList<IResource>();

    public boolean cancelActivityDispatcher = false;

    protected IPreferenceStore prefStore;

    /**
     * This thread executes pending activities in the SWT thread.<br>
     * Reason: When a batch of activities arrives, it's not enough to exec them
     * in a new Runnable with Util.runSafeSWTAsync(). Activity messages are
     * ~1000ms apart, but a VCSActivity typically takes longer than that. Let's
     * say this activity is dispatched to the SWT thread in a new Runnable r1.
     * It now runs an SVN operation in a new thread t1. After 1000ms, another
     * activity arrives, and is dispatched to SWT in runSafeSWTAsync(r2). Since
     * r1 is mostly waiting on t1, the SWT thread is available to run the next
     * runnable r2, because we used runSafeSWTAsync(r1).<br>
     * That's also the reason why we have to use Util.runSafeSWTSync in the
     * activityDispatcher thread.
     * 
     * @see Utils#runSafeSWTAsync(Logger, Runnable)
     * @see Display#asyncExec(Runnable)
     */
    /*
     * Note: transformation and executing has to be performed together in the
     * SWT thread. Else, it would be possible that local activities are executed
     * between transformation and application of remote operations. In other
     * words, the transformation would be applied to an out-dated state.
     */
    private Thread activityDispatcher = new Thread("Saros Activity Dispatcher") { //$NON-NLS-1$
        @Override
        public void run() {
            try {
                while (!cancelActivityDispatcher) {
                    final List<IActivity> activities = pendingActivityLists
                        .take();
                    Utils.runSafeSWTSync(log, new Runnable() {
                        public void run() {
                            TransformationResult transformed = concurrentDocumentClient
                                .transformIncoming(activities);

                            for (QueueItem item : transformed.getSendToPeers()) {
                                sendActivity(item.recipients, item.activity);
                            }

                            for (IActivity activity : transformed.executeLocally) {
                                for (IActivityProvider executor : activityProviders) {
                                    executor.exec(activity);
                                    handleFileAndFolderActivities(activity);
                                }
                            }
                        }
                    });
                }
            } catch (InterruptedException e) {
                if (!cancelActivityDispatcher)
                    log.error("activityDispatcher interrupted prematurely!", e); //$NON-NLS-1$
            }
        }
    };

    public static class QueueItem {

        public final List<User> recipients;
        public final IActivity activity;

        public QueueItem(List<User> recipients, IActivity activity) {
            if (recipients.size() == 0)
                log.fatal("Empty list of recipients in constructor", //$NON-NLS-1$
                    new StackTrace());
            this.recipients = recipients;
            this.activity = activity;
        }

        public QueueItem(User host, IActivity activity) {
            this(Collections.singletonList(host), activity);
        }
    }

    /**
     * Common constructor code for host and client side.
     */
    /*
     * FIXME ITransmitter, DataTransferManager, and DispatchThreadContext are
     * dependencies of ActivitySequencer, not SarosSession.
     */
    protected SarosSession(ITransmitter transmitter,
        DispatchThreadContext threadContext, int myColorID,
        DateTime sessionStart, SarosContext sarosContext) {

        sarosContext.initComponent(this);

        assert transmitter != null;
        assert sarosNet.getMyJID() != null;

        this.sessionStart = sessionStart;

        this.localUser = new User(this, sarosNet.getMyJID(), myColorID);

        this.prefStore = saros.getPreferenceStore();
        int updateInterval = prefStore
            .getInt(PreferenceConstants.MILLIS_UPDATE);

        this.activitySequencer = new ActivitySequencer(this, transmitter,
            transferManager, threadContext, updateInterval);

        activityDispatcher.setDaemon(true);
        activityDispatcher.start();
    }

    /**
     * Constructor called for SarosSession of the host
     */
    public SarosSession(ITransmitter transmitter,
        DispatchThreadContext threadContext, DateTime sessionStart,
        SarosContext sarosContext) {

        this(transmitter, threadContext, 0, sessionStart, sarosContext);

        freeColors = new FreeColors(MAX_USERCOLORS - 1);
        host = localUser;
        host.invitationCompleted();

        participants.put(host.getJID(), host);

        /** add host to {@link User.Permission#WRITE_ACCESS} list. */
        concurrentDocumentServer = new ConcurrentDocumentServer(this);
        concurrentDocumentClient = new ConcurrentDocumentClient(this);
    }

    /**
     * Constructor of client
     */
    public SarosSession(ITransmitter transmitter,
        DispatchThreadContext threadContext, JID hostID, int myColorID,
        DateTime sessionStart, SarosContext sarosContext) {

        this(transmitter, threadContext, myColorID, sessionStart, sarosContext);

        host = new User(this, hostID, 0);
        host.invitationCompleted();

        participants.put(hostID, host);
        participants.put(sarosNet.getMyJID(), localUser);

        concurrentDocumentClient = new ConcurrentDocumentClient(this);
    }

    public void addSharedResources(IProject project, String projectID,
        List<IResource> dependentResources) {
        if (!isCompletelyShared(project) && dependentResources != null) {
            for (IResource iResource : dependentResources) {
                if (iResource instanceof IFolder) {
                    addMembers(iResource, dependentResources);
                }
            }
            if (selectedResources != null) {
                selectedResources.removeAll(dependentResources);
                dependentResources.addAll(selectedResources);
                selectedResources.clear();
            }
        }
        if (!projectMapper.isShared(project)) {
            projectMapper.addMapping(projectID, project, new SharedProject(
                project, this));
            projectMapper.addResourceMapping(project, dependentResources);
            projectMapper.addUserToProjectMapping(getLocalUser().getJID(),
                project, projectID);
        } else {
            List<IResource> resources = getSharedResources(project);
            if (resources != null && dependentResources != null) {
                resources.addAll(dependentResources);
                projectMapper.addResourceMapping(project, resources);
            } else if (resources != null && dependentResources == null) {
                projectMapper.addResourceMapping(project, null);
            }
        }
        execQueuedActivities(projectID);
    }

    public Collection<User> getParticipants() {
        return participants.values();
    }

    public List<User> getRemoteUsersWithReadOnlyAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isLocal())
                continue;
            if (user.hasReadOnlyAccess())
                result.add(user);
        }
        return result;
    }

    public List<User> getUsersWithReadOnlyAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.hasReadOnlyAccess())
                result.add(user);
        }
        return result;
    }

    public List<User> getUsersWithWriteAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.hasWriteAccess())
                result.add(user);
        }
        return result;
    }

    public List<User> getRemoteUsersWithWriteAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isLocal())
                continue;
            if (user.hasWriteAccess())
                result.add(user);
        }
        return result;
    }

    public List<User> getRemoteUsers() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isRemote())
                result.add(user);
        }
        return result;
    }

    public ActivitySequencer getSequencer() {
        return activitySequencer;
    }

    /**
     * {@inheritDoc}
     */
    public void initiatePermissionChange(final User user,
        final Permission newPermission, SubMonitor progress)
        throws CancellationException, InterruptedException {

        if (!localUser.isHost()) {
            throw new IllegalArgumentException(
                Messages.SarosSession_only_inviter_can_initate_permission_changes);
        }

        if (user.isHost()) {

            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    activityCreated(new PermissionActivity(getLocalUser(),
                        user, newPermission));

                    setPermission(user, newPermission);
                }
            });

        } else {
            StartHandle startHandle = stopManager.stop(user,
                Messages.SarosSession_performing_permission_change, progress);

            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    activityCreated(new PermissionActivity(getLocalUser(),
                        user, newPermission));

                    setPermission(user, newPermission);
                }
            });

            if (!startHandle.start())
                log.error("Didn't unblock. " //$NON-NLS-1$
                    + "There still exist unstarted StartHandles."); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setPermission(final User user, final Permission permission) {

        assert Utils.isSWT() : "Must be called from SWT Thread"; //$NON-NLS-1$

        if (user == null || permission == null)
            throw new IllegalArgumentException();

        user.setPermission(permission);

        log.info("Buddy " + user + " is now a " + permission); //$NON-NLS-1$ //$NON-NLS-2$

        listenerDispatch.permissionChanged(user);
    }

    /**
     * {@inheritDoc}
     */
    public void userInvitationCompleted(final User user) {
        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                userInvitationCompletedWrapped(user);
            }
        });
    }

    public void userInvitationCompletedWrapped(final User user) {

        assert Utils.isSWT() : "Must be called from SWT Thread"; //$NON-NLS-1$

        if (user == null)
            throw new IllegalArgumentException();

        user.invitationCompleted();

        log.debug("The invitation of " + Utils.prefix(user.getJID()) //$NON-NLS-1$
            + " is now complete"); //$NON-NLS-1$

        listenerDispatch.invitationCompleted(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public User getHost() {
        return host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    public boolean isHost() {
        return localUser.isHost();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean hasWriteAccess() {
        return localUser.hasWriteAccess();
    }

    public boolean hasExclusiveWriteAccess() {
        if (!hasWriteAccess()) {
            return false;
        }
        for (User user : getParticipants()) {
            if (user.isRemote() && user.hasWriteAccess()) {
                return false;
            }
        }
        return true;
    }

    public void addUser(User user) {

        assert user.getSarosSession().equals(this);

        JID jid = user.getJID();

        if (participants.putIfAbsent(jid, user) != null) {
            log.error("Buddy " + Utils.prefix(jid) //$NON-NLS-1$
                + " added twice to SarosSession", new StackTrace()); //$NON-NLS-1$
            throw new IllegalArgumentException();
        }

        listenerDispatch.userJoined(user);

        log.info("Buddy " + Utils.prefix(jid) + " joined session."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void removeUser(User user) {
        JID jid = user.getJID();
        if (participants.remove(jid) == null) {
            log.warn("Tried to remove buddy who was not in participants:"
                + Utils.prefix(jid));
            return;
        }
        if (isHost()) {
            returnColor(user.getColorID());
        }

        activitySequencer.userLeft(user);

        // TODO what is to do here if no user with write access exists anymore?
        listenerDispatch.userLeft(user);

        // Disconnect bytestream connection when user leaves session to
        // prevent idling connection when not needed anymore.
        transferManager.closeConnection(jid);

        log.info("Buddy " + Utils.prefix(jid) + " left session"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    public void addListener(ISharedProjectListener listener) {
        listenerDispatch.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    public void removeListener(ISharedProjectListener listener) {
        listenerDispatch.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    public Set<IProject> getProjects() {
        return projectMapper.getProjects();
    }

    public Thread requestTransmitter = null;

    public void start() {
        if (!stopped) {
            throw new IllegalStateException();
        }
        activitySequencer.start();

        stopped = false;

    }

    // TODO Review sendRequest for InterruptedException and remove this flag.
    boolean stopped = true;

    public boolean isStopped() {
        return stopped;
    }

    /**
     * Stops the associated activityDataObject sequencer.
     * 
     * @throws IllegalStateException
     *             if the shared project is already stopped.
     */
    public void stop() {
        if (stopped) {
            throw new IllegalStateException();
        }

        activitySequencer.stop();
        stopManager.sessionStopped();

        stopped = true;
    }

    public void dispose() {
        if (concurrentDocumentServer != null) {
            concurrentDocumentServer.dispose();
        }
        concurrentDocumentClient.dispose();
        cancelActivityDispatcher = true;
        activityDispatcher.interrupt();
    }

    /**
     * {@inheritDoc}
     */
    public User getUser(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        if (jid.isBareJID()) {
            throw new IllegalArgumentException(MessageFormat.format(
                Messages.SarosSession_jids_should_be_resource_qualified,
                Utils.prefix(jid)));
        }

        User user = participants.get(jid);

        if (user == null || !user.getJID().strictlyEquals(jid))
            return null;

        return user;
    }

    /**
     * Given a JID (with resource or not), will return the resource qualified
     * JID associated with this user or null if no user for the given JID exists
     * in this SarosSession.
     */
    public JID getResourceQualifiedJID(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        User user = participants.get(jid);

        if (user == null)
            return null;

        return user.getJID();
    }

    public User getLocalUser() {
        return localUser;
    }

    public ConcurrentDocumentClient getConcurrentDocumentClient() {
        return concurrentDocumentClient;
    }

    public ConcurrentDocumentServer getConcurrentDocumentServer() {
        return concurrentDocumentServer;
    }

    public Saros getSaros() {
        return saros;
    }

    public int getFreeColor() {
        return freeColors.get();
    }

    public void returnColor(int colorID) {
        freeColors.add(colorID);
    }

    public void exec(List<IActivityDataObject> activityDataObjects) {
        // Convert me

        List<IActivity> activities = convertAndQueueProjectActivities(activityDataObjects);
        if (isHost()) {
            TransformationResult transformed = concurrentDocumentServer
                .transformIncoming(activities);

            activities = transformed.getLocalActivities();

            for (QueueItem item : transformed.getSendToPeers()) {
                sendActivity(item.recipients, item.activity);
            }
        }

        pendingActivityLists.add(activities);
    }

    private List<IActivity> convertAndQueueProjectActivities(
        List<IActivityDataObject> activityDataObjects) {

        List<IActivity> result = new ArrayList<IActivity>(
            activityDataObjects.size());

        for (IActivityDataObject dataObject : activityDataObjects) {
            try {
                if (!hadToBeQueued(dataObject)) {
                    result.add(dataObject.getActivity(this));
                }

            } catch (IllegalArgumentException e) {
                log.warn("DataObject could not be attached to SarosSession: " //$NON-NLS-1$
                    + dataObject, e);
            }
        }

        return result;
    }

    String lastID;

    /**
     * 
     * @param dataObject
     * @return <code>true</code> if this activity can be executed now
     */
    private boolean hadToBeQueued(IActivityDataObject dataObject) {
        if (dataObject instanceof IProjectActivityDataObject) {
            String projectID = ((IProjectActivityDataObject) dataObject)
                .getProjectID();

            /*
             * some activities (e.g. EditorActivity) can return null for
             * projectID
             */
            if (dataObject instanceof EditorActivityDataObject) {
                IProject project;
                if (projectID != null) {
                    lastID = projectID;
                    project = getProject(projectID);
                } else {
                    project = getProject(lastID);
                }
                /*
                 * If we don't have that shared project, but will have it in
                 * future we will queue the activity.
                 * 
                 * When the project negotiation is done the method
                 * execQueuedActivities() will be executed
                 */
                if (project == null) {
                    log.info("Activity " + dataObject.toString()
                        + " for Project " + projectID + " was queued.");
                    if (!queuedActivities.containsValue(dataObject)) {
                        if (projectID == null) {
                            queuedActivities.put(lastID, dataObject);
                        } else {
                            queuedActivities.put(projectID, dataObject);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Execute activities queued in the SarosSession. At the moment these can
     * only be activities which belong to a specific project. Therefore we need
     * the <code><b>projectID</b></code> to identify the now executable
     * activities
     */
    protected void execQueuedActivities(String projectID) {
        List<IActivityDataObject> list = queuedActivities.remove(projectID);
        if (list == null) {
            return;
        }
        log.info("All activities for project \"" + projectID //$NON-NLS-1$
            + "\" will be executed now"); //$NON-NLS-1$
        exec(list);
    }

    public void activityCreated(IActivity activity) {

        assert Utils.isSWT() : "Must be called from the SWT Thread"; //$NON-NLS-1$

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null"); //$NON-NLS-1$

        /*
         * Let ConcurrentDocumentManager have a look at the activities first
         */
        List<QueueItem> toSend = concurrentDocumentClient
            .transformOutgoing(activity);

        for (QueueItem item : toSend) {
            sendActivity(item.recipients, item.activity);
        }
    }

    /**
     * Convenience method to address a single recipient.
     * 
     * @see #sendActivity(List, IActivity)
     */
    public void sendActivity(User recipient, IActivity activity) {
        sendActivity(Collections.singletonList(recipient), activity);
    }

    public void sendActivity(List<User> toWhom, final IActivity activity) {
        if (toWhom == null)
            throw new IllegalArgumentException();

        if (activity == null)
            throw new IllegalArgumentException();
        /*
         * If we don't have any sharedProjects don't send File-, Folder- or
         * EditorActivities.
         */
        if (projectMapper.size() == 0
            && (activity instanceof EditorActivity
                || activity instanceof FolderActivity || activity instanceof FileActivity)) {
            return;
        }

        /*
         * If the need based synchronization is not disabled, process this
         * JupiterActivity.
         */
        if (activity instanceof JupiterActivity)
            needBasedSynchronization(((JupiterActivity) activity), toWhom);

        // avoid consistency control during project negotiation to relieve the
        // general transmission process
        if (isInProjectNegotiation(activity)
            && activity instanceof ChecksumActivity)
            return;

        // avoid sending of unwanted editor related activities

        if (activity instanceof TextSelectionActivity
            || activity instanceof ViewportActivity
            || activity instanceof JupiterActivity) {
            if (!needBasedPathsList.contains(activity.getPath())
                && !isShared(activity.getPath().getResource()))
                return;
        }

        toSend = true;
        // handle FileActivities and FolderActivities to update ProjectMapper
        if (activity instanceof FolderActivity
            || activity instanceof FileActivity)
            handleFileAndFolderActivities(activity);

        if (!toSend)
            return;

        try {
            activitySequencer.sendActivity(toWhom,
                activity.getActivityDataObject(this));
        } catch (IllegalArgumentException e) {
            log.warn("Could not convert Activity to DataObject: ", e);
        }
    }

    /**
     * Convenient method to determine if Project of given {@link IActivity} is
     * currently transmitted.
     * 
     * @param activity
     * @return <b>true</b> if the activity is send during a project transmission<br>
     *         <b>false</b> if no transmission is running
     */
    private boolean isInProjectNegotiation(IActivity activity) {
        if (activity == null)
            throw new IllegalArgumentException();

        SPath path = activity.getPath();
        if (path == null)
            return false;

        // determine if we are in a transmission process with our project
        Collection<ProjectNegotiation> projectNegotiations = projectNegotiationObservable
            .getProcesses().values();
        Set<String> projectIDs = null;
        for (ProjectNegotiation projectNegotiation : projectNegotiations) {
            projectIDs = projectNegotiation.getProjectNames().keySet();
        }
        if (projectIDs != null) {
            return projectIDs.contains(getProjectID(path.getProject()));
        }
        return false;
    }

    List<SPath> needBasedPathsList = new ArrayList<SPath>();

    /**
     * Method to enable the need based sync.
     * 
     * @param jupiterActivity
     *            {@link JupiterActivity} that triggers the need based
     *            synchronization
     * @param toWhom
     */
    private void needBasedSynchronization(JupiterActivity jupiterActivity,
        List<User> toWhom) {
        if (jupiterActivity == null)
            throw new IllegalArgumentException();

        if (preferenceUtils.isNeedsBasedSyncEnabled().equals("false"))
            return;

        final SPath path = jupiterActivity.getPath();
        IProject iProject = path.getProject();

        if (!isOwnedProject(iProject))
            return;

        if (needBasedPathsList.contains(path))
            return;

        boolean isProjectTransmitted = isInProjectNegotiation(jupiterActivity);

        /*
         * need-based transmission when file is not shared or file is in
         * transmission process
         */
        if (isProjectTransmitted
            || (!isShared(path.getFile()) && !isProjectTransmitted)) {
            if (preferenceUtils.isNeedsBasedSyncEnabled().equals("undefined")) {
                if (!CollaborationUtils.activateNeedBasedSynchronization(saros))
                    return;
            }

            needBasedPathsList.add(path);

            try {
                sendSingleFile(path);
                sendActivity(toWhom, jupiterActivity);
            } catch (FileNotFoundException e) {
                log.error("File could not be found, despite existing: " + path,
                    e);
            }
        }
    }

    /**
     * 
     * This Method enables a reliable way to automatically synchronize single
     * Files to all other session participants.
     * 
     * @param path
     *            identifies the file to synchronize to all session participants
     * @throws FileNotFoundException
     */
    protected void sendSingleFile(final SPath path)
        throws FileNotFoundException {
        if (path == null)
            throw new IllegalArgumentException();

        for (final User recipient : getRemoteUsers()) {
            final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                EditorAPI.getAWorkbenchWindow().getShell());
            final SarosSession session = this;
            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    try {
                        dialog.run(true, true, new IRunnableWithProgress() {
                            public void run(IProgressMonitor monitor) {
                                // synchronize the file and check if it is
                                // correctly transmitted
                                FileUtils.syncSingleFile(recipient, session,
                                    path, SubMonitor.convert(monitor));

                                /*
                                 * Notify the session participants of your
                                 * activated editor. This is mandatory to avoid
                                 * inconsistencies with the editor manager and
                                 * by that with the follow mode.
                                 */
                                editorManager.sendPartActivated();
                            }
                        });
                    } catch (InvocationTargetException e) {
                        try {
                            throw e.getCause();
                        } catch (CancellationException c) {
                            log.info("Need based sync was cancelled by local user");
                        } catch (Throwable t) {
                            log.error("Internal Error: ", t);
                        }
                    } catch (InterruptedException e) {
                        log.debug(
                            "Thread is interrupted, either before or during the need based synchronization of "
                                + path, e);
                    }
                }
            });

        }
    }

    boolean toSend = true;

    /**
     * Method to update the ProjectMapper when changes on shared files oder
     * folders happened.
     * 
     * @param activity
     *            {@link FileActivity} or {@link FolderActivity} to handle
     */
    protected void handleFileAndFolderActivities(IActivity activity) {
        if (!(activity instanceof FileActivity)
            && !(activity instanceof FolderActivity))
            return;

        if (activity instanceof FileActivity) {
            FileActivity fileActivity = ((FileActivity) activity);
            SPath path = fileActivity.getPath();
            IFile file = path.getFile();

            if (isInProjectNegotiation(fileActivity)
                && !fileActivity.isNeedBased()) {
                toSend = false;
                return;
            }

            if (file == null)
                return;

            IProject project = file.getProject();
            List<IResource> resources = getSharedResources(project);

            switch (fileActivity.getType()) {
            case Created:
                if (!file.exists())
                    return;

                if (resources != null && !resources.contains(file)) {
                    resources.add(file);
                    projectMapper.addResourceMapping(project, resources);
                }
                break;
            case Removed:
                if (!isShared(file)) {
                    toSend = false;
                    return;
                }
                if (resources != null && resources.contains(file)) {
                    resources.remove(file);
                    projectMapper.addResourceMapping(project, resources);
                }
                break;
            case Moved:
                IFile oldFile = fileActivity.getOldPath().getFile();
                if (oldFile == null || !isShared(oldFile)) {
                    toSend = false;
                    return;
                }
                List<IResource> res = getSharedResources(oldFile.getProject());
                if (res != null) {
                    if (res.contains(oldFile))
                        res.remove(oldFile);
                    if (!res.contains(file))
                        res.add(file);
                    projectMapper.addResourceMapping(project, res);
                }
                break;
            }
        } else if (activity instanceof FolderActivity) {
            FolderActivity folderActivity = ((FolderActivity) activity);
            IFolder folder = folderActivity.getPath().getFolder();

            if (folder == null)
                return;

            IProject iProject = folder.getProject();
            List<IResource> resources = getSharedResources(iProject);

            if (resources != null) {
                switch (folderActivity.getType()) {
                case Created:
                    if (!resources.contains(folder)
                        && isShared(folder.getParent())) {
                        resources.add(folder);
                        projectMapper.addResourceMapping(iProject, resources);
                    }
                    break;
                case Removed:
                    if (!isShared(folder)) {
                        toSend = false;
                        return;
                    }
                    if (resources.contains(folder)) {
                        resources.remove(folder);
                        projectMapper.addResourceMapping(iProject, resources);
                    }
                }
            }
        }
        return;
    }

    public void addActivityProvider(IActivityProvider provider) {
        if (activityProviders.addIfAbsent(provider))
            provider.addActivityListener(this);
    }

    public void removeActivityProvider(IActivityProvider provider) {
        activityProviders.remove(provider);
        provider.removeActivityListener(this);
    }

    public DateTime getSessionStart() {
        return sessionStart;
    }

    public boolean isShared(IResource resource) {
        return projectMapper.isShared(resource);
    }

    public List<IResource> getAllSharedResources() {
        List<IResource> allSharedResources = new ArrayList<IResource>();
        Collection<List<IResource>> resources = projectMapper.getResources();
        for (List<IResource> list : resources) {
            if (list != null)
                allSharedResources.addAll(list);
        }
        return allSharedResources;
    }

    protected void addMembers(IResource iResource,
        List<IResource> dependentResources) {
        if (iResource instanceof IFolder || iResource instanceof IProject) {

            if (!isShared(iResource)) {
                selectedResources.add(iResource);
            } else {
                return;
            }
            List<IResource> childResources = null;
            try {
                childResources = ArrayUtils.getAdaptableObjects(
                    ((IContainer) iResource).members(), IResource.class);
            } catch (CoreException e) {
                log.debug("Can't get children of Project/Folder. ", e); //$NON-NLS-1$
            }
            if (childResources != null && (childResources.size() > 0)) {
                for (IResource childResource : childResources) {
                    addMembers(childResource, dependentResources);
                }
            }
        } else if (iResource instanceof IFile) {
            if (!isShared(iResource)) {
                selectedResources.add(iResource);
            }
        }
    }

    public boolean useVersionControl() {
        /*
         * It is not possible to enable version control support during a
         * session.
         */
        if (!useVersionControl)
            return false;
        return useVersionControl = preferenceUtils.useVersionControl();
    }

    public SharedProject getSharedProject(IProject project) {
        if (!isShared(project))
            return null;
        return projectMapper.getSharedProject(projectMapper.getID(project));
    }

    public List<SharedProject> getSharedProjects() {
        return projectMapper.getSharedProjects();
    }

    public String getProjectID(IProject project) {
        return projectMapper.getID(project);
    }

    public IProject getProject(String projectID) {
        return projectMapper.getProject(projectID);
    }

    public void synchronizeUserList(ITransmitter transmitter, JID peer,
        String invitationID, SubMonitor monitor)
        throws SarosCancellationException {

        Collection<User> participants = this.getParticipants();
        log.debug("Inv" + Utils.prefix(peer) + ": Synchronizing userlist " //$NON-NLS-1$ //$NON-NLS-2$
            + participants);

        SarosPacketCollector userListConfirmationCollector = transmitter
            .getUserListConfirmationCollector();

        for (User user : this.getRemoteUsers()) {
            transmitter.sendUserList(user.getJID(), invitationID, participants);
        }

        log.debug("Inv" + Utils.prefix(peer) //$NON-NLS-1$
            + ": Waiting for user list confirmations..."); //$NON-NLS-1$
        transmitter.receiveUserListConfirmation(userListConfirmationCollector,
            this.getRemoteUsers(), monitor);
        log.debug("Inv" + Utils.prefix(peer) //$NON-NLS-1$
            + ": All user list confirmations have arrived."); //$NON-NLS-1$

    }

    public HashMap<IProject, List<IResource>> getProjectResourcesMapping() {
        return projectMapper.getProjectResourceMapping();
    }

    public List<IResource> getSharedResources(IProject project) {
        return projectMapper.getProjectResourceMapping().get(project);
    }

    public boolean isCompletelyShared(IProject project) {
        return projectMapper.isCompletelyShared(project);
    }

    private boolean isOwnedProject(IProject iProject) {
        ArrayList<IProject> ownedProjects = projectMapper
            .getOwnedProjectIDs(getLocalUser().getJID());

        if (ownedProjects == null)
            return false;

        return ownedProjects.contains(iProject);
    }

    public void addProjectOwnership(String projectID, IProject project,
        JID ownerJID) {
        if (projectMapper.getSharedProject(projectID) == null) {
            projectMapper.addMapping(projectID, project, new SharedProject(
                project, this));
            projectMapper.addResourceMapping(project,
                new ArrayList<IResource>());
            projectMapper.addUserToProjectMapping(ownerJID, project, projectID);
        }
    }

    public StopManager getStopManager() {
        return stopManager;
    }
}
