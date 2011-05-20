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
package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.invitation.IncomingSessionNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
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

    protected WizardDialogAccessable wizardDialog;
    protected boolean updateSelected;
    public IncomingSessionNegotiation process;

    protected ShowDescriptionPage descriptionPage;
    protected boolean requested = false;
    protected DataTransferManager dataTransferManager;
    protected PreferenceUtils preferenceUtils;
    protected VersionManager manager;

    public JoinSessionWizard(IncomingSessionNegotiation process,
        DataTransferManager dataTransferManager,
        PreferenceUtils preferenceUtils, VersionManager manager) {
        this.process = process;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;
        this.manager = manager;

        EnterProjectNamePageUtils.preferenceUtils = preferenceUtils;

        process.setInvitationUI(this);
        setWindowTitle("Session Invitation");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);

        descriptionPage = new ShowDescriptionPage(this, manager, process);
        addPage(descriptionPage);
    }

    public PreferenceUtils getPreferenceUtils() {
        return preferenceUtils;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        this.descriptionPage.createControl(pageContainer);
        this.wizardDialog.setWizardButtonLabel(IDialogConstants.FINISH_ID,
            "Accept");
    }

    @Override
    public boolean performFinish() {
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        JoinSessionWizard.this.process.accept(SubMonitor
                            .convert(monitor));
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            processException(e.getCause());
            return false;
        } catch (InterruptedException e) {
            log.error("Code not designed to be interrupted.", e);
            processException(e);
            return false;
        }

        getShell().forceActive();
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
            ok.setText("Yes");
            Button no = createButton(parent, IDialogConstants.CANCEL_ID, "No",
                true);
            no.moveBelow(ok);
            no.setFocus();
        }
    }

    @Override
    public boolean performCancel() {
        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                process.localCancel(null, CancelOption.NOTIFY_PEER);
            }
        });
        return true;
    }

    public void setWizardDlg(WizardDialogAccessable wd) {
        this.wizardDialog = wd;

        /**
         * Listen to page changes so we can cancel our automatic clicking the
         * next button
         */
        this.wizardDialog.addPageChangingListener(new IPageChangingListener() {
            public void handlePageChanging(PageChangingEvent event) {
                pageChanges++;
            }
        });

    }

    /**
     * Variable is only used to count how many times pageChanges were registered
     * so we only press the next button if no page changes occurred.
     * 
     * The only place this is needed is below in the pressWizardButton, if you
     * want to know the number of page changes, count them yourself.
     */
    private int pageChanges = 0;

    protected boolean disposed = false;

    /**
     * Will wait one second and then press the next button, if the dialog is
     * still on the given page (i.e. if the user presses next then this will not
     * call next again).
     */
    public void pressWizardButton(final int buttonID) {

        final int pageChangesAtStart = pageChanges;

        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interruptable", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                Utils.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        // User clicked next in the meantime
                        if (pageChangesAtStart != pageChanges)
                            return;

                        // Dialog already closed
                        if (disposed)
                            return;

                        // Button existent
                        if (wizardDialog.getWizardButton(buttonID) == null) {
                            return;
                        }
                        // Button not enabled
                        if (!wizardDialog.getWizardButton(buttonID).isEnabled())
                            return;

                        wizardDialog.buttonPressed(buttonID);
                    }
                });
            }
        });
    }

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

        Utils.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                Shell shell = wizardDialog.getShell();
                if (shell == null || shell.isDisposed())
                    return;
                wizardDialog.close();
            }
        });

        Utils.runSafeSWTAsync(log, new Runnable() {
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
                    "Invitation Cancelled",
                    "Your invitation has been cancelled "
                        + "locally because of an error:\n\n" + errorMsg);
                break;
            case REMOTE:
                DialogUtils.openErrorMessageDialog(getShell(),
                    "Invitation Cancelled",
                    "Your invitation has been cancelled " + "remotely by "
                        + peer + " because of an error:\n\n" + errorMsg);
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                DialogUtils.openInformationMessageDialog(getShell(),
                    "Invitation Cancelled",
                    "Your invitation has been cancelled remotely by " + peer
                        + "!");
            }
        }
    }

    protected void processException(Throwable t) {
        if (t instanceof LocalCancellationException) {
            cancelWizard(process.getPeer(), t.getMessage(),
                CancelLocation.LOCAL);
        } else if (t instanceof RemoteCancellationException) {
            cancelWizard(process.getPeer(), t.getMessage(),
                CancelLocation.REMOTE);
        } else {
            log.error("This type of exception is not expected here: ", t);
            cancelWizard(process.getPeer(), "Unkown error: " + t.getMessage(),
                CancelLocation.REMOTE);
        }
    }
}
