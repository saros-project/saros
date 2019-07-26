package saros.ui.wizards.pages;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import saros.negotiation.ProjectNegotiationData;
import saros.net.IConnectionManager;
import saros.net.xmpp.JID;
import saros.preferences.Preferences;
import saros.session.ISarosSession;
import saros.ui.ImageManager;
import saros.ui.Messages;
import saros.ui.util.SWTUtils;
import saros.ui.widgets.wizard.ProjectOptionComposite;
import saros.ui.widgets.wizard.events.ProjectNameChangedEvent;
import saros.ui.widgets.wizard.events.ProjectOptionListener;

/** A wizard page that allows to enter the new project name or to choose to overwrite a project. */
public class EnterProjectNamePage extends WizardPage {

  private final JID peer;

  private final ISarosSession session;

  /** Map containing the mapping from the remote project id to the remote project name. */
  private final Map<String, String> remoteProjectIdToNameMapping;

  private final Map<String, ProjectOptionComposite> projectOptionComposites =
      new HashMap<String, ProjectOptionComposite>();

  /** Map containing the current error messages for every project id. */
  private final Map<String, String> currentErrors = new HashMap<>();

  /**
   * Map containing the desired project name mapping as remote project name to local project name
   */
  private final Map<String, String> desiredRemoteToLocalProjectNameMapping;

  private Preferences preferences;

  private final IConnectionManager connectionManager;

  private final Set<String> unsupportedCharsets = new HashSet<String>();

  public EnterProjectNamePage(
      ISarosSession session,
      IConnectionManager connectionManager,
      Preferences preferences,
      JID peer,
      List<ProjectNegotiationData> projectNegotiationData,
      Map<String, String> desiredRemoteToLocalProjectNameMapping) {

    super(Messages.EnterProjectNamePage_title);
    this.session = session;
    this.connectionManager = connectionManager;
    this.preferences = preferences;
    this.peer = peer;

    this.desiredRemoteToLocalProjectNameMapping =
        desiredRemoteToLocalProjectNameMapping != null
            ? desiredRemoteToLocalProjectNameMapping
            : Collections.emptyMap();

    remoteProjectIdToNameMapping = new HashMap<String, String>();

    for (final ProjectNegotiationData data : projectNegotiationData) {

      remoteProjectIdToNameMapping.put(data.getProjectID(), data.getProjectName());

      unsupportedCharsets.addAll(getUnsupportedCharsets(data.getFileList().getEncodings()));
    }

    setPageComplete(false);
    setTitle(Messages.EnterProjectNamePage_title2);
  }

  /** Returns the name of the project to use during the shared session. */
  public String getTargetProjectName(String projectID) {
    return projectOptionComposites.get(projectID).getProjectName();
  }

  /**
   * @return <code>true</code> if the synchronization option chosen by the user could lead to
   *     overwriting project resources, <code>false</code> otherwise.
   */
  public boolean overwriteResources(String projectID) {
    return projectOptionComposites.get(projectID).useExistingProject();
  }

  @Override
  public void performHelp() {
    SWTUtils.openExternalBrowser(Messages.EnterProjectNamePage_saros_url);
  }

  @Override
  public void createControl(Composite parent) {

    Composite composite =
        new Composite(parent, SWT.NONE) {
          // dirty hack - if someone knows how to do it right with layout please change this
          @Override
          public Point computeSize(int wHint, int hHint, boolean changed) {
            final Point result = super.computeSize(wHint, hHint, changed);

            final int maxSize = 800; // prevent the TAB folder exploding horizontal

            if (result.x < maxSize) return result;

            result.x = maxSize;

            return result;
          }
        };

    setControl(composite);

    composite.setLayout(new FillLayout());

    TabFolder tabFolder = new TabFolder(composite, SWT.TOP);

    for (final String projectID : remoteProjectIdToNameMapping.keySet()) {

      TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
      tabItem.setText(remoteProjectIdToNameMapping.get(projectID));

      ProjectOptionComposite tabComposite = new ProjectOptionComposite(tabFolder, projectID);

      tabItem.setControl(tabComposite);

      projectOptionComposites.put(projectID, tabComposite);
    }

    updateConnectionStatus();

    attachListeners();

    // invokes updatePageComplete for every project id
    preselectProjectNames();
  }

  private void attachListeners() {
    for (ProjectOptionComposite composite : projectOptionComposites.values()) {

      composite.addProjectOptionListener(
          new ProjectOptionListener() {
            @Override
            public void projectNameChanged(ProjectNameChangedEvent event) {
              updatePageComplete(event.projectID);
            }
          });
    }
  }

  /**
   * Checks if the project options for the given project id are valid.
   *
   * @return an error message if the options are not valid, otherwise the error message is <code>
   *     null</code>
   */
  private String isProjectSelectionValid(String projectID) {

    ProjectOptionComposite projectOptionComposite = projectOptionComposites.get(projectID);

    String projectName = projectOptionComposite.getProjectName();

    if (projectName.isEmpty())
      return Messages.EnterProjectNamePage_set_project_name
          + " for remote project "
          + remoteProjectIdToNameMapping.get(projectID);

    IStatus status = ResourcesPlugin.getWorkspace().validateName(projectName, IResource.PROJECT);

    if (!status.isOK())
      // FIXME display remote project name
      return status.getMessage();

    List<String> currentProjectNames = getCurrentProjectNames(projectID);

    if (currentProjectNames.contains(projectName))
      // FIXME display the project ... do not let the user guess
      return MessageFormat.format(
          Messages.EnterProjectNamePage_error_projectname_in_use, projectName);

    IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

    if (projectOptionComposite.useExistingProject() && !project.exists())
      // FIXME crap error message
      return Messages.EnterProjectNamePage_error_wrong_name
          + " "
          + projectOptionComposite.getProjectName();

    if (!projectOptionComposite.useExistingProject() && project.exists())
      // FIXME we are working with tabs ! always display the remote
      // project name
      return MessageFormat.format(
          Messages.EnterProjectNamePage_error_projectname_exists, projectName);

    return null;
  }

  private void updatePageComplete(String currentProjectID) {

    // first update all others because errors may be no longer valid

    for (String projectID : projectOptionComposites.keySet()) {
      if (projectID.equals(currentProjectID)) continue;

      updateProjectSelectionStatus(projectID);
    }

    /*
     * this assumes the focus on the project option composite with the
     * current project id !
     */

    currentErrors.remove(currentProjectID);

    /*
     * do not generate errors for empty project names as long as the user is
     * on the current tab as it would be confusing
     */
    if (projectOptionComposites.get(currentProjectID).getProjectName().isEmpty()) {
      showLatestErrorMessage();
      setPageComplete(false);
      return;
    }

    updateProjectSelectionStatus(currentProjectID);

    if (!currentErrors.isEmpty()) {
      showLatestErrorMessage();
      setPageComplete(false);
      return;
    }

    setErrorMessage(null);

    String warningMessage = findAndReportProjectArtifacts();

    if (!unsupportedCharsets.isEmpty()) {
      if (warningMessage == null) warningMessage = "";
      else warningMessage += "\n";

      warningMessage +=
          "At least one remote project contains files "
              + "with a character encoding that is not available on this "
              + "Java platform. "
              + "Working on these projects may result in data loss or "
              + "corruption.\n"
              + "The following character encodings are not available: "
              + StringUtils.join(unsupportedCharsets, ", ");
    }

    setMessage(warningMessage, WARNING);

    setPageComplete(true);
  }

  /**
   * Shows the 'latest' error message (random) if there is currently anyone in the wizard page. If
   * there is no error message present the error message status of the wizard page is cleared.
   */
  private void showLatestErrorMessage() {
    if (!currentErrors.isEmpty())
      setErrorMessage(currentErrors.entrySet().iterator().next().getValue());
    else setErrorMessage(null);
  }

  private void updateProjectSelectionStatus(String projectID) {
    String errorMessage = isProjectSelectionValid(projectID);

    if (errorMessage != null) currentErrors.put(projectID, errorMessage);
    else currentErrors.remove(projectID);
  }

  /**
   * Scans the current Eclipse Workspace for project artifacts.
   *
   * @return string containing a warning message if artifacts are found, <code>null</code> otherwise
   */
  private String findAndReportProjectArtifacts() {
    IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();

    if (workspacePath == null) return null;

    File workspaceDirectory = workspacePath.toFile();

    List<String> dirtyProjectNames = new ArrayList<String>();

    for (ProjectOptionComposite composite : projectOptionComposites.values()) {

      if (composite.useExistingProject()) continue;

      String projectName = composite.getProjectName();

      if (projectName.isEmpty()) continue;

      if (new File(workspaceDirectory, projectName).exists()) dirtyProjectNames.add(projectName);
    }

    String warningMessage = null;

    if (!dirtyProjectNames.isEmpty()) {
      warningMessage =
          MessageFormat.format(
              Messages.EnterProjectNamePage_warning_project_artifacts_found,
              StringUtils.join(dirtyProjectNames, ", "));
    }

    return warningMessage;
  }

  /**
   * Preselect project names for the remote projects. First this method will try to use an existing
   * shared project and then disable the option to change the preselected values for this project.
   *
   * <p>Afterwards it tries to assign a desired project mapping and lastly will generate a unique
   * project name for projects that are still unassigned.
   */
  private void preselectProjectNames() {

    final Set<String> reservedProjectNames = new HashSet<String>();

    // force pre-selection of already shared projects
    for (Entry<String, ProjectOptionComposite> entry : projectOptionComposites.entrySet()) {

      String projectID = entry.getKey();
      ProjectOptionComposite projectOptionComposite = entry.getValue();

      saros.filesystem.IProject project = session.getProject(projectID);

      // not shared yet
      if (project == null) continue;

      projectOptionComposite.setProjectName(project.getName(), true);
      projectOptionComposite.setEnabled(false);
      reservedProjectNames.add(project.getName());
    }

    // try to assign local names for the remaining remote projects
    for (Entry<String, ProjectOptionComposite> entry : projectOptionComposites.entrySet()) {

      String projectID = entry.getKey();
      ProjectOptionComposite projectOptionComposite = entry.getValue();

      saros.filesystem.IProject project = session.getProject(projectID);

      // already shared
      if (project != null) continue;

      final String remoteProjectName = remoteProjectIdToNameMapping.get(projectID);

      final String desiredLocalProjectName =
          desiredRemoteToLocalProjectNameMapping.get(remoteProjectName);

      boolean existingProject = false;

      String projectNameProposal = null;

      /*
       * find a proposal based on the desired name only if the name is not already used and such a
       * project already exists in the workspace
       */
      if (desiredLocalProjectName != null
          && !reservedProjectNames.contains(desiredLocalProjectName)) {
        existingProject =
            ResourcesPlugin.getWorkspace().getRoot().getProject(desiredLocalProjectName).exists();
        projectNameProposal = existingProject ? desiredLocalProjectName : null;
      }

      /*
       * if we failed to find a proposal, generate one and suggest it a new project
       */
      if (projectNameProposal == null)
        projectNameProposal =
            findProjectNameProposal(remoteProjectName, reservedProjectNames.toArray(new String[0]));

      projectOptionComposite.setProjectName(projectNameProposal, existingProject);
      reservedProjectNames.add(projectNameProposal);
    }
  }

  private List<String> getCurrentProjectNames(String... projectIDsToExclude) {
    final List<String> currentProjectNames = new ArrayList<String>();

    final Set<String> excludedProjectIDs = new HashSet<String>(Arrays.asList(projectIDsToExclude));

    for (Entry<String, ProjectOptionComposite> entry : projectOptionComposites.entrySet()) {

      String projectID = entry.getKey();
      ProjectOptionComposite projectOptionComposite = entry.getValue();

      if (excludedProjectIDs.contains(projectID)) continue;

      currentProjectNames.add(projectOptionComposite.getProjectName());
    }

    return currentProjectNames;
  }

  /** get transfer mode and set header information of the wizard. */
  private void updateConnectionStatus() {

    // FIXME we are using Smack for File transfer so we might end up with other transfer modes
    switch (connectionManager.getTransferMode(ISarosSession.SESSION_CONNECTION_ID, peer)) {
      case SOCKS5_MEDIATED:
        if (preferences.isLocalSOCKS5ProxyEnabled())
          setDescription(Messages.EnterProjectNamePage_description_socks5proxy);
        else setDescription(Messages.EnterProjectNamePage_description_file_transfer);
        setImageDescriptor(ImageManager.getImageDescriptor("icons/wizban/socks5m.png"));
        break;

      case SOCKS5_DIRECT:
        // all is fine
      case NONE:
        // lost data connection
        break;

      case IBB:
        if (preferences.forceIBBTransport()) {
          setDescription(Messages.EnterProjectNamePage_direct_filetransfer_deactivated);
        } else {
          setDescription(Messages.EnterProjectNamePage_direct_filetransfer_nan);
        }

        new FlashTask(
                this,
                1000,
                ImageManager.getImage("icons/wizban/ibb.png"),
                ImageManager.getImage("icons/wizban/ibbFaded.png"))
            .run();

        break;
      default:
        setDescription(Messages.EnterProjectNamePage_unknown_transport_method);
        break;
    }
  }

  /**
   * Tests if the given project name does not already exist in the current workspace.
   *
   * @param projectName the name of the project
   * @param reservedProjectNames further project names that are already reserved even if the
   *     projects do not exist physically
   * @return <code>true</code> if the project name does not exist in the current workspace and does
   *     not exist in the reserved project names, <code>false</code> if the project name is an empty
   *     string or the project already exists in the current workspace
   */
  boolean projectNameIsUnique(String projectName, String... reservedProjectNames) {

    if (projectName == null) throw new NullPointerException("projectName is null");

    if (projectName.length() == 0) return false;

    Set<IProject> projects =
        new HashSet<IProject>(
            Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects()));

    for (String reservedProjectName : reservedProjectNames) {
      projects.add(ResourcesPlugin.getWorkspace().getRoot().getProject(reservedProjectName));
    }

    return !projects.contains(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
  }

  /**
   * Proposes a project name based on the existing project names in the current workspace. The
   * proposed project name is unique.
   *
   * @param projectName project name which shall be checked
   * @param reservedProjectNames additional project names that should not be returned when finding a
   *     name proposal for a project
   * @return a unique project name based on the original project name
   */
  String findProjectNameProposal(String projectName, String... reservedProjectNames) {

    int idx;

    for (idx = projectName.length() - 1;
        idx >= 0 && Character.isDigit(projectName.charAt(idx));
        idx--) {
      // NOP
    }

    String newProjectName;

    if (idx < 0) newProjectName = "";
    else newProjectName = projectName.substring(0, idx + 1);

    if (idx == projectName.length() - 1) idx = 2;
    else {
      try {
        idx = Integer.valueOf(projectName.substring(idx + 1));
      } catch (NumberFormatException e) {
        idx = 2;
      }
    }

    projectName = newProjectName;

    while (!projectNameIsUnique(projectName, reservedProjectNames)) {
      projectName = newProjectName + idx;
      idx++;
    }

    return projectName;
  }

  /** Returns all charsets from a given set that are not available on the current JVM. */
  private Set<String> getUnsupportedCharsets(Set<String> charsets) {
    Set<String> missingCharsets = new HashSet<String>();

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

    public FlashTask(final IDialogPage page, final int delay, final Image... images) {
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
