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
package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.SessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.ui.wizards.pages.ShowDescriptionPage;
import de.fu_berlin.inf.dpp.ui.wizards.utils.EnterProjectNamePageUtils;
import de.fu_berlin.inf.dpp.util.Utils;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * A wizard that guides the user through an incoming invitation process.
 * 
 * TODO Automatically switch to follow mode
 * 
 * TODO Create a separate Wizard class with the following concerns implemented
 * more nicely: Long-Running Operation after each step, cancellation by a remote
 * party, auto-advance.
 * 
 * @author rdjemili
 */
public class JoinSessionWizard extends Wizard {

    private static final Logger log = Logger.getLogger(JoinSessionWizard.class);

    private boolean accepted = false;

    private IncomingSessionNegotiation process;

    private ShowDescriptionPage descriptionPage;

    private SessionNegotiation.Status invitationStatus;

    @Inject
    private VersionManager manager;

    @Inject
    private PreferenceUtils preferenceUtils;

    public JoinSessionWizard(IncomingSessionNegotiation process) {
        this.process = process;
        SarosPluginContext.initComponent(this);

        EnterProjectNamePageUtils.setPreferenceUtils(preferenceUtils);

        process.setInvitationUI(this);
        setWindowTitle(Messages.JoinSessionWizard_title);
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);

        descriptionPage = new ShowDescriptionPage(manager, process);
        addPage(descriptionPage);
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        this.descriptionPage.createControl(pageContainer);

        if (getContainer() instanceof WizardDialogAccessable) {
            ((WizardDialogAccessable) getContainer()).setWizardButtonLabel(
                IDialogConstants.FINISH_ID, Messages.JoinSessionWizard_accept);
        }
    }

    @Override
    public boolean performFinish() {

        accepted = true;

        try {
            getContainer().run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        invitationStatus = process.accept(monitor);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause == null)
                cause = e;

            asyncShowCancelMessage(process.getPeer(), e.getMessage(),
                CancelLocation.LOCAL);

            // give up, close the wizard as we cannot do anything here !
            return true;
        }

        switch (invitationStatus) {
        case OK:
            break;
        case CANCEL:
        case ERROR:
            asyncShowCancelMessage(process.getPeer(),
                process.getErrorMessage(), CancelLocation.LOCAL);
            break;
        case REMOTE_CANCEL:
        case REMOTE_ERROR:
            asyncShowCancelMessage(process.getPeer(),
                process.getErrorMessage(), CancelLocation.REMOTE);
            break;

        }
        return true;
    }

    @Override
    public boolean performCancel() {
        Utils.runSafeAsync("CancelJoinSessionWizard", log, new Runnable() {
            @Override
            public void run() {
                process.localCancel(null, CancelOption.NOTIFY_PEER);
            }
        });
        return true;
    }

    /**
     * Get rid of this method, use a listener !
     */
    public void cancelWizard(final JID jid, final String errorMsg,
        final CancelLocation cancelLocation) {

        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {

                /*
                 * do NOT CLOSE the wizard if it performs async operations
                 * 
                 * see performFinish() -> getContainer().run(boolean, boolean,
                 * IRunnableWithProgress)
                 */
                if (accepted)
                    return;

                Shell shell = JoinSessionWizard.this.getShell();
                if (shell == null || shell.isDisposed())
                    return;

                ((WizardDialog) JoinSessionWizard.this.getContainer()).close();

                asyncShowCancelMessage(jid, errorMsg, cancelLocation);
            }
        });
    }

    private void asyncShowCancelMessage(final JID jid, final String errorMsg,
        final CancelLocation cancelLocation) {
        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                showCancelMessage(jid, errorMsg, cancelLocation);
            }
        });
    }

    private void showCancelMessage(JID jid, String errorMsg,
        CancelLocation cancelLocation) {

        String peer = jid.getBase();

        Shell shell = EditorAPI.getShell();

        if (errorMsg != null) {
            switch (cancelLocation) {
            case LOCAL:
                DialogUtils.openErrorMessageDialog(shell,
                    Messages.JoinSessionWizard_inv_cancelled,
                    Messages.JoinSessionWizard_inv_cancelled_text
                        + Messages.JoinSessionWizard_8 + errorMsg);
                break;
            case REMOTE:
                DialogUtils.openErrorMessageDialog(shell,

                Messages.JoinSessionWizard_inv_cancelled, MessageFormat.format(
                    Messages.JoinSessionWizard_inv_cancelled_text2, peer,
                    errorMsg));
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                DialogUtils.openInformationMessageDialog(shell,
                    Messages.JoinSessionWizard_inv_cancelled, MessageFormat
                        .format(Messages.JoinSessionWizard_inv_cancelled_text3,
                            peer));
            }
        }
    }
}
