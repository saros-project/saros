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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess;
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

    private WizardDialogAccessable wizardDialog;
    private boolean updateSelected;
    private IncomingSessionNegotiation process;

    private ShowDescriptionPage descriptionPage;
    private PreferenceUtils preferenceUtils;

    private InvitationProcess.Status invitationStatus;

    public JoinSessionWizard(IncomingSessionNegotiation process,
        PreferenceUtils preferenceUtils, VersionManager manager) {
        this.process = process;
        this.preferenceUtils = preferenceUtils;

        EnterProjectNamePageUtils.setPreferenceUtils(preferenceUtils);

        process.setInvitationUI(this);
        setWindowTitle(Messages.JoinSessionWizard_title);
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);

        descriptionPage = new ShowDescriptionPage(manager, process);
        addPage(descriptionPage);
    }

    public PreferenceUtils getPreferenceUtils() {
        return preferenceUtils;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        this.descriptionPage.createControl(pageContainer);
        this.wizardDialog.setWizardButtonLabel(IDialogConstants.FINISH_ID,
            Messages.JoinSessionWizard_accept);
    }

    @Override
    public boolean performFinish() {

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
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
        } catch (InvocationTargetException e) {
            processException(e.getCause());
            return false;
        } catch (InterruptedException e) {
            log.error("Code not designed to be interrupted.", e); //$NON-NLS-1$
            processException(e);
            return false;
        }

        switch (invitationStatus) {
        case OK:
            break;
        case CANCEL:
        case ERROR:
            showCancelMessage(process.getPeer(), process.getErrorMessage(),
                CancelLocation.LOCAL);
            break;
        case REMOTE_CANCEL:
        case REMOTE_ERROR:
            showCancelMessage(process.getPeer(), process.getErrorMessage(),
                CancelLocation.REMOTE);
            break;

        }
        return true;
    }

    public static class OverwriteErrorDialog extends ErrorDialog {

        public OverwriteErrorDialog(Shell parentShell, String dialogTitle,
            String dialogMessage, IStatus status) {
            super(parentShell, dialogTitle, dialogMessage, status, IStatus.OK
                | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
        }

        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);
            Button ok = getButton(IDialogConstants.OK_ID);
            ok.setText(Messages.JoinSessionWizard_yes);
            Button no = createButton(parent, IDialogConstants.CANCEL_ID,
                Messages.JoinSessionWizard_no, true);
            no.moveBelow(ok);
            no.setFocus();
        }
    }

    @Override
    public boolean performCancel() {
        Utils.runSafeAsync(log, new Runnable() {
            @Override
            public void run() {
                process.localCancel(null, CancelOption.NOTIFY_PEER);
            }
        });
        return true;
    }

    protected boolean disposed = false;

    @Override
    public void dispose() {
        disposed = true;
        super.dispose();
    }

    public boolean isUpdateSelected() {
        return updateSelected;
    }

    public void cancelWizard(final JID jid, final String errorMsg,
        final CancelLocation cancelLocation) {

        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                Shell shell = wizardDialog.getShell();
                if (shell == null || shell.isDisposed())
                    return;
                wizardDialog.close();
            }
        });

        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                showCancelMessage(jid, errorMsg, cancelLocation);
            }
        });

    }

    public void showCancelMessage(JID jid, String errorMsg,
        CancelLocation cancelLocation) {

        String peer = jid.getBase();

        if (errorMsg != null) {
            switch (cancelLocation) {
            case LOCAL:
                DialogUtils.openErrorMessageDialog(getShell(),
                    Messages.JoinSessionWizard_inv_cancelled,
                    Messages.JoinSessionWizard_inv_cancelled_text
                        + Messages.JoinSessionWizard_8 + errorMsg);
                break;
            case REMOTE:
                DialogUtils.openErrorMessageDialog(getShell(),

                Messages.JoinSessionWizard_inv_cancelled, MessageFormat.format(
                    Messages.JoinSessionWizard_inv_cancelled_text2, peer,
                    errorMsg));
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                DialogUtils.openInformationMessageDialog(getShell(),
                    Messages.JoinSessionWizard_inv_cancelled, MessageFormat
                        .format(Messages.JoinSessionWizard_inv_cancelled_text3,
                            peer));
            }
        }
    }

    protected void processException(Throwable t) {
        log.error("This type of exception is not expected here: ", t); //$NON-NLS-1$
        cancelWizard(process.getPeer(), "Unkown error: " + t.getMessage(), //$NON-NLS-1$
            CancelLocation.REMOTE);
    }
}
