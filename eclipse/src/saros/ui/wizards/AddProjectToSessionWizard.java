package saros.ui.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import saros.Saros;
import saros.SarosPluginContext;
import saros.editor.internal.EditorAPI;
import saros.filesystem.IChecksumCache;
import saros.filesystem.ResourceAdapterFactory;
import saros.monitoring.ProgressMonitorAdapterFactory;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.CancelListener;
import saros.negotiation.FileList;
import saros.negotiation.FileListDiff;
import saros.negotiation.FileListFactory;
import saros.negotiation.NegotiationTools;
import saros.negotiation.NegotiationTools.CancelLocation;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.ProjectNegotiation;
import saros.negotiation.ProjectNegotiationData;
import saros.net.IConnectionManager;
import saros.net.xmpp.JID;
import saros.preferences.Preferences;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.DialogUtils;
import saros.ui.util.SWTUtils;
import saros.ui.views.SarosView;
import saros.ui.wizards.dialogs.WizardDialogAccessable;
import saros.ui.wizards.pages.EnterProjectNamePage;
import saros.util.ThreadUtils;

public class AddProjectToSessionWizard extends Wizard {

  private static Logger log = Logger.getLogger(AddProjectToSessionWizard.class);

  private EnterProjectNamePage namePage;
  private WizardDialogAccessable wizardDialog;
  private AbstractIncomingProjectNegotiation negotiation;
  private JID peer;

  private boolean isExceptionCancel;

  private final ResourceMappingStorage mappingStorage;

  @Inject private IChecksumCache checksumCache;

  @Inject private IConnectionManager connectionManager;

  @Inject private Preferences preferences;

  @Inject private IPreferenceStore preferenceStore;

  @Inject private ISarosSessionManager sessionManager;

  private static class OverwriteErrorDialog extends ErrorDialog {

    public OverwriteErrorDialog(
        Shell parentShell, String dialogTitle, String dialogMessage, IStatus status) {
      super(
          parentShell,
          dialogTitle,
          dialogMessage,
          status,
          IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
      super.createButtonsForButtonBar(parent);
      Button ok = getButton(IDialogConstants.OK_ID);
      ok.setText(Messages.JoinSessionWizard_yes);
      Button no =
          createButton(parent, IDialogConstants.CANCEL_ID, Messages.JoinSessionWizard_no, true);
      no.moveBelow(ok);
      no.setFocus();
    }
  }

  private final CancelListener cancelListener =
      new CancelListener() {

        @Override
        public void canceled(final NegotiationTools.CancelLocation location, final String message) {
          cancelWizard(peer, message, location);
        }
      };

  public AddProjectToSessionWizard(AbstractIncomingProjectNegotiation negotiation) {

    SarosPluginContext.initComponent(this);

    this.mappingStorage = new ResourceMappingStorage(preferenceStore);

    this.negotiation = negotiation;
    this.peer = negotiation.getPeer();

    setWindowTitle(Messages.AddProjectToSessionWizard_title);
    setHelpAvailable(true);
    setNeedsProgressMonitor(true);
    setDefaultPageImageDescriptor(
        ImageManager.getImageDescriptor(ImageManager.WIZBAN_SESSION_ADD_PROJECTS));
    /* holds if the wizard close is because of an exception or not */
    isExceptionCancel = false;
  }

  @Override
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);
    negotiation.addCancelListener(cancelListener);
  }

  public void setWizardDlg(WizardDialogAccessable wizardDialog) {
    this.wizardDialog = wizardDialog;
  }

  @Override
  public void addPages() {
    ISarosSession session = sessionManager.getSession();

    if (session == null) return;

    final Map<String, String> lastProjectNameMapping = mappingStorage.getMapping(peer);

    namePage =
        new EnterProjectNamePage(
            session,
            connectionManager,
            preferences,
            peer,
            negotiation.getProjectNegotiationData(),
            lastProjectNameMapping);

    addPage(namePage);
  }

  @Override
  public boolean performFinish() {

    if (namePage == null) return true;

    final Map<String, IProject> targetProjectMapping = getTargetProjectMapping();

    final Collection<IEditorPart> openEditors =
        getOpenEditorsForSharedProjects(targetProjectMapping.values());

    final List<IEditorPart> dirtyEditors = new ArrayList<IEditorPart>();

    boolean containsDirtyEditors = false;

    for (IEditorPart editor : openEditors) {
      if (editor.isDirty()) {
        containsDirtyEditors = true;
        dirtyEditors.add(editor);
      }
    }

    if (containsDirtyEditors) {
      SWTUtils.runSafeSWTAsync(
          log,
          new Runnable() {
            @Override
            public void run() {
              if (AddProjectToSessionWizard.this.getShell().isDisposed()) return;

              displaySaveDirtyEditorsDialog(dirtyEditors);
            }
          });

      return false;
    }

    final Map<String, FileListDiff> modifiedResources;

    try {
      modifiedResources = createProjectsAndGetModifiedResources(targetProjectMapping);
    } catch (CoreException e) {
      log.error("could not compute local file list", e);
      MessageDialog.openError(
          getShell(),
          "Error computing file list",
          "Could not compute local file list: " + e.getMessage());
      return false;
    } catch (RuntimeException e) {
      log.error("internal error while computing file list", e);
      MessageDialog.openError(
          getShell(), "Error computing file list", "Internal error: " + e.getMessage());

      return false;
    }

    if (!confirmOverwritingResources(modifiedResources)) return false;

    storeCurrentProjectNameMapping(peer);

    triggerProjectNegotiation(targetProjectMapping, openEditors);

    return true;
  }

  public void cancelWizard(
      final JID jid, final String errorMsg, final CancelLocation cancelLocation) {

    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {
          @Override
          public void run() {
            Shell shell = wizardDialog.getShell();
            if (shell == null || shell.isDisposed()) return;
            isExceptionCancel = true;
            wizardDialog.close();
          }
        });

    SWTUtils.runSafeSWTAsync(
        log,
        new Runnable() {
          @Override
          public void run() {
            showCancelMessage(jid, errorMsg, cancelLocation);
          }
        });
  }

  @Override
  public boolean performCancel() {
    if (!isExceptionCancel) {
      ISarosSession session = sessionManager.getSession();
      if (session != null
          && !session.isHost()
          && !DialogUtils.popUpYesNoQuestion(
              Messages.AddProjectToSessionWizard_leave_session,
              Messages.AddProjectToSessionWizard_leave_session_text,
              false)) {
        return false;
      }
      ThreadUtils.runSafeAsync(
          "CancelAddProjectWizard",
          log,
          new Runnable() {
            @Override
            public void run() {
              negotiation.localCancel(null, CancelOption.NOTIFY_PEER);
            }
          });
    }
    isExceptionCancel = false;
    return true;
  }

  private void triggerProjectNegotiation(
      final Map<String, IProject> targetProjectMapping,
      final Collection<IEditorPart> editorsToClose) {

    final Job job =
        new Job("Synchronizing") {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            try {

              SWTUtils.runSafeSWTSync(
                  log,
                  new Runnable() {
                    @Override
                    public void run() {
                      /*
                       * close all editors to avoid any conflicts.
                       */
                      for (final IEditorPart editor : editorsToClose)
                        EditorAPI.closeEditor(editor, true);
                    }
                  });

              final IWorkspace workspace = ResourcesPlugin.getWorkspace();

              final IWorkspaceDescription description = workspace.getDescription();

              final boolean isAutoBuilding = description.isAutoBuilding();

              if (isAutoBuilding) {
                description.setAutoBuilding(false);
                try {
                  workspace.setDescription(description);
                } catch (CoreException e) {
                  log.warn("could not disable auto building", e);
                }
              }

              final Map<String, saros.filesystem.IProject> convertedMapping =
                  new HashMap<String, saros.filesystem.IProject>();

              for (final Entry<String, IProject> entry : targetProjectMapping.entrySet()) {
                convertedMapping.put(
                    entry.getKey(), ResourceAdapterFactory.create(entry.getValue()));
              }

              final ProjectNegotiation.Status status =
                  negotiation.run(convertedMapping, ProgressMonitorAdapterFactory.convert(monitor));

              if (isAutoBuilding) {
                description.setAutoBuilding(true);
                try {
                  workspace.setDescription(description);
                } catch (CoreException e) {
                  log.warn("could not re-enable auto building", e);
                }
              }

              if (status != ProjectNegotiation.Status.OK) return Status.CANCEL_STATUS;

              final List<String> projectNames = new ArrayList<String>();

              for (final IProject project : targetProjectMapping.values())
                projectNames.add(project.getName());

              SarosView.showNotification(
                  Messages.AddProjectToSessionWizard_synchronize_finished_notification_title,
                  MessageFormat.format(
                      Messages.AddProjectToSessionWizard_synchronize_finished_notification_text,
                      StringUtils.join(projectNames, ", ")));

            } catch (RuntimeException e) {
              log.error("unknown error during project negotiation: " + e.getMessage(), e);
              return Status.CANCEL_STATUS;
            } finally {
              SWTUtils.runSafeSWTAsync(
                  log,
                  new Runnable() {
                    @Override
                    public void run() {
                      for (IEditorPart editor : editorsToClose) {
                        if (((IFileEditorInput) editor.getEditorInput()).getFile().exists())
                          EditorAPI.openEditor(editor);
                      }
                    }
                  });
            }
            return Status.OK_STATUS;
          }
        };
    job.setUser(true);
    job.schedule();
  }

  private boolean confirmOverwritingResources(final Map<String, FileListDiff> modifiedResources) {

    String message = Messages.AddProjectToSessionWizard_synchronize_projects;
    String pluginID = Saros.PLUGIN_ID;

    MultiStatus info = new MultiStatus(pluginID, 1, message, null);

    // TODO include folder information

    for (String projectName : modifiedResources.keySet()) {

      FileListDiff diff = modifiedResources.get(projectName);

      info.add(
          new Status(
              IStatus.INFO,
              pluginID,
              1,
              MessageFormat.format(Messages.AddProjectToSessionWizard_files_affected, projectName),
              null));

      for (String path : diff.getRemovedFiles()) {
        info.add(
            new Status(
                IStatus.WARNING,
                pluginID,
                1,
                MessageFormat.format(Messages.AddProjectToSessionWizard_file_toRemove, path),
                null));
      }

      for (String path : diff.getAlteredFiles()) {
        info.add(
            new Status(
                IStatus.WARNING,
                pluginID,
                1,
                MessageFormat.format(Messages.AddProjectToSessionWizard_file_overwritten, path),
                null));
      }

      info.add(new Status(IStatus.INFO, pluginID, 1, "", null)); // $NON-NLS-1$
    }

    return new OverwriteErrorDialog(
                getShell(), Messages.AddProjectToSessionWizard_delete_local_changes, null, info)
            .open()
        == IDialogConstants.OK_ID;
  }

  private void showCancelMessage(JID jid, String errorMsg, CancelLocation cancelLocation) {

    final String peerJid = jid.getBase();
    final Shell shell = SWTUtils.getShell();

    if (errorMsg != null) {
      switch (cancelLocation) {
        case LOCAL:
          DialogUtils.openErrorMessageDialog(
              shell,
              Messages.AddProjectToSessionWizard_invitation_canceled,
              MessageFormat.format(
                  Messages.AddProjectToSessionWizard_invitation_canceled_text, errorMsg));
          break;
        case REMOTE:
          DialogUtils.openErrorMessageDialog(
              shell,
              Messages.AddProjectToSessionWizard_invitation_canceled,
              MessageFormat.format(
                  Messages.AddProjectToSessionWizard_invitation_canceled_text2, peerJid, errorMsg));
      }
    } else {
      switch (cancelLocation) {
        case LOCAL:
          break;
        case REMOTE:
          DialogUtils.openInformationMessageDialog(
              shell,
              Messages.AddProjectToSessionWizard_invitation_canceled,
              MessageFormat.format(
                  Messages.AddProjectToSessionWizard_invitation_canceled_text3, peerJid));
      }
    }
  }

  private Collection<IEditorPart> getOpenEditorsForSharedProjects(Collection<IProject> projects) {

    List<IEditorPart> openEditors = new ArrayList<IEditorPart>();
    Set<IEditorPart> editors = EditorAPI.getOpenEditors();

    for (IProject project : projects) {
      for (IEditorPart editor : editors) {
        if (editor.getEditorInput() instanceof IFileEditorInput) {
          IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
          if (project.equals(file.getProject())) openEditors.add(editor);
        }
      }
    }
    return openEditors;
  }

  /**
   * Returns all modified resources (either changed or deleted) for the current project mapping.
   * Creates non existing projects if necessary.
   */
  private Map<String, FileListDiff> createProjectsAndGetModifiedResources(
      final Map<String, IProject> projectMapping) throws CoreException {

    final Map<String, IProject> modifiedProjects = getModifiedProjects(projectMapping);
    final Map<String, FileListDiff> result = new HashMap<String, FileListDiff>();

    try {
      getContainer()
          .run(
              true,
              false,
              new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException {
                  try {

                    for (final IProject project : projectMapping.values()) {
                      if (!modifiedProjects.values().contains(project)) {

                        if (!project.exists()) project.create(null);
                      }

                      if (!project.isOpen()) project.open(null);
                    }

                    result.putAll(getModifiedResources(modifiedProjects, monitor));
                  } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                  } catch (RuntimeException e) {
                    throw new InvocationTargetException(e);
                  }
                }
              });
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();

      if (cause instanceof CoreException) throw (CoreException) cause;
      else if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      else throw new RuntimeException(cause.getMessage(), cause);
    } catch (InterruptedException e) {
      throw new RuntimeException("unexpected interruption", e);
    }

    return result;
  }

  /**
   * Returns all modified resources (either changed or deleted) for the current project mapping.
   *
   * <p><b>Important:</b> Do not call this inside the SWT Thread. This is a long running operation !
   */
  private Map<String, FileListDiff> getModifiedResources(
      final Map<String, IProject> projectMapping, final IProgressMonitor monitor)
      throws CoreException {

    final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

    final ISarosSession session = sessionManager.getSession();

    // FIXME the wizard should handle the case that the session may stop in
    // the meantime !

    if (session == null) throw new IllegalStateException("no session running");

    final SubMonitor subMonitor =
        SubMonitor.convert(
            monitor, "Searching for files that will be modified...", projectMapping.size());

    for (final Entry<String, IProject> entry : projectMapping.entrySet()) {

      final String projectID = entry.getKey();
      final IProject project = entry.getValue();

      final saros.filesystem.IProject adaptedProject =
          ResourceAdapterFactory.create(entry.getValue());

      if (!session.isShared(adaptedProject)) project.refreshLocal(IResource.DEPTH_INFINITE, null);

      final FileList localFileList;

      try {
        localFileList =
            FileListFactory.createFileList(
                adaptedProject,
                checksumCache,
                ProgressMonitorAdapterFactory.convert(
                    subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS)));
      } catch (IOException e) {
        Throwable cause = e.getCause();

        if (cause instanceof CoreException) throw (CoreException) cause;

        throw new CoreException(
            new org.eclipse.core.runtime.Status(
                IStatus.ERROR, Saros.PLUGIN_ID, "failed to compute local file list", e));
      }

      final ProjectNegotiationData data = negotiation.getProjectNegotiationData(projectID);

      final FileListDiff diff = FileListDiff.diff(localFileList, data.getFileList());

      if (!diff.getRemovedFolders().isEmpty()
          || !diff.getRemovedFiles().isEmpty()
          || !diff.getAlteredFiles().isEmpty()) {
        modifiedResources.put(project.getName(), diff);
      }
    }

    return modifiedResources;
  }

  /**
   * Returns a project mapping that contains all projects that will be modified on synchronization.
   */
  private Map<String, IProject> getModifiedProjects(Map<String, IProject> projectMapping) {
    Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

    for (Entry<String, IProject> entry : projectMapping.entrySet()) {
      if (!namePage.overwriteResources(entry.getKey())) continue;

      modifiedProjects.put(entry.getKey(), entry.getValue());
    }

    return modifiedProjects;
  }

  /**
   * Returns the project ids and their target project as selected in the {@link
   * EnterProjectNamePage}.
   *
   * <p>This method must only be called after the page in completed state !
   */
  private Map<String, IProject> getTargetProjectMapping() {

    final Map<String, IProject> result = new HashMap<String, IProject>();

    for (final ProjectNegotiationData data : negotiation.getProjectNegotiationData()) {
      final String projectID = data.getProjectID();

      result.put(
          projectID,
          ResourcesPlugin.getWorkspace()
              .getRoot()
              .getProject(namePage.getTargetProjectName(projectID)));
    }

    return result;
  }

  private void displaySaveDirtyEditorsDialog(final List<IEditorPart> dirtyEditors) {
    int max = Math.min(20, dirtyEditors.size());
    int more = dirtyEditors.size() - max;

    List<String> dirtyEditorNames = new ArrayList<String>();

    for (IEditorPart editor : dirtyEditors.subList(0, max)) dirtyEditorNames.add(editor.getTitle());

    Collections.sort(dirtyEditorNames);

    if (more > 0)
      dirtyEditorNames.add(
          MessageFormat.format(
              Messages.AddProjectToSessionWizard_unsaved_changes_dialog_more, more));

    String allDirtyEditorNames = StringUtils.join(dirtyEditorNames, ", ");

    String dialogText =
        MessageFormat.format(
            Messages.AddProjectToSessionWizard_unsaved_changes_dialog_text, allDirtyEditorNames);

    boolean proceed =
        DialogUtils.openQuestionMessageDialog(
            getShell(),
            Messages.AddProjectToSessionWizard_unsaved_changes_dialog_title,
            dialogText);

    /*
     * The wizard can be closed automatically and so 'proceed' would be true
     * if this happens although the user did not clicked anything.
     */
    if (getShell().isDisposed()) return;

    if (proceed) {
      for (IEditorPart editor : dirtyEditors) editor.doSave(new NullProgressMonitor());
    }
  }

  private void storeCurrentProjectNameMapping(final JID jid) {
    Map<String, String> currentProjectNameMapping = new HashMap<>();

    for (final ProjectNegotiationData data : negotiation.getProjectNegotiationData()) {
      final String projectID = data.getProjectID();
      currentProjectNameMapping.put(
          data.getProjectName(), namePage.getTargetProjectName(projectID));
    }

    mappingStorage.updateMapping(jid, currentProjectNameMapping);
  }

  private static final class ResourceMappingStorage {
    private static final String RESOURCE_MAPPING_STORE_PREFIX = "resource.mapping.storage.";

    private static final CharSequence DELIMITER = ":";

    private static final CharSequence ESCAPE_CHARACTER = "\\";

    private static final CharSequence ESCAPE_SEQUENCE =
        ESCAPE_CHARACTER.toString() + DELIMITER.toString();

    private static final Pattern SPLIT_PATTERN =
        Pattern.compile(
            "(?<!"
                + Pattern.quote(ESCAPE_CHARACTER.toString())
                + ")"
                + Pattern.quote(DELIMITER.toString()));

    private final IPreferenceStore store;

    public ResourceMappingStorage(final IPreferenceStore store) {
      this.store = store;
    }

    public Map<String, String> getMapping(final JID jid) {
      final Map<String, String> result = new HashMap<>();

      final String mapping = store.getString(RESOURCE_MAPPING_STORE_PREFIX + jid.getBase());

      if (mapping.isEmpty()) return result;

      final String[] names = SPLIT_PATTERN.split(mapping);

      for (int i = 0; i < names.length - 1; i += 2) {
        if (names[i].isEmpty() || names[i + 1].isEmpty()) continue;

        result.put(unescape(names[i]), unescape(names[i + 1]));
      }

      return result;
    }

    public void updateMapping(final JID jid, final Map<String, String> resourceNameMapping) {

      final Map<String, String> lastResourceNameMapping = getMapping(jid);

      final Set<String> resourceNameDestinations = new HashSet<>(resourceNameMapping.values());

      /*
       * Remove existing mappings to avoid following problem:
       * 1. We add A->A, B->B, C->C
       * 2. Afterwards  A->A, B->C
       * 3. When we now lookup A,B,C we will get A->A, B->C, C->C which is an invalid mapping
       *
       * In other words, ensure that the mapping is injective.
       */

      final Iterator<Entry<String, String>> it = lastResourceNameMapping.entrySet().iterator();

      while (it.hasNext()) {

        final Entry<String, String> entry = it.next();

        if (resourceNameDestinations.contains(entry.getValue())) it.remove();
      }

      lastResourceNameMapping.putAll(resourceNameMapping);

      if (lastResourceNameMapping.isEmpty()) return;

      final StringBuilder builder = new StringBuilder();

      for (final Entry<String, String> entry : lastResourceNameMapping.entrySet()) {
        builder.append(escape(entry.getKey()));
        builder.append(DELIMITER);
        builder.append(escape(entry.getValue()));
        builder.append(DELIMITER);
      }

      builder.setLength(builder.length() - DELIMITER.length());

      store.setValue(RESOURCE_MAPPING_STORE_PREFIX + jid.getBase(), builder.toString());
    }

    private static String escape(final String value) {
      return value.replace(DELIMITER, ESCAPE_SEQUENCE);
    }

    private static String unescape(final String value) {
      return value.replace(ESCAPE_SEQUENCE, DELIMITER);
    }
  }
}
