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
package de.fu_berlin.inf.dpp.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.net.ConnectionState;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.wizards.AddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.util.EclipseUtils;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * Some helper functionality to interface with Eclipse.
 */
@Component(module = "ui")
public class SarosUI {

    private static final Logger log = Logger.getLogger(SarosUI.class.getName());

    private static final String SESSION_VIEW = "de.fu_berlin.inf.dpp.ui.SessionView";

    private static final String ROSTER_VIEW = "de.fu_berlin.inf.dpp.ui.RosterView";

    private static final String VIDEO_PLAYER_VIEW = "de.fu_berlin.inf.dpp.videosharing.player.VideoPlayerView";

    @Inject
    protected DataTransferManager dataTransferManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected Saros saros;

    @Inject
    VersionManager manager;

    @Inject
    XMPPAccountStore accountStore;

    protected SarosSessionManager sessionManager;

    public SarosUI(SarosSessionManager sessionManager) {

        this.sessionManager = sessionManager;

    }

    public JoinSessionWizard showIncomingInvitationUI(
        IncomingSessionNegotiation process) {

        JoinSessionWizard sessionWizard = new JoinSessionWizard(process,
            dataTransferManager, preferenceUtils, manager);
        final WizardDialogAccessable wizardDialog = new WizardDialogAccessable(
            EditorAPI.getShell(), sessionWizard);

        // TODO Provide help :-)
        wizardDialog.setHelpAvailable(false);

        sessionWizard.setWizardDlg(wizardDialog);

        // Fixes #2727848: InvitationDialog is opened in the
        // background
        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                EclipseUtils.openWindow(wizardDialog);
            }
        });
        return sessionWizard;
    }

    public AddProjectToSessionWizard showIncomingProjectUI(
        IncomingProjectNegotiation process) {
        AddProjectToSessionWizard projectWizard = new AddProjectToSessionWizard(
            process, dataTransferManager, preferenceUtils, process.getPeer(),
            process.getRemoteFileList(), process.getProjectName());
        final WizardDialogAccessable wizardDialog = new WizardDialogAccessable(
            EditorAPI.getShell(), projectWizard);

        wizardDialog.setHelpAvailable(false);
        projectWizard.setWizardDlg(wizardDialog);
        Util.runSafeSWTAsync(log, new Runnable() {

            public void run() {
                EclipseUtils.openWindow(wizardDialog);
            }
        });
        return projectWizard;
    }

    /**
     * @swt
     */
    public void openSarosViews() {
        // Create Session View
        createSessionView();
        // Open Roster so that a participant can be invited
        activateRosterView();
    }

    /**
     * @swt
     */
    public void activateRosterView() {
        activateView(SarosUI.ROSTER_VIEW);
    }

    /**
     * @swt
     */
    public void createSessionView() {
        if (Util.findView(SarosUI.SESSION_VIEW) == null)
            createView(SarosUI.SESSION_VIEW);
    }

    /**
     * @swt
     */
    public void bringToFrontSessionView() {
        bringToFrontView(SarosUI.SESSION_VIEW);
    }

    /**
     * @swt
     */
    public void activateSessionView() {
        activateView(SarosUI.SESSION_VIEW);
    }

    protected void bringToFrontView(String view) {
        showView(view, IWorkbenchPage.VIEW_VISIBLE);
    }

    /**
     * @swt
     */
    public void createVideoPlayerView() {
        if (Util.findView(SarosUI.VIDEO_PLAYER_VIEW) == null)
            createView(SarosUI.VIDEO_PLAYER_VIEW);
    }

    /**
     * @swt
     */
    public void activateVideoPlayerView() {
        activateView(SarosUI.VIDEO_PLAYER_VIEW);
    }

    protected void activateView(String view) {
        showView(view, IWorkbenchPage.VIEW_ACTIVATE);
    }

    protected void createView(String view) {
        showView(view, IWorkbenchPage.VIEW_CREATE);
    }

    /**
     * TODO What to do if no WorkbenchWindows are are active?
     */
    protected void showView(String view, int mode) {
        try {
            IWorkbench workbench = PlatformUI.getWorkbench();
            if (workbench == null) {
                log.error("Workbench not created when trying to show view!");
                return;
            }

            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if (window == null) {
                log.error("No Active WorkbenchWindow found "
                    + "(the platform is shutting down)"
                    + " when trying to show view!");
                return;
            }

            window.getActivePage().showView(view, null, mode);
        } catch (PartInitException e) {
            log.error("Could not create View " + view, e);
        }
    }

    /**
     * @param state
     * @return a nice string description of the given state, which can be used
     *         to be shown in labels (e.g. CONNECTING becomes "Connecting...").
     */
    public String getDescription(ConnectionState state) {
        String activeAccount = "No account detected.";
        if (accountStore.hasActiveAccount()) {
            activeAccount = accountStore.getActiveAccount().toString();
        }
        switch (state) {
        case NOT_CONNECTED:
            return activeAccount + " Not connected";
        case CONNECTING:
            return activeAccount + " Connecting...";
        case CONNECTED:
            return " Connected as " + saros.getConnection().getUser();
        case DISCONNECTING:
            return "Disconnecting...";
        case ERROR:
            Exception e = saros.getConnectionError();
            if (e == null) {
                return "Error";
            } else {
                return "Error (" + e.getMessage() + ")";
            }
        }

        return "";
    }

    public static Composite createLabelComposite(Composite parent, String text) {
        Composite composite = new Composite(parent, SWT.NONE);

        FillLayout layout = new FillLayout(SWT.NONE);
        layout.marginHeight = 20;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText(text);

        return composite;
    }

    public static Image getImage(String path) {
        return new Image(Display.getDefault(), SarosUI.getImageDescriptor(path)
            .getImageData());
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(
            "de.fu_berlin.inf.dpp", path);
    }

    /**
     * @swt
     */
    public void performPermissionChange(final User user,
        final Permission newPermission) {

        ProgressMonitorDialog dialog = new ProgressMonitorDialog(EditorAPI
            .getAWorkbenchWindow().getShell());

        try {
            dialog.run(true, true, new IRunnableWithProgress() {
                public void run(final IProgressMonitor monitor) {

                    final SubMonitor progress = SubMonitor.convert(monitor);

                    try {

                        progress.beginTask("Performing permission change",
                            IProgressMonitor.UNKNOWN);

                        sessionManager.getSarosSession()
                            .initiatePermissionChange(user, newPermission,
                                progress);

                    } catch (CancellationException e) {
                        log.warn("Permission change failed because buddy"
                            + " canceled the permission change");
                        Util.runSafeSWTSync(log, new Runnable() {
                            public void run() {
                                MessageDialog.openInformation(EditorAPI
                                    .getAWorkbenchWindow().getShell(),
                                    "Permission change failed",
                                    "The permission change was canceled. "
                                        + Util.getUserDescription(user));
                            }
                        });
                    } catch (InterruptedException e) {
                        log.error("Code not designed to be interruptable", e);
                    } finally {
                        progress.done();
                    }
                }
            });
        } catch (InvocationTargetException e) {
            log.error("Internal Error: ", e);
            MessageDialog.openError(EditorAPI.getAWorkbenchWindow().getShell(),
                "Permission change failed",
                "Permission change failed because of an internal error. "
                    + Util.getUserDescription(user) + " Please try again.");
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
        }
    }
}
