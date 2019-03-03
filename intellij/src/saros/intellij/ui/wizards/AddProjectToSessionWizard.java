package saros.intellij.ui.wizards;

import com.intellij.ide.highlighter.ModuleFileType;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleWithNameAlreadyExists;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.UIUtil;
import java.awt.Dimension;
import java.awt.Window;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import saros.SarosPluginContext;
import saros.filesystem.IChecksumCache;
import saros.filesystem.IProject;
import saros.filesystem.IReferencePointManager;
import saros.intellij.context.SharedIDEContext;
import saros.intellij.editor.DocumentAPI;
import saros.intellij.filesystem.Filesystem;
import saros.intellij.filesystem.IntelliJProjectImpl;
import saros.intellij.negotiation.ModuleConfiguration;
import saros.intellij.negotiation.ModuleConfigurationInitializer;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.NotificationPanel;
import saros.intellij.ui.widgets.progress.ProgessMonitorAdapter;
import saros.intellij.ui.wizards.pages.HeaderPanel;
import saros.intellij.ui.wizards.pages.PageActionListener;
import saros.intellij.ui.wizards.pages.TextAreaPage;
import saros.intellij.ui.wizards.pages.moduleselection.ModuleSelectionResult;
import saros.intellij.ui.wizards.pages.moduleselection.SelectLocalModuleRepresentationPage;
import saros.monitoring.IProgressMonitor;
import saros.monitoring.NullProgressMonitor;
import saros.monitoring.SubProgressMonitor;
import saros.negotiation.AbstractIncomingProjectNegotiation;
import saros.negotiation.CancelListener;
import saros.negotiation.FileList;
import saros.negotiation.FileListDiff;
import saros.negotiation.FileListFactory;
import saros.negotiation.NegotiationTools;
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

//  FIXME: Add facility for more than one project.
public class AddProjectToSessionWizard extends Wizard {
  private static final Logger LOG = Logger.getLogger(AddProjectToSessionWizard.class);

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

  private final SelectLocalModuleRepresentationPage selectLocalModuleRepresentationPage;
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

          ModuleSelectionResult moduleSelectionResult;

          try {
            moduleSelectionResult =
                selectLocalModuleRepresentationPage.getModuleSelectionResult(remoteProjectName);

          } catch (IllegalStateException e) {
            noisyCancel("Request to get module selection result failed: " + e.getMessage(), e);

            return;
          }

          if (moduleSelectionResult == null) {
            noisyCancel(
                "Could not find a module selection result for the module " + remoteProjectName,
                null);

            return;
          }

          Project project = moduleSelectionResult.getProject();

          sessionManager.getSession().getComponent(SharedIDEContext.class).setProject(project);

          switch (moduleSelectionResult.getLocalRepresentationOption()) {
            case CREATE_NEW_MODULE:
              String newModuleName = moduleSelectionResult.getNewModuleName();
              Path newModuleBasePath = moduleSelectionResult.getNewModuleBasePath();

              if (newModuleName == null || newModuleBasePath == null) {
                noisyCancel("No valid new module name or base path was given", null);

                return;
              }

              doNewModule(project, newModuleName, newModuleBasePath);
              break;

            case USE_EXISTING_MODULE:
              Module existingModule = moduleSelectionResult.getExistingModule();

              if (existingModule == null) {
                noisyCancel("No valid existing module was given", null);

                return;
              }

              doExistingModule(existingModule);
              break;

            default:
              noisyCancel("No valid option on how to represent the shared module was given", null);
          }
        }

        /**
         * Creates a stub module and starts the project negotiation with the newly created module.
         *
         * @param project the project to create the module in
         * @param moduleName the name for the new module
         * @param moduleBasePath the base path for the new module
         * @see AddProjectToSessionWizard#createBaseModule(String, String, Path, Project)
         */
        private void doNewModule(
            @NotNull Project project, @NotNull String moduleName, @NotNull Path moduleBasePath) {

          Map<String, String> moduleParameters =
              negotiation.getProjectNegotiationData(remoteProjectID).getAdditionalProjectData();

          ModuleConfiguration moduleConfiguration =
              new ModuleConfiguration(moduleParameters, false);

          String moduleType = moduleConfiguration.getModuleType();

          if (moduleType == null) {
            LOG.error("Aborted module creation as no module type was received.");

            cancelNegotiation("Failed to create shared module");

            NotificationPanel.showError(
                Messages.AddProjectToSessionWizard_no_module_type_received_message,
                Messages.AddProjectToSessionWizard_no_module_type_received_title);

            return;
          }

          Module module;

          try {
            module = createBaseModule(moduleName, moduleType, moduleBasePath, project);

          } catch (IOException e) {
            LOG.error("Could not create the shared module " + moduleName + ".", e);

            cancelNegotiation("Failed to create shared module");

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.Contact_saros_message_conditional,
                    MessageFormat.format(
                            Messages
                                .AddProjectToSessionWizard_module_creation_failed_message_condition,
                            moduleName)
                        + "\n"
                        + e),
                Messages.AddProjectToSessionWizard_module_creation_failed_title);

            return;

          } catch (ModuleWithNameAlreadyExists e) {
            LOG.warn("Could not create the shared module " + moduleName + ".", e);

            cancelNegotiation("Failed to create shared module");

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.Contact_saros_message_conditional,
                    MessageFormat.format(
                        Messages.AddProjectToSessionWizard_module_already_exists_message_condition,
                        moduleName)),
                Messages.AddProjectToSessionWizard_module_already_exists_title);

            return;
          }

          queueModuleConfigurationChange(module, moduleConfiguration);

          IProject sharedProject = new IntelliJProjectImpl(module);

          localProjects.put(remoteProjectID, sharedProject);

          triggerProjectNegotiation();
        }

        /**
         * Checks if the chosen module is valid and then starts the project negotiation with the
         * module.
         *
         * @param existingModule the existing module to use for the project negotiation
         */
        private void doExistingModule(@NotNull Module existingModule) {
          String moduleName = existingModule.getName();

          IProject sharedProject;

          try {
            sharedProject = new IntelliJProjectImpl(existingModule);

          } catch (IllegalArgumentException e) {
            LOG.debug("No session is started as an invalid module was chosen");

            cancelNegotiation("Invalid module chosen by client");

            NotificationPanel.showError(
                MessageFormat.format(
                    Messages.Contact_saros_message_conditional,
                    MessageFormat.format(
                        Messages.AddProjectToSessionWizard_invalid_module_message_condition,
                        moduleName)),
                Messages.AddProjectToSessionWizard_invalid_module_title);

            return;
          }

          Map<String, String> moduleParameters =
              negotiation.getProjectNegotiationData(remoteProjectID).getAdditionalProjectData();

          ModuleConfiguration moduleConfiguration = new ModuleConfiguration(moduleParameters, true);

          /*
           * TODO move to finish of FilesChangedPage iff back button is configured to function as
           *  the module might change in that case
           */
          queueModuleConfigurationChange(existingModule, moduleConfiguration);

          localProjects.put(remoteProjectID, sharedProject);

          prepareFilesChangedPage(localProjects);

          setTopPanelText(Messages.AddProjectToSessionWizard_description_changed_files);
        }

        /**
         * Adds the given module configuration to the {@link ModuleConfigurationInitializer}.
         *
         * <p>Cancels the negotiation if the session or session context is no longer valid.
         *
         * @param module the module the configuration belongs to
         * @param moduleConfiguration the module configuration
         */
        private void queueModuleConfigurationChange(
            @NotNull Module module, @NotNull ModuleConfiguration moduleConfiguration) {

          ISarosSession session = sessionManager.getSession();

          if (session == null) {
            LOG.error("Encountered project negotiation without running session");

            NotificationPanel.showError(
                Messages.AddProjectToSessionWizard_no_session_message,
                Messages.AddProjectToSessionWizard_no_session_title);

            return;
          }

          ModuleConfigurationInitializer moduleConfigurationInitializer =
              session.getComponent(ModuleConfigurationInitializer.class);

          if (moduleConfigurationInitializer == null) {
            LOG.error(
                "Could not obtain class from session context - "
                    + ModuleConfigurationInitializer.class.getSimpleName());

            NotificationPanel.showError(
                Messages.AddProjectToSessionWizard_context_teardown_message,
                Messages.AddProjectToSessionWizard_context_teardown_title);

            return;
          }

          moduleConfigurationInitializer.enqueueModuleConfigurationChange(
              module, moduleConfiguration);
        }

        /**
         * Cancels the project negotiation. Informs all channels of this cancellation by logging an
         * error and showing an error notification to the local user.
         *
         * @param reason the reason for the cancellation
         */
        private void noisyCancel(@NotNull String reason, @Nullable Throwable throwable) {
          if (throwable != null) {
            LOG.error("Encountered error reading module selection results: " + reason, throwable);
          } else {
            LOG.error("Encountered error reading module selection results: " + reason);
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
        LOG,
        new Runnable() {

          @Override
          public void run() {
            negotiation.localCancel(reason, NegotiationTools.CancelOption.NOTIFY_PEER);
          }
        });

    close();
  }

  /**
   * Creates an empty base module with the given name, base path, and module type in the given
   * project.
   *
   * <p>The created module does not contain any further configuration. The needed configuration
   * options will be added through {@link ModuleConfigurationInitializer}.
   *
   * @param moduleName name of the module
   * @param moduleType the type of the module
   * @param targetBasePath the base path of the created module
   * @param targetProject the project to create the module in
   * @return a <code>Module</code> object with the given parameters
   * @throws ModuleWithNameAlreadyExists if a module with the given name already exists in the given
   *     project
   * @throws FileNotFoundException if the base directory or module file of the created module could
   *     not be found in the local filesystem
   * @throws IOException if the creation of the module did not return a valid <code>Module</code>
   *     object
   */
  @NotNull
  private Module createBaseModule(
      @NotNull String moduleName,
      @NotNull String moduleType,
      @NotNull Path targetBasePath,
      @NotNull Project targetProject)
      throws FileNotFoundException, IOException, ModuleWithNameAlreadyExists {

    for (Module module : ModuleManager.getInstance(targetProject).getModules()) {
      if (moduleName.equals(module.getName()))
        throw new ModuleWithNameAlreadyExists(
            "Could not create stub module as a module with the chosen name already exists",
            moduleName);
    }

    Module module =
        Filesystem.runWriteAction(
            new ThrowableComputable<Module, IOException>() {

              @Override
              public Module compute() throws IOException {
                Path moduleBasePath = targetBasePath.resolve(moduleName);

                Path moduleFilePath =
                    moduleBasePath.resolve(moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION);

                ModifiableModuleModel modifiableModuleModel =
                    ModuleManager.getInstance(targetProject).getModifiableModel();

                Module module =
                    modifiableModuleModel.newModule(moduleFilePath.toString(), moduleType);

                modifiableModuleModel.commit();
                targetProject.save();

                VirtualFile moduleFile = module.getModuleFile();

                if (moduleFile == null) {
                  throw new FileNotFoundException(
                      "Could not find module file for module " + module + " after creating it.");
                }

                VirtualFile moduleRoot = moduleFile.getParent();

                if (moduleRoot == null) {
                  throw new FileNotFoundException(
                      "Could not  find base directory for module " + module + ".");
                }

                ModifiableRootModel modifiableRootModel =
                    ModuleRootManager.getInstance(module).getModifiableModel();

                try {
                  modifiableRootModel.addContentEntry(moduleRoot);
                  modifiableRootModel.commit();

                } finally {
                  if (!modifiableRootModel.isDisposed()) {
                    modifiableRootModel.dispose();
                  }
                }

                return module;
              }
            },
            ModalityState.defaultModalityState());

    if (module == null) {
      throw new IOException(
          "The creation of the module "
              + moduleName
              + " did not return a valid reference to the new module.");
    }

    return module;
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

  private final CancelListener cancelListener =
      new CancelListener() {

        @Override
        public void canceled(final NegotiationTools.CancelLocation location, final String message) {
          cancelWizard(peer, message, location);
        }
      };

  /**
   * Instantiates the wizard and its pages.
   *
   * @param project the Intellij project to use for the wizard
   * @param parent the parent window relative to which the dialog is positioned
   * @param negotiation The IPN this wizard handles
   */
  public AddProjectToSessionWizard(
      Project project, Window parent, AbstractIncomingProjectNegotiation negotiation) {

    super(
        project,
        parent,
        Messages.AddProjectToSessionWizard_title,
        new HeaderPanel(
            Messages.AddProjectToSessionWizard_title2,
            Messages.AddProjectToSessionWizard_description));

    SarosPluginContext.initComponent(this);

    this.negotiation = negotiation;
    this.peer = negotiation.getPeer();

    this.setPreferredSize(new Dimension(650, 535));

    List<ProjectNegotiationData> data = negotiation.getProjectNegotiationData();

    localProjects = new HashMap<String, IProject>();

    remoteProjectID = data.get(0).getProjectID();
    remoteProjectName = data.get(0).getProjectName();

    selectLocalModuleRepresentationPage =
        new SelectLocalModuleRepresentationPage(
            SELECT_MODULE_REPRESENTATION_PAGE_ID,
            selectProjectsPageListener,
            Collections.singleton(remoteProjectName));

    registerPage(selectLocalModuleRepresentationPage);

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
        "Wizard canceled "
            + (type.equals(NegotiationTools.CancelLocation.LOCAL)
                ? "locally "
                : "remotely by " + peer);
    UIUtil.invokeLaterIfNeeded(
        new Runnable() {
          @Override
          public void run() {

            /*
             *  if we already triggered the negotiation the message will
             *  be displayed in the trigger logic, so do not popup another dialog here
             */
            if (!triggered)
              NotificationPanel.showInformation(
                  message + (errorMsg != null ? "\n\n" + errorMsg : ""), message);

            close();
          }
        });
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
                project, "Sharing project...", true, PerformInBackgroundOption.DEAF) {

              @Override
              public void run(ProgressIndicator indicator) {
                final ProjectNegotiation.Status status =
                    negotiation.run(localProjects, new ProgessMonitorAdapter(indicator));

                indicator.stop();

                UIUtil.invokeLaterIfNeeded(
                    new Runnable() {
                      @Override
                      public void run() {
                        if (status == ProjectNegotiation.Status.ERROR) {
                          NotificationPanel.showError(
                              "Error during project negotiation",
                              "The project could not be shared: " + negotiation.getErrorMessage());
                        } else if (status == ProjectNegotiation.Status.OK) {
                          NotificationPanel.showInformation(
                              "Project shared", "Project successfully shared");
                        } else
                          NotificationPanel.showError(
                              "Project negotiation aborted", "Project negotiation was canceled");
                      }
                    });
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

    fillReferencePointManager(session, new HashSet<>(projectMapping.values()));

    for (Map.Entry<String, IProject> entry : projectMapping.entrySet()) {

      String projectID = entry.getKey();
      IProject project = entry.getValue();

      try {

        final ProjectNegotiationData data = negotiation.getProjectNegotiationData(projectID);

        if (data.isPartial()) throw new IllegalStateException("partial sharing is not supported");

        FileList localFileList =
            FileListFactory.createFileList(
                project,
                null,
                checksumCache,
                new SubProgressMonitor(monitor, 1, SubProgressMonitor.SUPPRESS_SETTASKNAME));

        final FileListDiff diff = FileListDiff.diff(localFileList, data.getFileList(), false);

        if (!diff.getRemovedFolders().isEmpty()
            || !diff.getRemovedFiles().isEmpty()
            || !diff.getAlteredFiles().isEmpty()
            || !diff.getAddedFiles().isEmpty()
            || !diff.getAddedFolders().isEmpty()) {

          modifiedResources.put(project.getName(), diff);
        }

      } catch (IOException e) {
        LOG.warn("could not refresh project: " + project, e);
      }
    }
    return modifiedResources;
  }

  private void fillReferencePointManager(ISarosSession session, Set<IProject> projects) {
    IReferencePointManager referencePointManager =
        session.getComponent(IReferencePointManager.class);

    referencePointManager.putSetOfProjects(projects);
  }
}
