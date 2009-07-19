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
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.picocontainer.Disposable;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.FileActivity;
import de.fu_berlin.inf.dpp.activities.FolderActivity;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.RoleActivity;
import de.fu_berlin.inf.dpp.activities.TextEditActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.JupiterActivity;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager.Side;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.internal.OutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.project.IActivityProvider;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.synchronize.Blockable;
import de.fu_berlin.inf.dpp.synchronize.StartHandle;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * TODO Review if SharedProject, ConcurrentDocumentManager, ActivitySequencer
 * all honor start() and stop() semantics.
 */
public class SharedProject implements ISharedProject, Disposable {

    public static Logger log = Logger.getLogger(SharedProject.class.getName());

    public static final int MAX_USERCOLORS = 5;

    /* Dependencies */
    protected Saros saros;

    protected ITransmitter transmitter;

    protected ActivitySequencer activitySequencer;

    protected ConcurrentDocumentManager concurrentDocumentManager;

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

    protected Blockable stopManagerListener = new Blockable() {

        public void unblock() {
            if (isDriver())
                setProjectReadonly(false);
        }

        public void block() {
            setProjectReadonly(true);
            // TODO Setting readonly possibly confuses the consistency watchdog
        }
    };

    protected SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager, IProject project,
        StopManager stopManager) {

        assert (transmitter != null);

        this.saros = saros;
        this.transmitter = transmitter;
        this.project = project;
        this.transferManager = transferManager;
        this.activitySequencer = new ActivitySequencer(this, transmitter,
            transferManager);
        this.stopManager = stopManager;
        stopManager.addBlockable(stopManagerListener);
    }

    /**
     * Constructor called for SharedProject of the host
     */
    public SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager, IProject project, JID myID,
        StopManager stopManager) {

        this(saros, transmitter, transferManager, project, stopManager);
        assert (myID != null);

        this.freeColors = new FreeColors(MAX_USERCOLORS - 1);

        this.localUser = new User(this, myID, 0);
        localUser.setUserRole(UserRole.DRIVER);
        this.host = localUser;

        this.participants.put(this.host.getJID(), this.host);

        /* add host to driver list. */
        this.concurrentDocumentManager = new ConcurrentDocumentManager(
            Side.HOST_SIDE, this.host, myID, this, activitySequencer);

        setProjectReadonly(false);
    }

    /**
     * Constructor of client
     */
    public SharedProject(Saros saros, ITransmitter transmitter,
        DataTransferManager transferManager, IProject project, JID myID,
        JID hostID, int myColorID, StopManager stopManager) {

        this(saros, transmitter, transferManager, project, stopManager);

        this.host = new User(this, hostID, 0);
        this.host.setUserRole(UserRole.DRIVER);

        this.localUser = new User(this, myID, myColorID);

        this.participants.put(hostID, host);
        this.participants.put(myID, localUser);

        this.concurrentDocumentManager = new ConcurrentDocumentManager(
            Side.CLIENT_SIDE, this.host, myID, this, activitySequencer);
    }

    public Collection<User> getParticipants() {
        return this.participants.values();
    }

    public List<User> getRemoteUsers() {
        Collection<User> users = getParticipants();
        List<User> result = new ArrayList<User>(users.size() - 1);
        for (User user : users) {
            if (user.isRemote()) {
                result.add(user);
            }
        }
        return result;
    }

    public ActivitySequencer getSequencer() {
        return this.activitySequencer;
    }

    /**
     * {@inheritDoc}
     */
    public void initiateRoleChange(final User user, final UserRole newRole) {
        assert localUser.isHost() : "Only the host can initiate role changes";

        // TODO open a progress dialog for offering the possibility to cancel
        final SubMonitor progress = SubMonitor
            .convert(new NullProgressMonitor());

        if (user.isHost()) {
            activityCreated(new RoleActivity(
                getLocalUser().getJID().toString(), user.getJID().toString(),
                newRole));

            setUserRole(user, newRole);
        } else {

            Util.runSafeAsync(log, new Runnable() {
                public void run() {
                    try {
                        progress.beginTask("Performing role change",
                            IProgressMonitor.UNKNOWN);
                        StartHandle startHandle = stopManager.stop(user,
                            "Performing role change", progress);

                        activityCreated(new RoleActivity(getLocalUser()
                            .getJID().toString(), user.getJID().toString(),
                            newRole));

                        setUserRole(user, newRole);

                        if (!startHandle.start())
                            log
                                .warn("Didn't unblock. There still exist unstarted StartHandles.");
                    } catch (CancellationException e) {
                        log
                            .warn("Role change failed because user canceled the role change");
                    } catch (InterruptedException e) {
                        log
                            .warn("Role change failed because of an InterruptedException");
                    } finally {
                        progress.done();
                    }
                }
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setUserRole(final User user, final UserRole role) {

        assert user != null;
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                user.setUserRole(role);

                log.info("User " + user + " is now a " + role);
                if (user.isLocal()) {
                    setProjectReadonly(user.isObserver());
                }

                for (ISharedProjectListener listener : SharedProject.this.listeners) {
                    listener.roleChanged(user);
                }
            }
        });
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
        return this.host.equals(localUser);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean isDriver() {
        return localUser.isDriver();
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
        if (this.participants.containsKey(jid)) {
            log.warn("User " + jid + " added twice to SharedProject");
        }
        participants.putIfAbsent(jid, user);
        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user);
        }
        SharedProject.log.info("User " + jid + " joined session");
    }

    public void removeUser(User user) {
        JID jid = user.getJID();
        if (this.participants.remove(jid) == null) {
            log
                .warn("Tried to remove user who was not in participants: "
                    + jid);
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

        SharedProject.log.info("User " + jid + " left session");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IOutgoingInvitationProcess invite(JID jid, String description,
        boolean inactive, IInvitationUI inviteUI, FileList filelist,
        SubMonitor monitor) {

        return new OutgoingInvitationProcess(saros, transmitter,
            transferManager, jid, this, description, inactive, inviteUI,
            getFreeColor(), filelist, monitor);
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
     * Stops the associated activity sequencer.
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
        concurrentDocumentManager.dispose();
    }

    /**
     * {@inheritDoc}
     * 
     * @deprecated Use {@link #getUser(JID)} and
     *             {@link #getResourceQualifiedJID(JID)} instead.
     */
    @Deprecated
    public User getParticipant(JID jid) {

        if (jid == null)
            throw new IllegalArgumentException();

        if (jid.isBareJID()) {
            log.error(
                "JIDs used for the SharedProject should always be resource qualified: "
                    + jid, new StackTrace());
        }

        User user = this.participants.get(jid);
        if (user == null)
            return null;

        if (!user.getJID().strictlyEquals(jid)) {
            log.error("getParticipant is deprecated and wrongly used:"
                + " The given JID has a resource qualifier not found "
                + "in the shared project");
        }

        return user;
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
                    + jid);
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

    public void setProjectReadonly(final boolean readonly) {
        /* TODO run project read only settings in progress monitor thread. */
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                FileUtil.setReadOnly(getProject(), readonly);
            }
        });
    }

    public ConcurrentDocumentManager getConcurrentDocumentManager() {
        return concurrentDocumentManager;
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

    public void execTransformedActivity(TextEditActivity activity) {

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

        assert Util.isSWT();

        try {
            log.debug("Executing   transformed activity: " + activity);

            for (IActivityProvider exec : this.activityProviders) {
                exec.exec(activity);
            }

            /*
             * FIXME The following will send the activities to everybody, so all
             * drivers will receive the message twice (once through Jupiter once
             * as a Activity)
             */

            // Send activity to every remote user.
            if (this.concurrentDocumentManager.isHostSide()) {
                activitySequencer.sendActivity(getRemoteUsers(), activity);
            }
        } catch (Exception e) {
            log.error("Error while executing activity.", e);
        }
    }

    public void exec(final IActivity activity) {

        /*
         * TODO Replace this with a single call to the ConcurrentDocumentManager
         * and use the ActivityReceiver to handle all cases.
         * 
         * TODO Check all this for problems with concurrency.
         * 
         * TODO Idea for a changed API: Receive a list of activities and call
         * the ConcurrentDocumentManager to produce lists of activities to
         * execute locally and one to send to remote users.
         */
        try {
            if (activity instanceof EditorActivity) {
                this.concurrentDocumentManager.execEditorActivity(activity);
            }
            if (activity instanceof FileActivity) {
                this.concurrentDocumentManager.execFileActivity(activity);
            }
            if (activity instanceof FolderActivity) {
                // TODO [FileOps] Does not handle FolderActivity
            }
            if (activity instanceof JupiterActivity) {
                this.concurrentDocumentManager
                    .receiveJupiterActivity((JupiterActivity) activity);
                return;
            }
        } catch (Exception e) {
            log.error("Error while executing activity.", e);
        }
        /*
         * TODO Maybe this one Runnable per activity handed over to the SWT
         * thread is one reason why the characters appear so slowly.
         */
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {

                if (activity instanceof TextEditActivity) {
                    if (concurrentDocumentManager.isHostSide()
                        || concurrentDocumentManager
                            .isManagedByJupiter(activity)) {
                        return;
                    }
                }

                // Execute all other activities
                for (IActivityProvider executor : activityProviders) {
                    executor.exec(activity);
                }
            }
        });
    }

    public void activityCreated(IActivity activity) {

        if (activity == null)
            throw new IllegalArgumentException("Activity cannot be null");

        /* Let ConcurrentDocumentManager have a look at the activities first */
        boolean consumed = this.concurrentDocumentManager
            .activityCreated(activity);

        if (!consumed) {
            activitySequencer.sendActivity(getRemoteUsers(), activity);
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
}
