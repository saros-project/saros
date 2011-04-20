package de.fu_berlin.inf.dpp.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.wizards.JoinSessionWizard.OverwriteErrorDialog;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EnterProjectNamePage;
import de.fu_berlin.inf.dpp.util.Utils;

public class AddProjectToSessionWizard extends Wizard {

    private static Logger log = Logger
        .getLogger(AddProjectToSessionWizard.class);

    protected EnterProjectNamePage namePage;
    protected WizardDialogAccessable wizardDialog;
    protected IncomingProjectNegotiation process;
    protected JID peer;
    protected List<FileList> fileLists;
    protected boolean isExceptionCancel;
    /**
     * projectID => projectName
     * 
     */
    protected Map<String, String> remoteProjectNames;
    protected DataTransferManager dataTransferManager;
    protected PreferenceUtils preferenceUtils;

    public AddProjectToSessionWizard(IncomingProjectNegotiation process,
        DataTransferManager dataTransferManager,
        PreferenceUtils preferenceUtils, JID peer, List<FileList> fileLists,
        Map<String, String> projectNames) {
        this.process = process;
        this.peer = peer;
        this.fileLists = fileLists;
        this.remoteProjectNames = projectNames;
        this.dataTransferManager = dataTransferManager;
        this.preferenceUtils = preferenceUtils;
        setWindowTitle("Add Projects");
        
        /** holds if the wizard close is because of an exception or not */
        isExceptionCancel = false;      

        this.setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        // this.namePage = new EnterProjectNamePage(dataTransferManager,
        // preferenceUtils, fileLists, peer, remoteProjectName, wizardDialog);
        this.namePage = new EnterProjectNamePage(dataTransferManager,
            preferenceUtils, fileLists, peer, this.remoteProjectNames,
            wizardDialog);
        addPage(namePage);
    }

    @Override
    public boolean performFinish() {

        Map<String, IProject> sources = new HashMap<String, IProject>();
        final Map<String, String> projectNames = new HashMap<String, String>();
        final Map<String, Boolean> skipProjectSyncing = new HashMap<String, Boolean>();
        final boolean useVersionControl = namePage.useVersionControl();

        for (FileList fList : this.fileLists) {
            sources.put(fList.getProjectID(),
                namePage.getSourceProject(fList.getProjectID()));
            projectNames.put(fList.getProjectID(),
                namePage.getTargetProjectName(fList.getProjectID()));
            skipProjectSyncing.put(
                fList.getProjectID(),
                new Boolean(namePage.isSyncSkippingSelected(fList
                    .getProjectID())));
        }

        /*
         * Ask the user whether to overwrite local resources only if resources
         * are supposed to be overwritten based on the synchronization options
         * and if there are differences between the remote and local project.
         */
        Map<String, FileListDiff> projectsToOverrideWithDiff = new HashMap<String, FileListDiff>();
        for (String projectID : sources.keySet()) {
            if (namePage.overwriteProjectResources(projectID)
                && !preferenceUtils.isAutoReuseExisting()) {
                FileListDiff diff;

                if (!sources.get(projectID).isOpen()) {
                    try {
                        sources.get(projectID).open(null);
                    } catch (CoreException e1) {
                        log.debug(
                            "An error occur while opening the source file", e1);
                    }
                }

                try {
                    diff = FileListDiff.diff(
                        new FileList(sources.get(projectID)),
                        process.getRemoteFileList(projectID));
                } catch (CoreException e) {
                    MessageDialog.openError(getShell(),
                        "Error computing FileList",
                        "Could not compute local FileList: " + e.getMessage());
                    return false;
                }
                if (diff.getRemovedPaths().size() > 0
                    || diff.getAlteredPaths().size() > 0) {
                    projectsToOverrideWithDiff.put(sources.get(projectID)
                        .getName(), diff);
                }
            }
        }
        if (!confirmOverwritingProjectResources(projectsToOverrideWithDiff))
            return false;
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {

                        // AddProjectToSessionWizard.this.process.accept(source,
                        // SubMonitor.convert(monitor), target, skip);
                        AddProjectToSessionWizard.this.process.accept(
                            projectNames, SubMonitor.convert(monitor),
                            skipProjectSyncing, useVersionControl);
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
                isExceptionCancel = true;
                showCancelMessage(jid, errorMsg, cancelLocation);
                wizardDialog.close();
            }
        });
    }

    @Override
    public boolean performCancel() {
        if (!isExceptionCancel) {
            if (!Utils
                .popUpYesNoQuestion(
                    "Leaving the Session",
                    "The session participants must remain synchronised at all times."
                        + " Declining an invitation will therefore eject you from the session. "
                        + " Are you sure you want to leave?", false)) {
                return false;
            }
            Utils.runSafeAsync(log, new Runnable() {
                public void run() {
                    process.localCancel(null, CancelOption.NOTIFY_PEER);
                }
            });
        }
        isExceptionCancel = false;
        return true;
    }

    public boolean confirmOverwritingProjectResources(
        final Map<String, FileListDiff> everyThing) {
        try {
            return Utils.runSWTSync(new Callable<Boolean>() {
                public Boolean call() {

                    String message = "Each project you are accepting will be synchronised with the inviter's copy. It has been found that some of your local files differ from those of the inviter.\n\n"
                        + "All local differences will be overwritten!\n\n"
                        + "Press \"No\" and then select \"Create copy...\" if you are unsure.\n"
                        + "Press \"Details\" to find out what changes will occur if you proceed.\n\n"
                        + "Do you want to proceed?";

                    String PID = Saros.SAROS;
                    MultiStatus info = new MultiStatus(PID, 1, message, null);
                    for (String projectName : everyThing.keySet()) {
                        FileListDiff diff = everyThing.get(projectName);
                        info.add(new Status(IStatus.INFO, PID, 1,
                            "Following files in project '" + projectName
                                + "' are affected:", null));
                        for (IPath path : diff.getRemovedPaths()) {
                            info.add(new Status(IStatus.WARNING, PID, 1,
                                "  File will be removed: " + path.toOSString(),
                                null));
                        }
                        for (IPath path : diff.getAlteredPaths()) {
                            info.add(new Status(IStatus.WARNING, PID, 1,
                                "  File will be overwritten: "
                                    + path.toOSString(), null));
                        }
                        for (IPath path : diff.getAddedPaths()) {
                            info.add(new Status(IStatus.INFO, PID, 1,
                                "  File will be added: " + path.toOSString(),
                                null));
                        }
                        info.add(new Status(IStatus.INFO, PID, 1, "", null));
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
