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
package de.fu_berlin.inf.dpp.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IProgressConstants;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.joda.time.DateTime;
import org.picocontainer.annotations.Inject;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosContext;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferencesNegotiatingManager;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.OutgoingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.OutgoingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.discoveryManager.DiscoveryManager;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.ProjectNegotiationObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceManager;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.ImageManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.wizards.InvitationWizard;
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
public class SarosSessionManager implements IConnectionListener,
    ISarosSessionManager {

    private static Logger log = Logger.getLogger(SarosSessionManager.class
        .getName());

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected XMPPTransmitter transmitter;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    // FIXME dependency of OIP
    protected StopManager stopManager;

    @Inject
    // FIXME dependency of other classes
    protected InvitationProcessObservable invitationProcesses;

    @Inject
    // FIXME dependency of other classes
    protected VersionManager versionManager;

    @Inject
    protected MUCSessionPreferencesNegotiatingManager comNegotiatingManager;

    @Inject
    // FIXME dependency of other class
    protected RosterTracker rosterTracker;

    @Inject
    // FIXME dependency of other class
    protected DispatchThreadContext dispatchThreadContext;

    @Inject
    protected ProjectNegotiationObservable projectExchangeProcesses;

    @Inject
    protected SarosContext sarosContext;

    private final List<ISarosSessionListener> sarosSessionListeners = new CopyOnWriteArrayList<ISarosSessionListener>();

    protected Saros saros;

    protected List<IResource> partialProjectResources;

    /**
     * Should invitations send the project archive via StreamSession?
     */
    protected IPreferenceStore prefStore;

    protected boolean doStreamingInvitation = false;

    public SarosSessionManager(Saros saros) {
        this.saros = saros;
        this.prefStore = saros.getPreferenceStore();
        saros.addListener(this);
    }

    protected static final Random sessionRandom = new Random();

    public void startSession(List<IProject> projects,
        List<IResource> partialProjectResources) throws XMPPException {
        if (!saros.isConnected()) {
            throw new XMPPException("No connection");
        }

        this.sessionID.setValue(String.valueOf(sessionRandom
            .nextInt(Integer.MAX_VALUE)));
        this.partialProjectResources = partialProjectResources;

        SarosSession sarosSession = new SarosSession(this.transmitter,
            dispatchThreadContext, new DateTime(), sarosContext);

        this.sarosSessionObservable.setValue(sarosSession);

        notifySarosSessionStarting(sarosSession);
        sarosSession.start();
        notifySarosSessionStarted(sarosSession);

        for (IProject project : projects) {
            String projectID = String.valueOf(sessionRandom
                .nextInt(Integer.MAX_VALUE));
            sarosSession.addSharedProject(project, projectID);
            notifyProjectAdded(project);
        }

        SarosSessionManager.log.info("Session started");
    }

    /**
     * {@inheritDoc}
     */
    public ISarosSession joinSession(JID host, int colorID,
        DateTime sessionStart) {

        SarosSession sarosSession = new SarosSession(transmitter,
            dispatchThreadContext, host, colorID, sessionStart, sarosContext);

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
    public void stopSarosSession() {

        if (Utils.isSWT()) {
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
                return;
            }

            notifySessionEnding(sarosSession);

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

            notifySessionEnd(sarosSession);

            clearSessionID();
            log.info("Session left");
        } finally {
            stopSharedProjectLock.unlock();
        }
    }

    public void clearSessionID() {
        sessionID.setValue(SessionIDObservable.NOT_IN_SESSION);
    }

    public ISarosSession getSarosSession() {
        return this.sarosSessionObservable.getValue();
    }

    public void invitationReceived(JID from, String sessionID, int colorID,
        VersionInfo versionInfo, DateTime sessionStart, final SarosUI sarosUI,
        String invitationID, MUCSessionPreferences comPrefs, String description) {

        this.sessionID.setValue(sessionID);

        final IncomingSessionNegotiation process = new IncomingSessionNegotiation(
            this, transmitter, from, colorID, invitationProcesses,
            versionManager, versionInfo, sessionStart, sarosUI, invitationID,
            saros, description, sarosContext);
        comNegotiatingManager.setSessionPreferences(comPrefs);

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                process.acknowledgeInvitation();
                sarosUI.showIncomingInvitationUI(process);
                sarosUI.openSarosView();
            }
        });
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
     * @param doStream
     *            If <code>true</code>, the files of the projects will be
     *            streamed.
     */
    public void incomingProjectReceived(JID from, final SarosUI sarosUI,
        List<ProjectExchangeInfo> projectInfos, String processID,
        boolean doStream) {
        final IncomingProjectNegotiation process = new IncomingProjectNegotiation(
            transmitter, from, projectExchangeProcesses, processID,
            projectInfos, doStream, sarosContext);

        Utils.runSafeSWTAsync(log, new Runnable() {

            public void run() {
                sarosUI.showIncomingProjectUI(process);

            }

        });
    }

    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {

        if (newState == ConnectionState.DISCONNECTING) {
            stopSarosSession();
        }
    }

    public void onReconnect(Map<JID, Integer> expectedSequenceNumbers) {

        ISarosSession sarosSession = sarosSessionObservable.getValue();

        if (sarosSession == null) {
            return;
        }

        this.transmitter.sendRemainingFiles();
        this.transmitter.sendRemainingMessages();

        /*
         * ask for next expected activityDataObjects (in case I missed something
         * while being not available)
         */

        // TODO this is currently disabled
        this.transmitter.sendRequestForActivity(sarosSession,
            expectedSequenceNumbers, true);
    }

    public void openInviteDialog(final @Nullable List<JID> toInvite) {
        final ISarosSession sarosSession = sarosSessionObservable.getValue();

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                // Instantiates and initializes the wizard
                InvitationWizard wizard = new InvitationWizard(saros,
                    sarosSession, rosterTracker, discoveryManager,
                    SarosSessionManager.this, versionManager,
                    invitationProcesses);

                // Instantiates the wizard container with the wizard and opens
                // it
                Shell dialogShell = EditorAPI.getShell();
                if (dialogShell == null)
                    dialogShell = new Shell();
                WizardDialog dialog = new WizardDialog(dialogShell, wizard);
                dialog.create();
                dialog.open();
            }
        });

    }

    /**
     * Invites a user to the shared project.
     * 
     * @param toInvite
     *            the JID of the user that is to be invited.
     */
    public void invite(JID toInvite, String description) {
        ISarosSession sarosSession = sarosSessionObservable.getValue();

        OutgoingSessionNegotiation result = new OutgoingSessionNegotiation(
            (transmitter), toInvite, sarosSession.getFreeColor(),
            invitationProcesses, sarosSession, description, versionManager,
            discoveryManager, comNegotiatingManager, sarosContext);

        OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(
            result);
        outgoingInvitationJob.schedule();
    }

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
            super("Inviting " + process.getPeer().getBase() + "...");
            this.process = process;
            this.peer = process.getPeer().getBase();
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                ImageManager
                    .getImageDescriptor("/icons/elcl16/project_share_tsk.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {

                registerCancelListener();
                process.start(SubMonitor.convert(monitor));

            } catch (LocalCancellationException e) {

                return Status.CANCEL_STATUS;

            } catch (RemoteCancellationException e) {

                if (e.getMessage() == null) { // buddy canceled purposely

                    SarosView.showNotification("Canceled Invitation", peer
                        + " has canceled your Invitation.");

                    return new Status(IStatus.CANCEL, Saros.SAROS, peer
                        + " has canceled your Invitation.");

                } else {

                    SarosView
                        .showNotification(
                            "Error during Invitation",
                            "Your invitation to "
                                + peer
                                + " has been canceled remotely because of an error:\n\n"
                                + e.getMessage());

                    return new Status(
                        IStatus.ERROR,
                        Saros.SAROS,
                        "Your invitation to "
                            + peer
                            + " has been canceled remotely because of an error:\n\n"
                            + e.getMessage());
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
            Utils.runSafeSWTSync(log, new Runnable() {

                public void run() {
                    SarosSessionManager.this
                        .addSarosSessionListener(cancelListener);
                }

            });
        }

        protected void releaseCancelListener() {
            Utils.runSafeSWTSync(log, new Runnable() {

                public void run() {
                    SarosSessionManager.this
                        .removeSarosSessionListener(cancelListener);
                }

            });
        }
    }

    /**
     * Adds projects to an existing session and starts to share the
     * {@code projectsToAdd}
     * 
     * @param projectsToAdd
     *            List of projects that will be added to the session
     * 
     */
    public void addProjectsToSession(List<IProject> projectsToAdd) {
        for (IProject project : projectsToAdd) {
            this.getSarosSession().addSharedProject(project, project.getName());
            notifyProjectAdded(project);
        }
        boolean doStream = prefStore
            .getBoolean(PreferenceConstants.STREAM_PROJECT);
        for (User user : this.getSarosSession().getRemoteUsers()) {
            OutgoingProjectNegotiation out = new OutgoingProjectNegotiation(
                transmitter, user.getJID(), this.getSarosSession(),
                projectsToAdd, projectExchangeProcesses, stopManager,
                sessionID, doStream, sarosContext);
            OutgoingProjectJob job = new OutgoingProjectJob(out);
            job.schedule();
        }
    }

    /**
     * Will start sharing all projects in session with a participant. This
     * should be called after a the invitation to a session was completed
     * successfully.
     * 
     * @param user
     *            JID of session participant to share projects with
     */
    public void startSharingProjects(JID user) {
        boolean doStream = prefStore
            .getBoolean(PreferenceConstants.STREAM_PROJECT);
        List<IProject> projectsToShare = new ArrayList<IProject>(this
            .getSarosSession().getProjects());

        if (!projectsToShare.isEmpty()) {
            OutgoingProjectNegotiation out = new OutgoingProjectNegotiation(
                transmitter, user, this.getSarosSession(), projectsToShare,
                projectExchangeProcesses, stopManager, sessionID, doStream,
                sarosContext);
            OutgoingProjectJob job = new OutgoingProjectJob(out);
            job.schedule();
        }

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

        public OutgoingProjectJob(
            OutgoingProjectNegotiation outgoingProjectNegotiation) {
            super("Sharing project "
                + outgoingProjectNegotiation.getProjectNames() + ".");
            this.process = outgoingProjectNegotiation;
            this.peer = process.getPeer().getBase();
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                ImageManager.getImageDescriptor("/icons/invites.png"));
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {

                registerCancelListener();
                process.start(SubMonitor.convert(monitor));

            } catch (LocalCancellationException e) {

                return Status.CANCEL_STATUS;

            } catch (RemoteCancellationException e) {

                if (e.getMessage() == null) { // remote user canceled purposely
                    String message = peer
                        + " has canceled the project sharing.";

                    SarosView.showNotification("Canceled project sharing",
                        message);

                    return new Status(IStatus.ERROR, Saros.SAROS, message);

                } else {
                    String message = "The project sharing with "
                        + peer
                        + " has been canceled remotely because of an error:\n\n"
                        + e.getMessage();
                    SarosView.showNotification("Error during project sharing",
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
            Utils.runSafeSWTSync(log, new Runnable() {

                public void run() {
                    SarosSessionManager.this
                        .addSarosSessionListener(cancelListener);
                }

            });
        }

        protected void releaseCancelListener() {
            Utils.runSafeSWTSync(log, new Runnable() {

                public void run() {
                    SarosSessionManager.this
                        .removeSarosSessionListener(cancelListener);
                }

            });
        }

    }

    public void addSarosSessionListener(ISarosSessionListener listener) {
        if (!this.sarosSessionListeners.contains(listener)) {
            /*
             * HACK PreferencesManager relies on the fact that a project is
             * added only when a session is started, and it might create a new
             * file ".settings/org.eclipse.core.resources.prefs" for the project
             * specific settings. Adding PreferencesManager as the last listener
             * makes sure that the file creation is registered by the
             * SharedResourcesManager.
             */
            if (listener instanceof PreferenceManager) {
                this.sarosSessionListeners.add(listener);
            } else {
                this.sarosSessionListeners.add(0, listener);
            }
        }
    }

    public void removeSarosSessionListener(ISarosSessionListener listener) {
        this.sarosSessionListeners.remove(listener);
    }

    public void notifyPreIncomingInvitationCompleted(SubMonitor subMonitor) {
        try {
            for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
                sarosSessionListener.preIncomingInvitationCompleted(subMonitor);
            }
        } catch (RuntimeException e) {
            log.error("Internal error in notifying listener"
                + " of an incoming invitation: ", e);
        }
    }

    public void notifyPostOutgoingInvitationCompleted(SubMonitor subMonitor,
        User user) {
        try {
            for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
                sarosSessionListener.postOutgoingInvitationCompleted(
                    subMonitor, user);
            }
        } catch (RuntimeException e) {
            log.error("Internal error in notifying listener"
                + " of an outgoing invitation: ", e);
        }
    }

    public void notifySarosSessionStarting(ISarosSession sarosSession) {
        try {
            for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
                sarosSessionListener.sessionStarting(sarosSession);
            }
        } catch (RuntimeException e) {
            log.error("Internal error in notifying listener"
                + " of SarosSession starting: ", e);
        }
    }

    public void notifySarosSessionStarted(ISarosSession sarosSession) {
        for (ISarosSessionListener sarosSessionListener : this.sarosSessionListeners) {
            try {
                sarosSessionListener.sessionStarted(sarosSession);
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of SarosSession start: ", e);
            }
        }
    }

    public void notifySessionEnding(ISarosSession sarosSession) {
        for (ISarosSessionListener saroSessionListener : this.sarosSessionListeners) {
            try {
                saroSessionListener.sessionEnding(sarosSession);
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of SarosSession ending: ", e);
            }
        }
    }

    public void notifySessionEnd(ISarosSession sarosSession) {
        for (ISarosSessionListener listener : this.sarosSessionListeners) {
            try {
                listener.sessionEnded(sarosSession);
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of SarosSession end: ", e);
            }
        }
    }

    public void notifyProjectAdded(IProject project) {
        for (ISarosSessionListener listener : this.sarosSessionListeners) {
            try {
                listener.projectAdded(getSarosSession().getProjectID(project));
            } catch (RuntimeException e) {
                log.error("Internal error in notifying listener"
                    + " of an added project: ", e);
            }
        }
    }
}
