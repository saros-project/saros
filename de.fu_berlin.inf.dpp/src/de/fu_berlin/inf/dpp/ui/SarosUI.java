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

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.Saros.ConnectionState;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
import de.fu_berlin.inf.dpp.optional.cdt.CDTFacade;
import de.fu_berlin.inf.dpp.optional.jdt.JDTFacade;
import de.fu_berlin.inf.dpp.project.AbstractSessionListener;
import de.fu_berlin.inf.dpp.project.ISessionManager;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard;
import de.fu_berlin.inf.dpp.ui.wizards.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.util.EclipseUtils;
import de.fu_berlin.inf.dpp.util.Util;

public class SarosUI {

    private static final Logger log = Logger.getLogger(SarosUI.class.getName());

    private static final String SESSION_VIEW = "de.fu_berlin.inf.dpp.ui.SessionView";

    private static final String ROSTER_VIEW = "de.fu_berlin.inf.dpp.ui.RosterView";

    public SarosUI(ISessionManager sessionManager, JDTFacade jdtFacade,
        CDTFacade cdtFacade) {

        // It would be nice to eliminiate these, because they cause dependencies
        // to the JDT and CDT, but they are necessary to prevent Observers
        // from editing the documents
        if (jdtFacade.isJDTAvailable()) {
            jdtFacade.installSharedDocumentProvider();
        }

        if (cdtFacade.isCDTAvailable()) {
            cdtFacade.installSharedDocumentProvider();
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

        JoinSessionWizard sessionWizard = new JoinSessionWizard(process);
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
     * TODO What to do if no Views are active?
     * 
     * @swt
     */
    public void openSarosViews() {
        try {
            // Create Session View
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            window.getActivePage().showView(SarosUI.SESSION_VIEW, null,
                IWorkbenchPage.VIEW_CREATE);
        } catch (PartInitException e) {
            log.error("Could not create Session View", e);
        }

        try {
            // Open Roster so that a participant can be invited
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            window.getActivePage().showView(SarosUI.ROSTER_VIEW, null,
                IWorkbenchPage.VIEW_ACTIVATE);
        } catch (PartInitException e) {
            log.error("Could not create Session View", e);
        }
    }

    /**
     * @param state
     * @return a nice string description of the given state, which can be used
     *         to be shown in labels (e.g. CONNECTING becomes "Connecting...").
     */
    public static String getDescription(ConnectionState state) {
        switch (state) {
        case NOT_CONNECTED:
            return "Not connected";
        case CONNECTING:
            return "Connecting...";
        case CONNECTED:
            return "Connected (as "
                + Saros.getDefault().getConnection().getUser() + ")";
        case DISCONNECTING:
            return "Disconnecting...";
        case ERROR:
            return "Error (" + Saros.getDefault().getConnectionError() + ")";
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
