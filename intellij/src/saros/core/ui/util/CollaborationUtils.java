package saros.core.ui.util;

import com.intellij.openapi.project.Project;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import saros.SarosPluginContext;
import saros.core.monitoring.IStatus;
import saros.core.monitoring.Status;
import saros.filesystem.IContainer;
import saros.filesystem.IFile;
import saros.filesystem.IProject;
import saros.filesystem.IResource;
import saros.intellij.SarosComponent;
import saros.intellij.runtime.UIMonitoredJob;
import saros.intellij.ui.Messages;
import saros.intellij.ui.util.DialogUtils;
import saros.intellij.ui.util.NotificationPanel;
import saros.monitoring.IProgressMonitor;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.internal.SarosSession;
import saros.util.ThreadUtils;

/**
 * Offers convenient methods for collaboration actions like sharing a project resources.
 *
 * @author bkahlert
 * @author kheld
 */
public class CollaborationUtils {

  private static final Logger log = Logger.getLogger(CollaborationUtils.class);

  @Inject private static ISarosSessionManager sessionManager;

  static {
    SarosPluginContext.initComponent(new CollaborationUtils());
  }

  private CollaborationUtils() {}

  /**
   * Starts a new session and shares the given projects with given contacts.<br>
   * Does nothing if a {@link ISarosSession session} is already running.
   *
   * @param projects the projects to share
   * @param contacts the contacts to share the projects with
   * @nonBlocking
   */
  public static void startSession(Set<IProject> projects, final List<JID> contacts) {

    UIMonitoredJob sessionStartupJob =
        new UIMonitoredJob("Session Startup") {

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask("Starting session...", IProgressMonitor.UNKNOWN);
            try {
              sessionManager.startSession(projects);
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

              log.error("could not start a Saros session", e);
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
  public static void leaveSession(Project project) {

    ISarosSession sarosSession = sessionManager.getSession();

    if (sarosSession == null) {
      log.warn("cannot leave a non-running session");
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
                project,
                Messages.CollaborationUtils_confirm_closing_title,
                Messages.CollaborationUtils_confirm_closing_message);
      }
    } else {
      reallyLeave =
          DialogUtils.showConfirm(
              project,
              Messages.CollaborationUtils_confirm_leaving_title,
              Messages.CollaborationUtils_confirm_leaving_message);
    }

    if (!reallyLeave) {
      return;
    }

    ThreadUtils.runSafeAsync(
        "StopSession",
        log,
        new Runnable() {
          @Override
          public void run() {
            sessionManager.stopSession(SessionEndReason.LOCAL_USER_LEFT);
          }
        });
  }

  /**
   * Adds the given projects to the session.<br>
   * Does nothing if no {@link SarosSession session} is running.
   *
   * @param projectsToAdd the projects to add to the session
   * @nonBlocking
   */
  public static void addResourcesToSession(Set<IProject> projectsToAdd) {

    final ISarosSession sarosSession = sessionManager.getSession();

    if (sarosSession == null) {
      log.warn("cannot add resources to a non-running session");
      return;
    }

    if (projectsToAdd.isEmpty()) {
      return;
    }

    ThreadUtils.runSafeAsync(
        "AddResourceToSession",
        log,
        new Runnable() {
          @Override
          public void run() {

            if (sarosSession.hasWriteAccess()) {
              sessionManager.addProjectsToSession(projectsToAdd);
              return;
            }

            NotificationPanel.showError(
                Messages.CollaborationUtils_insufficient_privileges_message,
                Messages.CollaborationUtils_insufficient_privileges_title);
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
      log.warn("cannot add contacts to a non-running session");
      return;
    }

    ThreadUtils.runSafeAsync(
        "AddContactToSession",
        log,
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

    Set<IProject> projects = sarosSession.getProjects();

    StringBuilder result = new StringBuilder();

    try {
      for (IProject project : projects) {

        Pair<Long, Long> fileCountAndSize;

        fileCountAndSize = getFileCountAndSize(project.members());

        result.append(
            String.format(
                "\nReference Point: %s, Files: %d, Size: %s",
                project.getName(),
                fileCountAndSize.getRight(),
                format(fileCountAndSize.getLeft())));
      }
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return "Could not get description";
    }

    return result.toString();
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
   * @return a pair containing the file size (left element) and file count (right element) for the
   *     given resources
   */
  private static Pair<Long, Long> getFileCountAndSize(Collection<? extends IResource> resources) {

    long totalFileSize = 0;
    long totalFileCount = 0;

    for (IResource resource : resources) {
      switch (resource.getType()) {
        case FILE:
          totalFileCount++;

          try {
            IFile file = (IFile) resource;

            totalFileSize += file.getSize();
          } catch (IOException e) {
            log.warn("failed to retrieve size of file " + resource, e);
          }
          break;
        case PROJECT:
        case FOLDER:
          try {
            IContainer container = (IContainer) resource;

            Pair<Long, Long> subFileCountAndSize = getFileCountAndSize(container.members());

            totalFileSize += subFileCountAndSize.getLeft();
            totalFileCount += subFileCountAndSize.getRight();

          } catch (Exception e) {
            log.warn("failed to process container: " + resource, e);
          }
          break;
        default:
          break;
      }
    }

    return Pair.of(totalFileSize, totalFileCount);
  }
}
