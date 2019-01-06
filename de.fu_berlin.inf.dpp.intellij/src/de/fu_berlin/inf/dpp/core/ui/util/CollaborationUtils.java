package de.fu_berlin.inf.dpp.core.ui.util;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import de.fu_berlin.inf.dpp.SarosPluginContext;
import de.fu_berlin.inf.dpp.core.monitoring.IStatus;
import de.fu_berlin.inf.dpp.core.monitoring.Status;
import de.fu_berlin.inf.dpp.filesystem.IContainer;
import de.fu_berlin.inf.dpp.filesystem.IFile;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.filesystem.IResource;
import de.fu_berlin.inf.dpp.intellij.SarosComponent;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJProjectImpl;
import de.fu_berlin.inf.dpp.intellij.filesystem.IntelliJReferencePointManager;
import de.fu_berlin.inf.dpp.intellij.runtime.UIMonitoredJob;
import de.fu_berlin.inf.dpp.intellij.ui.Messages;
import de.fu_berlin.inf.dpp.intellij.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.intellij.ui.util.NotificationPanel;
import de.fu_berlin.inf.dpp.monitoring.IProgressMonitor;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.session.IReferencePointManager;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import de.fu_berlin.inf.dpp.session.ISarosSessionManager;
import de.fu_berlin.inf.dpp.session.ReferencePointManager;
import de.fu_berlin.inf.dpp.session.SessionEndReason;
import de.fu_berlin.inf.dpp.session.User;
import de.fu_berlin.inf.dpp.session.internal.SarosSession;
import de.fu_berlin.inf.dpp.util.ThreadUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
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

  @Inject private static IntelliJReferencePointManager intelliJReferencePointManager;

  static {
    SarosPluginContext.initComponent(new CollaborationUtils());
  }

  private CollaborationUtils() {}

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

    final Map<IReferencePoint, List<IResource>> referencePointResources = new HashMap<>();

    IReferencePointManager referencePointManager = new ReferencePointManager();

    for (Map.Entry<IProject, List<IResource>> entry : newResources.entrySet()) {
      IProject project = entry.getKey();

      fillReferencePointManager(project, referencePointManager);

      referencePointResources.put(project.getReferencePoint(), entry.getValue());
    }

    UIMonitoredJob sessionStartupJob =
        new UIMonitoredJob("Session Startup") {

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask("Starting session...", IProgressMonitor.UNKNOWN);
            try {
              sessionManager.startSession(referencePointResources, referencePointManager);
              Set<JID> participantsToAdd = new HashSet<JID>(contacts);

              monitor.worked(50);

              ISarosSession session = sessionManager.getSession();

              if (session == null) {
                return Status.CANCEL_STATUS;
              }
              monitor.setTaskName("Inviting participants...");
              sessionManager.invite(participantsToAdd, getShareProjectDescription(session));

              monitor.done();

            } catch (Exception e) {

              LOG.error("could not start a Saros session", e);
              return new Status(IStatus.ERROR, SarosComponent.PLUGIN_ID, e.getMessage(), e);
            }

            return Status.OK_STATUS;
          }
        };

    sessionStartupJob.schedule();
  }

  /**
   * Leaves the currently running {@link SarosSession}<br>
   * Does nothing if no {@link SarosSession} is running.
   */
  public static void leaveSession() {

    ISarosSession sarosSession = sessionManager.getSession();

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
            DialogUtils.showConfirm(
                null,
                Messages.CollaborationUtils_confirm_closing,
                Messages.CollaborationUtils_confirm_closing_text);
      }
    } else {
      reallyLeave =
          DialogUtils.showConfirm(
              null,
              Messages.CollaborationUtils_confirm_leaving,
              Messages.CollaborationUtils_confirm_leaving_text);
    }

    if (!reallyLeave) {
      return;
    }

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

    final ISarosSession sarosSession = sessionManager.getSession();

    if (sarosSession == null) {
      LOG.warn("cannot add resources to a non-running session");
      return;
    }

    final Map<IProject, List<IResource>> projectResources;

    projectResources = acquireResources(resourcesToAdd, sarosSession);

    if (projectResources.isEmpty()) {
      return;
    }

    ThreadUtils.runSafeAsync(
        "AddResourceToSession",
        LOG,
        new Runnable() {
          @Override
          public void run() {

            if (sarosSession.hasWriteAccess()) {

              final Map<IReferencePoint, List<IResource>> referencePointResources = new HashMap<>();

              final IReferencePointManager referencePointManager =
                  sarosSession.getComponent(IReferencePointManager.class);

              for (Map.Entry<IProject, List<IResource>> entry : projectResources.entrySet()) {
                IProject project = entry.getKey();

                fillReferencePointManager(project, referencePointManager);

                referencePointResources.put(project.getReferencePoint(), entry.getValue());
              }

              sessionManager.addResourcesToSession(referencePointResources);
              return;
            }

            NotificationPanel.showError(
                Messages.CollaborationUtils_insufficient_privileges_text,
                Messages.CollaborationUtils_insufficient_privileges);
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

            for (User user : sarosSession.getUsers()) {
              participantsToAdd.remove(user.getJID());
            }

            if (participantsToAdd.size() > 0) {
              sessionManager.invite(participantsToAdd, getShareProjectDescription(sarosSession));
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
  private static String getShareProjectDescription(ISarosSession sarosSession) {

    IReferencePointManager referencePointManager =
        sarosSession.getComponent(IReferencePointManager.class);

    Set<IProject> projects = referencePointManager.getProjects(sarosSession.getReferencePoints());

    StringBuilder result = new StringBuilder();

    try {
      for (IProject project : projects) {

        Pair<Long, Long> fileCountAndSize;

        if (sarosSession.isCompletelyShared(project.getReferencePoint())) {
          fileCountAndSize =
              getFileCountAndSize(Arrays.asList(project.members()), true, IContainer.FILE);

          result.append(
              String.format(
                  "\nModule: %s, Files: %d, Size: %s",
                  project.getName(),
                  fileCountAndSize.getRight(),
                  format(fileCountAndSize.getLeft())));
        } else {
          List<IResource> resources = sarosSession.getSharedResources(project.getReferencePoint());

          fileCountAndSize = getFileCountAndSize(resources, false, IResource.NONE);

          result.append(
              String.format(
                  "\nModule: %s, Files: %s, Size: %s",
                  project.getName() + " " + Messages.CollaborationUtils_partial,
                  fileCountAndSize.getRight(),
                  format(fileCountAndSize.getLeft())));
        }
      }
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return "Could not get description";
    }

    return result.toString();
  }

  /**
   * Decides if selected resource is a complete shared project in contrast to partial shared ones.
   * The result is stored in {@link HashMap}:
   *
   * <ul>
   *   <li>complete shared project: {@link IProject} --> null
   *   <li>partial shared project: {@link IProject} --> List<IResource>
   * </ul>
   *
   * Adds to partial shared projects additional files which are needed for proper project
   * synchronization.
   *
   * @param selectedResources
   * @param sarosSession
   * @return
   */
  private static Map<IProject, List<IResource>> acquireResources(
      List<IResource> selectedResources, ISarosSession sarosSession) {

    Map<IProject, Set<IResource>> projectsResources = new HashMap<IProject, Set<IResource>>();

    if (sarosSession != null) {
      selectedResources.removeAll(sarosSession.getSharedResources());
    }

    final int resourcesSize = selectedResources.size();

    IResource[] preSortedResources = new IResource[resourcesSize];

    int frontIdx = 0;
    int backIdx = resourcesSize - 1;

    // move projects to the front so the algorithm is working as expected
    for (IResource resource : selectedResources) {
      if (resource.getType() == IResource.PROJECT) {
        preSortedResources[frontIdx++] = resource;
      } else {
        preSortedResources[backIdx--] = resource;
      }
    }

    for (IResource resource : preSortedResources) {

      if (resource.getType() == IResource.PROJECT) {
        projectsResources.put((IProject) resource, null);
        continue;
      }

      IProject project = resource.getProject();

      if (project == null) {
        continue;
      }

      if (!projectsResources.containsKey(project)) {
        projectsResources.put(project, new HashSet<IResource>());
      }

      Set<IResource> resources = projectsResources.get(project);

      // if the resource set is null, it is a full shared project
      if (resources != null) {
        resources.add(resource);
      }
    }

    for (Entry<IProject, Set<IResource>> entry : projectsResources.entrySet()) {

      IProject project = entry.getKey();
      Set<IResource> resources = entry.getValue();

      if (resources == // * full shared *//*
          null) {
        continue;
      }

      List<IResource> additionalFilesForPartialSharing = new ArrayList<IResource>();

      /*
       * TODO should be adjusted after partial sharing is implemented
       *
       * we need the .iml file, otherwise the project type will not be set
       * correctly on the other side
       */
      IntelliJProjectImpl intelliJProject =
          (IntelliJProjectImpl) project.getAdapter(IntelliJProjectImpl.class);

      Module module = intelliJProject.getModule();
      VirtualFile moduleFile = module.getModuleFile();

      if (moduleFile == null || !moduleFile.exists()) {
        LOG.error(
            "The module file for the module " + module + " does not exist or could not be found");

        NotificationPanel.showWarning(
            "The module file for the shared module "
                + module
                + " could not be found. This could lead to the session not"
                + " working as expected.",
            "Module file not found!");
      } else {
        IFile moduleFileResource = intelliJProject.getFile(moduleFile);

        if (moduleFileResource != null) {
          additionalFilesForPartialSharing.add(moduleFileResource);
        } else {
          LOG.error("The module file " + moduleFile + " could not be converted to an IFile.");

          NotificationPanel.showWarning(
              "There was an error handling the  module file for"
                  + " the shared module "
                  + module
                  + ". This could lead"
                  + " to the session not working as expected.",
              "Error handling module file!");
        }
      }

      resources.addAll(additionalFilesForPartialSharing);
    }

    HashMap<IProject, List<IResource>> resources = new HashMap<IProject, List<IResource>>();

    for (Entry<IProject, Set<IResource>> entry : projectsResources.entrySet()) {
      resources.put(
          entry.getKey(),
          entry.getValue() == null ? null : new ArrayList<IResource>(entry.getValue()));
    }

    return resources;
  }

  private static String format(long size) {

    if (size < 1000) {
      return "< 1 KB";
    }

    if (size < 1000 * 1000) {
      return String.format(Locale.US, "%.2f KB", size / (1000F));
    }

    if (size < 1000 * 1000 * 1000) {
      return String.format(Locale.US, "%.2f MB", size / (1000F * 1000F));
    }

    return String.format(Locale.US, "%.2f GB", size / (1000F * 1000F * 1000F));
  }

  /**
   * Calculates the total file count and size for all resources.
   *
   * @param resources collection containing the resources that file sizes and file count should be
   *     calculated
   * @param includeMembers <code>true</code> to include the members of resources that represents a
   *     {@linkplain IContainer container}
   * @param flags additional flags on how to process the members of containers
   * @return a pair containing the {@linkplain de.fu_berlin.inf.dpp.util.Pair#p file size} and
   *     {@linkplain de.fu_berlin.inf.dpp.util.Pair#v file count} for the given resources
   */
  private static Pair<Long, Long> getFileCountAndSize(
      Collection<? extends IResource> resources, boolean includeMembers, int flags) {

    long totalFileSize = 0;
    long totalFileCount = 0;

    for (IResource resource : resources) {
      switch (resource.getType()) {
        case IResource.FILE:
          totalFileCount++;

          try {
            IFile file = (IFile) resource.getAdapter(IFile.class);

            totalFileSize += file.getSize();
          } catch (IOException e) {
            LOG.warn("failed to retrieve size of file " + resource, e);
          }
          break;
        case IResource.PROJECT:
        case IResource.FOLDER:
          if (!includeMembers) {
            break;
          }

          try {
            IContainer container = ((IContainer) resource.getAdapter(IContainer.class));

            Pair<Long, Long> subFileCountAndSize =
                getFileCountAndSize(Arrays.asList(container.members(flags)), true, flags);

            totalFileSize += subFileCountAndSize.getLeft();
            totalFileCount += subFileCountAndSize.getRight();

          } catch (Exception e) {
            LOG.warn("failed to process container: " + resource, e);
          }
          break;
        default:
          break;
      }
    }

    return Pair.of(totalFileSize, totalFileCount);
  }

  private static void fillReferencePointManager(
      de.fu_berlin.inf.dpp.filesystem.IProject project,
      IReferencePointManager referencePointManager) {
    referencePointManager.put(project.getReferencePoint(), project);

    IntelliJProjectImpl intelliJProject = (IntelliJProjectImpl) project;

    intelliJReferencePointManager.put(intelliJProject.getModule());
  }
}
