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
import de.fu_berlin.inf.dpp.activities.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.RoleActivityDataObject;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentClient;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentServer;
import de.fu_berlin.inf.dpp.concurrent.management.TransformationResult;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer.QueueItem;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
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

    protected IProject project;

    protected StopManager stopManager;

    /* Instance fields */
    protected User localUser;

    protected ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    protected List<ISharedProjectListener> listeners = new ArrayList<ISharedProjectListener>();

    protected User host;

    protected FreeColors freeColors = null;

    protected DateTime sessionStart;

    protected Blockable stopManagerListener = new Blockable() {

        public void unblock() {
            // TODO see #block()
        }

        public void block() {
            // TODO find a way to effectively block the user from doing anything
        }
    };

    /**
     * Common constructor code for host and client side.
     */
    protected SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager,
        DispatchThreadContext threadContext, IProject project,
        StopManager stopManager, JID myJID, int myColorID, DateTime sessionStart) {

        assert transmitter != null;
        assert myJID != null;

        this.saros = saros;
        this.transmitter = transmitter;
        this.project = project;
        this.transferManager = transferManager;
        this.stopManager = stopManager;
        this.sessionStart = sessionStart;

        this.localUser = new User(this, myJID, myColorID);
        this.activitySequencer = new ActivitySequencer(this, transmitter,
            transferManager, threadContext);

        stopManager.addBlockable(stopManagerListener);
    }

    /**
     * Constructor called for SharedProject of the host
     */
    public SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager,
        DispatchThreadContext threadContext, IProject project, JID myID,
        StopManager stopManager, DateTime sessionStart) {

        this(saros, transmitter, transferManager, threadContext, project,
            stopManager, myID, 0, sessionStart);

        this.freeColors = new FreeColors(MAX_USERCOLORS - 1);
        this.localUser.setUserRole(UserRole.DRIVER);
        this.host = localUser;

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
        DispatchThreadContext threadContext, IProject project, JID myID,
        JID hostID, int myColorID, StopManager stopManager,
        DateTime sessionStart) {

        this(saros, transmitter, transferManager, threadContext, project,
            stopManager, myID, myColorID, sessionStart);

        this.host = new User(this, hostID, 0);
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
                    activityCreated(new RoleActivityDataObject(getLocalUser().getJID(),
                        user.getJID(), newRole));

                    setUserRole(user, newRole);
                }
            });

        } else {
            StartHandle startHandle = stopManager.stop(user,
                "Performing role change", progress);

            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    activityCreated(new RoleActivityDataObject(getLocalUser().getJID(),
                        user.getJID(), newRole));

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

        for (ISharedProjectListener listener : SharedProject.this.listeners) {
            listener.roleChanged(user);
        }
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

        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user);
        }

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

        for (ISharedProjectListener listener : this.listeners) {
            listener.userLeft(user);
        }

        log.info("User " + Util.prefix(jid) + " left session");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void addListener(ISharedProjectListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void removeListener(ISharedProjectListener listener) {
        this.listeners.remove(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IProject getProject() {
        return this.project;
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

        if (isHost()) {
            TransformationResult transformed = concurrentDocumentServer
                .transformIncoming(activityDataObjects);

            activityDataObjects = transformed.getLocalActivities();

            for (QueueItem item : transformed.getSendToPeers()) {
                sendActivity(item.recipients, item.activityDataObject);
            }
        }

        final List<IActivityDataObject> stillToExecute = activityDataObjects;

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {

                TransformationResult transformed = concurrentDocumentClient
                    .transformIncoming(stillToExecute);

                for (QueueItem item : transformed.getSendToPeers()) {
                    sendActivity(item.recipients, item.activityDataObject);
                }

                for (IActivityDataObject activityDataObject : transformed.executeLocally) {
                    for (IActivityProvider executor : activityProviders) {
                        executor.exec(activityDataObject);
                    }
                }
            }
        });
    }

    public void activityCreated(IActivityDataObject activityDataObject) {

        assert Util.isSWT() : "Must be called from the SWT Thread";

        if (activityDataObject == null)
            throw new IllegalArgumentException("Activity cannot be null");

        /* Let ConcurrentDocumentManager have a look at the activityDataObjects first */
        List<QueueItem> toSend = this.concurrentDocumentClient
            .transformOutgoing(activityDataObject);

        for (QueueItem item : toSend) {
            sendActivity(item.recipients, item.activityDataObject);
        }
    }

    /**
     * Convenience method to address a single recipient.
     * 
     * @see #sendActivity(List, IActivityDataObject)
     */
    public void sendActivity(User recipient, IActivityDataObject activityDataObject) {
        sendActivity(Collections.singletonList(recipient), activityDataObject);
    }

    public void sendActivity(List<User> toWhom, final IActivityDataObject activityDataObject) {
        if (toWhom == null)
            throw new IllegalArgumentException();

        if (activityDataObject == null)
            throw new IllegalArgumentException();

        activitySequencer.sendActivity(toWhom, activityDataObject);
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
}
