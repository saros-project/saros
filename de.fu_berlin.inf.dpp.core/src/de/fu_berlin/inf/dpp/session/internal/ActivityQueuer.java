package de.fu_berlin.inf.dpp.session.internal;

import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.session.User;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** This class enables the queuing of {@linkplain IActivity activities} for given projects. */
public class ActivityQueuer {

  private static class ProjectQueue {
    private final IReferencePoint referencePoint;
    private final List<IResourceActivity> buffer;
    private int readyToFlush;

    private ProjectQueue(IReferencePoint referencePoint) {
      this.referencePoint = referencePoint;
      buffer = new ArrayList<IResourceActivity>();
      readyToFlush = 1;
    }
  }

  private final List<ProjectQueue> projectQueues;

  public ActivityQueuer() {
    projectQueues = new ArrayList<ProjectQueue>();
  }

  /**
   * Processes the incoming {@linkplain IActivity activities} and decides which activities should be
   * queued. All {@linkplain IResourceActivity resource related activities} which relate to a
   * project that is configured for queuing using {@link #enableQueuing} will be queued. The method
   * returns all other activities which should not be queued.
   *
   * <p>If a flushing of the queue was previously requested by calling {@link #disableQueuing} than
   * the method will return a list of all queued activities.
   *
   * @param activities
   * @return the activities that are not queued
   */
  public synchronized List<IActivity> process(final List<IActivity> activities) {

    if (projectQueues.isEmpty()) return activities;

    final List<IActivity> activitiesToExecute = new ArrayList<IActivity>();

    flushQueues(activitiesToExecute);
    queueActivities(activitiesToExecute, activities);

    return activitiesToExecute;
  }

  /**
   * Enables the queuing of {@link IActivity activities} related to the given project.
   *
   * <p>{@link #enableQueuing} and {@link #disableQueuing} can be called multiples time for a given
   * project, increasing or decreasing the internal counter. Activities can be flushed when the
   * counter reaches zero.
   *
   * @param referencePoint
   */
  public synchronized void enableQueuing(final IReferencePoint referencePoint) {
    for (final ProjectQueue projectQueue : projectQueues) {

      if (projectQueue.referencePoint.equals(referencePoint)) {

        projectQueue.readyToFlush++;
        return;
      }
    }

    projectQueues.add(new ProjectQueue(referencePoint));
  }

  /**
   * Disables the queuing for all projects. Currently queued activities will be flushed after the
   * next invocation of {@link #process} if the project is marked as flush-able.
   *
   * <p>{@link #enableQueuing} and {@link #disableQueuing} can be called multiples time for a given
   * project, increasing or decreasing the internal counter. Activities can be flushed when the
   * counter reaches zero.
   *
   * <p><b>Note: </b> This method <b>MUST</b> be called at the end of an invitation process because
   * it stops the queuing for the given project which at least releases the queued activities to
   * prevent memory leaks.
   *
   * @param referencePoint
   */
  public synchronized void disableQueuing(final IReferencePoint referencePoint) {
    for (final ProjectQueue projectQueue : projectQueues) {

      if (projectQueue.referencePoint.equals(referencePoint)) {

        if (projectQueue.readyToFlush > 0) projectQueue.readyToFlush--;

        return;
      }
    }
  }

  private boolean alreadyRememberedEditorActivity(
      final Map<SPath, List<User>> editorActivities, final SPath spath, final User user) {

    final List<User> users = editorActivities.get(spath);
    return users != null && users.contains(user);
  }

  private void rememberEditorActivity(
      final Map<SPath, List<User>> editorActivities, final SPath spath, final User user) {

    List<User> users = editorActivities.get(spath);

    if (users == null) {
      users = new ArrayList<User>();
      editorActivities.put(spath, users);
    }

    if (!users.contains(user)) users.add(user);
  }

  private void queueActivities(
      final List<IActivity> activitiesToExecute, final List<IActivity> activities) {

    ProjectQueue projectQueue = null;

    for (final IActivity activity : activities) {
      if (activity instanceof IResourceActivity) {

        IResourceActivity resourceActivity = (IResourceActivity) activity;

        SPath path = resourceActivity.getPath();

        // can't queue activities without path
        if (path != null) {

          // try to reuse the queue as lookup is O(n)
          if (projectQueue == null
              || !projectQueue.referencePoint.equals(path.getReferencePoint())) {
            projectQueue = getProjectQueue(path.getReferencePoint());
          }

          if (projectQueue != null) {
            projectQueue.buffer.add(resourceActivity);
            continue;
          }
        }
      }

      activitiesToExecute.add(activity);
    }
  }

  private void flushQueues(final List<IActivity> activities) {
    final List<ProjectQueue> projectQueuesToRemove = new ArrayList<ProjectQueue>();

    for (final ProjectQueue projectQueue : projectQueues) {

      if (projectQueue.readyToFlush > 0) continue;

      /*
       * HACK: ensure that an editor activated activity is included for
       * all queued JupiterActivities and EditorActivities. Otherwise we
       * will get lost updates because the changes are not saved. See the
       * editor package and its classes for additional details. As we can
       * start queuing at any point we might miss the editor activated
       * activity or we joined the session after those activities were
       * fired on the remote sides.
       */

      final Map<SPath, List<User>> editorActivities = new HashMap<SPath, List<User>>();

      for (final IResourceActivity resourceActivity : projectQueue.buffer) {

        // path cannot be null, see for-loop below
        final SPath path = resourceActivity.getPath();
        final User source = resourceActivity.getSource();

        if (resourceActivity instanceof EditorActivity) {

          final EditorActivity ea = (EditorActivity) resourceActivity;

          if (!alreadyRememberedEditorActivity(editorActivities, path, source)
              && ea.getType() != Type.ACTIVATED) {
            activities.add(new EditorActivity(ea.getSource(), Type.ACTIVATED, path));
          }

          rememberEditorActivity(editorActivities, path, source);
        } else if (resourceActivity instanceof JupiterActivity
            && !alreadyRememberedEditorActivity(editorActivities, path, source)) {

          activities.add(new EditorActivity(resourceActivity.getSource(), Type.ACTIVATED, path));

          rememberEditorActivity(editorActivities, path, source);
        }

        activities.add(resourceActivity);
      }

      projectQueuesToRemove.add(projectQueue);
    }

    for (final ProjectQueue projectQueue : projectQueuesToRemove)
      projectQueues.remove(projectQueue);
  }

  private ProjectQueue getProjectQueue(final IReferencePoint referencePoint) {

    for (final ProjectQueue projectQueue : projectQueues) {
      if (projectQueue.referencePoint.equals(referencePoint)) return projectQueue;
    }

    return null;
  }
}
