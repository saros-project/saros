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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import de.fu_berlin.inf.dpp.SarosContext;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.joda.time.DateTime;
import org.picocontainer.Disposable;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IProjectActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.management.TransformationResult;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
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
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
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
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected DataTransferManager transferManager;

    @Inject
    protected StopManager stopManager;

    @Inject
    protected ProjectNegotiationObservable projectNegotiationObservable;

    protected ActivitySequencer activitySequencer;

    protected ConcurrentDocumentClient concurrentDocumentClient;

    protected ConcurrentDocumentServer concurrentDocumentServer;

    protected final List<IActivityProvider> activityProviders = new LinkedList<IActivityProvider>();

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

    protected SharedProject sharedProject;

    /**
     * projectID => SharedProject
     * 
     * TODO: Think of joining this with the projectMapper
     */
    protected Map<String, SharedProject> sharedProjects = new HashMap<String, SharedProject>();

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
    private Thread activityDispatcher = new Thread("Saros Activity Dispatcher") {
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
                                }
                            }
                        }
                    });
                }
            } catch (InterruptedException e) {
                if (!cancelActivityDispatcher)
                    log.error("activityDispatcher interrupted prematurely!", e);
            }
        }
    };

    public static class QueueItem {

        public final List<User> recipients;
        public final IActivity activity;

        public QueueItem(List<User> recipients, IActivity activity) {
            if (recipients.size() == 0)
                log.fatal("Empty list of recipients in constructor",
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
        assert saros.getMyJID() != null;

        this.sessionStart = sessionStart;

        this.localUser = new User(this, saros.getMyJID(), myColorID);

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
        DispatchThreadContext threadContext, DateTime sessionStart, SarosContext sarosContext) {

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
        participants.put(saros.getMyJID(), localUser);

        concurrentDocumentClient = new ConcurrentDocumentClient(this);
    }

    public void addSharedProject(IProject project, String projectID) {
        projectMapper.addMapping(projectID, project);
        sharedProjects.put(projectID, new SharedProject(project, this));
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
                "Only the inviter can initiate permission changes.");
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
                "Performing permission change", progress);

            Utils.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    activityCreated(new PermissionActivity(getLocalUser(),
                        user, newPermission));

                    setPermission(user, newPermission);
                }
            });

            if (!startHandle.start())
                log.error("Didn't unblock. "
                    + "There still exist unstarted StartHandles.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setPermission(final User user, final Permission permission) {

        assert Utils.isSWT() : "Must be called from SWT Thread";

        if (user == null || permission == null)
            throw new IllegalArgumentException();

        user.setPermission(permission);

        log.info("Buddy " + user + " is now a " + permission);

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

        assert Utils.isSWT() : "Must be called from SWT Thread";

        if (user == null)
            throw new IllegalArgumentException();

        user.invitationCompleted();

        log.debug("The invitation of " + Utils.prefix(user.getJID())
            + " is now complete");

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
            log.error("Buddy " + Utils.prefix(jid)
                + " added twice to SarosSession", new StackTrace());
            throw new IllegalArgumentException();
        }

        listenerDispatch.userJoined(user);

        log.info("Buddy " + Utils.prefix(jid) + " joined session.");
    }

    public void removeUser(User user) {
        JID jid = user.getJID();
        if (participants.remove(jid) == null) {
            log.warn("Tried to remove buddy who was not in participants: "
                + Utils.prefix(jid));
            return;
        }
        if (isHost()) {
            returnColor(user.getColorID());
        }

        activitySequencer.userLeft(jid);

        // TODO what is to do here if no user with write access exists anymore?
        listenerDispatch.userLeft(user);

        log.info("Buddy " + Utils.prefix(jid) + " left session");
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
            throw new IllegalArgumentException(
                "JIDs used for the SarosSession should always be resource qualified: "
                    + Utils.prefix(jid));
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
                log.warn("DataObject could not be attached to SarosSession: "
                    + dataObject, e);
            }
        }

        return result;
    }

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
            if (projectID != null) {
                IProject project = getProject(projectID);
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
                        queuedActivities.put(projectID, dataObject);
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
        log.info("All activities for project \"" + projectID
            + "\" will be executed now");
        exec(list);
    }

    public void activityCreated(IActivity activity) {

        assert Utils.isSWT() : "Must be called from the SWT Thread";

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

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
        try {
            activitySequencer.sendActivity(toWhom,
                activity.getActivityDataObject(this));
        } catch (IllegalArgumentException e) {
            log.warn("Could not convert Activity to DataObject: ", e);
        }
    }

    public void addActivityProvider(IActivityProvider provider) {
        if (!activityProviders.contains(provider)) {
            activityProviders.add(provider);
            provider.addActivityListener(this);
        }
    }

    public void removeActivityProvider(IActivityProvider provider) {
        activityProviders.remove(provider);
        provider.removeActivityListener(this);
    }

    public DateTime getSessionStart() {
        return sessionStart;
    }

    public boolean isShared(IProject project) {
        return projectMapper.isShared(project);
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
        return sharedProjects.get(projectMapper.getID(project));
    }

    public List<SharedProject> getSharedProjects() {
        return new ArrayList<SharedProject>(sharedProjects.values());

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
        log.debug("Inv" + Utils.prefix(peer) + ": Synchronizing userlist "
            + participants);

        SarosPacketCollector userListConfirmationCollector = transmitter
            .getUserListConfirmationCollector();

        for (User user : this.getRemoteUsers()) {
            transmitter.sendUserList(user.getJID(), invitationID, participants);
        }

        log.debug("Inv" + Utils.prefix(peer)
            + ": Waiting for user list confirmations...");
        transmitter.receiveUserListConfirmation(userListConfirmationCollector,
            this.getRemoteUsers(), monitor);
        log.debug("Inv" + Utils.prefix(peer)
            + ": All user list confirmations have arrived.");

    }
}
