package de.fu_berlin.inf.dpp.ui.util;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.filesystem.EclipseFolderImpl_V2;
import de.fu_berlin.inf.dpp.filesystem.EclipseProjectImpl;
import de.fu_berlin.inf.dpp.filesystem.EclipseProjectImpl_V2;
import de.fu_berlin.inf.dpp.filesystem.EclipseReferencePointManager;
import de.fu_berlin.inf.dpp.filesystem.ResourceAdapterFactory;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ReferencePointManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
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
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.picocontainer.annotations.Inject;

/**
 * Offers convenient methods for collaboration actions like sharing a project resources.
 *
 * @author bkahlert
 * @author kheld
 */
public class CollaborationUtils {

  private static final Logger LOG = Logger.getLogger(CollaborationUtils.class);

  @Inject private static ISarosSessionManager sessionManager;

  @Inject private static EclipseReferencePointManager eclipseReferencePointManager;

  private static IReferencePointManager referencePointManager;

  static {
    SarosPluginContext.initComponent(new CollaborationUtils());
  }

  private CollaborationUtils() {
    // NOP
  }

  /**
   * Starts a new session and shares the given resources with given contacts.<br>
   * Does nothing if a {@link ISarosSession session} is already running.
   *
   * @param resources
   * @param contacts
   * @nonBlocking
   */
  public static void startSession(List<IResource> resources, final List<JID> contacts) {

    final Map<IProject, List<IResource>> newResources = acquireResources(resources, null);

    Job sessionStartupJob =
        new Job("Session Startup") {

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask("Starting session...", IProgressMonitor.UNKNOWN);

            try {
              refreshProjects(newResources.keySet(), null);
              referencePointManager = new ReferencePointManager();
              fillReferencePointManager(newResources.keySet());
              sessionManager.startSession(
                  convert(newResources, referencePointManager), referencePointManager);
              Set<JID> participantsToAdd = new HashSet<JID>(contacts);

              ISarosSession session = sessionManager.getSession();

              if (session == null) return Status.CANCEL_STATUS;

              sessionManager.invite(participantsToAdd, getSessionDescription(session));

            } catch (Exception e) {

              LOG.error("could not start a Saros session", e);
              return new Status(IStatus.ERROR, Saros.PLUGIN_ID, e.getMessage(), e);
            }

            return Status.OK_STATUS;
          }
        };

    sessionStartupJob.setPriority(Job.SHORT);
    sessionStartupJob.setUser(true);
    sessionStartupJob.schedule();
  }

  /**
   * Leaves the currently running {@link SarosSession}<br>
   * Does nothing if no {@link SarosSession} is running.
   */
  public static void leaveSession() {

    ISarosSession sarosSession = sessionManager.getSession();

    Shell shell = SWTUtils.getShell();

    if (sarosSession == null) {
      LOG.warn("cannot leave a non-running session");
      return;
    }

    boolean reallyLeave;

    if (sarosSession.isHost()) {
      if (sarosSession.getUsers().size() == 1) {
        // Do not ask when host is alone...
        reallyLeave = true;
      } else {
        reallyLeave =
            MessageDialog.openQuestion(
                shell,
                Messages.CollaborationUtils_confirm_closing,
                Messages.CollaborationUtils_confirm_closing_text);
      }
    } else {
      reallyLeave =
          MessageDialog.openQuestion(
              shell,
              Messages.CollaborationUtils_confirm_leaving,
              Messages.CollaborationUtils_confirm_leaving_text);
    }

    if (!reallyLeave) return;

    ThreadUtils.runSafeAsync(
        "StopSession",
        LOG,
        new Runnable() {
          @Override
          public void run() {
            sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
          }
        });
  }

  /**
   * Adds the given project resources to the session.<br>
   * Does nothing if no {@link SarosSession session} is running.
   *
   * @param resourcesToAdd
   * @nonBlocking
   */
  public static void addResourcesToSession(List<IResource> resourcesToAdd) {

    final ISarosSession session = sessionManager.getSession();

    if (session == null) {
      LOG.warn("cannot add resources to a non-running session");
      return;
    }

    final Map<IProject, List<IResource>> projectResources =
        acquireResources(resourcesToAdd, session);

    if (projectResources.isEmpty()) return;

    ThreadUtils.runSafeAsync(
        "AddResourceToSession",
        LOG,
        new Runnable() {
          @Override
          public void run() {

            if (!session.hasWriteAccess()) {
              DialogUtils.popUpFailureMessage(
                  Messages.CollaborationUtils_insufficient_privileges,
                  Messages.CollaborationUtils_insufficient_privileges_text,
                  false);
              return;
            }

            final List<IProject> projectsToRefresh = new ArrayList<IProject>();

            for (IProject project : projectResources.keySet()) {
              if (!session.isShared(ResourceAdapterFactory.create(project)))
                projectsToRefresh.add(project);
            }

            try {
              refreshProjects(projectsToRefresh, null);
            } catch (CoreException e) {
              LOG.warn("failed to refresh projects", e);
              /*
               * FIXME use a Job instead of a plain thread and so better
               * execption handling !
               */
            }
            fillReferencePointManager(projectResources.keySet());
            sessionManager.addResourcesToSession(convert(projectResources, referencePointManager));
          }
        });
  }

  /**
   * Adds the given contacts to the session.<br>
   * Does nothing if no {@link ISarosSession session} is running.
   *
   * @param contacts
   * @nonBlocking
   */
  public static void addContactsToSession(final List<JID> contacts) {

    final ISarosSession sarosSession = sessionManager.getSession();

    if (sarosSession == null) {
      LOG.warn("cannot add contacts to a non-running session");
      return;
    }

    ThreadUtils.runSafeAsync(
        "AddContactToSession",
        LOG,
        new Runnable() {
          @Override
          public void run() {

            Set<JID> participantsToAdd = new HashSet<JID>(contacts);

            for (User user : sarosSession.getUsers()) participantsToAdd.remove(user.getJID());

            if (participantsToAdd.size() > 0) {
              sessionManager.invite(participantsToAdd, getSessionDescription(sarosSession));
            }
          }
        });
  }

  /**
   * Creates the message that invitees see on an incoming project share request. Currently it
   * contains the project names along with the number of shared files and total file size for each
   * shared project.
   *
   * @param sarosSession
   * @return
   */
  private static String getSessionDescription(ISarosSession sarosSession) {

    Set<de.fu_berlin.inf.dpp.filesystem.IFolder_V2> projects =
        referencePointManager.getProjects(sarosSession.getReferencePoints());

    final StringBuilder result = new StringBuilder();

    for (de.fu_berlin.inf.dpp.filesystem.IFolder_V2 project : projects) {

      final Pair<Long, Long> fileCountAndSize;

      final boolean isCompletelyShared =
          sarosSession.isCompletelyShared(project.getReferencePoint());

      final List<IResource> resources;

      if (isCompletelyShared)
        resources = Collections.singletonList(((EclipseProjectImpl_V2) project).getDelegate());
      else
        resources =
            ResourceAdapterFactory.convertBack(
                sarosSession.getSharedResources(project.getReferencePoint()));

      fileCountAndSize =
          FileUtils.getFileCountAndSize(
              resources,
              isCompletelyShared ? true : false,
              isCompletelyShared ? IContainer.EXCLUDE_DERIVED : IResource.NONE);

      result.append(
          String.format(
              "\nProject: %s (%s), Files: %d, Size: %s",
              project.getName(),
              isCompletelyShared ? "complete" : "partial",
              fileCountAndSize.getRight(),
              format(fileCountAndSize.getLeft())));
    }

    return result.toString();
  }

  /**
   * Determines which of the the selected resources belong to fully shared projects or to partially
   * shared ones. The result is returned as a {@link Map} of the following structure:
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
   * @param selectedResources
   * @param sarosSession
   * @return
   */
  private static Map<IProject, List<IResource>> acquireResources(
      List<IResource> selectedResources, ISarosSession sarosSession) {

    if (sarosSession != null) {
      List<IResource> sharedResources =
          ResourceAdapterFactory.convertBack(sarosSession.getSharedResources());
      selectedResources.removeAll(sharedResources);
    }

    final int resourcesSize = selectedResources.size();

    IResource[] preSortedResources = new IResource[resourcesSize];

    int frontIdx = 0;
    int backIdx = resourcesSize - 1;

    // move projects to the front so the algorithm is working as expected
    for (IResource resource : selectedResources) {
      if (resource.getType() == IResource.PROJECT) preSortedResources[frontIdx++] = resource;
      else preSortedResources[backIdx--] = resource;
    }

    Map<IProject, Set<IResource>> projectsResources = new HashMap<IProject, Set<IResource>>();

    for (IResource resource : preSortedResources) {
      if (resource.getType() == IResource.PROJECT) {
        projectsResources.put((IProject) resource, null);
        continue;
      }

      IProject project = resource.getProject();

      if (project == null) continue;

      if (!projectsResources.containsKey(project))
        projectsResources.put(project, new HashSet<IResource>());

      Set<IResource> resources = projectsResources.get(project);

      // if the resource set is null, it is a full shared project
      if (resources != null) resources.add(resource);
    }

    List<IResource> additionalFilesForPartialSharing = new ArrayList<IResource>();

    for (Entry<IProject, Set<IResource>> entry : projectsResources.entrySet()) {

      IProject project = entry.getKey();
      Set<IResource> resources = entry.getValue();

      if (resources == /* full shared */ null) continue;

      additionalFilesForPartialSharing.clear();

      /*
       * we need this file otherwise creating a new project on the remote
       * will produce garbage because the project nature is not set /
       * updated correctly
       */
      IFile projectFile = project.getFile(".project");

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

      IFolder settingsFolder = project.getFolder(".settings");

      if (settingsFolder.exists() /* remove to execute block */ && false) {

        additionalFilesForPartialSharing.add(settingsFolder);

        try {
          for (IResource resource : settingsFolder.members()) {
            // TODO are sub folders possible ?
            if (resource.getType() == IResource.FILE)
              additionalFilesForPartialSharing.add(resource);
          }
        } catch (CoreException e) {
          LOG.warn("could not read the contents of the settings folder", e);
        }
      }

      resources.addAll(additionalFilesForPartialSharing);
    }

    HashMap<IProject, List<IResource>> resources = new HashMap<IProject, List<IResource>>();

    for (Entry<IProject, Set<IResource>> entry : projectsResources.entrySet())
      resources.put(
          entry.getKey(),
          entry.getValue() == null ? null : new ArrayList<IResource>(entry.getValue()));

    return resources;
  }

  private static String format(long size) {

    if (size < 1000) return "< 1 KB";

    if (size < 1000 * 1000) return String.format(Locale.US, "%.2f KB", size / (1000F));

    if (size < 1000 * 1000 * 1000)
      return String.format(Locale.US, "%.2f MB", size / (1000F * 1000F));

    return String.format(Locale.US, "%.2f GB", size / (1000F * 1000F * 1000F));
  }

  private static Map<
          de.fu_berlin.inf.dpp.filesystem.IReferencePoint,
          List<de.fu_berlin.inf.dpp.filesystem.IResource>>
      convert(Map<IProject, List<IResource>> data, IReferencePointManager referencePointManager) {

    Map<
            de.fu_berlin.inf.dpp.filesystem.IReferencePoint,
            List<de.fu_berlin.inf.dpp.filesystem.IResource>>
        result =
            new HashMap<
                de.fu_berlin.inf.dpp.filesystem.IReferencePoint,
                List<de.fu_berlin.inf.dpp.filesystem.IResource>>();

    for (Entry<IProject, List<IResource>> entry : data.entrySet()) {
      de.fu_berlin.inf.dpp.filesystem.IFolder_V2 coreProject =
         new EclipseProjectImpl_V2(entry.getKey());

      fillReferencePointManager(coreProject, referencePointManager);

      result.put(
          EclipseReferencePointManager.create(entry.getKey()), ResourceAdapterFactory.convertTo(entry.getValue()));
    }

    return result;
  }

  private static void refreshProjects(
      final Collection<IProject> projects, final IProgressMonitor monitor) throws CoreException {

    final SubMonitor progress =
        SubMonitor.convert(monitor, "Refreshing projects...", projects.size());

    for (final IProject project : projects) {
      if (!project.isOpen()) project.open(progress.newChild(0));

      project.refreshLocal(IResource.DEPTH_INFINITE, progress.newChild(1));
    }

    progress.done();
  }

  private static void fillReferencePointManager(
      de.fu_berlin.inf.dpp.filesystem.IFolder_V2 project,
      IReferencePointManager referencePointManager) {
    referencePointManager.put(project.getReferencePoint(), project);
  }

  private static void fillReferencePointManager(Collection<IProject> projects) {
    for (IProject project : projects) {
      eclipseReferencePointManager.put(project);
    }
  }
}
