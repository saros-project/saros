package saros.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IContainer;
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
import saros.Saros;
import saros.SarosPluginContext;
import saros.filesystem.EclipseProjectImpl;
import saros.filesystem.ResourceAdapterFactory;
import saros.net.xmpp.JID;
import saros.repackaged.picocontainer.annotations.Inject;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;
import saros.session.SessionEndReason;
import saros.session.User;
import saros.session.internal.SarosSession;
import saros.ui.Messages;
import saros.util.FileUtils;
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
  public static void startSession(final List<IResource> resources, final List<JID> contacts) {
    assert resources.stream().allMatch(resource -> resource instanceof IProject)
        : "Encountered non-project resource to share";

    final Set<IProject> projects =
        resources.stream().map(resource -> (IProject) resource).collect(Collectors.toSet());

    startSession(projects, contacts);
  }

  /**
   * Starts a new session and shares the given projects with given contacts.<br>
   * Does nothing if a {@link ISarosSession session} is already running.
   *
   * @param projects the projects share
   * @param contacts the contacts to share the projects with
   * @nonBlocking
   */
  public static void startSession(final Set<IProject> projects, final List<JID> contacts) {
    Job sessionStartupJob =
        new Job("Session Startup") {

          @Override
          protected IStatus run(IProgressMonitor monitor) {
            monitor.beginTask("Starting session...", IProgressMonitor.UNKNOWN);

            try {
              refreshProjects(projects, null);
              sessionManager.startSession(convert(projects));
              Set<JID> participantsToAdd = new HashSet<JID>(contacts);

              ISarosSession session = sessionManager.getSession();

              if (session == null) return Status.CANCEL_STATUS;

              sessionManager.invite(participantsToAdd, getSessionDescription(session));

            } catch (Exception e) {

              log.error("could not start a Saros session", e);
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
        log,
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

    if (resourcesToAdd.isEmpty()) return;

    assert resourcesToAdd.stream().allMatch(resource -> resource instanceof IProject)
        : "Encountered non-project resource to share";

    final Set<IProject> projects =
        resourcesToAdd.stream().map(resource -> (IProject) resource).collect(Collectors.toSet());

    addResourcesToSession(projects);
  }

  /**
   * Adds the given project resources to the session.<br>
   * Does nothing if no {@link SarosSession session} is running.
   *
   * @param projectsToAdd the projects to add to the session
   * @nonBlocking
   */
  public static void addResourcesToSession(Set<IProject> projectsToAdd) {

    final ISarosSession session = sessionManager.getSession();

    if (session == null) {
      log.warn("cannot add resources to a non-running session");
      return;
    }

    ThreadUtils.runSafeAsync(
        "AddResourceToSession",
        log,
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

            for (IProject project : projectsToAdd) {
              if (!session.isShared(ResourceAdapterFactory.create(project)))
                projectsToRefresh.add(project);
            }

            try {
              refreshProjects(projectsToRefresh, null);
            } catch (CoreException e) {
              log.warn("failed to refresh projects", e);
              /*
               * FIXME use a Job instead of a plain thread and so better
               * execption handling !
               */
            }

            sessionManager.addProjectsToSession(convert(projectsToAdd));
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

    final Set<saros.filesystem.IProject> projects = sarosSession.getProjects();

    final StringBuilder result = new StringBuilder();

    for (saros.filesystem.IProject project : projects) {

      final Pair<Long, Long> fileCountAndSize;

      final List<IResource> resources =
          Collections.singletonList(((EclipseProjectImpl) project).getDelegate());

      fileCountAndSize = FileUtils.getFileCountAndSize(resources, true, IContainer.EXCLUDE_DERIVED);

      result.append(
          String.format(
              "\nProject: %s (%s), Files: %d, Size: %s",
              project.getName(),
              "complete",
              fileCountAndSize.getRight(),
              format(fileCountAndSize.getLeft())));
    }

    return result.toString();
  }

  private static String format(long size) {

    if (size < 1000) return "< 1 KB";

    if (size < 1000 * 1000) return String.format(Locale.US, "%.2f KB", size / (1000F));

    if (size < 1000 * 1000 * 1000)
      return String.format(Locale.US, "%.2f MB", size / (1000F * 1000F));

    return String.format(Locale.US, "%.2f GB", size / (1000F * 1000F * 1000F));
  }

  private static Set<saros.filesystem.IProject> convert(Set<IProject> projects) {
    return projects.stream().map(ResourceAdapterFactory::create).collect(Collectors.toSet());
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
}
