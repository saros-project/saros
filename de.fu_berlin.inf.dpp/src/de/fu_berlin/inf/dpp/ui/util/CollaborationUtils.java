package de.fu_berlin.inf.dpp.ui.util;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.filesystem.EclipseProjectImpl;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.ui.Messages;
import de.fu_berlin.inf.dpp.util.FileUtils;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

/** Offers convenient methods for collaboration actions like sharing a project resources. */
public class CollaborationUtils {

  private static final Logger log = Logger.getLogger(CollaborationUtils.class);

  @Inject private static ISarosSessionManager sessionManager;

  static {
    SarosPluginContext.initComponent(new CollaborationUtils());
  }

  private CollaborationUtils() {
    // NOP
  }

  /**
   * Starts a new session and shares the given resources with given contacts. The operation is
   * performed <b>asynchronously</b> and therefore this method returns immediately.
   *
   * <p>The provided list will be <b>altered</b> and therefore <b>should not</b> be used any
   * further.
   *
   * <p>Does nothing if a session is already running.
   *
   * @param resources
   * @param contacts
   */
  public static void startSession(final List<IResource> resources, final List<JID> contacts) {

    final IJobFunction startSessionFunction =
        (final IProgressMonitor monitor) -> {
          monitor.beginTask("Starting session...", IProgressMonitor.UNKNOWN);

          try {

            final Map<IProject, List<IResource>> resourcesToShare =
                getResourceMapping(resources, null);

            refreshProjectsIfNeeded(resourcesToShare.keySet(), null, null);

            sessionManager.startSession(convert(resourcesToShare));

            final Set<JID> participantsToAdd = new HashSet<JID>(contacts);

            final ISarosSession session = sessionManager.getSession();

            if (session == null) return Status.CANCEL_STATUS;

            sessionManager.invite(participantsToAdd, getSessionDescription(session));

          } catch (Exception e) {
            log.error("could not start a Saros session", e);
            return new Status(IStatus.ERROR, Saros.PLUGIN_ID, e.getMessage(), e);
          } finally {
            monitor.done();
          }

          return Status.OK_STATUS;
        };

    final Job sessionStartupJob = Job.create("Session Startup", startSessionFunction);

    sessionStartupJob.setPriority(Job.SHORT);
    sessionStartupJob.setUser(true);
    sessionStartupJob.schedule();
  }

  /**
   * Leaves / stops the current session. This method asks for user confirmation and blocks until the
   * user has either confirmed or declined the request. The actual leaving / stopping of the session
   * is performed <b>asynchronously</b>.
   *
   * <p>Does nothing if no session is running.
   */
  public static void leaveSession() {

    final ISarosSession session = sessionManager.getSession();

    if (session == null) {
      log.warn("cannot leave a non-running session");
      return;
    }

    if (!confirmSessionShutdown(session.isHost(), session.getUsers().size() == 1)) return;

    final IJobFunction stopSessionFunction =
        (final IProgressMonitor monitor) -> {
          monitor.beginTask("Leaving session...", IProgressMonitor.UNKNOWN);

          try {
            sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
          } catch (Exception e) {
            log.error("could not stop the current session", e);
            return new Status(IStatus.ERROR, Saros.PLUGIN_ID, e.getMessage(), e);
          } finally {
            monitor.done();
          }

          return Status.OK_STATUS;
        };

    final Job sessionStartupJob = Job.create("Session Shutdown", stopSessionFunction);

    sessionStartupJob.setPriority(Job.SHORT);
    sessionStartupJob.setUser(true);
    sessionStartupJob.schedule();
  }

  /**
   * Adds the given project resources to the session. The operation is performed
   * <b>asynchronously</b> and therefore this method returns immediately.
   *
   * <p>Does nothing if no session is running.
   *
   * @param resources the resources to add
   */
  public static void addResourcesToSession(List<IResource> resources) {

    final ISarosSession session = sessionManager.getSession();

    if (session == null) {
      log.warn("cannot add resources to a non-running session");
      return;
    }

    final Map<IProject, List<IResource>> resourcesToShare = getResourceMapping(resources, session);

    if (resourcesToShare.isEmpty()) return;

    ThreadUtils.runSafeAsync(
        "AddResourceToSession",
        log,
        () -> {
          if (!session.hasWriteAccess()) {
            DialogUtils.popUpFailureMessage(
                Messages.CollaborationUtils_insufficient_privileges,
                Messages.CollaborationUtils_insufficient_privileges_text,
                false);
            return;
          }

          try {
            refreshProjectsIfNeeded(resourcesToShare.keySet(), session, null);
          } catch (CoreException e) {
            log.warn("failed to refresh projects", e);
            /*
             * FIXME use a Job instead of a plain thread and so better
             * exception handling !
             */
          }

          sessionManager.addResourcesToSession(convert(resourcesToShare));
        });
  }

  /**
   * Adds the given contacts to the session. The operation is performed <b>asynchronously</b> and
   * therefore this method returns immediately.
   *
   * <p>Does nothing if no session is running.
   *
   * @param contacts
   */
  public static void addContactsToSession(final List<JID> contacts) {

    final ISarosSession session = sessionManager.getSession();

    if (session == null) {
      log.warn("cannot add contacts to a non-running session");
      return;
    }

    ThreadUtils.runSafeAsync(
        "AddContactToSession",
        log,
        () -> {
          final Set<JID> participantsToAdd = new HashSet<JID>(contacts);

          for (final User user : session.getUsers()) participantsToAdd.remove(user.getJID());

          if (!participantsToAdd.isEmpty())
            sessionManager.invite(participantsToAdd, getSessionDescription(session));
        });
  }

  // FIXME This is only displayed once. If you add another project during a session this helpful
  // information about the size is NOT shown at all!
  /**
   * Creates the message that the invitee(s) see on an incoming project share request. Currently it
   * contains the project names along with the number of shared files and total file size for each
   * shared project.
   *
   * @param sarosSession
   * @return
   */
  private static String getSessionDescription(ISarosSession sarosSession) {

    final Set<de.fu_berlin.inf.dpp.filesystem.IProject> projects = sarosSession.getProjects();

    final StringBuilder result = new StringBuilder();

    for (de.fu_berlin.inf.dpp.filesystem.IProject project : projects) {

      final List<IResource> resources;
      final boolean searchRecursive;
      final int searchFlags;
      final String projectSharingMode;

      if (sarosSession.isCompletelyShared(project)) {
        resources = Collections.singletonList(((EclipseProjectImpl) project).getDelegate());
        searchRecursive = true;
        searchFlags = IContainer.EXCLUDE_DERIVED;
        projectSharingMode = "complete";
      } else {
        resources = ResourceAdapterFactory.convertBack(sarosSession.getSharedResources(project));
        searchRecursive = false;
        searchFlags = IResource.NONE;
        projectSharingMode = "partial";
      }

      final Pair<Long, Long> fileCountAndSize =
          FileUtils.getFileCountAndSize(resources, searchRecursive, searchFlags);

      result.append(
          String.format(
              "\nProject: %s (%s), Files: %d, Size: %s",
              project.getName(),
              projectSharingMode,
              fileCountAndSize.getRight(),
              format(fileCountAndSize.getLeft())));
    }

    return result.toString();
  }

  /**
   * Calculates the required mapping for the {@link ISarosSessionManager#startSession(Map) start
   * session call} and {@link ISarosSessionManager#addResourcesToSession(Map) add resources call}.
   *
   * <p>The result is returned as a {@link Map} of the following structure:
   *
   * <ul>
   *   <li>fully shared project: {@link IProject} --> <code>null</code>
   *   <li>partially shared project: {@link IProject} --> <code>List&lt;{@link IResource}&gt;</code>
   * </ul>
   *
   * In case of partially shared project, this method also adds files and folders that are needed
   * for a consistent project on the receiver's side, even when there were not selected by the user
   * (e.g ".project" files).
   *
   * @param resources the resources to add to the session
   * @param session the current session or <code>null</code>
   * @return
   */
  private static Map<IProject, List<IResource>> getResourceMapping(
      final List<IResource> resources, final ISarosSession session) {

    if (session != null) {
      // remove already shared resources for partial projects and all full shared projects
      resources.removeAll(ResourceAdapterFactory.convertBack(session.getProjects()));
      resources.removeAll(ResourceAdapterFactory.convertBack(session.getSharedResources()));
    }

    final int resourcesSize = resources.size();

    final IResource[] preSortedResources = new IResource[resourcesSize];

    int frontIdx = 0;
    int backIdx = resourcesSize - 1;

    // move projects to the front so the algorithm is working as expected
    for (final IResource resource : resources) {
      if (resource.getType() == IResource.PROJECT) preSortedResources[frontIdx++] = resource;
      else preSortedResources[backIdx--] = resource;
    }

    final Map<IProject, Set<IResource>> projectsToResourcesMapping =
        new HashMap<IProject, Set<IResource>>();

    for (final IResource resource : preSortedResources) {

      // init full shared projects
      if (resource.getType() == IResource.PROJECT) {
        projectsToResourcesMapping.put(resource.getAdapter(IProject.class), null);
        continue;
      }

      final IProject project = resource.getProject();

      if (project == null) continue;

      // init partial shared projects
      if (!projectsToResourcesMapping.containsKey(project))
        projectsToResourcesMapping.put(project, new HashSet<IResource>());

      final Set<IResource> projectResources = projectsToResourcesMapping.get(project);

      // if the project resource set is null, it is a full shared project
      if (projectResources != null) projectResources.add(resource);
    }

    for (Entry<IProject, Set<IResource>> entry : projectsToResourcesMapping.entrySet()) {

      final IProject project = entry.getKey();
      final Set<IResource> projectResources = entry.getValue();

      if (projectResources == /* full shared */ null) continue;

      // do not add the files again in case this is an already partial shared project
      if (session != null && session.isShared(ResourceAdapterFactory.create(project))) continue;

      final List<IResource> additionalFilesForPartialSharing = new ArrayList<IResource>();

      /*
       * we need this file otherwise creating a new project on the remote
       * will produce garbage because the project nature is not set /
       * updated correctly
       */
      final IFile projectFile = project.getFile(".project");

      if (projectFile.exists()) additionalFilesForPartialSharing.add(projectFile);

      // do not include them, this is causing malfunctions if developers
      // do
      // not use variables in their classpath but absolute paths.

      // IFile classpathFile = project.getFile(".classpath");

      // if (classpathFile.exists())
      // additionalFilesForPartialSharing.add(classpathFile);

      /*
       * FIXME adding files from this folder may "corrupt" a lot of remote
       * files. The byte content will not be corrupted, but the document
       * provider (editor) will fail to render the file input correctly. I
       * think we should negotiate the project encodings and forbid
       * further proceeding if they do not match ! The next step should be
       * to also transmit the encoding in FileActivities, because it is
       * possible to change the encoding of files independently of the
       * project encoding settings.
       */

      final IFolder settingsFolder = project.getFolder(".settings");

      if (settingsFolder.exists() /* remove to execute block */ && false) {

        additionalFilesForPartialSharing.add(settingsFolder);

        try {
          for (final IResource resource : settingsFolder.members()) {
            // TODO are sub folders possible ?
            if (resource.getType() == IResource.FILE)
              additionalFilesForPartialSharing.add(resource);
          }
        } catch (CoreException e) {
          log.warn("could not read the contents of the settings folder", e);
        }
      }

      projectResources.addAll(additionalFilesForPartialSharing);
    }

    final HashMap<IProject, List<IResource>> result = new HashMap<>();

    for (final Entry<IProject, Set<IResource>> entry : projectsToResourcesMapping.entrySet())
      result.put(
          entry.getKey(),
          entry.getValue() == null ? null : new ArrayList<IResource>(entry.getValue()));

    return result;
  }

  private static String format(long size) {

    if (size < 1000) return "< 1 KB";

    if (size < 1000 * 1000) return String.format(Locale.US, "%.2f KB", size / (1000F));

    if (size < 1000 * 1000 * 1000)
      return String.format(Locale.US, "%.2f MB", size / (1000F * 1000F));

    return String.format(Locale.US, "%.2f GB", size / (1000F * 1000F * 1000F));
  }

  private static Map<
          de.fu_berlin.inf.dpp.filesystem.IProject, List<de.fu_berlin.inf.dpp.filesystem.IResource>>
      convert(Map<IProject, List<IResource>> data) {

    Map<de.fu_berlin.inf.dpp.filesystem.IProject, List<de.fu_berlin.inf.dpp.filesystem.IResource>>
        result =
            new HashMap<
                de.fu_berlin.inf.dpp.filesystem.IProject,
                List<de.fu_berlin.inf.dpp.filesystem.IResource>>();

    for (Entry<IProject, List<IResource>> entry : data.entrySet())
      result.put(
          ResourceAdapterFactory.create(entry.getKey()),
          ResourceAdapterFactory.convertTo(entry.getValue()));

    return result;
  }

  /**
   * Refreshes the given projects if they are not already shared. This is necessary to prevent
   * unwanted file addition in case of partial sharing and unnecessary file activities in case of
   * full shared projects when the user refreshes the project manually during a session.
   *
   * @param projects the projects to refresh
   * @param session the current session or <code>null</code>
   * @param monitor
   * @throws CoreException
   */
  private static void refreshProjectsIfNeeded(
      final Collection<IProject> projects,
      final ISarosSession session,
      final IProgressMonitor monitor)
      throws CoreException {

    final List<IProject> projectsToRefresh = new ArrayList<>();

    for (final IProject project : projects) {
      if (session == null || !session.isShared(ResourceAdapterFactory.create(project)))
        projectsToRefresh.add(project);
    }

    final SubMonitor progress =
        SubMonitor.convert(monitor, "Refreshing projects...", projectsToRefresh.size());

    for (final IProject project : projectsToRefresh) {

      if (!project.isOpen()) project.open(progress.newChild(0));

      project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(1));
    }

    progress.done();
  }

  private static boolean confirmSessionShutdown(boolean isHost, boolean isLastUser) {

    if (isHost && isLastUser) return true;

    final Shell shell = SWTUtils.getShell();

    final String questionTitle;
    final String questionMessage;

    if (isHost) {
      questionTitle = Messages.CollaborationUtils_confirm_closing;
      questionMessage = Messages.CollaborationUtils_confirm_closing_text;
    } else {
      questionTitle = Messages.CollaborationUtils_confirm_leaving;
      questionMessage = Messages.CollaborationUtils_confirm_leaving_text;
    }

    return MessageDialog.openQuestion(shell, questionTitle, questionMessage);
  }
}
