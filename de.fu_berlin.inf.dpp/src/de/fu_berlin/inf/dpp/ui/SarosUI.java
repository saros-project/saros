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

import org.apache.log4j.Logger;
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
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.optional.cdt.CDTFacade;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.util.EclipseUtils;
import de.fu_berlin.inf.dpp.util.Util;

/**
 * Some helper functionality to interface with Eclipse.
 */
@Component(module = "ui")
public class SarosUI {

    private static final Logger log = Logger.getLogger(SarosUI.class.getName());

    private static final String SESSION_VIEW = "de.fu_berlin.inf.dpp.ui.SessionView";

    private static final String ROSTER_VIEW = "de.fu_berlin.inf.dpp.ui.RosterView";

    @Inject
    protected DataTransferManager dataTransferManager;

    @Inject
    protected PreferenceUtils preferenceUtils;

    @Inject
    protected Saros saros;

    public SarosUI(SessionManager sessionManager, JDTFacade jdtFacade,
        CDTFacade cdtFacade) {

        // It would be nice to eliminate these, because they cause dependencies
        // to the JDT and CDT, but they are necessary to prevent Observers
        // from editing the documents
        if (jdtFacade.isJDTAvailable()) {
            jdtFacade.installSharedDocumentProvider(sessionManager);
        }

        if (cdtFacade.isCDTAvailable()) {
            cdtFacade.installSharedDocumentProvider(sessionManager);
        }

        sessionManager.addSessionListener(new AbstractSessionListener() {
            @Override
            public void invitationReceived(
                final IIncomingInvitationProcess process) {
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {
                        showIncomingInvitationUI(process);
                    }
                });
            }

            @Override
            public void sessionStarted(ISharedProject sharedProject) {
                Util.runSafeSWTSync(log, new Runnable() {
                    public void run() {
                        openSarosViews();
                    }
                });
            }

        });
    }

    protected void showIncomingInvitationUI(IIncomingInvitationProcess process) {

        JoinSessionWizard sessionWizard = new JoinSessionWizard(process,
            dataTransferManager, preferenceUtils);
        WizardDialogAccessable wizardDialog = new WizardDialogAccessable(
            EditorAPI.getShell(), sessionWizard);

        // TODO Provide help :-)
        wizardDialog.setHelpAvailable(false);
        sessionWizard.setWizardDlg(wizardDialog);
        process.setInvitationUI(sessionWizard.getInvitationUI());

        // Fixes #2727848: InvitationDialog is opened in the
        // background
        EclipseUtils.openWindow(wizardDialog);
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
            log.error("Could not create Session View", e);
        }
    }

    /**
     * @param state
     * @return a nice string description of the given state, which can be used
     *         to be shown in labels (e.g. CONNECTING becomes "Connecting...").
     */
    public String getDescription(ConnectionState state) {
        switch (state) {
        case NOT_CONNECTED:
            return "Not connected";
        case CONNECTING:
            return "Connecting...";
        case CONNECTED:
            return "Connected (as " + saros.getConnection().getUser() + ")";
        case DISCONNECTING:
            return "Disconnecting...";
        case ERROR:
            return "Error (" + saros.getConnectionError() + ")";
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

}
