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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
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
import de.fu_berlin.inf.dpp.invitation.OutgoingInvitationProcess;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.IConnectionListener;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.RosterTracker;
import de.fu_berlin.inf.dpp.net.business.DispatchThreadContext;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.net.internal.DiscoveryManager;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatTransmitter;
import de.fu_berlin.inf.dpp.observables.InvitationProcessObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.internal.SharedProject;
import de.fu_berlin.inf.dpp.synchronize.StopManager;
import de.fu_berlin.inf.dpp.ui.SarosUI;
import de.fu_berlin.inf.dpp.ui.wizards.InvitationWizard;
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
    protected SharedProjectObservable currentlySharedProject;

    @Inject
    protected DiscoveryManager discoveryManager;

    @Inject
    protected XMPPChatReceiver xmppReceiver;

    @Inject
    protected XMPPChatTransmitter transmitter;

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
    protected RosterTracker rosterTracker;

    @Inject
    protected DispatchThreadContext dispatchThreadContext;

    private final List<ISessionListener> listeners = new CopyOnWriteArrayList<ISessionListener>();

    protected Saros saros;

    protected List<IResource> partialProjectResources;

    public SessionManager(Saros saros) {
        this.saros = saros;
        saros.addListener(this);
    }

    protected static final Random sessionRandom = new Random();

    public void startSession(IProject project,
        List<IResource> partialProjectResources) throws XMPPException {
        if (!saros.isConnected()) {
            throw new XMPPException("No connection");
        }

        JID myJID = saros.getMyJID();
        this.sessionID.setValue(String.valueOf(sessionRandom
            .nextInt(Integer.MAX_VALUE)));
        this.partialProjectResources = partialProjectResources;

        SharedProject sharedProject = new SharedProject(saros,
            this.transmitter, this.transferManager, dispatchThreadContext,
            project, myJID, stopManager, new DateTime());

        this.currentlySharedProject.setValue(sharedProject);

        sharedProject.start();

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(sharedProject);
        }

        openInviteDialog(preferenceUtils.getAutoInviteUsers());
        SessionManager.log.info("Session started");
    }

    /**
     * {@inheritDoc}
     */
    public ISharedProject joinSession(IProject project, JID host, int colorID,
        DateTime sessionStart) {

        SharedProject sharedProject = new SharedProject(saros,
            this.transmitter, this.transferManager, dispatchThreadContext,
            project, saros.getMyJID(), host, colorID, stopManager, sessionStart);
        this.currentlySharedProject.setValue(sharedProject);

        for (ISessionListener listener : this.listeners) {
            listener.sessionStarted(sharedProject);
        }

        log.info("Shared project joined");

        return sharedProject;
    }

    /**
     * Used to make stopSharedProject reentrant
     */
    private Lock stopSharedProjectLock = new ReentrantLock();

    /**
     * @nonSWT
     */
    public void stopSharedProject() {

        if (Util.isSWT()) {
            log.warn("StopSharedProject should not be called from SWT",
                new StackTrace());
        }

        if (!stopSharedProjectLock.tryLock())
            return;

        try {
            SharedProject project = currentlySharedProject.getValue();

            if (project == null) {
                return;
            }

            this.transmitter.sendLeaveMessage(project);

            try {
                project.stop();
                project.dispose();
            } catch (RuntimeException e) {
                log.error("Error stopping project: ", e);
            }

            this.currentlySharedProject.setValue(null);

            for (ISessionListener listener : this.listeners) {
                try {
                    listener.sessionEnded(project);
                } catch (RuntimeException e) {
                    log.error("Internal error in notifying listener"
                        + " of SharedProject end: ", e);
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

    public ISharedProject getSharedProject() {
        return this.currentlySharedProject.getValue();
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
        String invitationID) {

        this.sessionID.setValue(sessionID);

        final IncomingInvitationProcess process = new IncomingInvitationProcess(
            this, this.transmitter, from, projectName, description, colorID,
            invitationProcesses, versionManager, versionInfo, sessionStart,
            sarosUI, invitationID);

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                sarosUI.showIncomingInvitationUI(process);
                sarosUI.openSarosViews();
            }
        });

        for (ISessionListener listener : this.listeners) {
            listener.invitationReceived(process);
        }
    }

    public void connectionStateChanged(XMPPConnection connection,
        ConnectionState newState) {

        if (newState == ConnectionState.DISCONNECTING) {
            stopSharedProject();
        }
    }

    public void onReconnect(Map<JID, Integer> expectedSequenceNumbers) {

        SharedProject project = currentlySharedProject.getValue();

        if (project == null) {
            return;
        }

        this.transmitter.sendRemainingFiles();
        this.transmitter.sendRemainingMessages();

        /*
         * ask for next expected activityDataObjects (in case I missed something
         * while being not available)
         */

        // TODO this is currently disabled
        this.transmitter.sendRequestForActivity(project,
            expectedSequenceNumbers, true);
    }

    public void openInviteDialog(final @Nullable List<JID> toInvite) {
        final SharedProject sharedProject = currentlySharedProject.getValue();

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                // Instantiates and initializes the wizard
                InvitationWizard wizard = new InvitationWizard(saros,
                    sharedProject, rosterTracker, discoveryManager,
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
    public void invite(JID toInvite) {
        ISharedProject project = currentlySharedProject.getValue();
        String description = project.getProject().getName();

        final OutgoingInvitationProcess result = new OutgoingInvitationProcess(
            transmitter, toInvite, project, partialProjectResources,
            description, project.getFreeColor(), invitationProcesses,
            versionManager, stopManager, discoveryManager);

        OutgoingInvitationJob outgoingInvitationJob = new OutgoingInvitationJob(
            result);
        outgoingInvitationJob.schedule();
    }

    public void invite(Collection<JID> jidsToInvite) {
        for (JID jid : jidsToInvite)
            invite(jid);
    }

    /**
     * Represents the connection between the {@link OutgoingInvitationProcess}
     * and the GUI. It wraps the instance of {@link OutgoingInvitationProcess}
     * and cares about handling the exceptions and notifying the user about the
     * progress using the Eclipse Jobs API.
     * 
     * TODO: the jobs should be started in a dialog with the option
     * "Run in background". But in this case if more jobs are started
     * concurrently, a lots of dialogs pop up. Can they integrated in one
     * dialog?
     */
    protected class OutgoingInvitationJob extends Job {
        protected OutgoingInvitationProcess process;
        protected String peer;
        protected Shell dialogShell;

        public OutgoingInvitationJob(OutgoingInvitationProcess process) {
            super("Inviting " + process.getPeer().getBase() + "...");
            this.process = process;
            this.peer = process.getPeer().getBase();
            setProperty(IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY,
                true);
            setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
            setProperty(IProgressConstants.ICON_PROPERTY, SarosUI
                .getImageDescriptor("/icons/invites.png"));
            dialogShell = EditorAPI.getShell();
            if (dialogShell == null)
                dialogShell = new Shell();
        }

        protected void jobCompleted() {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    MessageDialog.openInformation(dialogShell,
                        "Invitation Complete",
                        "Your invitation has completed!\n\n" + peer
                            + " is now in the session.");
                }
            });
        }

        protected Action jobCompletedAction() {
            return new Action("Invitation has completed.") {
                @Override
                public void run() {
                    jobCompleted();
                }
            };
        }

        protected void jobCancelledLocally(final String errorMsg) {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    if (errorMsg != null)
                        MessageDialog
                            .openError(dialogShell, "Invitation Cancelled",
                                "The invitation of " + peer
                                    + " has been cancelled "
                                    + "locally because of an error:\n\n"
                                    + errorMsg);
                    else
                        MessageDialog.openInformation(dialogShell,
                            "Invitation Cancelled",
                            "You have cancelled the invitation of " + peer
                                + "!");
                }
            });
        }

        protected Action jobCancelledLocallyAction(final String errorMsg) {
            return new Action("Invitation has completed.") {
                @Override
                public void run() {
                    jobCancelledLocally(errorMsg);
                }
            };
        }

        protected void jobCancelledRemotely(final String errorMsg) {
            Util.runSafeSWTSync(log, new Runnable() {
                public void run() {
                    if (errorMsg == null)
                        MessageDialog.openInformation(dialogShell,
                            "Invitation Cancelled",
                            "Your invitation has been cancelled "
                                + "remotely by " + peer + "!");
                    else
                        MessageDialog.openError(dialogShell,
                            "Invitation Cancelled",
                            "Your invitation has been cancelled "
                                + "remotely by " + peer
                                + " because of an error:\n\n" + errorMsg);
                }
            });
        }

        protected Action jobCancelledRemotelyAction(final String errorMsg) {
            return new Action("Invitation has completed.") {
                @Override
                public void run() {
                    jobCancelledRemotely(errorMsg);
                }
            };
        }

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            try {
                process.start(SubMonitor.convert(monitor));
            } catch (LocalCancellationException e) {
                if (isModal(this)) {
                    setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.FALSE);
                    jobCancelledLocally(e.getMessage());
                } else {
                    setProperty(IProgressConstants.ACTION_PROPERTY,
                        jobCancelledLocallyAction(e.getMessage()));
                }
                return Status.CANCEL_STATUS;
            } catch (RemoteCancellationException e) {
                if (isModal(this)) {
                    setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.FALSE);
                    jobCancelledRemotely(e.getMessage());
                } else {
                    setProperty(IProgressConstants.ACTION_PROPERTY,
                        jobCancelledRemotelyAction(e.getMessage()));
                }
                return Status.CANCEL_STATUS;
            } catch (Exception e) {
                // TODO: the user should be notified
                log.error("This exception is not expected here: ", e);
            }

            // Everything went well.
            if (isModal(this)) {
                setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.FALSE);
                jobCompleted();
            } else {
                setProperty(IProgressConstants.ACTION_PROPERTY,
                    jobCompletedAction());
            }
            return Status.OK_STATUS;
        }

        public boolean isModal(Job job) {
            Boolean isModal = (Boolean) job
                .getProperty(IProgressConstants.PROPERTY_IN_DIALOG);
            if (isModal == null)
                return false;
            return isModal.booleanValue();
        }
    }
}
