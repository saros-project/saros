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
package de.fu_berlin.inf.dpp.project.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.jivesoftware.smack.packet.PacketExtension;
import org.joda.time.DateTime;
import org.picocontainer.Disposable;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.ISarosContext;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.activities.business.ChecksumActivity;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity;
import de.fu_berlin.inf.dpp.activities.business.FileActivity;
import de.fu_berlin.inf.dpp.activities.business.FolderActivity;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.business.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.business.NOPActivity;
import de.fu_berlin.inf.dpp.activities.business.PermissionActivity;
import de.fu_berlin.inf.dpp.activities.business.TextSelectionActivity;
import de.fu_berlin.inf.dpp.activities.business.ViewportActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.watchdog.ConsistencyWatchdogHandler;
import de.fu_berlin.inf.dpp.editor.EditorManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.feedback.DataTransferCollector;
import de.fu_berlin.inf.dpp.feedback.ErrorLogManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import de.fu_berlin.inf.dpp.feedback.FollowModeCollector;
import de.fu_berlin.inf.dpp.feedback.JumpFeatureUsageCollector;
import de.fu_berlin.inf.dpp.feedback.ParticipantCollector;
import de.fu_berlin.inf.dpp.feedback.PermissionChangeCollector;
import de.fu_berlin.inf.dpp.feedback.SelectionCollector;
import de.fu_berlin.inf.dpp.feedback.SessionDataCollector;
import de.fu_berlin.inf.dpp.feedback.StatisticManager;
import de.fu_berlin.inf.dpp.feedback.TextEditCollector;
import de.fu_berlin.inf.dpp.feedback.VoIPCollector;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.PingPongCentral;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.extensions.KickUserExtension;
import de.fu_berlin.inf.dpp.net.internal.extensions.UserListExtension;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.AbstractActivityProvider;
import de.fu_berlin.inf.dpp.project.IActivityListener;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.Messages;
import de.fu_berlin.inf.dpp.project.SharedProject;
import de.fu_berlin.inf.dpp.project.SharedResourcesManager;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.synchronize.UISynchronizer;
import de.fu_berlin.inf.dpp.ui.util.CollaborationUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.util.ArrayUtils;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;

/**
 * TODO Review if SarosSession, ConcurrentDocumentManager, ActivitySequencer all
 * honor start() and stop() semantics.
 */
public final class SarosSession implements ISarosSession, Disposable {

    private static final Logger log = Logger.getLogger(SarosSession.class);

    public static final int MAX_USERCOLORS = 5;

    @Inject
    private UISynchronizer synchronizer;

    /* Dependencies */
    @Inject
    private Saros saros;

    /*
     * isn't it wonderful that the Saros session does not even know its own ID
     * ?!
     */
    @Inject
    private SessionIDObservable sessionIDObservable;

    @Inject
    private ITransmitter transmitter;

    @Inject
    private SarosNet sarosNet;

    @Inject
    private PreferenceUtils preferenceUtils;

    @Inject
    private DataTransferManager transferManager;

    @Inject
    private ProjectNegotiationObservable projectNegotiationObservable;

    @Inject
    private EditorManager editorManager;

    private final ISarosContext sarosContext;

    private ConcurrentDocumentClient concurrentDocumentClient;

    private ConcurrentDocumentServer concurrentDocumentServer;

    private ActivityHandler activityHandler;

    private final CopyOnWriteArrayList<IActivityProvider> activityProviders = new CopyOnWriteArrayList<IActivityProvider>();

    /* Instance fields */
    private User localUser;

    private final ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    private final SharedProjectListenerDispatch listenerDispatch = new SharedProjectListenerDispatch();

    private User host;

    private final DateTime sessionStart;

    private final SarosProjectMapper projectMapper = new SarosProjectMapper();

    private boolean useVersionControl = true;

    private List<IResource> selectedResources = new ArrayList<IResource>();

    private MutablePicoContainer sessionContainer;

    private StopManager stopManager;

    private ChangeColorManager changeColorManager;

    private ActivitySequencer activitySequencer;

    private final ActivityQueuer activityQueuer;

    private final IActivityListener activityListener = new IActivityListener() {

        /**
         * @JTourBusStop 5, Activity sending, Forwarding the IActivity:
         * 
         *               This is where the SarosSession will receive the
         *               activity, it is not part of the ISarosSession interface
         *               to avoid misuse.
         */
        @Override
        public void activityCreated(final IActivity activityData) {
            handleActivityCreated(activityData);
        }
    };

    private final IActivityHandlerCallback activityCallback = new IActivityHandlerCallback() {

        @Override
        public void send(List<User> recipients, IActivity activity) {
            sendActivity(recipients, activity);
        }

        @Override
        public void execute(IActivity activity) {
            for (IActivityProvider executor : activityProviders) {
                executor.exec(activity);
                handleFileAndFolderActivities(activity);
            }
        }

    };

    /**
     * Common constructor code for host and client side.
     */
    protected SarosSession(DateTime sessionStart, ISarosContext sarosContext,
        int colorID) {

        sarosContext.initComponent(this);

        this.activityQueuer = new ActivityQueuer();
        this.sarosContext = sarosContext;
        this.sessionStart = sessionStart;

        // FIXME that should be passed in !
        JID localUserJID = sarosNet.getMyJID();

        assert localUserJID != null;

        this.localUser = new User(this, localUserJID, colorID, colorID);
    }

    /**
     * Constructor called for SarosSession of the host
     */
    public SarosSession(int colorID, DateTime sessionStart,
        ISarosContext sarosContext) {

        this(sessionStart, sarosContext, colorID);

        host = localUser;

        participants.put(host.getJID(), host);

        initializeSessionContainer(sarosContext);
    }

    /**
     * Constructor of client
     */
    public SarosSession(JID hostID, int myColorID, DateTime sessionStart,
        ISarosContext sarosContext, JID inviterID, int inviterColorID) {

        this(sessionStart, sarosContext, myColorID);

        /*
         * HACK abuse the fact that non-host inviting is currently disabled and
         * so the inviterColorID is always the colorID of the host
         */

        host = new User(this, hostID, inviterColorID, inviterColorID);

        participants.put(hostID, host);
        participants.put(localUser.getJID(), localUser);

        assert inviterID.equals(hostID) : "non host inviting is disabled";
        /*
         * As the host is still a special person, we must find out if we were
         * invited by the host...
         */
        // if (!inviterID.equals(hostID)) {
        // /*
        // * ... or another participant whom we have to add to this session
        // * too!
        // */
        // if (freeColors.remove(inviterColorID)) {
        // log.debug("INVITERS colorID (" + inviterColorID
        // + ") was removed from the list.");
        // } else {
        // log.warn("INVITERS colorID couldn't be removed from the list!");
        // }
        //
        // User inviter = new User(this, inviterID, inviterColorID);
        // inviter.invitationCompleted();
        // participants.put(inviterID, inviter);
        // }

        initializeSessionContainer(sarosContext);
    }

    @Override
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
                project);
        } else {
            List<IResource> resources = getSharedResources(project);
            if (resources != null && dependentResources != null) {
                resources.addAll(dependentResources);
                projectMapper.addResourceMapping(project, resources);
            } else if (resources != null && dependentResources == null) {
                projectMapper.addResourceMapping(project, null);
            }
        }
    }

    @Override
    public List<User> getParticipants() {
        return new ArrayList<User>(participants.values());
    }

    @Override
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

    @Override
    public List<User> getUsersWithReadOnlyAccess() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.hasReadOnlyAccess())
                result.add(user);
        }
        return result;
    }

    @Override
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

    @Override
    public List<User> getRemoteUsers() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isRemote())
                result.add(user);
        }
        return result;
    }

    @Override
    public ActivitySequencer getSequencer() {
        return activitySequencer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initiatePermissionChange(final User user,
        final Permission newPermission, SubMonitor progress)
        throws CancellationException, InterruptedException {

        if (!localUser.isHost()) {
            throw new IllegalArgumentException(
                Messages.SarosSession_only_inviter_can_initate_permission_changes);
        }

        if (user.isHost()) {

            synchronizer.syncExec(Utils.wrapSafe(log, new Runnable() {
                @Override
                public void run() {
                    abuseActivityCreated(new PermissionActivity(getLocalUser(),
                        user, newPermission));

                    setPermission(user, newPermission);
                }
            }));

        } else {
            StartHandle startHandle = getStopManager().stop(user,
                Messages.SarosSession_performing_permission_change, progress);

            synchronizer.syncExec(Utils.wrapSafe(log, new Runnable() {
                @Override
                public void run() {
                    abuseActivityCreated(new PermissionActivity(getLocalUser(),
                        user, newPermission));

                    setPermission(user, newPermission);
                }
            }));

            if (!startHandle.start())
                log.error("Didn't unblock. " //$NON-NLS-1$
                    + "There still exist unstarted StartHandles."); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPermission(final User user, final Permission permission) {

        assert SWTUtils.isSWT() : "Must be called from SWT Thread"; //$NON-NLS-1$

        if (user == null || permission == null)
            throw new IllegalArgumentException();

        user.setPermission(permission);

        log.info("Buddy " + user + " is now a " + permission); //$NON-NLS-1$ //$NON-NLS-2$

        listenerDispatch.permissionChanged(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    @Override
    public User getHost() {
        return host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    @Override
    public boolean isHost() {
        return localUser.isHost();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    @Override
    public boolean hasWriteAccess() {
        return localUser.hasWriteAccess();
    }

    @Override
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

    @Override
    public void addUser(final User user) {

        // TODO synchronize this method !

        assert user.getSarosSession().equals(this);

        JID jid = user.getJID();

        if (participants.putIfAbsent(jid, user) != null) {
            log.error("Buddy " + Utils.prefix(jid) //$NON-NLS-1$
                + " added twice to SarosSession", new StackTrace()); //$NON-NLS-1$
            throw new IllegalArgumentException();
        }

        /*
         * welcome to a dual host-client and P2P architecture
         * 
         * as long as we do not know when something is send to someone this will
         * always produce errors ... swapping synchronizeUserList and userJoined
         * can produce different results
         */

        if (isHost())
            synchronizeUserList();

        synchronizer.syncExec(Utils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                listenerDispatch.userJoined(user);
            }
        }));

        log.info("Buddy " + Utils.prefix(jid) + " joined session."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void removeUser(User user) {
        JID jid = user.getJID();
        if (participants.remove(jid) == null) {
            log.warn("Tried to remove buddy who was not in participants:"
                + Utils.prefix(jid));
            return;
        }

        getSequencer().userLeft(user);

        // TODO what is to do here if no user with write access exists anymore?
        listenerDispatch.userLeft(user);

        // Disconnect bytestream connection when user leaves session to
        // prevent idling connection when not needed anymore.
        transferManager.closeConnection(jid);

        log.info("Buddy " + Utils.prefix(jid) + " left session"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void kickUser(final User user) {

        if (!isHost())
            throw new IllegalStateException(
                "only the host can kick users from the current session");

        if (user.equals(getLocalUser()))
            throw new IllegalArgumentException(
                "the local user cannot kick itself out of the session");

        try {
            transmitter.sendToSessionUser(user.getJID(),
                KickUserExtension.PROVIDER.create(new KickUserExtension(
                    sessionIDObservable.getValue())));
        } catch (IOException e) {
            log.warn("could not kick user "
                + user
                + " from the session because the connection to the user is already lost");
        }

        synchronizer.asyncExec(Utils.wrapSafe(log, new Runnable() {
            @Override
            public void run() {
                removeUser(user);
            }
        }));
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    @Override
    public void addListener(ISharedProjectListener listener) {
        listenerDispatch.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    @Override
    public void removeListener(ISharedProjectListener listener) {
        listenerDispatch.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISarosSession
     */
    @Override
    public Set<IProject> getProjects() {
        return projectMapper.getProjects();
    }

    @Override
    public void start() {
        if (!stopped) {
            throw new IllegalStateException();
        }

        sessionContainer.start();
        stopped = false;

    }

    // TODO Review sendRequest for InterruptedException and remove this flag.
    boolean stopped = true;

    @Override
    public boolean isStopped() {
        return stopped;
    }

    /**
     * Stops the associated activityDataObject sequencer.
     * 
     * @throws IllegalStateException
     *             if the shared project is already stopped.
     */
    @Override
    public void stop() {
        if (stopped) {
            throw new IllegalStateException();
        }

        sessionContainer.stop();
        stopped = true;
    }

    @Override
    public void dispose() {
        /*
         * BUG in Pico Container 2.7 ... lifecycle will throw already disposed
         * exception ... found no bug entry on http://jira.codehaus.org
         */
        // sessionContainer.dispose();
        sarosContext.removeChildContainer(sessionContainer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public JID getResourceQualifiedJID(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        User user = participants.get(jid);

        if (user == null)
            return null;

        return user.getJID();
    }

    @Override
    public User getLocalUser() {
        return localUser;
    }

    @Override
    public ConcurrentDocumentClient getConcurrentDocumentClient() {
        return concurrentDocumentClient;
    }

    @Override
    public ConcurrentDocumentServer getConcurrentDocumentServer() {
        if (!isHost())
            throw new IllegalStateException(
                "the session is running in client mode");

        return concurrentDocumentServer;
    }

    @Override
    public Saros getSaros() {
        return saros;
    }

    /**
     * @JTourBusStop 7, Activity sending, Incoming activities:
     * 
     *               The ActivitySequencer will call this function for new
     *               activities, they are transformed again, forwarded and put
     *               into the queue of the activity dispatcher.
     * 
     */

    @Override
    public void exec(List<IActivityDataObject> activityDataObjects) {
        List<IActivityDataObject> dataObjects = activityQueuer
            .process(activityDataObjects);
        List<IActivity> activities = convertActivities(dataObjects);
        activityHandler.handleIncomingActivities(activities);
    }

    private List<IActivity> convertActivities(
        List<IActivityDataObject> activityDataObjects) {
        List<IActivity> result = new ArrayList<IActivity>(
            activityDataObjects.size());

        for (IActivityDataObject dataObject : activityDataObjects) {
            try {
                result.add(dataObject.getActivity(this));
            } catch (IllegalArgumentException e) {
                log.warn("DataObject could not be attached to SarosSession: " //$NON-NLS-1$
                    + dataObject, e);
            }
        }

        return result;
    }

    /**
     * TODO: Methods calling this function need to become an IActivityProvider
     * and use the {@link AbstractActivityProvider#fireActivity} method to send
     * their activities.
     * 
     * @param activity
     */
    @Deprecated
    private void abuseActivityCreated(IActivity activity) {
        handleActivityCreated(activity);
    }

    private void handleActivityCreated(IActivity activity) {

        if (activity == null)
            throw new NullPointerException("activity is null");

        activityHandler.handleOutgoingActivities(Collections
            .singletonList(activity));
    }

    /**
     * Convenience method to address a single recipient.
     * 
     * @see #sendActivity(List, IActivity)
     */
    @Override
    public void sendActivity(User recipient, IActivity activity) {
        sendActivity(Collections.singletonList(recipient), activity);
    }

    private void sendActivity(List<User> toWhom, final IActivity activity) {
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

        if (activity instanceof IResourceActivity
            && (activity instanceof TextSelectionActivity
                || activity instanceof ViewportActivity || activity instanceof JupiterActivity)) {
            IResourceActivity resActivity = (IResourceActivity) activity;
            if (!needBasedPathsList.contains(resActivity.getPath())
                && !isShared(resActivity.getPath().getResource()))
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
            getSequencer().sendActivity(toWhom,
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

        if (!(activity instanceof IResourceActivity))
            return false;

        SPath path = ((IResourceActivity) activity).getPath();
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

        /*
         * quick adding when file is not shared
         */
        if (!isShared(path.getFile())) {
            if (preferenceUtils.isNeedsBasedSyncEnabled().equals("undefined")) {
                if (!CollaborationUtils.activateNeedBasedSynchronization(saros))
                    return;
            }

            needBasedPathsList.add(path);

            try {
                sendSingleFile(path);
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

            // GUI code in the business logic is a real WTF !
            SWTUtils.runSafeSWTSync(log, new Runnable() {
                @Override
                public void run() {
                    try {
                        dialog.run(true, true, new IRunnableWithProgress() {
                            @Override
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

    /*
     * FIXME: this needs a serious code review as Saros is not a single threaded
     * application
     */
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

    @Override
    public void addActivityProvider(IActivityProvider provider) {
        if (activityProviders.addIfAbsent(provider))
            provider.addActivityListener(this.activityListener);
    }

    @Override
    public void removeActivityProvider(IActivityProvider provider) {
        activityProviders.remove(provider);
        provider.removeActivityListener(this.activityListener);
    }

    @Override
    public DateTime getSessionStart() {
        return sessionStart;
    }

    @Override
    public boolean isShared(IResource resource) {
        return projectMapper.isShared(resource);
    }

    @Override
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
                    ((IContainer) iResource).members(), IResource.class,
                    Platform.getAdapterManager());
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

    @Override
    public boolean useVersionControl() {
        /*
         * It is not possible to enable version control support during a
         * session.
         */
        if (!useVersionControl)
            return false;
        return useVersionControl = preferenceUtils.useVersionControl();
    }

    @Override
    public SharedProject getSharedProject(IProject project) {
        if (!isShared(project))
            return null;
        return projectMapper.getSharedProject(projectMapper.getID(project));
    }

    @Override
    public List<SharedProject> getSharedProjects() {
        return projectMapper.getSharedProjects();
    }

    @Override
    public String getProjectID(IProject project) {
        return projectMapper.getID(project);
    }

    @Override
    public IProject getProject(String projectID) {
        return projectMapper.getProject(projectID);
    }

    // TODO this needs a major refactor
    private void synchronizeUserList() {

        final IProgressMonitor monitor = new NullProgressMonitor();

        log.debug("synchronizing user list");

        Collection<User> participants = getParticipants();

        SarosPacketCollector userListConfirmationCollector = transmitter
            .getUserListConfirmationCollector();

        PacketExtension userList = UserListExtension.PROVIDER
            .create(new UserListExtension(sessionIDObservable.getValue(),
                participants));

        List<User> remoteUsers = getRemoteUsers();

        if (remoteUsers.isEmpty())
            return;

        for (Iterator<User> it = remoteUsers.iterator(); it.hasNext();) {
            User user = it.next();
            try {
                transmitter.sendToSessionUser(user.getJID(), userList);
            } catch (IOException e) {
                log.error("could not send user list to session user " + user, e);
                it.remove();
            }
        }

        /*
         * the code below is NOT need IF sendToSessionUser will always use the
         * stream connection and not the chat !
         */

        // see BUG #3544930 , the confirmation is useless
        log.debug("waiting for confirmations");

        /*
         * FIXME use the direct connection as this will throw an IOException so
         * we can remove the user that is not responding
         */

        Utils.runSafeAsync("SynchronizeUserlistTimer", log, new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    // NOP
                } finally {
                    monitor.setCanceled(true);
                }

            }
        });

        try {
            transmitter.receiveUserListConfirmation(
                userListConfirmationCollector, remoteUsers, monitor);
        } catch (LocalCancellationException e) {
            // HACK for now
            throw new RuntimeException("synchronizing user list timed out");
        }

        log.debug("synchronized user list");
    }

    @Override
    public HashMap<IProject, List<IResource>> getProjectResourcesMapping() {
        return projectMapper.getProjectResourceMapping();
    }

    @Override
    public List<IResource> getSharedResources(IProject project) {
        return projectMapper.getProjectResourceMapping().get(project);
    }

    @Override
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

    @Override
    public void addProjectOwnership(String projectID, IProject project,
        JID ownerJID) {
        if (projectMapper.getSharedProject(projectID) == null) {
            projectMapper.addMapping(projectID, project, new SharedProject(
                project, this));
            projectMapper.addResourceMapping(project,
                new ArrayList<IResource>());
            projectMapper.addUserToProjectMapping(ownerJID, project);
        }
    }

    @Override
    public void removeProjectOwnership(String projectID, IProject project,
        JID ownerJID) {
        if (projectMapper.getSharedProject(projectID) != null) {
            projectMapper.removeResourceMapping(project);
            projectMapper.removeMapping(projectID);
            projectMapper.removeUserToProjectMapping(ownerJID, project);
        }
    }

    @Override
    public StopManager getStopManager() {
        return stopManager;
    }

    @Override
    public void changeColor(int colorID) {
        if (colorID < 0 || colorID >= MAX_USERCOLORS)
            throw new IllegalArgumentException("color id '" + colorID
                + "'  must be in range of 0 <= id < " + MAX_USERCOLORS);

        changeColorManager.changeColorID(colorID);
    }

    @Override
    public Set<Integer> getAvailableColors() {
        return changeColorManager.getAvailableColors();
    }

    @Override
    public void enableQueuing(String projectId) {
        activityQueuer.enableQueuing(projectId);
    }

    @Override
    public void disableQueuing() {
        activityQueuer.disableQueuing();
        // send us a dummy activity to ensure the queues get flushed
        sendActivity(localUser, new NOPActivity(localUser, localUser, 0));
    }

    private void initializeSessionContainer(ISarosContext context) {
        sessionContainer = context.createSimpleChildContainer();
        sessionContainer.addComponent(ISarosSession.class, this);
        sessionContainer.addComponent(StopManager.class);
        sessionContainer.addComponent(ActivitySequencer.class);

        // Concurrent Editing

        sessionContainer.addComponent(ConcurrentDocumentClient.class);
        /*
         * as Pico Container complains about null, just add the server even in
         * client mode as it will not matter because it is not accessed
         */
        sessionContainer.addComponent(ConcurrentDocumentServer.class);

        // Classes belonging to a session
        sessionContainer.addComponent(PingPongCentral.class);

        // Core Managers
        sessionContainer.addComponent(ChangeColorManager.class);
        sessionContainer.addComponent(SharedResourcesManager.class);
        sessionContainer.addComponent(ProjectsAddedManager.class);
        sessionContainer.addComponent(PermissionManager.class);
        sessionContainer.addComponent(PreferenceManager.class);

        // Statistic collectors. Make sure to add new collectors to the
        // StatisticCollectorTest as well
        sessionContainer.addComponent(StatisticManager.class);
        sessionContainer.addComponent(DataTransferCollector.class);
        sessionContainer.addComponent(PermissionChangeCollector.class);
        sessionContainer.addComponent(ParticipantCollector.class);
        sessionContainer.addComponent(SessionDataCollector.class);
        sessionContainer.addComponent(TextEditCollector.class);
        sessionContainer.addComponent(JumpFeatureUsageCollector.class);
        sessionContainer.addComponent(FollowModeCollector.class);
        sessionContainer.addComponent(SelectionCollector.class);
        sessionContainer.addComponent(VoIPCollector.class);

        // Feedback
        sessionContainer.addComponent(ErrorLogManager.class);
        sessionContainer.addComponent(FeedbackManager.class);

        // Handlers
        sessionContainer.addComponent(ConsistencyWatchdogHandler.class);
        sessionContainer.addComponent(ActivityHandler.class);
        sessionContainer.addComponent(activityCallback);

        // Force the creation of the above components.
        sessionContainer.getComponents();

        concurrentDocumentServer = sessionContainer
            .getComponent(ConcurrentDocumentServer.class);

        concurrentDocumentClient = sessionContainer
            .getComponent(ConcurrentDocumentClient.class);

        activityHandler = sessionContainer.getComponent(ActivityHandler.class);

        stopManager = sessionContainer.getComponent(StopManager.class);

        changeColorManager = sessionContainer
            .getComponent(ChangeColorManager.class);

        activitySequencer = sessionContainer
            .getComponent(ActivitySequencer.class);

        // ensure that the container uses caching
        assert sessionContainer.getComponent(ActivityHandler.class) == sessionContainer
            .getComponent(ActivityHandler.class) : "container is wrongly configurated - no cache support";
    }

    /**
     * This method is only meant to be used by a unit tests to verify the
     * cleanup of activity providers.
     * 
     * @return the size of the internal activity providers collection
     */
    public int getActivityProviderCount() {
        return activityProviders.size();
    }
}
