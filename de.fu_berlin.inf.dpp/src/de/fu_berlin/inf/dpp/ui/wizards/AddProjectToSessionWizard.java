package de.fu_berlin.inf.dpp.ui.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.editor.internal.EditorAPI;
import de.fu_berlin.inf.dpp.filesystem.IChecksumCache;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.invitation.FileList;
import de.fu_berlin.inf.dpp.invitation.FileListDiff;
import de.fu_berlin.inf.dpp.invitation.FileListFactory;
import de.fu_berlin.inf.dpp.invitation.IncomingProjectNegotiation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelLocation;
import de.fu_berlin.inf.dpp.invitation.ProcessTools.CancelOption;
import de.fu_berlin.inf.dpp.invitation.ProjectNegotiation;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.DataTransferManager;
import de.fu_berlin.inf.dpp.preferences.PreferenceUtils;
import de.fu_berlin.inf.dpp.project.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import de.fu_berlin.inf.dpp.ui.views.SarosView;
import de.fu_berlin.inf.dpp.ui.wizards.dialogs.WizardDialogAccessable;
import de.fu_berlin.inf.dpp.ui.wizards.pages.EnterProjectNamePage;
import de.fu_berlin.inf.dpp.util.ThreadUtils;

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

    @Inject
    protected EditorAPI editorAPI;

    @Inject
    private IChecksumCache checksumCache;

    @Inject
    private DataTransferManager dataTransferManager;

    @Inject
    private PreferenceUtils preferenceUtils;

    @Inject
    private ISarosSessionManager sessionManager;

    private static class OverwriteErrorDialog extends ErrorDialog {

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

    public AddProjectToSessionWizard(IncomingProjectNegotiation process,
        JID peer, List<FileList> fileLists, Map<String, String> projectNames) {

        SarosPluginContext.initComponent(this);

        this.process = process;
        this.peer = peer;
        this.fileLists = fileLists;
        this.remoteProjectNames = projectNames;
        setWindowTitle(Messages.AddProjectToSessionWizard_title);
        setHelpAvailable(true);
        setNeedsProgressMonitor(true);

        process.setProjectInvitationUI(this);

        /** holds if the wizard close is because of an exception or not */
        isExceptionCancel = false;

    }

    @Override
    public void addPages() {
        ISarosSession session = sessionManager.getSarosSession();

        if (session == null)
            return;

        namePage = new EnterProjectNamePage(session, dataTransferManager,
            preferenceUtils, fileLists, peer, this.remoteProjectNames);

        addPage(namePage);
    }

    @Override
    public boolean performFinish() {

        if (namePage == null)
            return true;

        final Map<String, IProject> sources = new HashMap<String, IProject>();
        final Map<String, String> projectNames = new HashMap<String, String>();
        final boolean useVersionControl = namePage.useVersionControl();

        for (FileList fList : this.fileLists) {
            String projectName = namePage.getTargetProjectName(fList
                .getProjectID());

            projectNames.put(fList.getProjectID(), projectName);
            sources.put(fList.getProjectID(), ResourcesPlugin.getWorkspace()
                .getRoot().getProject(projectName));
        }

        List<IProject> existingProjects = new ArrayList<IProject>();

        for (IProject project : sources.values()) {
            if (project != null)
                existingProjects.add(project);
        }

        final Collection<IEditorPart> openEditors = getOpenEditorsForSharedProjects(existingProjects);

        final List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();

        boolean containsDirtyEditors = false;

        for (IEditorPart editor : openEditors) {
            if (editor.isDirty()) {
                containsDirtyEditors = true;
                dirtyEditors.add(editor);
            }
        }

        if (containsDirtyEditors) {
            SWTUtils.runSafeSWTAsync(log, new Runnable() {
                @Override
                public void run() {
                    if (AddProjectToSessionWizard.this.getShell().isDisposed()) {
                        return;
                    }

                    int max = Math.min(20, dirtyEditors.size());
                    int more = dirtyEditors.size() - max;

                    List<String> dirtyEditorNames = new ArrayList<String>();

                    for (IEditorPart editor : dirtyEditors.subList(0, max))
                        dirtyEditorNames.add(editor.getTitle());

                    Collections.sort(dirtyEditorNames);

                    if (more > 0)
                        dirtyEditorNames.add(MessageFormat
                            .format(
                                Messages.AddProjectToSessionWizard_unsaved_changes_dialog_more,
                                more));

                    String allDirtyEditorNames = StringUtils.join(
                        dirtyEditorNames, ", ");

                    String dialogText = MessageFormat
                        .format(
                            Messages.AddProjectToSessionWizard_unsaved_changes_dialog_text,
                            allDirtyEditorNames);

                    boolean proceed = DialogUtils.openQuestionMessageDialog(
                        AddProjectToSessionWizard.this.getShell(),
                        Messages.AddProjectToSessionWizard_unsaved_changes_dialog_title,
                        dialogText);

                    if (proceed) {
                        for (IEditorPart editor : openEditors)
                            editor.doSave(new NullProgressMonitor());
                    }
                }
            });

            return false;
        }

        /*
         * Ask the user whether to overwrite local resources only if resources
         * are supposed to be overwritten based on the synchronization options
         * and if there are differences between the remote and local project.
         */
        final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
        final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        modifiedProjects.putAll(getModifiedProjects(sources));

        try {
            getContainer().run(true, false, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                    try {
                        modifiedResources.putAll(calculateModifiedResources(
                            modifiedProjects, monitor));
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        } catch (Exception e) {
            Throwable cause = e.getCause();

            if (cause instanceof CoreException) {
                MessageDialog.openError(getShell(),
                    "Error computing file list",
                    "Could not compute local file list: " + cause.getMessage());
            } else {
                MessageDialog
                    .openError(
                        getShell(),
                        "Error computing file list",
                        "Internal error while computing local file list: "
                            + (cause == null ? e.getMessage() : cause
                                .getMessage()));
            }

            return false;
        }
        if (!confirmOverwritingResources(modifiedResources))
            return false;

        /*
         * close all editors to avoid any conflicts. this will be needed for
         * rsync as it needs to move files around the file system
         */
        for (IEditorPart editor : openEditors)
            editorAPI.closeEditor(editor);

        Job job = new Job("Synchronizing") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    ProjectNegotiation.Status status = process.accept(
                        projectNames, monitor, useVersionControl);

                    if (status != ProjectNegotiation.Status.OK)
                        return Status.CANCEL_STATUS;

                    SarosView
                        .showNotification(
                            Messages.AddProjectToSessionWizard_synchronize_finished_notification_title,
                            MessageFormat
                                .format(
                                    Messages.AddProjectToSessionWizard_synchronize_finished_notification_text,
                                    StringUtils.join(projectNames.values(),
                                        ", ")));

                } catch (Exception e) {
                    log.error(
                        "unkown error during project negotiation: "
                            + e.getMessage(), e);
                    return Status.CANCEL_STATUS;
                } finally {
                    SWTUtils.runSafeSWTAsync(log, new Runnable() {
                        @Override
                        public void run() {
                            for (IEditorPart editor : openEditors) {
                                if (((IFileEditorInput) editor.getEditorInput())
                                    .getFile().exists())
                                    editorAPI.openEditor(editor);
                            }
                        }
                    });
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(true);
        job.schedule();

        return true;
    }

    public void cancelWizard(final JID jid, final String errorMsg,
        final CancelLocation cancelLocation) {

        SWTUtils.runSafeSWTAsync(log, new Runnable() {
            @Override
            public void run() {
                Shell shell = wizardDialog.getShell();
                if (shell == null || shell.isDisposed())
                    return;
                isExceptionCancel = true;
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

    @Override
    public boolean performCancel() {
        if (!isExceptionCancel) {
            if (!DialogUtils.popUpYesNoQuestion(
                Messages.AddProjectToSessionWizard_leave_session,
                Messages.AddProjectToSessionWizard_leave_session_text, false)) {
                return false;
            }
            ThreadUtils.runSafeAsync("CancelAddProjectWizard", log,
                new Runnable() {
                    @Override
                    public void run() {
                        process.localCancel(null, CancelOption.NOTIFY_PEER);
                    }
                });
        }
        isExceptionCancel = false;
        return true;
    }

    public boolean confirmOverwritingResources(
        final Map<String, FileListDiff> modifiedResources) {

        String message = Messages.AddProjectToSessionWizard_synchronize_projects;

        String PID = Saros.SAROS;
        MultiStatus info = new MultiStatus(PID, 1, message, null);
        for (String projectName : modifiedResources.keySet()) {
            FileListDiff diff = modifiedResources.get(projectName);
            info.add(new Status(IStatus.INFO, PID, 1,
                MessageFormat.format(
                    Messages.AddProjectToSessionWizard_files_affected,
                    projectName), null));
            for (String path : diff.getRemovedPaths()) {
                info.add(new Status(IStatus.WARNING, PID, 1, MessageFormat
                    .format(Messages.AddProjectToSessionWizard_file_toRemove,
                        path), null));
            }
            for (String path : diff.getAlteredPaths()) {
                info.add(new Status(IStatus.WARNING, PID, 1, MessageFormat
                    .format(
                        Messages.AddProjectToSessionWizard_file_overwritten,
                        path), null));
            }
            for (String path : diff.getAddedPaths()) {
                info.add(new Status(IStatus.INFO, PID, 1, MessageFormat.format(
                    Messages.AddProjectToSessionWizard_file_added, path), null));
            }
            info.add(new Status(IStatus.INFO, PID, 1, "", null)); //$NON-NLS-1$
        }
        return new OverwriteErrorDialog(getShell(),
            Messages.AddProjectToSessionWizard_delete_local_changes, null, info)
            .open() == IDialogConstants.OK_ID;
    }

    private void showCancelMessage(JID jid, String errorMsg,
        CancelLocation cancelLocation) {

        final String peer = jid.getBase();
        final Shell shell = SWTUtils.getShell();

        if (errorMsg != null) {
            switch (cancelLocation) {
            case LOCAL:
                DialogUtils
                    .openErrorMessageDialog(
                        shell,
                        Messages.AddProjectToSessionWizard_invitation_cancelled,
                        MessageFormat
                            .format(
                                Messages.AddProjectToSessionWizard_invitation_cancelled_text,
                                errorMsg));
                break;
            case REMOTE:
                DialogUtils
                    .openErrorMessageDialog(
                        shell,
                        Messages.AddProjectToSessionWizard_invitation_cancelled,
                        MessageFormat
                            .format(
                                Messages.AddProjectToSessionWizard_invitation_cancelled_text2,
                                peer, errorMsg));
            }
        } else {
            switch (cancelLocation) {
            case LOCAL:
                break;
            case REMOTE:
                DialogUtils
                    .openInformationMessageDialog(
                        shell,
                        Messages.AddProjectToSessionWizard_invitation_cancelled,
                        MessageFormat
                            .format(
                                Messages.AddProjectToSessionWizard_invitation_cancelled_text3,
                                peer));
            }
        }
    }

    public void setWizardDlg(WizardDialogAccessable wizardDialog) {
        this.wizardDialog = wizardDialog;
    }

    private Collection<IEditorPart> getOpenEditorsForSharedProjects(
        Collection<IProject> projects) {

        List<IEditorPart> openEditors = new ArrayList<IEditorPart>();
        Set<IEditorPart> editors = EditorAPI.getOpenEditors();

        for (IProject project : projects) {
            for (IEditorPart editor : editors) {
                if (editor.getEditorInput() instanceof IFileEditorInput) {
                    IFile file = ((IFileEditorInput) editor.getEditorInput())
                        .getFile();
                    if (project.equals(file.getProject()))
                        openEditors.add(editor);
                }
            }
        }
        return openEditors;
    }

    /**
     * Returns all modified resources (either changed or deleted) for the
     * current project mapping.
     */
    private Map<String, FileListDiff> calculateModifiedResources(
        Map<String, IProject> projectMapping, IProgressMonitor monitor)
        throws IOException {
        Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

        ISarosSession session = sessionManager.getSarosSession();

        // FIXME the wizard should handle the case that the session may stop in
        // the meantime !

        if (session == null)
            throw new IllegalStateException("no session running");

        SubMonitor subMonitor = SubMonitor.convert(monitor,
            "Searching for files that will be modified...",
            projectMapping.size() * 2);

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {

            String projectID = entry.getKey();
            de.fu_berlin.inf.dpp.filesystem.IProject project = ResourceAdapterFactory
                .create(entry.getValue());

            if (!project.isOpen())
                project.open();

            /*
             * do not refresh already partially shared projects as this may
             * trigger resource change events
             */
            try {
                if (!session.isShared(project))
                    project.refreshLocal();
            } catch (IOException e) {
                log.warn("could not refresh project: " + project, e);
            }

            FileList remoteFileList = process.getRemoteFileList(projectID);

            if (session.isShared(project)) {
                FileList sharedFileList = FileListFactory.createFileList(
                    project, session.getSharedResources(project),
                    checksumCache, true,
                    subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS));

                // FIXME FileList objects should be immutable after creation
                remoteFileList.getPaths().addAll(sharedFileList.getPaths());
            } else
                subMonitor.worked(1);

            FileListDiff diff = FileListDiff.diff(FileListFactory
                .createFileList(project, null, checksumCache, true,
                    subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS)),
                remoteFileList);

            if (process.isPartialRemoteProject(projectID))
                diff.clearRemovedPaths();

            if (!diff.getRemovedPaths().isEmpty()
                || !diff.getAlteredPaths().isEmpty()) {
                modifiedResources.put(project.getName(), diff);
            }
        }
        return modifiedResources;
    }

    /**
     * Returns a project mapping that contains all projects that will be
     * modified on synchronization.
     * 
     * @SWT must be called in the SWT thread context
     */
    private Map<String, IProject> getModifiedProjects(
        Map<String, IProject> projectMapping) {
        Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

        for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {
            if (!namePage.overwriteResources(entry.getKey()))
                continue;

            modifiedProjects.put(entry.getKey(), entry.getValue());
        }

        return modifiedProjects;
    }
}
