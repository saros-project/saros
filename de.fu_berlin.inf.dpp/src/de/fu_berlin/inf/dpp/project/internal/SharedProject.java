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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.UserRole;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.concurrent.jupiter.Request;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager;
import de.fu_berlin.inf.dpp.concurrent.management.ConcurrentDocumentManager.Side;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IOutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.IInvitationProcess.IInvitationUI;
import de.fu_berlin.inf.dpp.invitation.internal.OutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.ActivitySequencer;
import de.fu_berlin.inf.dpp.project.IActivityManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
import de.fu_berlin.inf.dpp.ui.InvitationDialog;
import de.fu_berlin.inf.dpp.util.FileUtil;
import de.fu_berlin.inf.dpp.util.Pair;
import de.fu_berlin.inf.dpp.util.Util;

public class SharedProject implements ISharedProject {
    private static Logger log = Logger.getLogger(SharedProject.class.getName());

    private static final int REQUEST_ACTIVITY_ON_AGE = 5;
    protected static final int MILLIS_UPDATE = 1000;

    protected JID myID;

    protected ConcurrentHashMap<JID, User> participants = new ConcurrentHashMap<JID, User>();

    private final IProject project;

    private final List<ISharedProjectListener> listeners = new ArrayList<ISharedProjectListener>();

    private User host;

    private final ITransmitter transmitter;

    private final ActivitySequencer activitySequencer = new ActivitySequencer();

    private static final int MAX_USERCOLORS = 5;
    private FreeColors freeColors = null;

    /**
     * Constructor called for SharedProject of the host
     */
    public SharedProject(ITransmitter transmitter, IProject project, JID myID) {
        assert (transmitter != null && myID != null);

        this.transmitter = transmitter;

        this.freeColors = new FreeColors(MAX_USERCOLORS - 1);

        this.myID = myID;
        User user = new User(myID, 0);
        user.setUserRole(UserRole.DRIVER);
        this.host = user;

        this.participants.put(this.host.getJID(), this.host);

        /* add host to driver list. */
        this.activitySequencer.initConcurrentManager(Side.HOST_SIDE, this.host,
            myID, this);

        this.project = project;
        setProjectReadonly(false);
    }

    /**
     * Constructor of client
     */
    public SharedProject(ITransmitter transmitter, IProject project, JID myID,
        JID hostID, int myColorID) {

        this.transmitter = transmitter;

        this.myID = myID;

        User host = new User(hostID, 0);
        host.setUserRole(UserRole.DRIVER);
        this.participants.put(hostID, host);
        this.participants.put(myID, new User(myID, myColorID));

        this.host = getParticipant(hostID);

        this.activitySequencer.initConcurrentManager(Side.CLIENT_SIDE,
            this.host, myID, this);

        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public Collection<User> getParticipants() {
        return this.participants.values();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public ActivitySequencer getSequencer() {
        return this.activitySequencer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IActivityManager getActivityManager() {
        return this.activitySequencer;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public void setUserRole(User user, UserRole role, boolean replicated) {

        assert user != null;

        user.setUserRole(role);
        if (user.equals(getParticipant(this.myID))) {
            setProjectReadonly(user.isObserver());
        }

        for (ISharedProjectListener listener : this.listeners) {
            listener.roleChanged(user, replicated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.ISharedProject
     */
    public boolean isDriver() {
        return getParticipant(this.myID).isDriver();
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
        return this.host.getJID().equals(this.myID);
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject#exclusiveDriver()
     */
    public boolean isExclusiveDriver() {
        if (!isDriver()) {
            return false;
        } else {
            for (User user : participants.values()) {
                if (user.equals(Saros.getDefault().getLocalUser()))
                    continue;
                else if (user.isDriver())
                    return false;
            }
            return true;
        }
    }

    public void addUser(User user) {

        if (this.participants.containsKey(user.getJID())) {
            log.warn("User " + user.getJID() + " added twice to SharedProject");
        }
        participants.putIfAbsent(user.getJID(), user);
        for (ISharedProjectListener listener : this.listeners) {
            listener.userJoined(user.getJID());
        }
        SharedProject.log.info("User " + user.getJID() + " joined session");
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
            listener.userLeft(jid);
        }

        SharedProject.log.info("User " + jid + " left session");
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public IOutgoingInvitationProcess invite(JID jid, String description,
        boolean inactive, IInvitationUI inviteUI) {
        return new OutgoingInvitationProcess(this.transmitter, jid, this,
            description, inactive, inviteUI, getFreeColor());
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

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public FileList getFileList() throws CoreException {
        return new FileList(this.project);
    }

    public Timer flushTimer = new Timer(true);
    public Thread requestTransmitter = null;
    private static int queuedsince = 0;

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void start() {

        this.flushTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                List<IActivity> activities = activitySequencer.flush();

                if (activities.size() > 0 && participants.size() > 1) {
                    // TODO Rather create a method in the activitySequencer to
                    // wrap and then send.
                    transmitter.sendActivities(SharedProject.this,
                        activitySequencer, activities);
                }

                // TODO CO 2009-02-06 this is disabled internally. Why?

                // missing activities? (can not execute all)
                if (activitySequencer.getQueuedActivitiesSize() > 0) {
                    SharedProject.queuedsince++;

                    // if i am missing activities for REQUEST_ACTIVITY_ON_AGE
                    // seconds, ask all (because I do not know the origin)
                    // to send it to me again.
                    /*
                     * TODO SharedProject.queuedsince is a "global" counter for
                     * all other users but there is a queue for each user now.
                     */
                    if (SharedProject.queuedsince >= SharedProject.REQUEST_ACTIVITY_ON_AGE) {

                        SharedProject.this.transmitter.sendRequestForActivity(
                            SharedProject.this,
                            SharedProject.this.activitySequencer
                                .getExpectedSequenceNumbers(), false);

                        SharedProject.queuedsince = 0;

                        // TODO: What if Request for Activity fails again and
                        // again?
                    }

                } else {
                    SharedProject.queuedsince = 0;
                }
            }
        }, 0, SharedProject.MILLIS_UPDATE);

        stopped = false;

        /* 2. start thread for sending jupiter requests. */
        this.requestTransmitter = new Thread(Util.wrapSafe(log, new Runnable() {
            /**
             * @review runSafe OK
             */
            public void run() {
                while (!stopped && !Thread.interrupted()) {
                    Pair<JID, Request> toSend;
                    try {
                        toSend = activitySequencer.getNextOutgoingRequest();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    transmitter.sendJupiterRequest(SharedProject.this,
                        toSend.v, toSend.p);
                }
            }
        }));
        this.requestTransmitter.start();
    }

    // TODO Review sendRequest for InterruptedException and remove this flag.
    boolean stopped;

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public void stop() {
        this.flushTimer.cancel();
        this.requestTransmitter.interrupt();
        stopped = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.fu_berlin.inf.dpp.project.ISharedProject
     */
    public User getParticipant(JID jid) {
        return this.participants.get(jid);
    }

    public User getADriver() {
        for (User user : getParticipants()) {
            if (user.isDriver()) {
                return user;
            }
        }
        return null;
    }

    public void startInvitation(final @Nullable List<JID> toInvite) {

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {

                /*
                 * TODO Since we are going to invite people, we need to stop
                 * changing the project
                 */
                if (!EditorAPI.saveProject(getProject())) {
                    log.info("User canceled starting an invitation (as host)");
                    return;
                }

                // TODO check if anybody is online, empty dialog feels
                // strange
                Window iw = new InvitationDialog(EditorAPI.getShell(), toInvite);
                iw.open();
            }
        });

    }

    public void setProjectReadonly(final boolean readonly) {

        /*
         * FIXME InvocationTargetException and Interrupted Exceptions are
         * incorrectly handled
         */

        /* run project read only settings in progress monitor thread. */
        Util.runSafeSWTSync(log, new Runnable() {
            public void run() {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                    EditorAPI.getShell());
                try {
                    dialog.run(true, false, new IRunnableWithProgress() {
                        public void run(final IProgressMonitor monitor) {
                            monitor.beginTask("Project settings ... ",
                                IProgressMonitor.UNKNOWN);

                            try {
                                getProject().accept(new IResourceVisitor() {
                                    public boolean visit(IResource resource)
                                        throws CoreException {

                                        FileUtil
                                            .setReadOnly(resource, readonly);
                                        monitor.worked(1);

                                        return true;
                                    }
                                });
                            } catch (CoreException e) {
                                log.warn("Failure to set readonly to "
                                    + readonly + ":", e);
                            } finally {
                                monitor.done();
                            }
                        }
                    });
                } catch (InvocationTargetException e) {
                    SharedProject.log.warn("", e);
                } catch (InterruptedException e) {
                    SharedProject.log.warn("", e);
                }
            }
        });

    }

    public ConcurrentDocumentManager getConcurrentDocumentManager() {
        return this.activitySequencer.getConcurrentDocumentManager();
    }

    public int getFreeColor() {
        return freeColors.get();
    }

    public void returnColor(int colorID) {
        freeColors.add(colorID);
    }
}
