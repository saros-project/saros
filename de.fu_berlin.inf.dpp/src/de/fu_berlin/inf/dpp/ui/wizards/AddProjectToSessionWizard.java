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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;

import de.fu_berlin.inf.dpp.FileList;
import de.fu_berlin.inf.dpp.FileListDiff;
import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.exceptions.RemoteCancellationException;
import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard.OverwriteErrorDialog;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EnterProjectNamePage;
import de.fu_berlin.inf.dpp.util.EclipseUtils;
import de.fu_berlin.inf.dpp.util.Utils;

public class AddProjectToSessionWizard extends Wizard {

    private static Logger log = Logger
        .getLogger(AddProjectToSessionWizard.class);

    protected EnterProjectNamePage namePage;
    protected WizardDialogAccessable wizardDialog;
    protected IncomingProjectNegotiation process;
    protected JID peer;
    protected FileList fileList;
    protected String remoteProjectName;
    protected DataTransferManager dataTransferManager;
    protected PreferenceUtils preferenceUtils;

    public AddProjectToSessionWizard(IncomingProjectNegotiation process,
        DataTransferManager dataTransferManager,
        PreferenceUtils preferenceUtils, JID peer, FileList fileList,
        String projectName) {
        this.process = process;
        this.peer = peer;
        this.fileList = fileList;
        this.remoteProjectName = projectName;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;
        setWindowTitle("Add Project");
    }

    @Override
    public void addPages() {
        this.namePage = new EnterProjectNamePage(dataTransferManager,
            preferenceUtils, fileList, peer, remoteProjectName, wizardDialog);
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
        if (namePage.overwriteProjectResources()
            && !preferenceUtils.isAutoReuseExisting()) {
            FileListDiff diff;

            if (!source.isOpen()) {
                try {
                    source.open(null);
                } catch (CoreException e1) {
                    log.debug("An error occur while opening the source file",
                        e1);
                }
            }

            try {
                diff = FileListDiff.diff(new FileList(source),
                    process.getRemoteFileList());
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
                        // JoinSessionWizard.this.process.accept(source, target,
                        // skip, SubMonitor.convert(monitor));
                        AddProjectToSessionWizard.this.process.accept(source,
                            SubMonitor.convert(monitor), target, skip);
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

    @Override
    public boolean performCancel() {
        Utils.runSafeAsync(log, new Runnable() {
            public void run() {
                process.localCancel(null, CancelOption.NOTIFY_PEER);
            }
        });
        return true;
    }

    public boolean confirmOverwritingProjectResources(final String projectName,
        final FileListDiff diff) {
        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {

                    String message = "The selected project '"
                        + projectName
                        + "' will be used as a target project to carry out the synchronization.\n\n"
                        + "All local changes in the project will be overwritten using the inviter's project and additional files will be deleted!\n\n"
                        + "Press No and select 'Create copy...' in the invitation dialog if you are unsure.\n\n"
                        + "Do you want to proceed?";

                    String PID = Saros.SAROS;
                    MultiStatus info = new MultiStatus(PID, 1, message, null);
                    for (IPath path : diff.getRemovedPaths()) {
                        info.add(new Status(IStatus.WARNING, PID, 1,
                            "File will be removed: " + path.toOSString(), null));
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

    public void setWizardDlg(WizardDialogAccessable wizardDialog) {
        this.wizardDialog = wizardDialog;

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

    private int pageChanges = 0;
}
