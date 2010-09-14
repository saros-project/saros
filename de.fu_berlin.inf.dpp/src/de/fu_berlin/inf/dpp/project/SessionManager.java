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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.invitation.IncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess.CancelOption;
import de.fu_berlin.inf.dpp.invitation.OutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.internal.SarosSession;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.InvitationWizard;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager.CommunicationPreferences;
import de.fu_berlin.inf.dpp.util.StackTrace;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;
import de.fu_berlin.inf.dpp.util.VersionManager.VersionInfo;

/**
 * The SessionManager is responsible for initiating new Saros sessions and for
 * reacting to invitations. The user can be only part of one session at most.
 * 
 * @author rdjemili
 */
@Component(module = "core")
public class SessionManager implements IConnectionListener, ISessionManager {

    private static Logger log = Logger
        .getLogger(SessionManager.class.getName());

    @Inject
    protected SarosSessionObservable sarosSessionObservable;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected XMPPReceiver xmppReceiver;

    @Inject
    protected XMPPTransmitter transmitter;

    @Inject
    protected DataTransferManager transferManager;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected StopManager stopManager;

    @Inject
    protected InvitationProcessObservable invitationProcesses;

    @Inject
    protected VersionManager versionManager;

    @Inject
    protected CommunicationNegotiatingManager comNegotiatingManager;

    @Inject
    protected RosterTracker rosterTracker;

    @Inject
    protected DispatchThreadContext dispatchThreadContext;

    private final List<ISessionListener> listeners = new CopyOnWriteArrayList<ISessionListener>();

    protected Saros saros;

    protected List<IResource> partialProjectResources;

    /**
     * Should invitations send the project archive via StreamSession?
     */
    protected IPreferenceStore prefStore;
    protected boolean doStreamingInvitation = false;

    public SessionManager(Saros saros) {
        this.saros = saros;
        saros.addListener(this);
    }

    protected static final Random sessionRandom = new Random();

    public void startSession(IProject project,
        List<IResource> partialProjectResources, boolean useVersionControl)
        throws XMPPException {
        if (!saros.isConnected()) {
            throw new XMPPException("No connection");
        }

        JID myJID = saros.getMyJID();
        this.sessionID.setValue(String.valueOf(sessionRandom
            .nextInt(Integer.MAX_VALUE)));
        this.partialProjectResources = partialProjectResources;

        this.prefStore = saros.getPreferenceStore();
        this.doStreamingInvitation = prefStore
            .getBoolean(PreferenceConstants.STREAM_PROJECT);

        SarosSession sarosSession = new SarosSession(saros, this.transmitter,
            this.transferManager, dispatchThreadContext, myJID, stopManager,
            new DateTime(), useVersionControl);
        sarosSession.addSharedProject(project, project.getName());

        this.sarosSessionObservable.setValue(sarosSession);

        sarosSession.start();

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(sarosSession);
        }

        openInviteDialog(preferenceUtils.getAutoInviteUsers());
        SessionManager.log.info("Session started");
    }

    /**
     * {@inheritDoc}
     */
    public ISarosSession joinSession(String projectID, IProject project,
        JID host, int colorID, DateTime sessionStart) {

        SarosSession sarosSession = new SarosSession(saros, this.transmitter,
            this.transferManager, dispatchThreadContext, saros.getMyJID(),
            host, colorID, stopManager, sessionStart);
        sarosSession.addSharedProject(project, projectID);
        this.sarosSessionObservable.setValue(sarosSession);

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(sarosSession);
        }

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

        if (Util.isSWT()) {
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

            for (ISessionListener listener : this.listeners) {
                try {
                    listener.sessionEnded(sarosSession);
                } catch (RuntimeException e) {
                    log.error("Internal error in notifying listener"
                        + " of SarosSession end: ", e);
                }
            }

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

    public void addSessionListener(ISessionListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeSessionListener(ISessionListener listener) {
        this.listeners.remove(listener);
    }

    public void invitationReceived(JID from, String sessionID,
        String projectName, String description, int colorID,
        VersionInfo versionInfo, DateTime sessionStart, final SarosUI sarosUI,
        String invitationID, boolean doStream, CommunicationPreferences comPrefs) {

        this.sessionID.setValue(sessionID);

        final IncomingInvitationProcess process = new IncomingInvitationProcess(
            this, this.transmitter, from, projectName, description, colorID,
            invitationProcesses, versionManager, versionInfo, sessionStart,
            sarosUI, invitationID, saros, doStream);
        comNegotiatingManager.setSessionPrefs(comPrefs);

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                sarosUI.showIncomingInvitationUI(process);
                sarosUI.openSarosViews();
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

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                // Instantiates and initializes the wizard
                InvitationWizard wizard = new InvitationWizard(saros,
                    sarosSession, rosterTracker, discoveryManager,
                    SessionManager.this, versionManager, invitationProcesses);

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

        // TODO We want to invite to all projects!!
        IProject toInviteTo = sarosSession.getProjects().iterator().next();

        doStreamingInvitation = prefStore
            .getBoolean(PreferenceConstants.STREAM_PROJECT);

        OutgoingInvitationProcess result = new OutgoingInvitationProcess(
            transmitter, toInvite, sarosSession, partialProjectResources,
            toInviteTo, description, sarosSession.getFreeColor(),
            invitationProcesses, versionManager, stopManager, discoveryManager,
            comNegotiatingManager, doStreamingInvitation);

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
     * {@link OutgoingInvitationProcess} and cares about handling the exceptions
     * like local or remote cancellation.
     * 
     * It notifies the user about the progress using the Eclipse Jobs API and
     * interrupts the process if the session closes.
     * 
     */
    protected class OutgoingInvitationJob extends Job {

        protected OutgoingInvitationProcess process;
        protected String peer;
        protected ISessionListener cancelListener = new ISessionListener() {

            public void invitationReceived(IncomingInvitationProcess invitation) {
                // Nothing to do here
            }

            public void sessionEnded(ISarosSession oldSharedProject) {
                process.localCancel(null, CancelOption.NOTIFY_PEER);
            }

            public void sessionStarted(ISarosSession newSharedProject) {
                // Nothing to do here
            }

        };

        public OutgoingInvitationJob(OutgoingInvitationProcess process) {
            super("Inviting " + process.getPeer().getBase() + "...");
            this.process = process;
            this.peer = process.getPeer().getBase();
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY,
                SarosUI.getImageDescriptor("/icons/invites.png"));
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

                    return new Status(IStatus.ERROR, Saros.SAROS, peer
                        + " has canceled your Invitation.");

                } else {

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
            Util.runSafeSWTSync(log, new Runnable() {

                public void run() {
                    SessionManager.this.addSessionListener(cancelListener);
                }

            });
        }

        protected void releaseCancelListener() {
            Util.runSafeSWTSync(log, new Runnable() {

                public void run() {
                    SessionManager.this.removeSessionListener(cancelListener);
                }

            });
        }
    }
}
