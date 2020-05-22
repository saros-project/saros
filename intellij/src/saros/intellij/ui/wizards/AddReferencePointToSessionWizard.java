package saros.intellij.ui.wizards;

import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.Dimension;
import java.awt.Window;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.SarosPluginContext;
import saros.filesystem.IProject;
import saros.filesystem.checksum.IChecksumCache;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.editor.DocumentAPI;
import saros.intellij.editor.ProjectAPI;
import saros.intellij.filesystem.IntellijReferencePoint;
import saros.intellij.runtime.FilesystemRunner;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.intellij.ui.widgets.progress.ProgressMonitorAdapter;
import saros.intellij.ui.wizards.pages.HeaderPanel;
import saros.intellij.ui.wizards.pages.PageActionListener;
import saros.intellij.ui.wizards.pages.TextAreaPage;
import saros.intellij.ui.wizards.pages.referencepointselection.ReferencePointSelectionResult;
import saros.intellij.ui.wizards.pages.referencepointselection.SelectLocalReferencePointRepresentationPage;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.monitoring.SubProgressMonitor;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.CancelListener;
import saros.negotiation.FileList;
import saros.negotiation.FileListDiff;
import saros.negotiation.FileListFactory;
import saros.negotiation.NegotiationTools;
import saros.negotiation.NegotiationTools.CancelLocation;
import saros.negotiation.ProjectNegotiation;
import saros.negotiation.ProjectNegotiationData;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.util.ThreadUtils;

/**
 * Wizard for adding projects to a session.
 *
 * <p>It consists of a selection page, where the user can select how to handle the incoming project:
 *
 * <ul>
 *   <li>create a new project
 *   <li reuse an existing project
 * </ul>
 *       <p>If the option to reuse an existing project is chosen, a second page is displayed, that
 *       displays the files necessary to modify.
 *       <p>
 */
// TODO adjust remaining javadoc, variable/method names, user messages, and log messages
//  FIXME: Add facility for more than one project.
public class AddReferencePointToSessionWizard extends Wizard {
  private static final Logger log = Logger.getLogger(AddReferencePointToSessionWizard.class);

  public static final String SELECT_MODULE_REPRESENTATION_PAGE_ID = "selectModuleRepresentation";
  public static final String FILE_LIST_PAGE_ID = "fileListPage";

  private final String remoteProjectID;
  private final String remoteProjectName;

  private final AbstractIncomingProjectNegotiation negotiation;
  private final JID peer;

  private boolean triggered = false;

  /** projectID => Project */
  private final Map<String, IProject> localProjects;

  @Inject private IChecksumCache checksumCache;

  @Inject private ISarosSessionManager sessionManager;

  private final SelectLocalReferencePointRepresentationPage
      selectLocalReferencePointRepresentationPage;
  private final TextAreaPage fileListPage;

  private final PageActionListener selectProjectsPageListener =
      new PageActionListener() {
        @Override
        public void back() {
          // Nothing to do
        }

        /**
         * Extracts the local project's name (currently it has to be the same as the original
         * project) and either displays the filesChangedPage or triggers the project negotiation
         */
        @Override
        public void next() {
          DocumentAPI.saveAllDocuments();

          ReferencePointSelectionResult referencePointSelectionResult;

          try {
            referencePointSelectionResult =
                selectLocalReferencePointRepresentationPage.getModuleSelectionResult(
                    remoteProjectName);

          } catch (IllegalStateException e) {
            noisyCancel("Request to get directory selection result failed: " + e.getMessage(), e);

            return;
          }

          if (referencePointSelectionResult == null) {
            noisyCancel(
                "Could not find a directory selection result for the reference point "
                    + remoteProjectName,
                null);

            return;
          }

          Project project = referencePointSelectionResult.getProject();

          sessionManager.getSession().getComponent(SharedIDEContext.class).setProject(project);

          switch (referencePointSelectionResult.getLocalRepresentationOption()) {
            case CREATE_NEW_DIRECTORY:
              String newDirectoryName = referencePointSelectionResult.getNewDirectoryName();
              VirtualFile newDirectoryBaseDirectory =
                  referencePointSelectionResult.getNewDirectoryBaseDirectory();

              if (newDirectoryName == null || newDirectoryBaseDirectory == null) {
                noisyCancel("No valid new directory name or base path was given", null);

                return;
              }

              doNewDirectory(project, newDirectoryName, newDirectoryBaseDirectory);
              break;

            case USE_EXISTING_DIRECTORY:
              VirtualFile existingDirectory = referencePointSelectionResult.getExistingDirectory();

              if (existingDirectory == null) {
                noisyCancel("No valid existing directory was given", null);

                return;
              }

              doExistingDirectory(project, existingDirectory);
              break;

            default:
              noisyCancel(
                  "No valid option on how to represent the shared reference point was given", null);
          }
        }

        /**
         * Creates a new directory for the reference point and starts the project negotiation with
         * it.
         *
         * @param project the project to bind the reference point to
         * @param directoryName the name for the new directory
         * @param baseDirectory the base directory for the new directory
         */
        private void doNewDirectory(
            @NotNull Project project,
            @NotNull String directoryName,
            @NotNull VirtualFile baseDirectory) {

          VirtualFile existingResource = baseDirectory.findChild(directoryName);

          if (existingResource != null) {
            log.warn(
                "Directory '"
                    + directoryName
                    + "' could not be created as a resource with the same name already exists: "
                    + existingResource);

            cancelNegotiation("Failed to create reference point " + remoteProjectName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.Contact_saros_message_conditional,
                    MessageFormat.format(
                        Messages
                            .AddProjectToSessionWizard_directory_already_exists_message_condition,
                        directoryName)),
                Messages.AddProjectToSessionWizard_directory_already_exists_title);

            return;
          }

          VirtualFile referencePointFile;

          try {
            referencePointFile =
                FilesystemRunner.runWriteAction(
                    () -> baseDirectory.createChildDirectory(this, directoryName),
                    ModalityState.defaultModalityState());

          } catch (IOException e) {
            log.error(
                "Failed to create the directory "
                    + directoryName
                    + " for reference point "
                    + remoteProjectName,
                e);

            cancelNegotiation("Failed to create reference point " + remoteProjectName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.Contact_saros_message_conditional,
                    MessageFormat.format(
                        Messages
                            .AddProjectToSessionWizard_directory_creation_failed_message_condition,
                        directoryName,
                        e.getMessage())),
                Messages.AddProjectToSessionWizard_directory_creation_failed_title);

            return;
          }

          if (ProjectAPI.isExcluded(project, referencePointFile)) {
            log.warn(
                "Could not share created directory "
                    + referencePointFile
                    + "  as it is excluded from the project scope.");

            cancelNegotiation("Failed to create reference point " + remoteProjectName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.AddProjectToSessionWizard_directory_excluded_message,
                    referencePointFile,
                    remoteProjectName),
                Messages.AddProjectToSessionWizard_directory_excluded_title);

            return;
          }

          IProject sharedReferencePoint;

          try {
            sharedReferencePoint = new IntellijReferencePoint(project, referencePointFile);

          } catch (IllegalArgumentException e) {
            log.error(
                "Failed to instantiate reference point '"
                    + remoteProjectName
                    + "' using the created directory "
                    + referencePointFile,
                e);

            cancelNegotiation("Failed to create reference point " + remoteProjectName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages
                        .AddProjectToSessionWizard_new_reference_point_instantiation_error_message,
                    referencePointFile,
                    remoteProjectName,
                    e.getMessage()),
                Messages.AddProjectToSessionWizard_new_reference_point_instantiation_error_title);

            return;
          }

          localProjects.put(remoteProjectID, sharedReferencePoint);

          triggerProjectNegotiation();
        }

        /**
         * Checks if the directory is valid and then starts the project negotiation with it.
         *
         * @param existingDirectory the existing directory to use for the project negotiation
         */
        private void doExistingDirectory(
            @NotNull Project project, @NotNull VirtualFile existingDirectory) {

          IProject referencePoint;

          try {
            referencePoint = new IntellijReferencePoint(project, existingDirectory);

          } catch (IllegalArgumentException e) {
            log.error(
                "Failed to instantiate reference point '"
                    + remoteProjectName
                    + "' using existing directory "
                    + existingDirectory,
                e);

            cancelNegotiation("Invalid local representation chosen for " + remoteProjectName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages
                        .AddProjectToSessionWizard_new_reference_point_instantiation_error_message,
                    existingDirectory,
                    remoteProjectName,
                    e.getMessage()),
                Messages.AddProjectToSessionWizard_new_reference_point_instantiation_error_title);

            return;
          }

          localProjects.put(remoteProjectID, referencePoint);

          prepareFilesChangedPage(localProjects);

          setTopPanelText(Messages.AddProjectToSessionWizard_description_changed_files);
        }

        /**
         * Cancels the project negotiation. Informs all channels of this cancellation by logging an
         * error and showing an error notification to the local user.
         *
         * @param reason the reason for the cancellation
         */
        private void noisyCancel(@NotNull String reason, @Nullable Throwable throwable) {
          if (throwable != null) {
            log.error("Encountered error reading module selection results: " + reason, throwable);
          } else {
            log.error("Encountered error reading module selection results: " + reason);
          }

          NotificationPanel.showError(
              MessageFormat.format(
                  Messages.AddProjectToSessionWizard_error_reading_module_selection_result_message,
                  reason),
              Messages.AddProjectToSessionWizard_error_reading_module_selection_result_title);

          cancelNegotiation("Encountered an error during project negotiation");
        }

        @Override
        public void cancel() {
          cancelNegotiation(null);
        }
      };

  /**
   * Cancels the project negotiation, notifies the host using the given reason, and closes the
   * wizard.
   *
   * <p><b>Note:</b> This is an asynchronous action. It is not guaranteed that the negotiation is
   * canceled when this method returns.
   *
   * @param reason description why the negotiation was canceled or <code>null</code> if it was
   *     actively canceled by the user
   */
  private void cancelNegotiation(@Nullable final String reason) {
    ThreadUtils.runSafeAsync(
        log,
        new Runnable() {

          @Override
          public void run() {
            negotiation.localCancel(reason, NegotiationTools.CancelOption.NOTIFY_PEER);
          }
        });

    close();
  }

  private final PageActionListener fileListPageListener =
      new PageActionListener() {
        @Override
        public void back() {
          // Nothing to do
        }

        @Override
        public void next() {
          triggerProjectNegotiation();
        }

        @Override
        public void cancel() {
          cancelNegotiation(null);
        }
      };

  @SuppressWarnings("FieldCanBeLocal")
  private final CancelListener cancelListener =
      new CancelListener() {

        @Override
        public void canceled(final CancelLocation location, final String message) {
          if (location != CancelLocation.LOCAL) cancelWizard(peer, message, location);
        }
      };

  /**
   * Instantiates the wizard and its pages.
   *
   * @param project the Intellij project to use for the wizard
   * @param parent the parent window relative to which the dialog is positioned
   * @param negotiation The IPN this wizard handles
   */
  public AddReferencePointToSessionWizard(
      Project project, Window parent, AbstractIncomingProjectNegotiation negotiation) {

    super(
        project,
        parent,
        Messages.AddProjectToSessionWizard_title,
        new HeaderPanel(
            Messages.AddProjectToSessionWizard_title2,
            Messages.AddProjectToSessionWizard_description));

    setModal(true);

    SarosPluginContext.initComponent(this);

    this.negotiation = negotiation;
    this.peer = negotiation.getPeer();

    this.setPreferredSize(new Dimension(650, 535));

    List<ProjectNegotiationData> data = negotiation.getProjectNegotiationData();

    localProjects = new HashMap<String, IProject>();

    remoteProjectID = data.get(0).getProjectID();
    remoteProjectName = data.get(0).getProjectName();

    selectLocalReferencePointRepresentationPage =
        new SelectLocalReferencePointRepresentationPage(
            SELECT_MODULE_REPRESENTATION_PAGE_ID,
            selectProjectsPageListener,
            Collections.singleton(remoteProjectName));

    registerPage(selectLocalReferencePointRepresentationPage);

    fileListPage =
        new TextAreaPage(
            FILE_LIST_PAGE_ID, "Changes applied to local modules:", fileListPageListener);
    registerPage(fileListPage);

    create();

    negotiation.addCancelListener(cancelListener);
  }

  /** Cancels the wizard and gives an informative error message. */
  public void cancelWizard(
      final JID peer, final String errorMsg, NegotiationTools.CancelLocation type) {
    final String message =
        "Project negotiation canceled "
            + (type.equals(NegotiationTools.CancelLocation.LOCAL)
                ? "locally "
                : "remotely by " + peer);

    /*
     *  if we already triggered the negotiation the message will
     *  be displayed in the trigger logic, so do not popup another dialog here
     */
    if (!triggered)
      NotificationPanel.showInformation(
          message + (errorMsg != null ? "\n\n" + errorMsg : ""), message);

    close();
  }

  /**
   * Runs {@link AbstractIncomingProjectNegotiation#run(java.util.Map, IProgressMonitor)} as a
   * background task through {@link #runTask(Runnable, String)}.
   *
   * <p>On success, a success notification is displayed, on error, a dialog is shown.
   */
  private void triggerProjectNegotiation() {

    if (triggered) return;

    triggered = true;

    ProgressManager.getInstance()
        .run(
            new Task.Backgroundable(
                project,
                Messages.AddProjectToSessionWizard_negotiation_progress_title,
                true,
                PerformInBackgroundOption.DEAF) {

              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                final ProjectNegotiation.Status status =
                    negotiation.run(localProjects, new ProgressMonitorAdapter(indicator));

                indicator.stop();

                if (status == ProjectNegotiation.Status.ERROR) {
                  NotificationPanel.showError(
                      MessageFormat.format(
                          Messages.AddProjectToSessionWizard_negotiation_error_message,
                          negotiation.getErrorMessage()),
                      Messages.AddProjectToSessionWizard_negotiation_error_title);
                } else if (status == ProjectNegotiation.Status.OK) {
                  NotificationPanel.showInformation(
                      Messages.AddProjectToSessionWizard_negotiation_successful_message,
                      Messages.AddProjectToSessionWizard_negotiation_successful_title);
                } else
                  NotificationPanel.showError(
                      Messages.AddProjectToSessionWizard_negotiation_aborted_message,
                      Messages.AddProjectToSessionWizard_negotiation_aborted_title);
              }
            });

    close();
  }

  private void prepareFilesChangedPage(final Map<String, IProject> projectMapping) {

    final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

    runTask(
        new Runnable() {
          @Override
          public void run() {
            modifiedResources.putAll(
                getModifiedResourcesFromMofifiableProjects(
                    projectMapping, new NullProgressMonitor()));
          }
        },
        "Gathering files that have to be modified...");
    fillFileListPage(modifiedResources);
  }

  private void fillFileListPage(Map<String, FileListDiff> modifiedResources) {
    boolean empty = true;
    for (Map.Entry<String, FileListDiff> key : modifiedResources.entrySet()) {
      fileListPage.addLine("Project [" + key.getKey() + "]:");
      FileListDiff diff = modifiedResources.get(key.getKey());

      /// TODO folders

      for (String path : diff.getAlteredFiles()) {
        fileListPage.addLine("changed: " + path);
        empty = false;
      }

      for (String path : diff.getRemovedFiles()) {
        fileListPage.addLine("removed: " + path);
        empty = false;
      }

      for (String path : diff.getAddedFiles()) {
        fileListPage.addLine("added: " + path);
        empty = false;
      }
    }

    if (empty) {
      fileListPage.addLine("No files have to be modified.");
    }
  }

  /** Creates a FileListDiff for all projects that will be modified. */
  private Map<String, FileListDiff> getModifiedResourcesFromMofifiableProjects(
      Map<String, IProject> projectMapping, IProgressMonitor monitor) {
    monitor.setTaskName("Calculating changed files...");

    final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
    final Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

    modifiedProjects.putAll(getModifiedProjects(projectMapping));
    modifiedResources.putAll(getModifiedResources(modifiedProjects, monitor));
    return modifiedResources;
  }

  /**
   * Returns a project mapping that contains all projects that will be modified on synchronization.
   *
   * <p>Currently these are simply all projects from projectMapping.
   *
   * <p>FIXME: Add a check for non-overwritable projects.
   */
  private Map<String, IProject> getModifiedProjects(Map<String, IProject> projectMapping) {
    Map<String, IProject> modifiedProjects = new HashMap<String, IProject>();

    for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {
      // TODO: Add check for non-overwritable projects
      modifiedProjects.put(entry.getKey(), entry.getValue());
    }

    return modifiedProjects;
  }

  /**
   * Returns all modified resources (either changed or deleted) for the current project mapping.
   *
   * <p><b>Important:</b> Do not call this inside the UI Thread. This is a long running operation !
   */
  private Map<String, FileListDiff> getModifiedResources(
      Map<String, IProject> projectMapping, IProgressMonitor monitor) {
    Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

    final ISarosSession session = sessionManager.getSession();

    // FIXME the wizard should handle the case that the session may stop in
    // the meantime !

    if (session == null) {
      throw new IllegalStateException("no session running");
    }

    SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, projectMapping.size());

    subMonitor.setTaskName("\"Searching for files that will be modified...\",");

    for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {

      String projectID = entry.getKey();
      IProject project = entry.getValue();

      try {

        final ProjectNegotiationData data = negotiation.getProjectNegotiationData(projectID);

        FileList localFileList =
            FileListFactory.createFileList(
                project,
                checksumCache,
                new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SETTASKNAME));

        final FileListDiff diff = FileListDiff.diff(localFileList, data.getFileList());

        if (!diff.getRemovedFolders().isEmpty()
            || !diff.getRemovedFiles().isEmpty()
            || !diff.getAlteredFiles().isEmpty()
            || !diff.getAddedFiles().isEmpty()
            || !diff.getAddedFolders().isEmpty()) {

          modifiedResources.put(project.getName(), diff);
        }

      } catch (IOException e) {
        log.warn("could not refresh project: " + project, e);
      }
    }
    return modifiedResources;
  }
}
