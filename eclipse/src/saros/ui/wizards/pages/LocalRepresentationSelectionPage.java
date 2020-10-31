package saros.ui.wizards.pages;

import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import saros.exception.IllegalInputException;
import saros.filesystem.IReferencePoint;
import saros.filesystem.ResourceConverter;
import saros.negotiation.ResourceNegotiationData;
import saros.negotiation.additional_resource_data.AbstractPossibleRepresentationProvider;
import saros.net.IConnectionManager;
import saros.net.xmpp.JID;
import saros.preferences.Preferences;
import saros.session.ISarosSession;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.widgets.wizard.ReferencePointOptionComposite;
import saros.ui.widgets.wizard.ReferencePointOptionResult;
import saros.ui.widgets.wizard.events.ReferencePointOptionListener;

/**
 * A wizard page that allows to choose how to represent the shared reference points in the local
 * workspace.
 *
 * @see ReferencePointOptionComposite
 */
// TODO add logic to help user differentiate multiple reference points with the same name
public class LocalRepresentationSelectionPage extends WizardPage {
  private final JID peer;

  private final ISarosSession session;

  /**
   * Map containing the mapping from the remote reference point id to the remote reference point
   * name.
   */
  private final Map<String, String> remoteReferencePointIdToNameMapping;

  private final Map<String, List<Pair<String, String>>>
      referencePointIdPossibleRepresentationMapping;

  private final Map<String, ReferencePointOptionComposite> referencePointOptionComposites;

  /** Map containing the current error messages for every reference point id. */
  private final Map<String, String> currentErrors;

  /**
   * Map containing the desired path for specific reference point names.
   *
   * <p>This mapping is created by saving which paths specific reference points were mapped to in
   * previous sessions.
   */
  private final Map<String, String> previousReferencePointPathMapping;

  private Preferences preferences;

  private final IConnectionManager connectionManager;

  private final Set<String> unsupportedCharsets;

  public LocalRepresentationSelectionPage(
      ISarosSession session,
      IConnectionManager connectionManager,
      Preferences preferences,
      JID peer,
      List<ResourceNegotiationData> resourceNegotiationData,
      Map<String, String> previousReferencePointPathMapping) {

    super(Messages.LocalRepresentationSelectionPage_page_name);

    this.session = session;
    this.connectionManager = connectionManager;
    this.preferences = preferences;
    this.peer = peer;

    this.previousReferencePointPathMapping =
        previousReferencePointPathMapping != null
            ? previousReferencePointPathMapping
            : Collections.emptyMap();

    this.remoteReferencePointIdToNameMapping = new HashMap<>();
    this.referencePointIdPossibleRepresentationMapping = new HashMap<>();
    this.referencePointOptionComposites = new HashMap<>();
    this.currentErrors = new HashMap<>();
    this.unsupportedCharsets = new HashSet<>();

    for (ResourceNegotiationData data : resourceNegotiationData) {
      String referencePointId = data.getReferencePointID();

      remoteReferencePointIdToNameMapping.put(referencePointId, data.getReferencePointName());

      List<Pair<String, String>> possibleRepresentations =
          AbstractPossibleRepresentationProvider.getPossibleRepresentations(data);
      referencePointIdPossibleRepresentationMapping.put(referencePointId, possibleRepresentations);

      unsupportedCharsets.addAll(getUnsupportedCharsets(data.getFileList().getEncodings()));
    }

    setPageComplete(false);
    setTitle(Messages.LocalRepresentationSelectionPage_title);
  }

  /**
   * Returns the result of the reference point option page for the given ID.
   *
   * @param referencePointId the ID of the reference point
   * @return the result of the reference point option page for the given ID
   */
  public ReferencePointOptionResult getResult(String referencePointId) {
    return referencePointOptionComposites.get(referencePointId).getResult();
  }

  @Override
  public void performHelp() {
    SWTUtils.openExternalBrowser(Messages.LocalRepresentationSelectionPage_saros_url);
  }

  @Override
  public void createControl(Composite parent) {
    Composite composite =
        new Composite(parent, SWT.NONE) {
          // dirty hack - if someone knows how to do it right with layout please change this
          @Override
          public Point computeSize(int wHint, int hHint, boolean changed) {
            Point result = super.computeSize(wHint, hHint, changed);

            int maxSize = 800; // prevent the TAB folder exploding horizontal

            if (result.x < maxSize) return result;

            result.x = maxSize;

            return result;
          }
        };

    setControl(composite);

    composite.setLayout(new FillLayout());

    TabFolder tabFolder = new TabFolder(composite, SWT.TOP);

    for (String referencePointID : remoteReferencePointIdToNameMapping.keySet()) {
      TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
      tabItem.setText(remoteReferencePointIdToNameMapping.get(referencePointID));

      ReferencePointOptionComposite tabComposite =
          new ReferencePointOptionComposite(tabFolder, referencePointID);

      tabItem.setControl(tabComposite);

      referencePointOptionComposites.put(referencePointID, tabComposite);
    }

    tabFolder.addSelectionListener(
        new SelectionListener() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            showNewTabErrorMessage(e);
          }

          @Override
          public void widgetDefaultSelected(SelectionEvent e) {
            showNewTabErrorMessage(e);
          }

          /**
           * Ensures that error message for the newly selected tab is shown if present.
           *
           * @param e the selection event
           */
          private void showNewTabErrorMessage(SelectionEvent e) {
            Control selectedTabComposite = ((TabItem) e.item).getControl();

            for (Entry<String, ReferencePointOptionComposite> entry :
                referencePointOptionComposites.entrySet()) {

              String referencePointId = entry.getKey();
              ReferencePointOptionComposite composite = entry.getValue();

              if (composite.equals(selectedTabComposite)) {
                updateErrorMessage(referencePointId);

                return;
              }
            }
          }
        });

    updateConnectionStatus();

    // invokes updatePageComplete for every reference point id
    preselectValues();

    // add listeners after setting initial values to avoid unnecessary checks
    attachListeners();
  }

  /**
   * Attaches reference point option listeners to all held reference point option composites. The
   * listeners are used to update the completion state of the page on user input.
   */
  private void attachListeners() {
    for (ReferencePointOptionComposite composite : referencePointOptionComposites.values()) {

      composite.addReferencePointOptionListener(
          new ReferencePointOptionListener() {
            @Override
            public void valueChanged(ReferencePointOptionComposite composite) {
              updatePageComplete(composite.getRemoteReferencePointId());
            }

            @Override
            public void selectedOptionChanged(ReferencePointOptionComposite composite) {
              updatePageComplete(composite.getRemoteReferencePointId());
            }
          });
    }
  }

  /**
   * Checks whether the current selection for the given reference point ID is valid.
   *
   * @param referencePointId the ID of the reference point whose input to check
   * @return the error message describing why the input is invalid or <code>null</code> if the input
   *     is valid
   */
  private String isReferencePointRepresentationSelectionValid(String referencePointId) {
    ReferencePointOptionComposite referencePointOptionComposite =
        referencePointOptionComposites.get(referencePointId);

    String referencePointName = remoteReferencePointIdToNameMapping.get(referencePointId);

    ReferencePointOptionResult result = referencePointOptionComposite.getResult();

    IContainer selectedContainer;
    try {
      selectedContainer = result.getSelectedContainerHandle(referencePointName);

    } catch (IllegalInputException e) {
      return e.getMessage();
    }

    IPath selectedPath = selectedContainer.getFullPath();

    // check for clashes in current selection
    for (Entry<String, IPath> entry : getOtherReferencePointPaths(referencePointId).entrySet()) {
      String otherReferencePointName = remoteReferencePointIdToNameMapping.get(entry.getKey());
      IPath otherReferencePointPath = entry.getValue();

      if (selectedPath.equals(otherReferencePointPath)) {
        return MessageFormat.format(
            Messages.LocalRepresentationSelectionPage_error_reference_point_path_clash,
            selectedPath.toOSString(),
            referencePointName,
            otherReferencePointName);

      } else if (otherReferencePointPath.isPrefixOf(selectedPath)) {
        return MessageFormat.format(
            Messages.LocalRepresentationSelectionPage_error_nested_selected_reference_point_paths,
            selectedPath.toOSString(),
            referencePointName,
            otherReferencePointName);
      }
    }

    // check for clashes with already shared reference points
    for (IReferencePoint sharedReferencePoint : session.getReferencePoints()) {
      IContainer referencePointDelegate = ResourceConverter.getDelegate(sharedReferencePoint);
      IPath referencePointPath = referencePointDelegate.getFullPath();

      if (referencePointPath.equals(selectedPath)) {
        return MessageFormat.format(
            Messages.LocalRepresentationSelectionPage_error_existing_reference_point_path_clash,
            selectedPath.toOSString(),
            referencePointName,
            sharedReferencePoint.getName());

      } else if (referencePointPath.isPrefixOf(selectedPath)) {
        return MessageFormat.format(
            Messages.LocalRepresentationSelectionPage_error_child_of_existing_reference_point,
            selectedPath.toOSString(),
            referencePointName,
            sharedReferencePoint.getName());

      } else if (selectedPath.isPrefixOf(referencePointPath)) {
        return MessageFormat.format(
            Messages.LocalRepresentationSelectionPage_error_parent_of_existing_reference_point,
            selectedPath.toOSString(),
            referencePointName,
            sharedReferencePoint.getName());
      }
    }

    return null;
  }

  /**
   * Updates the page completion of the wizard.
   *
   * <p>Updates the validity state of all tabs. Updates the shown error messages, prioritizing
   * errors for the given reference point ID.
   *
   * @param prioritizedReferencePointId the ID of the reference point whose error messages to
   *     prioritize or <code>null</code> if not reference point should be prioritized
   * @see #updateErrorMessage(String)
   */
  private void updatePageComplete(String prioritizedReferencePointId) {
    // also update all others because errors may be no longer valid
    for (String referencePointId : referencePointOptionComposites.keySet()) {
      updateReferencePointSelectionStatus(referencePointId);
    }

    if (!currentErrors.isEmpty()) {
      updateErrorMessage(prioritizedReferencePointId);

      setPageComplete(false);

      return;
    }

    setErrorMessage(null);

    String warningMessage;

    if (!unsupportedCharsets.isEmpty()) {
      warningMessage =
          MessageFormat.format(
              Messages.LocalRepresentationSelectionPage_warning_unsupported_encoding_found,
              StringUtils.join(unsupportedCharsets, ", "));

    } else {
      warningMessage = null;
    }

    setMessage(warningMessage, WARNING);

    setPageComplete(true);
  }

  /**
   * Updates the shown error message.
   *
   * <p>If there are not error messages, nothing is displayed.
   *
   * <p>If there is an error message for the given reference point id, it is displayed. Otherwise,
   * one of the held error messages is displayed. Which error message in particular is displayed is
   * not strictly defined. If the currently displayed error message is still valid, it is kept.
   *
   * @param prioritizedReferencePointId the ID of the reference point whose error messages to
   *     prioritize or <code>null</code> if no reference point should be prioritized
   */
  private void updateErrorMessage(String prioritizedReferencePointId) {
    if (currentErrors.isEmpty()) {
      setErrorMessage(null);

      return;
    }

    String prioritizedErrorMessage =
        prioritizedReferencePointId == null ? null : currentErrors.get(prioritizedReferencePointId);

    if (prioritizedErrorMessage != null) {
      setErrorMessage(prioritizedErrorMessage);

    } else {
      String displayedErrorMessage = getErrorMessage();

      // only set new error message if current message is not valid
      if (displayedErrorMessage == null
          || displayedErrorMessage.isEmpty()
          || !currentErrors.containsValue(displayedErrorMessage)) {

        setErrorMessage(currentErrors.values().iterator().next());
      }
    }
  }

  /**
   * Updates the validity status of the reference point selection tab for the given ID.
   *
   * <p>If the reference point selection tab selection is valid, the error message entry is dropped
   * (if present). Otherwise, the new error message is entered into the list of current errors. Any
   * existing error messages for the tab are overwritten.
   *
   * @param referencePointID the ID of the reference point tab to update
   * @see #isReferencePointRepresentationSelectionValid(String)
   */
  private void updateReferencePointSelectionStatus(String referencePointID) {
    String errorMessage = isReferencePointRepresentationSelectionValid(referencePointID);

    if (errorMessage != null) {
      currentErrors.put(referencePointID, errorMessage);

    } else {
      currentErrors.remove(referencePointID);
    }
  }

  /**
   * Sets preselected values for all contained reference point option composites and then runs the
   * input validation. The preselected values do not have to be valid.
   *
   * <p>Sets the reference point name as the default value for the new project and new directory
   * name.
   *
   * <p>If there is a previous path mapping for the reference point name, the option to use an
   * existing directory using that path is selected. Otherwise, if a proposed resource
   * representations applies to the local workspace, the option to use an existing directory using
   * the proposed resource is selected.
   */
  private void preselectValues() {
    // try to assign local names for the remaining remote reference points
    for (Entry<String, ReferencePointOptionComposite> entry :
        referencePointOptionComposites.entrySet()) {

      String referencePointId = entry.getKey();
      ReferencePointOptionComposite referencePointOptionComposite = entry.getValue();

      String remoteReferencePointName = remoteReferencePointIdToNameMapping.get(referencePointId);

      setReferencePointName(referencePointOptionComposite, remoteReferencePointName);

      String previousReferencePointPath =
          previousReferencePointPathMapping.get(remoteReferencePointName);

      /*
       * only use previous mapping if it does not clash with the current mapping
       */
      if (previousReferencePointPath != null) {
        IContainer desiredContainer =
            ReferencePointOptionResult.getContainerForPath(previousReferencePointPath);

        if (desiredContainer != null && desiredContainer.exists()) {
          String existingDirectoryPath = desiredContainer.getFullPath().toOSString();

          referencePointOptionComposite.setExistingDirectoryOptionSelected(existingDirectoryPath);
        }

      } else {

        for (Pair<String, String> possibleRepresentation :
            referencePointIdPossibleRepresentationMapping.get(referencePointId)) {

          String projectName = possibleRepresentation.getLeft();
          String projectRelativePath = possibleRepresentation.getRight();

          IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

          if (!project.exists()) {
            continue;
          }

          if (projectRelativePath.isEmpty()) {
            referencePointOptionComposite.setExistingDirectoryOptionSelected(
                project.getFullPath().toOSString());

            break;
          }

          IContainer container = project.getFolder(Path.fromPortableString(projectRelativePath));

          if (container.exists()) {
            referencePointOptionComposite.setExistingDirectoryOptionSelected(
                container.getFullPath().toOSString());

            break;
          }
        }
      }
    }

    // validate initial input state
    updatePageComplete(null);
  }

  /**
   * Sets the reference point name as the value in the fields for the new directory name in the
   * given reference point option composite.
   *
   * @param referencePointOptionComposite the reference point option composite to update
   * @param referencePointName the name to set in the name fields
   */
  private void setReferencePointName(
      ReferencePointOptionComposite referencePointOptionComposite, String referencePointName) {

    referencePointOptionComposite.setNewDirectoryOptionSelected(referencePointName, null);
  }

  /**
   * Returns the paths currently selected to represent other the shared reference points of the
   * negotiation.
   *
   * @param excludedReferencePointId the ID of the reference point ot exclude from the list
   * @return the paths currently selected to represent other the shared reference points of the
   *     negotiation
   */
  private Map<String, IPath> getOtherReferencePointPaths(String excludedReferencePointId) {
    Map<String, IPath> otherReferencePointPaths = new HashMap<>();

    for (Entry<String, ReferencePointOptionComposite> entry :
        referencePointOptionComposites.entrySet()) {

      String referencePointId = entry.getKey();

      if (referencePointId.equals(excludedReferencePointId)) {
        continue;
      }

      ReferencePointOptionResult result = entry.getValue().getResult();

      IContainer chosenContainer;
      try {
        chosenContainer =
            result.getSelectedContainerHandle(
                remoteReferencePointIdToNameMapping.get(referencePointId));

      } catch (IllegalInputException e) {
        continue;
      }

      otherReferencePointPaths.put(referencePointId, chosenContainer.getFullPath());
    }

    return otherReferencePointPaths;
  }

  /** get transfer mode and set header information of the wizard. */
  private void updateConnectionStatus() {

    // FIXME we are using Smack for File transfer so we might end up with other transfer modes
    switch (connectionManager.getTransferMode(ISarosSession.SESSION_CONNECTION_ID, peer)) {
      case SOCKS5_MEDIATED:
        if (preferences.isLocalSOCKS5ProxyEnabled())
          setDescription(Messages.LocalRepresentationSelectionPage_description_socks5proxy);
        else setDescription(Messages.LocalRepresentationSelectionPage_description_file_transfer);
        setImageDescriptor(ImageManager.getImageDescriptor("icons/wizban/socks5m.png"));
        break;

      case SOCKS5_DIRECT:
        // all is fine
      case NONE:
        // lost data connection
        break;

      case IBB:
        if (preferences.forceIBBTransport()) {
          setDescription(Messages.LocalRepresentationSelectionPage_direct_filetransfer_deactivated);
        } else {
          setDescription(Messages.LocalRepresentationSelectionPage_direct_filetransfer_nan);
        }

        new FlashTask(
                this,
                1000,
                ImageManager.getImage("icons/wizban/ibb.png"),
                ImageManager.getImage("icons/wizban/ibbFaded.png"))
            .run();

        break;
      default:
        setDescription(Messages.LocalRepresentationSelectionPage_unknown_transport_method);
        break;
    }
  }

  /**
   * Returns all charsets from the given set that are not available on the current JVM.
   *
   * @param charsets the charsets to check
   * @return the charsets not supported by the local JVM
   */
  private Set<String> getUnsupportedCharsets(Set<String> charsets) {
    Set<String> missingCharsets = new HashSet<>();

    for (String charset : charsets) {
      try {
        Charset.forName(charset);
      } catch (Exception e) {
        missingCharsets.add(charset);
      }
    }

    return missingCharsets;
  }

  private static class FlashTask implements Runnable {

    private final ImageDescriptor[] states;
    private final Display display;
    private final Control control;
    private final IDialogPage page;
    private final int delay;

    int state = 0;

    public FlashTask(IDialogPage page, int delay, Image... images) {
      this.page = page;
      this.delay = delay;

      control = page.getControl();
      display = page.getControl().getDisplay();
      states = new ImageDescriptor[images.length];

      for (int i = 0; i < states.length; i++)
        states[i] = ImageDescriptor.createFromImage(images[i]);
    }

    @Override
    public void run() {
      if (display.isDisposed() || control.isDisposed()) return;

      state %= states.length;

      page.setImageDescriptor(states[state++]);
      display.timerExec(delay, this);
    }
  }
}
