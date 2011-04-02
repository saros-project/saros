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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.User.Permission;
import de.fu_berlin.inf.dpp.accountManagement.XMPPAccountStore;
import de.fu_berlin.inf.dpp.activities.ProjectExchangeInfo;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.views.VideoPlayerView;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.wizards.AddProjectToSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * Some helper functionality to interface with Eclipse.
 */
@Component(module = "ui")
public class SarosUI {

    private static final Logger log = Logger.getLogger(SarosUI.class.getName());

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
        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                DialogUtils.openWindow(wizardDialog);
            }
        });
        return sessionWizard;
    }

    public AddProjectToSessionWizard showIncomingProjectUI(
        IncomingProjectNegotiation process) {
        List<ProjectExchangeInfo> pInfos = process.getProjectInfos();
        List<FileList> fileLists = new ArrayList<FileList>(pInfos.size());

        for (ProjectExchangeInfo pInfo : pInfos) {
            fileLists.add(pInfo.getFileList());
        }

        AddProjectToSessionWizard projectWizard = new AddProjectToSessionWizard(
            process, dataTransferManager, preferenceUtils, process.getPeer(),
            fileLists, process.getProjectNames());
        final WizardDialogAccessable wizardDialog = new WizardDialogAccessable(
            EditorAPI.getShell(), projectWizard);

        wizardDialog.setHelpAvailable(false);
        projectWizard.setWizardDlg(wizardDialog);
        Utils.runSafeSWTAsync(log, new Runnable() {

            public void run() {
                DialogUtils.openWindow(wizardDialog);
            }
        });
        return projectWizard;
    }

    /**
     * @swt
     */
    public void openSarosView() {
        createView(SarosView.ID);
        activateSarosView();
    }

    /**
     * @swt
     */
    public void activateSarosView() {
        activateView(SarosView.ID);
    }

    protected void bringToFrontView(String view) {
        showView(view, IWorkbenchPage.VIEW_VISIBLE);
    }

    /**
     * @swt
     */
    public void createVideoPlayerView() {
        if (Utils.findView(VideoPlayerView.ID) == null)
            createView(VideoPlayerView.ID);
    }

    /**
     * @swt
     */
    public void activateVideoPlayerView() {
        activateView(VideoPlayerView.ID);
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

    public static Composite createLabelComposite(Composite parent, String text) {
        Composite composite = new Composite(parent, SWT.NONE);

        FillLayout layout = new FillLayout(SWT.NONE);
        layout.marginHeight = 20;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setText(text);

        return composite;
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
                        Utils.runSafeSWTSync(log, new Runnable() {
                            public void run() {
                                MessageDialog.openInformation(EditorAPI
                                    .getAWorkbenchWindow().getShell(),
                                    "Permission Change Canceled",
                                    "The permission change was canceled.");
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
                "Permission Change Failed",
                "Permission change failed because of an internal error.\n\n"
                    + " Please try again.");
        } catch (InterruptedException e) {
            log.error("Code not designed to be interruptable", e);
        }
    }
}
