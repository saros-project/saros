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
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListDiff;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.exceptions.SarosCancellationException;
import de.fu_berlin.inf.dpp.invitation.IncomingInvitationProcess;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.InvitationProcess.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.util.EclipseUtils;
import de.fu_berlin.inf.dpp.util.Util;
import de.fu_berlin.inf.dpp.util.VersionManager;

/**
 * A wizard that guides the user through an incoming invitation process.
 * 
 * TODO Automatically switch to follow mode
 * 
 * TODO Suggest if the project is a CVS project that the user checks it out and
 * offers an option to transfer the settings
 * 
 * TODO Create a separate Wizard class with the following concerns implemented
 * more nicely: Long-Running Operation after each step, cancellation by a remote
 * party, auto-advance.
 * 
 * @author rdjemili
 */
public class JoinSessionWizard extends Wizard {

    private static final Logger log = Logger.getLogger(JoinSessionWizard.class);

    protected ShowDescriptionPage descriptionPage;
    protected EnterProjectNamePage namePage;
    protected WizardDialogAccessable wizardDialog;
    protected IncomingInvitationProcess process;
    protected boolean requested = false;
    protected String updateProjectName;
    protected boolean updateSelected;
    protected DataTransferManager dataTransferManager;
    protected PreferenceUtils preferenceUtils;
    protected VersionManager manager;

    public JoinSessionWizard(IncomingInvitationProcess process,
        DataTransferManager dataTransferManager,
        PreferenceUtils preferenceUtils, VersionManager manager) {
        this.process = process;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;
        this.manager = manager;

        process.setInvitationUI(this);
        setWindowTitle("Session Invitation");
        setHelpAvailable(false);
        setNeedsProgressMonitor(true);
    }

    public PreferenceUtils getPreferenceUtils() {
        return preferenceUtils;
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page.equals(descriptionPage) && !requested) {
            /*
             * Increment pages for auto-next, because the request will block too
             * long
             */
            pageChanges++;
            if (!requestHostFileList()) {
                getShell().forceActive();
                return null;
            }
            getShell().forceActive();
        }
        return super.getNextPage(page);
    }

    public boolean requestHostFileList() {
        requested = true;

        /* wait for getting project file list. */
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InterruptedException, InvocationTargetException {
                    try {
                        process.requestRemoteFileList(SubMonitor
                            .convert(monitor));
                    } catch (SarosCancellationException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (InvocationTargetException e) {
            processException(e.getCause());
            return false;
        } catch (InterruptedException e) {
            log.error("Not designed to be interrupted.");
            processException(e);
            return false;
        }

        if (process.getRemoteFileList() == null) {
            // Remote side canceled...
            getShell().close();
            return false;
        }

        if (preferenceUtils.isAutoReuseExisting()
            && JoinSessionWizardUtils.existsProjects(process.getProjectName())) {
            updateSelected = true;
            updateProjectName = process.getProjectName();
        } else {
            updateSelected = false;
            updateProjectName = "";
        }

        return true;
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        this.descriptionPage.createControl(pageContainer);
        // create namePage lazily
    }

    @Override
    public void addPages() {
        descriptionPage = new ShowDescriptionPage(this, manager, process);
        namePage = new EnterProjectNamePage(this, dataTransferManager,
            preferenceUtils);

        addPage(descriptionPage);
        addPage(namePage);
    }

    @Override
    public boolean performFinish() {

        final IProject source = namePage.getSourceProject();
        final String target = namePage.getTargetProjectName();
        final boolean skip = namePage.isSyncSkippingSelected();

        /*
         * Ask the user whether to overwrite local resources only if resources
         * are supposed to be overwritten based on the synchronization options
         * and if there are differences between the remote and local project.
         */
        if (namePage.overwriteProjectResources()) {
            FileListDiff diff;
            try {
                diff = new FileList(source).diff(process.getRemoteFileList());
            } catch (CoreException e) {
                MessageDialog.openError(getShell(), "Error computing FileList",
                    "Could not compute local FileList: " + e.getMessage());
                return false;
            }
            if (diff.getRemovedPaths().size() > 0
                || diff.getAlteredPaths().size() > 0)
                if (!confirmOverwritingProjectResources(source.getName(), diff))
                    return false;
        }

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        JoinSessionWizard.this.process.accept(source, target,
                            skip, SubMonitor.convert(monitor));
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

    public boolean confirmOverwritingProjectResources(final String projectName,
        final FileListDiff diff) {
        try {
            return Util.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {

                    String message = "The selected project '"
                        + projectName
                        + "' will be used as a target project to carry out the synchronization.\n\n"
                        + "All local changes in the project will be overwritten using the host's project and additional files will be deleted!\n\n"
                        + "Press No and select 'Create copy...' in the invitation dialog if you are unsure.\n\n"
                        + "Do you want to proceed?";

                    String PID = Saros.SAROS;
                    MultiStatus info = new MultiStatus(PID, 1, message, null);
                    for (IPath path : diff.getRemovedPaths()) {
                        info
                            .add(new Status(IStatus.WARNING, PID, 1,
                                "File will be removed: " + path.toOSString(),
                                null));
                    }
                    for (IPath path : diff.getAlteredPaths()) {
                        info.add(new Status(IStatus.WARNING, PID, 1,
                            "File will be overwritten: " + path.toOSString(),
                            null));
                    }
                    for (IPath path : diff.getAddedPaths()) {
                        info.add(new Status(IStatus.INFO, PID, 1,
                            "File will be added: " + path.toOSString(), null));
                    }
                    return new OverwriteErrorDialog(getShell(),
                        "Warning: Local changes will be deleted", null, info)
                        .open() == IDialogConstants.OK_ID;
                }
            });
        } catch (Exception e) {
            log.error(
                "An error ocurred while trying to open the confirm dialog.", e);
            return false;
        }
    }

    @Override
    public boolean performCancel() {
        Util.runSafeAsync(log, new Runnable() {
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

        Util.runSafeAsync(log, new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Code not designed to be interruptable", e);
                    Thread.currentThread().interrupt();
                    return;
                }
                Util.runSafeSWTAsync(log, new Runnable() {
                    public void run() {

                        // User clicked next in the meantime
                        if (pageChangesAtStart != pageChanges)
                            return;

                        // Dialog already closed
                        if (disposed)
                            return;

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

    public String getUpdateProject() {
        return updateProjectName;
    }

    public boolean isUpdateSelected() {
        return updateSelected;
    }

    public void cancelWizard(final JID jid, final String errorMsg,
        final CancelLocation cancelLocation) {

        Util.runSafeSWTAsync(log, new Runnable() {
            public void run() {
                wizardDialog.close();
            }
        });

        Util.runSafeSWTAsync(log, new Runnable() {
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
                EclipseUtils.openErrorMessageDialog(getShell(),
                    "Invitation Cancelled",
                    "Your invitation has been cancelled "
                        + "locally because of an error:\n\n" + errorMsg);
                break;
            case REMOTE:
                EclipseUtils.openErrorMessageDialog(getShell(),
                    "Invitation Cancelled",
                    "Your invitation has been cancelled " + "remotely by "
                        + peer + " because of an error:\n\n" + errorMsg);
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                EclipseUtils.openInformationMessageDialog(getShell(),
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
