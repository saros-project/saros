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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.SarosPluginContext;
import saros.filesystem.IReferencePoint;
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
import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.CancelListener;
import saros.negotiation.FileList;
import saros.negotiation.FileListDiff;
import saros.negotiation.FileListFactory;
import saros.negotiation.NegotiationTools;
import saros.negotiation.NegotiationTools.CancelLocation;
import saros.negotiation.ResourceNegotiation;
import saros.negotiation.ResourceNegotiationData;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.util.ThreadUtils;

/**
 * Wizard for adding reference points to a session.
 *
 * <p>The wizard consists of a selection page where the user can select how to represent the shared
 * reference points in the local environment.
 *
 * <ul>
 *   <li>A new directory can be created to represent the reference point.
 *   <li>An existing directory can be used to represent the reference point.
 * </ul>
 *
 * <p>If the option to reuse an existing reference point is chosen, a second page is displayed to
 * show the necessary modification that will be made by Saros to match the state of the remote
 * reference point.
 */
//  FIXME: Add facility for more than one reference point.
public class AddReferencePointToSessionWizard extends Wizard {
  private static final Logger log = Logger.getLogger(AddReferencePointToSessionWizard.class);

  public static final String SELECT_REFERENCE_POINT_REPRESENTATION_PAGE_ID =
      "selectReferencePointRepresentation";
  public static final String FILE_LIST_PAGE_ID = "fileListPage";

  private final String remoteReferencePointID;
  private final String remoteReferencePointName;

  private final AbstractIncomingResourceNegotiation negotiation;
  private final JID peer;

  private boolean triggered = false;

  /** reference point ID => reference point */
  private final Map<String, IReferencePoint> localReferencePoints;

  @Inject private ISarosSessionManager sessionManager;

  private final SelectLocalReferencePointRepresentationPage
      selectLocalReferencePointRepresentationPage;
  private final TextAreaPage modifiedResourcesListPage;

  private final PageActionListener selectReferencePointsPageListener =
      new PageActionListener() {
        @Override
        public void back() {
          // Nothing to do
        }

        /**
         * Extracts the local reference point's name and either displays the {@link
         * AddReferencePointToSessionWizard#modifiedResourcesListPage} or triggers the resource
         * negotiation.
         */
        @Override
        public void next() {
          DocumentAPI.saveAllDocuments();

          ReferencePointSelectionResult referencePointSelectionResult;

          try {
            referencePointSelectionResult =
                selectLocalReferencePointRepresentationPage.getReferencePointSelectionResult(
                    remoteReferencePointName);

          } catch (IllegalStateException e) {
            noisyCancel(
                "Request to get local representation selection result for "
                    + remoteReferencePointName
                    + " failed: "
                    + e.getMessage(),
                e);

            return;
          }

          if (referencePointSelectionResult == null) {
            noisyCancel(
                "Could not find a local representation selection result for the shared root directory "
                    + remoteReferencePointName,
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
                noisyCancel(
                    "No valid new directory name or base path was given for "
                        + remoteReferencePointName,
                    null);

                return;
              }

              doNewDirectory(project, newDirectoryName, newDirectoryBaseDirectory);
              break;

            case USE_EXISTING_DIRECTORY:
              VirtualFile existingDirectory = referencePointSelectionResult.getExistingDirectory();

              if (existingDirectory == null) {
                noisyCancel(
                    "No valid existing directory was given for " + remoteReferencePointName, null);

                return;
              }

              doExistingDirectory(project, existingDirectory);
              break;

            default:
              noisyCancel(
                  "No valid option on how to represent the shared root directory "
                      + remoteReferencePointName
                      + " was given",
                  null);
          }
        }

        /**
         * Creates a new directory for the reference point and starts the resource negotiation with
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

            cancelNegotiation(
                "Failed to create local representation of shared root directory "
                    + remoteReferencePointName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.Contact_saros_message_conditional,
                    MessageFormat.format(
                        Messages
                            .AddReferencePointToSessionWizard_directory_already_exists_message_condition,
                        directoryName)),
                Messages.AddReferencePointToSessionWizard_directory_already_exists_title);

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
                    + remoteReferencePointName,
                e);

            cancelNegotiation(
                "Failed to create local representation of shared root directory "
                    + remoteReferencePointName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.Contact_saros_message_conditional,
                    MessageFormat.format(
                        Messages
                            .AddReferencePointToSessionWizard_directory_creation_failed_message_condition,
                        directoryName,
                        e.getMessage())),
                Messages.AddReferencePointToSessionWizard_directory_creation_failed_title);

            return;
          }

          if (ProjectAPI.isExcluded(project, referencePointFile)) {
            log.warn(
                "Could not share created directory "
                    + referencePointFile
                    + "  as it is excluded from the project scope.");

            cancelNegotiation(
                "Failed to create local representation of shared root directory "
                    + remoteReferencePointName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.AddReferencePointToSessionWizard_directory_excluded_message,
                    referencePointFile,
                    remoteReferencePointName),
                Messages.AddReferencePointToSessionWizard_directory_excluded_title);

            return;
          }

          IReferencePoint sharedReferencePoint;

          try {
            sharedReferencePoint = new IntellijReferencePoint(project, referencePointFile);

          } catch (IllegalArgumentException e) {
            log.error(
                "Failed to instantiate reference point '"
                    + remoteReferencePointName
                    + "' using the created directory "
                    + referencePointFile,
                e);

            cancelNegotiation(
                "Failed to create local representation of shared root directory "
                    + remoteReferencePointName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages
                        .AddReferencePointToSessionWizard_new_reference_point_instantiation_error_message,
                    referencePointFile,
                    remoteReferencePointName,
                    e.getMessage()),
                Messages
                    .AddReferencePointToSessionWizard_new_reference_point_instantiation_error_title);

            return;
          }

          localReferencePoints.put(remoteReferencePointID, sharedReferencePoint);

          triggerResourceNegotiation();
        }

        /**
         * Checks if the directory is valid and then starts the resource negotiation with it.
         *
         * @param existingDirectory the existing directory to use for the resource negotiation
         */
        private void doExistingDirectory(
            @NotNull Project project, @NotNull VirtualFile existingDirectory) {

          IReferencePoint referencePoint;

          try {
            referencePoint = new IntellijReferencePoint(project, existingDirectory);

          } catch (IllegalArgumentException e) {
            log.error(
                "Failed to instantiate reference point '"
                    + remoteReferencePointName
                    + "' using existing directory "
                    + existingDirectory,
                e);

            cancelNegotiation(
                "Invalid local representation chosen for shared root directory "
                    + remoteReferencePointName);

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages
                        .AddReferencePointToSessionWizard_new_reference_point_instantiation_error_message,
                    existingDirectory,
                    remoteReferencePointName,
                    e.getMessage()),
                Messages
                    .AddReferencePointToSessionWizard_new_reference_point_instantiation_error_title);

            return;
          }

          localReferencePoints.put(remoteReferencePointID, referencePoint);

          prepareResourcesChangedPage(localReferencePoints);

          setTopPanelText(Messages.AddReferencePointToSessionWizard_description_changed_files);
        }

        /**
         * Cancels the resource negotiation. Informs all channels of this cancellation by logging an
         * error and showing an error notification to the local user.
         *
         * @param reason the reason for the cancellation
         */
        private void noisyCancel(@NotNull String reason, @Nullable Throwable throwable) {
          if (throwable != null) {
            log.error(
                "Encountered error reading reference point selection results: " + reason,
                throwable);

          } else {
            log.error("Encountered error reading reference point selection results: " + reason);
          }

          NotificationPanel.showError(
              MessageFormat.format(
                  Messages
                      .AddReferencePointToSessionWizard_error_reading_reference_point_selection_result_message,
                  reason),
              Messages
                  .AddReferencePointToSessionWizard_error_reading_reference_point_selection_result_title);

          cancelNegotiation("Encountered an error during resource negotiation");
        }

        @Override
        public void cancel() {
          cancelNegotiation(null);
        }
      };

  /**
   * Cancels the resource negotiation, notifies the host using the given reason, and closes the
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
          triggerResourceNegotiation();
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
   * @param project the project to use for the wizard
   * @param parent the parent window relative to which the dialog is positioned
   * @param negotiation The IPN this wizard handles
   */
  public AddReferencePointToSessionWizard(
      Project project, Window parent, AbstractIncomingResourceNegotiation negotiation) {

    super(
        project,
        parent,
        Messages.AddReferencePointToSessionWizard_title,
        new HeaderPanel(
            Messages.AddReferencePointToSessionWizard_title2,
            Messages.AddReferencePointToSessionWizard_description));

    setModal(true);

    SarosPluginContext.initComponent(this);

    this.negotiation = negotiation;
    this.peer = negotiation.getPeer();

    this.setPreferredSize(new Dimension(650, 535));

    List<ResourceNegotiationData> data = negotiation.getResourceNegotiationData();

    localReferencePoints = new HashMap<String, IReferencePoint>();

    remoteReferencePointID = data.get(0).getReferencePointID();
    remoteReferencePointName = data.get(0).getReferencePointName();

    selectLocalReferencePointRepresentationPage =
        new SelectLocalReferencePointRepresentationPage(
            SELECT_REFERENCE_POINT_REPRESENTATION_PAGE_ID, selectReferencePointsPageListener, data);

    registerPage(selectLocalReferencePointRepresentationPage);

    modifiedResourcesListPage =
        new TextAreaPage(
            FILE_LIST_PAGE_ID,
            "Changes applied to local representation of shared root directories:",
            fileListPageListener);
    registerPage(modifiedResourcesListPage);

    create();

    negotiation.addCancelListener(cancelListener);
  }

  /** Cancels the wizard and gives an informative error message. */
  public void cancelWizard(
      final JID peer, final String errorMsg, NegotiationTools.CancelLocation type) {
    final String message =
        "Resource negotiation canceled "
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
   * Runs {@link AbstractIncomingResourceNegotiation#run(java.util.Map, IProgressMonitor)} as a
   * background task through {@link #runTask(Runnable, String)}.
   *
   * <p>On success, a success notification is displayed, on error, a dialog is shown.
   */
  private void triggerResourceNegotiation() {

    if (triggered) return;

    triggered = true;

    ProgressManager.getInstance()
        .run(
            new Task.Backgroundable(
                project,
                Messages.AddReferencePointToSessionWizard_negotiation_progress_title,
                true,
                PerformInBackgroundOption.DEAF) {

              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                final ResourceNegotiation.Status status =
                    negotiation.run(localReferencePoints, new ProgressMonitorAdapter(indicator));

                indicator.stop();

                if (status == ResourceNegotiation.Status.ERROR) {
                  NotificationPanel.showError(
                      MessageFormat.format(
                          Messages.AddReferencePointToSessionWizard_negotiation_error_message,
                          negotiation.getErrorMessage()),
                      Messages.AddReferencePointToSessionWizard_negotiation_error_title);

                } else if (status == ResourceNegotiation.Status.OK) {
                  NotificationPanel.showInformation(
                      Messages.AddReferencePointToSessionWizard_negotiation_successful_message,
                      Messages.AddReferencePointToSessionWizard_negotiation_successful_title);

                } else
                  NotificationPanel.showError(
                      Messages.AddReferencePointToSessionWizard_negotiation_aborted_message,
                      Messages.AddReferencePointToSessionWizard_negotiation_aborted_title);
              }
            });

    close();
  }

  private void prepareResourcesChangedPage(
      final Map<String, IReferencePoint> referencePointMapping) {

    final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

    runTask(
        new Runnable() {
          @Override
          public void run() {
            modifiedResources.putAll(
                getModifiedResourcesForReferencePoints(
                    referencePointMapping, new NullProgressMonitor()));
          }
        },
        "Gathering files that have to be modified...");
    fillModifiedResourcesListPage(modifiedResources);
  }

  private void fillModifiedResourcesListPage(Map<String, FileListDiff> modifiedResources) {
    boolean empty = true;
    for (Map.Entry<String, FileListDiff> key : modifiedResources.entrySet()) {
      modifiedResourcesListPage.addLine("Shared Root Directory [" + key.getKey() + "]:");
      FileListDiff diff = modifiedResources.get(key.getKey());

      /// TODO folders

      for (String path : diff.getAlteredFiles()) {
        modifiedResourcesListPage.addLine("changed: " + path);
        empty = false;
      }

      for (String path : diff.getRemovedFiles()) {
        modifiedResourcesListPage.addLine("removed: " + path);
        empty = false;
      }

      for (String path : diff.getAddedFiles()) {
        modifiedResourcesListPage.addLine("added: " + path);
        empty = false;
      }
    }

    if (empty) {
      modifiedResourcesListPage.addLine("No files have to be modified.");
    }
  }

  /** Creates a FileListDiff for all reference points that will be modified. */
  private Map<String, FileListDiff> getModifiedResourcesForReferencePoints(
      Map<String, IReferencePoint> referencePointMapping, IProgressMonitor monitor) {

    monitor.setTaskName("Calculating changed resources...");

    final Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();
    final Map<String, IReferencePoint> modifiedReferencePoints =
        new HashMap<String, IReferencePoint>();

    modifiedReferencePoints.putAll(getModifiedReferencePoints(referencePointMapping));
    modifiedResources.putAll(getModifiedResources(modifiedReferencePoints, monitor));
    return modifiedResources;
  }

  /**
   * Returns a reference point mapping that contains all reference points that will be modified on
   * synchronization.
   *
   * <p>Currently these are simply all reference points from the given reference point mapping.
   */
  private Map<String, IReferencePoint> getModifiedReferencePoints(
      Map<String, IReferencePoint> referencePointMapping) {

    Map<String, IReferencePoint> modifiedReferencePoints = new HashMap<String, IReferencePoint>();

    for (Map.Entry<String, IReferencePoint> entry : referencePointMapping.entrySet()) {
      modifiedReferencePoints.put(entry.getKey(), entry.getValue());
    }

    return modifiedReferencePoints;
  }

  /**
   * Returns all modified resources (either changed or deleted) for the current reference point
   * mapping.
   *
   * <p><b>Important:</b> Do not call this inside the UI Thread. This is a long running operation!
   */
  private Map<String, FileListDiff> getModifiedResources(
      Map<String, IReferencePoint> referencePointMapping, IProgressMonitor monitor) {

    Map<String, FileListDiff> modifiedResources = new HashMap<String, FileListDiff>();

    final ISarosSession session = sessionManager.getSession();

    // FIXME the wizard should handle the case that the session may stop in
    // the meantime !

    if (session == null) {
      throw new IllegalStateException("no session running");
    }

    IChecksumCache checksumCache = session.getComponent(IChecksumCache.class);

    if (checksumCache == null) {
      throw new IllegalStateException("failed to obtain checksum cache from session context");
    }

    SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, referencePointMapping.size());

    subMonitor.setTaskName("\"Searching for files that will be modified...\",");

    for (Map.Entry<String, IReferencePoint> entry : referencePointMapping.entrySet()) {

      String referencePointID = entry.getKey();
      IReferencePoint referencePoint = entry.getValue();

      try {
        final ResourceNegotiationData data =
            negotiation.getResourceNegotiationData(referencePointID);

        FileList localFileList =
            FileListFactory.createFileList(
                referencePoint,
                checksumCache,
                new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SETTASKNAME));

        final FileListDiff diff = FileListDiff.diff(localFileList, data.getFileList());

        if (!diff.getRemovedFolders().isEmpty()
            || !diff.getRemovedFiles().isEmpty()
            || !diff.getAlteredFiles().isEmpty()
            || !diff.getAddedFiles().isEmpty()
            || !diff.getAddedFolders().isEmpty()) {

          modifiedResources.put(referencePoint.getName(), diff);
        }

      } catch (IOException e) {
        log.warn("could not calculate local file list for reference point " + referencePoint, e);
      }
    }
    return modifiedResources;
  }
}
