package saros.ui.wizards;

import static saros.ui.widgets.wizard.ReferencePointOptionComposite.LocalRepresentationOption.EXISTING_DIRECTORY;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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
import saros.exception.IllegalInputException;
import saros.filesystem.EclipseReferencePoint;
import saros.filesystem.IReferencePoint;
import saros.filesystem.checksum.IChecksumCache;
import saros.monitoring.ProgressMonitorAdapterFactory;
import saros.negotiation.AbstractIncomingResourceNegotiation;
import saros.negotiation.CancelListener;
import saros.negotiation.FileList;
import saros.negotiation.FileListDiff;
import saros.negotiation.FileListFactory;
import saros.negotiation.NegotiationTools;
import saros.negotiation.NegotiationTools.CancelLocation;
import saros.negotiation.NegotiationTools.CancelOption;
import saros.negotiation.ResourceNegotiation;
import saros.negotiation.ResourceNegotiationData;
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
import saros.ui.widgets.wizard.ReferencePointOptionResult;
import saros.ui.wizards.dialogs.WizardDialogAccessable;
import saros.ui.wizards.pages.LocalRepresentationSelectionPage;
import saros.util.ThreadUtils;

public class AddReferencePointsToSessionWizard extends Wizard {

  private static Logger log = Logger.getLogger(AddReferencePointsToSessionWizard.class);

  private LocalRepresentationSelectionPage localRepresentationSelectionPage;
  private WizardDialogAccessable wizardDialog;
  private AbstractIncomingResourceNegotiation negotiation;
  private JID peer;

  private boolean isExceptionCancel;

  private final ResourceMappingStorage mappingStorage;

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
        public void canceled(NegotiationTools.CancelLocation location, String message) {
          cancelWizard(peer, message, location);
        }
      };

  public AddReferencePointsToSessionWizard(AbstractIncomingResourceNegotiation negotiation) {

    SarosPluginContext.initComponent(this);

    this.mappingStorage = new ResourceMappingStorage(preferenceStore);

    this.negotiation = negotiation;
    this.peer = negotiation.getPeer();

    setWindowTitle(Messages.AddReferencePointsToSessionWizard_title);
    setHelpAvailable(true);
    setNeedsProgressMonitor(true);
    setDefaultPageImageDescriptor(
        ImageManager.getImageDescriptor(ImageManager.WIZBAN_SESSION_ADD_REFERENCE_POINTS));
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

    if (session == null) {
      return;
    }

    Map<String, String> lastReferencePointPathMapping = mappingStorage.getMapping(peer);

    localRepresentationSelectionPage =
        new LocalRepresentationSelectionPage(
            session,
            connectionManager,
            preferences,
            peer,
            negotiation.getResourceNegotiationData(),
            lastReferencePointPathMapping);

    addPage(localRepresentationSelectionPage);
  }

  @Override
  public boolean performFinish() {
    if (localRepresentationSelectionPage == null) {
      return true;
    }

    Map<String, IContainer> referencePointContainers = getReferencePointContainerMapping();

    Collection<IEditorPart> openEditors =
        getOpenEditorsForSharedContainers(referencePointContainers.values());

    List<IEditorPart> dirtyEditors = new ArrayList<>();

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
          () -> {
            if (AddReferencePointsToSessionWizard.this.getShell().isDisposed()) {
              return;
            }

            displaySaveDirtyEditorsDialog(dirtyEditors);
          });

      return false;
    }

    Map<String, FileListDiff> modifiedResources;

    try {
      modifiedResources = createContainersAndGetModifiedResources(referencePointContainers);
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

    if (!confirmOverwritingResources(modifiedResources)) {
      return false;
    }

    storeCurrentReferencePointPathMapping(peer, referencePointContainers);

    triggerResourceNegotiation(referencePointContainers, openEditors);

    return true;
  }

  public void cancelWizard(JID jid, String errorMsg, CancelLocation cancelLocation) {

    SWTUtils.runSafeSWTAsync(
        log,
        () -> {
          Shell shell = wizardDialog.getShell();
          if (shell == null || shell.isDisposed()) {
            return;
          }
          isExceptionCancel = true;
          wizardDialog.close();
        });

    SWTUtils.runSafeSWTAsync(log, () -> showCancelMessage(jid, errorMsg, cancelLocation));
  }

  @Override
  public boolean performCancel() {
    if (!isExceptionCancel) {
      ISarosSession session = sessionManager.getSession();
      if (session != null
          && !session.isHost()
          && !DialogUtils.popUpYesNoQuestion(
              Messages.AddReferencePointsToSessionWizard_leave_session,
              Messages.AddReferencePointsToSessionWizard_leave_session_text,
              false)) {
        return false;
      }
      ThreadUtils.runSafeAsync(
          "CancelAddReferencePointsToSessionWizard",
          log,
          () -> negotiation.localCancel(null, CancelOption.NOTIFY_PEER));
    }
    isExceptionCancel = false;
    return true;
  }

  private void triggerResourceNegotiation(
      Map<String, IContainer> referencePointContainers, Collection<IEditorPart> editorsToClose) {

    Job job =
        new Job("Synchronizing") {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            try {

              SWTUtils.runSafeSWTSync(
                  log,
                  () -> {
                    /*
                     * close all editors to avoid any conflicts.
                     */
                    for (IEditorPart editor : editorsToClose) {
                      EditorAPI.closeEditor(editor, true);
                    }
                  });

              IWorkspace workspace = ResourcesPlugin.getWorkspace();

              IWorkspaceDescription description = workspace.getDescription();

              boolean isAutoBuilding = description.isAutoBuilding();

              if (isAutoBuilding) {
                description.setAutoBuilding(false);
                try {
                  workspace.setDescription(description);
                } catch (CoreException e) {
                  log.warn("could not disable auto building", e);
                }
              }

              Map<String, IReferencePoint> convertedMapping = new HashMap<>();

              for (Entry<String, IContainer> entry : referencePointContainers.entrySet()) {
                convertedMapping.put(entry.getKey(), new EclipseReferencePoint(entry.getValue()));
              }

              ResourceNegotiation.Status status =
                  negotiation.run(convertedMapping, ProgressMonitorAdapterFactory.convert(monitor));

              if (isAutoBuilding) {
                description.setAutoBuilding(true);
                try {
                  workspace.setDescription(description);
                } catch (CoreException e) {
                  log.warn("could not re-enable auto building", e);
                }
              }

              if (status != ResourceNegotiation.Status.OK) {
                return Status.CANCEL_STATUS;
              }

              List<String> referencePointNames = new ArrayList<>();

              for (IReferencePoint referencePoint : convertedMapping.values()) {
                referencePointNames.add(referencePoint.getName());
              }

              SarosView.showNotification(
                  Messages
                      .AddReferencePointsToSessionWizard_synchronize_finished_notification_title,
                  MessageFormat.format(
                      Messages
                          .AddReferencePointsToSessionWizard_synchronize_finished_notification_text,
                      StringUtils.join(referencePointNames, ", ")));

            } catch (RuntimeException e) {
              log.error("unknown error during resource negotiation: " + e.getMessage(), e);
              return Status.CANCEL_STATUS;
            } finally {
              SWTUtils.runSafeSWTAsync(
                  log,
                  () -> {
                    for (IEditorPart editor : editorsToClose) {
                      if (((IFileEditorInput) editor.getEditorInput()).getFile().exists()) {
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

  private boolean confirmOverwritingResources(Map<String, FileListDiff> modifiedResources) {
    String message = Messages.AddReferencePointsToSessionWizard_synchronize_resource_roots;
    String pluginID = Saros.PLUGIN_ID;

    MultiStatus info = new MultiStatus(pluginID, 1, message, null);

    // TODO include folder information

    for (String referencePointName : modifiedResources.keySet()) {
      FileListDiff diff = modifiedResources.get(referencePointName);

      info.add(
          new Status(
              IStatus.INFO,
              pluginID,
              1,
              MessageFormat.format(
                  Messages.AddReferencePointsToSessionWizard_files_affected, referencePointName),
              null));

      for (String path : diff.getRemovedFiles()) {
        info.add(
            new Status(
                IStatus.WARNING,
                pluginID,
                1,
                MessageFormat.format(
                    Messages.AddReferencePointsToSessionWizard_file_toRemove, path),
                null));
      }

      for (String path : diff.getAlteredFiles()) {
        info.add(
            new Status(
                IStatus.WARNING,
                pluginID,
                1,
                MessageFormat.format(
                    Messages.AddReferencePointsToSessionWizard_file_overwritten, path),
                null));
      }

      info.add(new Status(IStatus.INFO, pluginID, 1, "", null)); // $NON-NLS-1$
    }

    return new OverwriteErrorDialog(
                getShell(),
                Messages.AddReferencePointsToSessionWizard_delete_local_changes,
                null,
                info)
            .open()
        == IDialogConstants.OK_ID;
  }

  private void showCancelMessage(JID jid, String errorMsg, CancelLocation cancelLocation) {

    String peerJid = jid.getBase();
    Shell shell = SWTUtils.getShell();

    if (errorMsg != null) {
      switch (cancelLocation) {
        case LOCAL:
          DialogUtils.openErrorMessageDialog(
              shell,
              Messages.AddReferencePointsToSessionWizard_invitation_canceled,
              MessageFormat.format(
                  Messages.AddReferencePointsToSessionWizard_invitation_canceled_text, errorMsg));
          break;
        case REMOTE:
          DialogUtils.openErrorMessageDialog(
              shell,
              Messages.AddReferencePointsToSessionWizard_invitation_canceled,
              MessageFormat.format(
                  Messages.AddReferencePointsToSessionWizard_invitation_canceled_text2,
                  peerJid,
                  errorMsg));
      }
    } else {
      switch (cancelLocation) {
        case LOCAL:
          break;
        case REMOTE:
          DialogUtils.openInformationMessageDialog(
              shell,
              Messages.AddReferencePointsToSessionWizard_invitation_canceled,
              MessageFormat.format(
                  Messages.AddReferencePointsToSessionWizard_invitation_canceled_text3, peerJid));
      }
    }
  }

  /**
   * Returns all open editors belonging to resources located under one of the given containers.
   *
   * @param referencePointContainers the containers used to represent the shared reference points in
   *     the local workspace
   * @return all open editors belonging to resources located under one of the given containers
   */
  private Collection<IEditorPart> getOpenEditorsForSharedContainers(
      Collection<IContainer> referencePointContainers) {

    Set<IPath> referencePointPaths =
        referencePointContainers.stream().map(IContainer::getFullPath).collect(Collectors.toSet());

    List<IEditorPart> openEditors = new ArrayList<>();

    Set<IEditorPart> editors = EditorAPI.getOpenEditors();

    for (IEditorPart editor : editors) {
      if (editor.getEditorInput() instanceof IFileEditorInput) {
        IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();

        for (IPath referencePointPath : referencePointPaths) {
          if (referencePointPath.isPrefixOf(file.getFullPath())) {
            openEditors.add(editor);

            break;
          }
        }
      }
    }

    return openEditors;
  }

  /**
   * Creates all given containers if necessary. Returns all resources that will be modified as part
   * of the resource negotiation.
   *
   * @param referencePointContainers the containers used to represent the shared reference points in
   *     the local workspace
   * @return all resources that will be modified as part of the resource negotiation
   */
  private Map<String, FileListDiff> createContainersAndGetModifiedResources(
      Map<String, IContainer> referencePointContainers) throws CoreException {

    Map<String, IContainer> existingReferencePointContainers =
        getExistingReferencePointContainers(referencePointContainers);

    Map<String, FileListDiff> result = new HashMap<>();

    try {
      getContainer()
          .run(
              true,
              false,
              monitor -> {
                try {

                  for (IContainer container : referencePointContainers.values()) {
                    if (!existingReferencePointContainers.containsValue(container)) {

                      if (!container.exists()) {
                        if (container instanceof IProject) {
                          ((IProject) container).create(null);

                        } else if (container instanceof IFolder) {
                          ((IFolder) container).create(true, true, null);

                        } else {
                          throw new IllegalStateException(
                              "Encountered reference point container of unexpected type "
                                  + container.getType()
                                  + " - "
                                  + container);
                        }

                      } else {
                        log.error(
                            "encountered container to create that already existed: " + container);
                      }
                    }

                    IProject containerProject = container.getProject();

                    if (!containerProject.isOpen()) {
                      containerProject.open(null);
                    }
                  }

                  result.putAll(getModifiedResources(existingReferencePointContainers, monitor));

                } catch (CoreException | RuntimeException e) {
                  throw new InvocationTargetException(e);
                }
              });
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();

      if (cause instanceof CoreException) {
        throw (CoreException) cause;
      } else if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      } else {
        throw new RuntimeException(cause.getMessage(), cause);
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("unexpected interruption", e);
    }

    return result;
  }

  /**
   * Returns all modified resources (either changed or deleted) for the given local reference point
   * mapping.
   *
   * <p><b>Important:</b> Do not call this inside the SWT Thread. This is a long running operation!
   *
   * @param referencePointContainers the containers used to represent the shared reference points in
   *     the local workspace
   * @param monitor the progress monitor to which to report
   * @return all modified resources (either changed or deleted) for the given local reference point
   *     mapping
   */
  private Map<String, FileListDiff> getModifiedResources(
      Map<String, IContainer> referencePointContainers, IProgressMonitor monitor)
      throws CoreException {

    Map<String, FileListDiff> modifiedResources = new HashMap<>();

    ISarosSession session = sessionManager.getSession();

    // FIXME the wizard should handle the case that the session may stop in
    // the meantime !

    if (session == null) {
      throw new IllegalStateException("no session running");
    }

    IChecksumCache checksumCache = session.getComponent(IChecksumCache.class);

    if (checksumCache == null) {
      throw new IllegalStateException("failed to obtain checksum cache from session context");
    }

    SubMonitor subMonitor =
        SubMonitor.convert(
            monitor,
            "Searching for files that will be modified...",
            referencePointContainers.size());

    for (Entry<String, IContainer> entry : referencePointContainers.entrySet()) {

      String referencePointId = entry.getKey();
      IContainer referencePointContainer = entry.getValue();

      IReferencePoint referencePoint = new EclipseReferencePoint(referencePointContainer);

      if (!session.isShared(referencePoint)) {
        referencePointContainer.refreshLocal(IResource.DEPTH_INFINITE, null);
      }

      FileList localFileList;

      try {
        localFileList =
            FileListFactory.createFileList(
                referencePoint,
                checksumCache,
                ProgressMonitorAdapterFactory.convert(
                    subMonitor.newChild(1, SubMonitor.SUPPRESS_ALL_LABELS)));
      } catch (IOException e) {
        Throwable cause = e.getCause();

        if (cause instanceof CoreException) {
          throw (CoreException) cause;
        }

        throw new CoreException(
            new org.eclipse.core.runtime.Status(
                IStatus.ERROR, Saros.PLUGIN_ID, "failed to compute local file list", e));
      }

      ResourceNegotiationData data = negotiation.getResourceNegotiationData(referencePointId);

      FileListDiff diff = FileListDiff.diff(localFileList, data.getFileList());

      if (!diff.getRemovedFolders().isEmpty()
          || !diff.getRemovedFiles().isEmpty()
          || !diff.getAlteredFiles().isEmpty()) {
        modifiedResources.put(referencePoint.getName(), diff);
      }
    }

    return modifiedResources;
  }

  /**
   * Returns all containers used to represent the shared reference points in the local workspace
   * that already exist. The content of such containers will be overwritten as part of the resource
   * negotiation.
   *
   * @param referencePointContainers the containers used to represent the shared reference points in
   *     the local workspace
   * @return all containers used to represent the shared reference points in the local workspace *
   *     that already exist
   */
  private Map<String, IContainer> getExistingReferencePointContainers(
      Map<String, IContainer> referencePointContainers) {

    Map<String, IContainer> modifiedResources = new HashMap<>();

    for (Entry<String, IContainer> entry : referencePointContainers.entrySet()) {
      String referencePointID = entry.getKey();
      IContainer referencePointContainer = entry.getValue();

      ReferencePointOptionResult result =
          localRepresentationSelectionPage.getResult(referencePointID);

      if (result.getLocalRepresentationOption() != EXISTING_DIRECTORY) {
        continue;
      }

      modifiedResources.put(referencePointID, referencePointContainer);
    }

    return modifiedResources;
  }

  /**
   * Returns the containers used to represent the shared reference points in the local workspace.
   *
   * @return the containers used to represent the shared reference points in the local workspace
   */
  private Map<String, IContainer> getReferencePointContainerMapping() {
    Map<String, IContainer> referencePointContainers = new HashMap<>();

    for (ResourceNegotiationData data : negotiation.getResourceNegotiationData()) {
      String referencePointID = data.getReferencePointID();
      String referencePointName = data.getReferencePointName();

      ReferencePointOptionResult result =
          localRepresentationSelectionPage.getResult(referencePointID);

      IContainer container;

      try {
        container = result.getSelectedContainerHandle(referencePointName);

      } catch (IllegalInputException e) {
        log.error(
            "Failed to obtain container to represent shared reference point "
                + referencePointName
                + "(id "
                + referencePointID
                + ")",
            e);

        continue;
      }

      referencePointContainers.put(referencePointID, container);
    }

    return referencePointContainers;
  }

  private void displaySaveDirtyEditorsDialog(List<IEditorPart> dirtyEditors) {
    int max = Math.min(20, dirtyEditors.size());
    int more = dirtyEditors.size() - max;

    List<String> dirtyEditorNames = new ArrayList<>();

    for (IEditorPart editor : dirtyEditors.subList(0, max)) {
      dirtyEditorNames.add(editor.getTitle());
    }

    Collections.sort(dirtyEditorNames);

    if (more > 0) {
      dirtyEditorNames.add(
          MessageFormat.format(
              Messages.AddReferencePointsToSessionWizard_unsaved_changes_dialog_more, more));
    }

    String allDirtyEditorNames = StringUtils.join(dirtyEditorNames, ", ");

    String dialogText =
        MessageFormat.format(
            Messages.AddReferencePointsToSessionWizard_unsaved_changes_dialog_text,
            allDirtyEditorNames);

    boolean proceed =
        DialogUtils.openQuestionMessageDialog(
            getShell(),
            Messages.AddReferencePointsToSessionWizard_unsaved_changes_dialog_title,
            dialogText);

    /*
     * The wizard can be closed automatically and so 'proceed' would be true
     * if this happens although the user did not clicked anything.
     */
    if (getShell().isDisposed()) {
      return;
    }

    if (proceed) {
      for (IEditorPart editor : dirtyEditors) {
        editor.doSave(new NullProgressMonitor());
      }
    }
  }

  /**
   * Stores the current reference point path mapping.
   *
   * @param jid the peer that started the resource negotiation
   * @param referencePointContainers the containers used to represent the shared reference points in
   *     the local workspace
   */
  private void storeCurrentReferencePointPathMapping(
      JID jid, Map<String, IContainer> referencePointContainers) {

    Map<String, String> currentReferencePointPaths = new HashMap<>();

    for (Entry<String, IContainer> entry : referencePointContainers.entrySet()) {
      String referencePointId = entry.getKey();

      String referencePointPath = entry.getValue().getFullPath().toPortableString();
      String referencePointName =
          negotiation.getResourceNegotiationData(referencePointId).getReferencePointName();

      currentReferencePointPaths.put(referencePointName, referencePointPath);
    }

    mappingStorage.updateMapping(jid, currentReferencePointPaths);
  }

  private static class ResourceMappingStorage {
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

    public ResourceMappingStorage(IPreferenceStore store) {
      this.store = store;
    }

    public Map<String, String> getMapping(JID jid) {
      Map<String, String> result = new HashMap<>();

      String mapping = store.getString(RESOURCE_MAPPING_STORE_PREFIX + jid.getBase());

      if (mapping.isEmpty()) {
        return result;
      }

      String[] names = SPLIT_PATTERN.split(mapping);

      for (int i = 0; i < names.length - 1; i += 2) {
        if (names[i].isEmpty() || names[i + 1].isEmpty()) {
          continue;
        }

        result.put(unescape(names[i]), unescape(names[i + 1]));
      }

      return result;
    }

    public void updateMapping(JID jid, Map<String, String> resourceNameMapping) {

      Map<String, String> lastResourceNameMapping = getMapping(jid);

      Set<String> resourceNameDestinations = new HashSet<>(resourceNameMapping.values());

      /*
       * Remove existing mappings to avoid following problem:
       * 1. We add A->A, B->B, C->C
       * 2. Afterwards  A->A, B->C
       * 3. When we now lookup A,B,C we will get A->A, B->C, C->C which is an invalid mapping
       *
       * In other words, ensure that the mapping is injective.
       */

      lastResourceNameMapping
          .entrySet()
          .removeIf(entry -> resourceNameDestinations.contains(entry.getValue()));

      lastResourceNameMapping.putAll(resourceNameMapping);

      if (lastResourceNameMapping.isEmpty()) {
        return;
      }

      StringBuilder builder = new StringBuilder();

      for (Entry<String, String> entry : lastResourceNameMapping.entrySet()) {
        builder.append(escape(entry.getKey()));
        builder.append(DELIMITER);
        builder.append(escape(entry.getValue()));
        builder.append(DELIMITER);
      }

      builder.setLength(builder.length() - DELIMITER.length());

      store.setValue(RESOURCE_MAPPING_STORE_PREFIX + jid.getBase(), builder.toString());
    }

    private static String escape(String value) {
      return value.replace(DELIMITER, ESCAPE_SEQUENCE);
    }

    private static String unescape(String value) {
      return value.replace(ESCAPE_SEQUENCE, DELIMITER);
    }
  }
}
