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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.SubMonitor;
import org.joda.time.DateTime;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.business.IActivity;
import de.fu_berlin.inf.dpp.activities.business.RoleActivity;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.management.TransformationResult;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.project.SarosProjectMapper;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * TODO Review if SharedProject, ConcurrentDocumentManager, ActivitySequencer
 * all honor start() and stop() semantics.
 */
public class SharedProject implements ISharedProject, Disposable {

    private static final Logger log = Logger.getLogger(SharedProject.class);

    public static final int MAX_USERCOLORS = 5;

    /* Dependencies */
    protected Saros saros;

    protected ITransmitter transmitter;

    protected ActivitySequencer activitySequencer;

    protected ConcurrentDocumentClient concurrentDocumentClient;

    protected ConcurrentDocumentServer concurrentDocumentServer;

    protected final List<IActivityProvider> activityProviders = new LinkedList<IActivityProvider>();

    protected DataTransferManager transferManager;

    protected StopManager stopManager;

    /* Instance fields */
    protected User localUser;

    protected ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    protected SharedProjectListenerDispatch listenerDispatch = new SharedProjectListenerDispatch();

    protected User host;

    protected FreeColors freeColors = null;

    protected DateTime sessionStart;

    protected SarosProjectMapper projectMapper = new SarosProjectMapper();

    protected final boolean useVersionControl;

    protected Blockable stopManagerListener = new Blockable() {

        public void unblock() {
            // TODO see #block()
        }

        public void block() {
            // TODO find a way to effectively block the user from doing anything
        }
    };

    public static class QueueItem {

        public final List<User> recipients;
        public final IActivity activity;

        public QueueItem(List<User> recipients, IActivity activity) {
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
    protected SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager,
        DispatchThreadContext threadContext, String projectID,
        IProject project, StopManager stopManager, JID myJID, int myColorID,
        DateTime sessionStart, boolean useVersionControl) {

        assert transmitter != null;
        assert myJID != null;

        this.saros = saros;
        this.transmitter = transmitter;
        this.projectMapper.addMapping(projectID, project);
        this.transferManager = transferManager;
        this.stopManager = stopManager;
        this.sessionStart = sessionStart;

        this.localUser = new User(this, myJID, myColorID);
        this.activitySequencer = new ActivitySequencer(this, transmitter,
            transferManager, threadContext);
        this.useVersionControl = useVersionControl;

        stopManager.addBlockable(stopManagerListener);
    }

    /**
     * Constructor called for SharedProject of the host
     */
    public SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager,
        DispatchThreadContext threadContext, IProject project, JID myID,
        StopManager stopManager, DateTime sessionStart,
        boolean useVersionControl) {

        this(saros, transmitter, transferManager, threadContext, project
            .getName(), project, stopManager, myID, 0, sessionStart,
            useVersionControl);

        this.freeColors = new FreeColors(MAX_USERCOLORS - 1);
        this.localUser.setUserRole(UserRole.DRIVER);
        this.host = localUser;
        this.host.invitationCompleted();

        this.participants.put(this.host.getJID(), this.host);

        /* add host to driver list. */
        this.concurrentDocumentServer = new ConcurrentDocumentServer(this);
        this.concurrentDocumentClient = new ConcurrentDocumentClient(this);
    }

    /**
     * Constructor of client
     */
    public SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager,
        DispatchThreadContext threadContext, String projectID,
        IProject project, JID myID, JID hostID, int myColorID,
        StopManager stopManager, DateTime sessionStart) {

        this(saros, transmitter, transferManager, threadContext, projectID,
            project, stopManager, myID, myColorID, sessionStart, true);

        this.host = new User(this, hostID, 0);
        this.host.invitationCompleted();
        this.host.setUserRole(UserRole.DRIVER);

        this.participants.put(hostID, host);
        this.participants.put(myID, localUser);

        this.concurrentDocumentClient = new ConcurrentDocumentClient(this);
    }

    public Collection<User> getParticipants() {
        return this.participants.values();
    }

    public List<User> getRemoteObservers() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isLocal())
                continue;
            if (user.isObserver())
                result.add(user);
        }
        return result;
    }

    public List<User> getObservers() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isObserver())
                result.add(user);
        }
        return result;
    }

    public List<User> getDrivers() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isDriver())
                result.add(user);
        }
        return result;
    }

    public List<User> getRemoteDrivers() {
        List<User> result = new ArrayList<User>();
        for (User user : getParticipants()) {
            if (user.isLocal())
                continue;
            if (user.isDriver())
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
        return this.activitySequencer;
    }

    /**
     * {@inheritDoc}
     */
    public void initiateRoleChange(final User user, final UserRole newRole,
        SubMonitor progress) throws CancellationException, InterruptedException {

        if (!localUser.isHost()) {
            throw new IllegalArgumentException(
                "Only the host can initiate role changes.");
        }

        if (user.isHost()) {

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    activityCreated(new RoleActivity(getLocalUser(), user,
                        newRole));

                    setUserRole(user, newRole);
                }
            });

        } else {
            StartHandle startHandle = stopManager.stop(user,
                "Performing role change", progress);

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    activityCreated(new RoleActivity(getLocalUser(), user,
                        newRole));

                    setUserRole(user, newRole);
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
    public void setUserRole(final User user, final UserRole role) {

        assert Util.isSWT() : "Must be called from SWT Thread";

        if (user == null || role == null)
            throw new IllegalArgumentException();

        user.setUserRole(role);

        log.info("User " + user + " is now a " + role);

        this.listenerDispatch.roleChanged(user);
    }

    /**
     * {@inheritDoc}
     */
    public void userInvitationCompleted(final User user) {
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                userInvitationCompletedWrapped(user);
            }
        });
    }

    public void userInvitationCompletedWrapped(final User user) {

        assert Util.isSWT() : "Must be called from SWT Thread";

        if (user == null)
            throw new IllegalArgumentException();

        user.invitationCompleted();

        log.debug("The invitation of " + Util.prefix(user.getJID())
            + " is now complete");

        listenerDispatch.invitationCompleted(user);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public User getHost() {
        return this.host;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public boolean isHost() {
        return this.localUser.isHost();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean isDriver() {
        return this.localUser.isDriver();
    }

    public boolean isExclusiveDriver() {
        if (!isDriver()) {
            return false;
        }
        for (User user : getParticipants()) {
            if (user.isRemote() && user.isDriver()) {
                return false;
            }
        }
        return true;
    }

    public void addUser(User user) {

        assert user.getSharedProject().equals(this);

        JID jid = user.getJID();

        if (participants.putIfAbsent(jid, user) != null) {
            log.error("User " + Util.prefix(jid)
                + " added twice to SharedProject", new StackTrace());
            throw new IllegalArgumentException();
        }

        listenerDispatch.userJoined(user);

        log.info("User " + Util.prefix(jid) + " joined session");
    }

    public void removeUser(User user) {
        JID jid = user.getJID();
        if (this.participants.remove(jid) == null) {
            log.warn("Tried to remove user who was not in participants: "
                + Util.prefix(jid));
            return;
        }
        if (isHost()) {
            returnColor(user.getColorID());
        }

        this.activitySequencer.userLeft(jid);

        // TODO what is to do here if no driver exists anymore?
        listenerDispatch.userLeft(user);

        log.info("User " + Util.prefix(jid) + " left session");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void addListener(ISharedProjectListener listener) {
        listenerDispatch.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void removeListener(ISharedProjectListener listener) {
        listenerDispatch.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public Set<IProject> getProjects() {
        return this.projectMapper.getProjects();
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
        stopManager.removeBlockable(stopManagerListener);

        if (concurrentDocumentServer != null) {
            concurrentDocumentServer.dispose();
        }
        concurrentDocumentClient.dispose();
    }

    /**
     * {@inheritDoc}
     */
    public User getUser(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        if (jid.isBareJID()) {
            throw new IllegalArgumentException(
                "JIDs used for the SharedProject should always be resource qualified: "
                    + Util.prefix(jid));
        }

        User user = this.participants.get(jid);

        if (user == null || !user.getJID().strictlyEquals(jid))
            return null;

        return user;
    }

    /**
     * Given a JID (with resource or not), will return the resource qualified
     * JID associated with this user or null if no user for the given JID exists
     * in this SharedProject.
     */
    public JID getResourceQualifiedJID(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        User user = this.participants.get(jid);

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

    public ITransmitter getTransmitter() {
        return transmitter;
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
        List<IActivity> activities = convert(activityDataObjects);

        if (isHost()) {
            TransformationResult transformed = concurrentDocumentServer
                .transformIncoming(activities);

            activities = transformed.getLocalActivities();

            for (QueueItem item : transformed.getSendToPeers()) {
                sendActivity(item.recipients, item.activity);
            }
        }

        final List<IActivity> stillToExecute = activities;

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {

                TransformationResult transformed = concurrentDocumentClient
                    .transformIncoming(stillToExecute);

                for (QueueItem item : transformed.getSendToPeers()) {
                    sendActivity(item.recipients, item.activity);
                }

                for (IActivity activityDataObject : transformed.executeLocally) {
                    for (IActivityProvider executor : activityProviders) {
                        executor.exec(activityDataObject);
                    }
                }
            }
        });
    }

    private List<IActivity> convert(
        List<IActivityDataObject> activityDataObjects) {

        List<IActivity> result = new ArrayList<IActivity>(
            activityDataObjects.size());

        for (IActivityDataObject dataObject : activityDataObjects) {
            try {
                result.add(dataObject.getActivity(this));
            } catch (IllegalArgumentException e) {
                log.warn("DataObject could not be attached to SharedProject: "
                    + dataObject, e);
            }
        }

        return result;
    }

    public void activityCreated(IActivity activity) {

        assert Util.isSWT() : "Must be called from the SWT Thread";

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

        /*
         * Let ConcurrentDocumentManager have a look at the activityDataObjects
         * first
         */
        List<QueueItem> toSend = this.concurrentDocumentClient
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
    public void sendActivity(User recipient, IActivity activityDataObject) {
        sendActivity(Collections.singletonList(recipient), activityDataObject);
    }

    public void sendActivity(List<User> toWhom, final IActivity activity) {
        if (toWhom == null)
            throw new IllegalArgumentException();

        if (activity == null)
            throw new IllegalArgumentException();

        try {
            activitySequencer.sendActivity(toWhom,
                activity.getActivityDataObject(this));
        } catch (IllegalArgumentException e) {
            log.warn("Could not convert Activity to DataObject: ", e);
        }
    }

    public void addActivityProvider(IActivityProvider provider) {
        if (!activityProviders.contains(provider)) {
            this.activityProviders.add(provider);
            provider.addActivityListener(this);
        }
    }

    public void removeActivityProvider(IActivityProvider provider) {
        this.activityProviders.remove(provider);
        provider.removeActivityListener(this);
    }

    public DateTime getSessionStart() {
        return sessionStart;
    }

    public boolean isShared(IProject project) {
        return projectMapper.isShared(project);
    }

    public SarosProjectMapper getProjectMapper() {
        return projectMapper;
    }

    public boolean useVersionControl() {
        return this.useVersionControl;
    }
}
