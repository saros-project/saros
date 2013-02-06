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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;
import org.jivesoftware.smack.Connection;
import org.joda.time.DateTime;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.OutgoingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.discoverymanager.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * The SessionManager is responsible for initiating new Saros sessions and for
 * reacting to invitations. The user can be only part of one session at most.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class SarosSessionManager implements ISarosSessionManager {

    private static final Logger log = Logger
        .getLogger(SarosSessionManager.class.getName());

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected XMPPTransmitter transmitter;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    // FIXME dependency of other classes
    protected InvitationProcessObservable invitationProcesses;

    @Inject
    // FIXME dependency of other classes
    protected VersionManager versionManager;

    @Inject
    // FIXME dependency of other class
    protected RosterTracker rosterTracker;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected SarosContext sarosContext;

    @Inject
    protected SarosUI sarosUI;

    protected SarosNet sarosNet;

    private final List<ISarosSessionListener> sarosSessionListeners = new CopyOnWriteArrayList<ISarosSessionListener>();

    protected final IConnectionListener listener = new IConnectionListener() {
        @Override
        public void connectionStateChanged(Connection connection,
            ConnectionState state) {

            if (state == ConnectionState.DISCONNECTING) {
                stopSarosSession();
            }
        }
    };

    public SarosSessionManager(SarosNet sarosNet) {
        this.sarosNet = sarosNet;
        this.sarosNet.addListener(listener);
    }

    protected static final Random sessionRandom = new Random();

    /**
     * @JTourBusStop 3, Invitation Process:
     * 
     *               This class manages the current Saros session.
     * 
     *               Saros makes a distinction between a session and a shared
     *               project. A session is an on-line collaboration between
     *               users which allows users to carry out activities. The main
     *               activity is to share projects. Hence, before you share a
     *               project, a session has to be started and all users added to
     *               it.
     * 
     *               (At the moment, this separation is invisible to the user.
     *               He/she must share a project in order to start a session.)
     * 
     */
    @Override
    public void startSession(
        final Map<IProject, List<IResource>> projectResourcesMapping) {

        sessionID.setValue(String.valueOf(sessionRandom
            .nextInt(Integer.MAX_VALUE)));

        final SarosSession sarosSession = new SarosSession(
            preferenceUtils.getFavoriteColorID(), new DateTime(), sarosContext);

        sarosSessionObservable.setValue(sarosSession);

        sessionStarting(sarosSession);
        sarosSession.start();
        sessionStarted(sarosSession);

        for (Entry<IProject, List<IResource>> mapEntry : projectResourcesMapping
            .entrySet()) {

            IProject project = mapEntry.getKey();
            List<IResource> resourcesList = mapEntry.getValue();

            if (!project.isOpen()) {
                try {
                    project.open(null);
                } catch (CoreException e) {
                    log.debug("an error occur while opening project: "
                        + project.getName(), e);
                    continue;
                }
            }

            String projectID = String.valueOf(sessionRandom
                .nextInt(Integer.MAX_VALUE));

            sarosSession.addSharedResources(project, projectID, resourcesList);

            projectAdded(projectID);
        }

        log.info("session started");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISarosSession joinSession(JID host, int colorID,
        DateTime sessionStart, JID inviter, int inviterColorID) {

        SarosSession sarosSession = new SarosSession(host, colorID,
            sessionStart, sarosContext, inviter, inviterColorID);

        this.sarosSessionObservable.setValue(sarosSession);

        log.info("Saros session joined");

        return sarosSession;
    }

    /**
     * Used to make stopSharedProject reentrant
     */
    private Lock stopSharedProjectLock = new ReentrantLock();

    /**
     * @nonSWT
     */
    @Override
    public void stopSarosSession() {

        if (SWTUtils.isSWT()) {
            log.warn("StopSharedProject should not be called from SWT",
                new StackTrace());
        }

        if (!stopSharedProjectLock.tryLock()) {
            log.debug("stopSharedProject() couldn't acquire "
                + "stopSharedProjectLock.");
            return;
        }

        try {

            SarosSession sarosSession = (SarosSession) sarosSessionObservable
                .getValue();

            if (sarosSession == null) {
                sessionID.setValue(SessionIDObservable.NOT_IN_SESSION);
                return;
            }

            sessionEnding(sarosSession);

            this.transmitter.sendLeaveMessage(sarosSession);
            log.debug("Leave message sent.");
            if (!sarosSession.isStopped()) {
                try {
                    sarosSession.stop();
                } catch (RuntimeException e) {
                    log.error("Error stopping project: ", e);
                }
            }
            sarosSession.dispose();

            this.sarosSessionObservable.setValue(null);

            sessionEnded(sarosSession);

            sessionID.setValue(SessionIDObservable.NOT_IN_SESSION);

            log.info("Session left");
        } finally {
            stopSharedProjectLock.unlock();
        }
    }

    /**
     * This method and the sarosSessionObservable are dangerous to use. The
     * session might be in the process of being destroyed while you call this
     * method. The caller needs to save the returned value to a local variable
     * and do a null check. For new code you should consider being scoped by the
     * SarosSession and get the SarosSession in the constructor.
     * 
     * @deprecated Error prone method, which produces NPE if not handled
     *             correctly. Will soon get removed.
     */
    @Override
    @Deprecated
    public ISarosSession getSarosSession() {
        return this.sarosSessionObservable.getValue();
    }

    @Override
    public void invitationReceived(JID from, String sessionID,
        String invitationID, DateTime sessionStart, VersionInfo versionInfo,
        String description) {

        /*
         * Side effect ! Setting the sessionID will reject further invitation
         * requests
         */

        this.sessionID.setValue(sessionID);

        final IncomingSessionNegotiation process = new IncomingSessionNegotiation(
            this, from, versionManager, versionInfo, sessionStart,
            invitationID, description, sarosContext);

        process.acknowledgeInvitation();
        sarosUI.showIncomingInvitationUI(process, true);
    }

    /**
     * This method is called when a new project was added to the session
     * 
     * @param from
     *            The one who added the project.
     * @param projectInfos
     *            what projects where added ({@link FileList}, projectName etc.)
     *            see: {@link ProjectExchangeInfo}
     * @param processID
     *            ID of the exchanging process
     */
    @Override
    public void incomingProjectReceived(JID from,
        List<ProjectExchangeInfo> projectInfos, String processID) {

        IncomingProjectNegotiation process = new IncomingProjectNegotiation(
            getSarosSession(), from, processID, projectInfos, sarosContext);

        sarosUI.showIncomingProjectUI(process);
    }

    @Override
    public void invite(JID toInvite, String description) {
        ISarosSession sarosSession = sarosSessionObservable.getValue();

        OutgoingSessionNegotiation result = new OutgoingSessionNegotiation(
            toInvite, sarosSession, description, sarosContext);

        OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(
            result);

        outgoingInvitationJob.setPriority(Job.SHORT);
        outgoingInvitationJob.schedule();
    }

    @Override
    public void invite(Collection<JID> jidsToInvite, String description) {
        for (JID jid : jidsToInvite)
            invite(jid, description);
    }

    /**
     * 
     * OutgoingInvitationJob wraps the instance of
     * {@link OutgoingSessionNegotiation} and cares about handling the
     * exceptions like local or remote cancellation.
     * 
     * It notifies the user about the progress using the Eclipse Jobs API and
     * interrupts the process if the session closes.
     * 
     */
    protected class OutgoingInvitationJob extends Job {

        protected OutgoingSessionNegotiation process;
        protected String peer;
        protected ISarosSessionListener cancelListener = new AbstractSarosSessionListener() {

            @Override
            public void sessionEnded(ISarosSession oldSharedProject) {
                process.localCancel(null, CancelOption.NOTIFY_PEER);
            }

        };

        public OutgoingInvitationJob(OutgoingSessionNegotiation process) {
            super(MessageFormat.format(
                Messages.SarosSessionManager_inviting_user, User
                    .getHumanReadableName(getSarosSession().getSaros()
                        .getSarosNet(), process.getPeer())));
            this.process = process;
            this.peer = process.getPeer().getBase();
            this.setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                ImageManager
                    .getImageDescriptor("/icons/elcl16/project_share_tsk.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                registerCancelListener();
                InvitationProcess.Status status = process.start(monitor);

                switch (status) {
                case CANCEL:
                    return Status.CANCEL_STATUS;
                case ERROR:
                    return new Status(IStatus.ERROR, Saros.SAROS,
                        process.getErrorMessage());
                case OK:
                    break;
                case REMOTE_CANCEL:
                    SarosView
                        .showNotification(
                            Messages.SarosSessionManager_canceled_invitation,
                            MessageFormat
                                .format(
                                    Messages.SarosSessionManager_canceled_invitation_text,
                                    peer));

                    return new Status(
                        IStatus.CANCEL,
                        Saros.SAROS,
                        MessageFormat
                            .format(
                                Messages.SarosSessionManager_canceled_invitation_text,
                                peer));

                case REMOTE_ERROR:
                    SarosView
                        .showNotification(
                            Messages.SarosSessionManager_error_during_invitation,
                            MessageFormat
                                .format(
                                    Messages.SarosSessionManager_error_during_invitation_text,
                                    peer, process.getErrorMessage()));

                    return new Status(
                        IStatus.ERROR,
                        Saros.SAROS,
                        MessageFormat
                            .format(
                                Messages.SarosSessionManager_error_during_invitation_text,
                                peer, process.getErrorMessage()));
                }
            } catch (Exception e) {
                log.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);

            } finally {
                releaseCancelListener();
            }

            startSharingProjects(process.getPeer());

            return Status.OK_STATUS;
        }

        protected void registerCancelListener() {
            SarosSessionManager.this.addSarosSessionListener(cancelListener);
        }

        protected void releaseCancelListener() {
            SarosSessionManager.this.removeSarosSessionListener(cancelListener);
        }
    }

    /**
     * Adds project resources to an existing session.
     * 
     * @param projectResourcesMapping
     * 
     */
    @Override
    public void addResourcesToSession(
        Map<IProject, List<IResource>> projectResourcesMapping) {

        ISarosSession session = getSarosSession();

        if (session == null) {
            log.warn("could not add resources because there is no active session");
            return;
        }

        /*
         * TODO: there are race conditions, USER A restricts USER B to read-only
         * while this code is executed
         */

        if (!session.hasWriteAccess()) {
            log.error("current local user has not enough privileges to add resources to the current session");
            return;
        }

        List<IProject> projectsToShare = new ArrayList<IProject>();

        for (Entry<IProject, List<IResource>> mapEntry : projectResourcesMapping
            .entrySet()) {
            IProject project = mapEntry.getKey();
            List<IResource> resourcesList = mapEntry.getValue();

            if (!project.isOpen()) {
                try {
                    project.open(null);
                } catch (CoreException e1) {
                    log.debug("An error occur while opening project", e1);
                    continue;
                }
            }

            // side effect: non shared projects are always partial -.-
            if (!session.isCompletelyShared(project)) {
                String projectID = String.valueOf(sessionRandom
                    .nextInt(Integer.MAX_VALUE));
                session.addSharedResources(project, projectID, resourcesList);
                projectAdded(projectID);
                projectsToShare.add(project);
            }
        }

        if (projectsToShare.isEmpty()) {
            log.warn("skipping project negotitation because no new projects were added to the current session");
            return;
        }

        for (User user : session.getRemoteUsers()) {

            OutgoingProjectNegotiation out = new OutgoingProjectNegotiation(
                user.getJID(), session, projectsToShare, sarosContext);

            OutgoingProjectJob job = new OutgoingProjectJob(out);
            job.setPriority(Job.SHORT);
            job.schedule();
        }
    }

    @Override
    public void startSharingProjects(JID user) {

        ISarosSession session = getSarosSession();

        if (session == null) {
            /*
             * as this currently only called by the OutgoingSessionNegotiation
             * job just silently return
             */
            log.error("cannot share projects when no session is running");
            return;
        }

        /*
         * this can trigger a ConcurrentModification exception as the
         * SarosProjectMapper is completely broken
         */
        List<IProject> currentSharedProjects = new ArrayList<IProject>(
            session.getProjects());

        if (currentSharedProjects.isEmpty())
            return;

        OutgoingProjectNegotiation out = new OutgoingProjectNegotiation(user,
            session, currentSharedProjects, sarosContext);

        OutgoingProjectJob job = new OutgoingProjectJob(out);
        job.setPriority(Job.SHORT);
        job.schedule();

    }

    protected class OutgoingProjectJob extends Job {

        protected OutgoingProjectNegotiation process;
        protected String peer;
        protected ISarosSessionListener cancelListener = new AbstractSarosSessionListener() {

            @Override
            public void sessionEnded(ISarosSession oldSharedProject) {
                process.localCancel(null, CancelOption.NOTIFY_PEER);
            }

        };

        @Override
        public boolean belongsTo(Object family) {
            return family.equals("invitational");
        }

        public OutgoingProjectJob(
            OutgoingProjectNegotiation outgoingProjectNegotiation) {
            super(Messages.SarosSessionManager_sharing_project);
            this.process = outgoingProjectNegotiation;
            this.peer = process.getPeer().getBase();
            this.setUser(true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                ImageManager.getImageDescriptor("/icons/invites.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                registerCancelListener();
                ProjectNegotiation.Status status = process.start(monitor);

                ISarosSession session = getSarosSession();
                SarosNet sarosNet = null;
                String peerName;
                if (session != null) {
                    sarosNet = session.getSaros().getSarosNet();
                    peerName = User.getHumanReadableName(sarosNet,
                        new JID(peer));
                } else {
                    peerName = Utils.prefix(new JID(peer));
                }

                String message;

                switch (status) {
                case CANCEL:
                    return Status.CANCEL_STATUS;
                case ERROR:
                    return new Status(IStatus.ERROR, Saros.SAROS,
                        process.getErrorMessage());
                case OK:
                    break;
                case REMOTE_CANCEL:
                    message = MessageFormat
                        .format(
                            Messages.SarosSessionManager_project_sharing_cancelled_text,
                            peerName);

                    return new Status(IStatus.ERROR, Saros.SAROS, message);

                case REMOTE_ERROR:
                    message = MessageFormat
                        .format(
                            Messages.SarosSessionManager_sharing_project_cancelled_remotely,
                            peerName, process.getErrorMessage());
                    SarosView
                        .showNotification(
                            Messages.SarosSessionManager_sharing_project_cancelled_remotely_text,
                            message);

                    return new Status(IStatus.ERROR, Saros.SAROS, message);
                }
            } catch (Exception e) {
                log.error("This exception is not expected here: ", e);
                return new Status(IStatus.ERROR, Saros.SAROS, e.getMessage(), e);

            } finally {
                releaseCancelListener();
            }

            return Status.OK_STATUS;
        }

        protected void registerCancelListener() {
            SarosSessionManager.this.addSarosSessionListener(cancelListener);
        }

        protected void releaseCancelListener() {
            SarosSessionManager.this.removeSarosSessionListener(cancelListener);
        }
    }

    @Override
    public void addSarosSessionListener(ISarosSessionListener listener) {
        if (!this.sarosSessionListeners.contains(listener)) {
            this.sarosSessionListeners.add(listener);
        }
    }

    @Override
    public void removeSarosSessionListener(ISarosSessionListener listener) {
        this.sarosSessionListeners.remove(listener);
    }

    @Override
    public void preIncomingInvitationCompleted(IProgressMonitor monitor) {
        try {
            for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
                sarosSessionListener.preIncomingInvitationCompleted(monitor);
            }
        } catch (RuntimeException e) {
            log.error("Internal error in notifying listener"
                + " of an incoming invitation: ", e);
        }
    }

    @Override
    public void postOutgoingInvitationCompleted(IProgressMonitor monitor,
        User user) {
        try {
            for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
                sarosSessionListener.postOutgoingInvitationCompleted(monitor,
                    user);
            }
        } catch (RuntimeException e) {
            log.error("Internal error in notifying listener"
                + " of an outgoing invitation: ", e);
        }
    }

    @Override
    public void sessionStarting(ISarosSession sarosSession) {
        try {
            for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
                sarosSessionListener.sessionStarting(sarosSession);
            }
        } catch (RuntimeException e) {
            log.error("Internal error in notifying listener"
                + " of SarosSession starting: ", e);
        }
    }

    @Override
    public void sessionStarted(ISarosSession sarosSession) {
        for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
            try {
                sarosSessionListener.sessionStarted(sarosSession);
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of SarosSession start: ", e);
            }
        }
    }

    private void sessionEnding(ISarosSession sarosSession) {
        for (ISarosSessionListener saroSessionListener : this.sarosSessionListeners) {
            try {
                saroSessionListener.sessionEnding(sarosSession);
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of SarosSession ending: ", e);
            }
        }
    }

    private void sessionEnded(ISarosSession sarosSession) {
        for (ISarosSessionListener listener : this.sarosSessionListeners) {
            try {
                listener.sessionEnded(sarosSession);
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of SarosSession end: ", e);
            }
        }
    }

    @Override
    public void projectAdded(String projectID) {
        for (ISarosSessionListener listener : this.sarosSessionListeners) {
            try {
                listener.projectAdded(projectID);
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of an added project: ", e);
            }
        }
    }
}
