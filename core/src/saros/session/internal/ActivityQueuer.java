package saros.session.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import saros.activities.EditorActivity;
import saros.activities.EditorActivity.Type;
import saros.activities.IActivity;
import saros.activities.IResourceActivity;
import saros.activities.JupiterActivity;
import saros.filesystem.IFile;
import saros.filesystem.IReferencePoint;
import saros.filesystem.IResource;
import saros.session.User;

/**
 * This class enables the queuing of {@linkplain IActivity activities} for given reference points.
 */
public class ActivityQueuer {

  private static class ReferencePointQueue {
    private final IReferencePoint referencePoint;
    private final List<IResourceActivity<? extends IResource>> buffer;
    private int readyToFlush;

    private ReferencePointQueue(IReferencePoint referencePoint) {
      this.referencePoint = referencePoint;
      buffer = new ArrayList<>();
      readyToFlush = 1;
    }
  }

  private final List<ReferencePointQueue> referencePointQueues;

  public ActivityQueuer() {
    referencePointQueues = new ArrayList<ReferencePointQueue>();
  }

  /**
   * Processes the incoming {@linkplain IActivity activities} and decides which activities should be
   * queued. All {@linkplain IResourceActivity resource related activities} which relate to a
   * reference point that is configured for queuing using {@link #enableQueuing} will be queued. The
   * method returns all other activities which should not be queued.
   *
   * <p>If a flushing of the queue was previously requested by calling {@link #disableQueuing} than
   * the method will return a list of all queued activities.
   *
   * @param activities activities
   * @return the activities that are not queued
   */
  public synchronized List<IActivity> process(final List<IActivity> activities) {

    if (referencePointQueues.isEmpty()) return activities;

    final List<IActivity> activitiesToExecute = new ArrayList<IActivity>();

    flushQueues(activitiesToExecute);
    queueActivities(activitiesToExecute, activities);

    return activitiesToExecute;
  }

  /**
   * Enables the queuing of {@link IActivity activities} related to the given reference point.
   *
   * <p>This method and {@link #disableQueuing} can be called multiples time for a given reference
   * point, increasing or decreasing the internal counter. Activities can be flushed when the
   * counter reaches zero.
   *
   * @param referencePoint reference point
   */
  public synchronized void enableQueuing(final IReferencePoint referencePoint) {
    for (final ReferencePointQueue referencePointQueue : referencePointQueues) {

      if (referencePointQueue.referencePoint.equals(referencePoint)) {

        referencePointQueue.readyToFlush++;
        return;
      }
    }

    referencePointQueues.add(new ReferencePointQueue(referencePoint));
  }

  /**
   * Disables the queuing for all reference points. Currently queued activities will be flushed
   * after the next invocation of {@link #process} if the reference point is marked as flush-able.
   *
   * <p>This method and {@link #enableQueuing} can be called multiples time for a given reference
   * point, increasing or decreasing the internal counter. Activities can be flushed when the
   * counter reaches zero.
   *
   * <p><b>Note: </b> This method <b>MUST</b> be called at the end of an invitation process because
   * it stops the queuing for the given reference point which at least releases the queued
   * activities to prevent memory leaks.
   *
   * @param referencePoint reference point
   */
  public synchronized void disableQueuing(final IReferencePoint referencePoint) {
    for (final ReferencePointQueue referencePointQueue : referencePointQueues) {

      if (referencePointQueue.referencePoint.equals(referencePoint)) {

        if (referencePointQueue.readyToFlush > 0) referencePointQueue.readyToFlush--;

        return;
      }
    }
  }

  private boolean alreadyRememberedEditorActivity(
      final Map<IFile, List<User>> editorActivities, final IFile file, final User user) {

    final List<User> users = editorActivities.get(file);
    return users != null && users.contains(user);
  }

  private void rememberEditorActivity(
      final Map<IFile, List<User>> editorActivities, final IFile file, final User user) {

    List<User> users = editorActivities.get(file);

    if (users == null) {
      users = new ArrayList<User>();
      editorActivities.put(file, users);
    }

    if (!users.contains(user)) users.add(user);
  }

  private void queueActivities(
      final List<IActivity> activitiesToExecute, final List<IActivity> activities) {

    ReferencePointQueue referencePointQueue = null;

    for (final IActivity activity : activities) {
      if (activity instanceof IResourceActivity) {

        IResourceActivity<? extends IResource> resourceActivity =
            (IResourceActivity<? extends IResource>) activity;

        IResource resource = resourceActivity.getResource();

        // can't queue activities without resource
        if (resource != null) {

          // try to reuse the queue as lookup is O(n)
          if (referencePointQueue == null
              || !referencePointQueue.referencePoint.equals(resource.getReferencePoint())) {
            referencePointQueue = getReferencePointQueue(resource.getReferencePoint());
          }

          if (referencePointQueue != null) {
            referencePointQueue.buffer.add(resourceActivity);
            continue;
          }
        }
      }

      activitiesToExecute.add(activity);
    }
  }

  private void flushQueues(final List<IActivity> activities) {
    final List<ReferencePointQueue> referencePointQueuesToRemove =
        new ArrayList<ReferencePointQueue>();

    for (final ReferencePointQueue referencePointQueue : referencePointQueues) {

      if (referencePointQueue.readyToFlush > 0) continue;

      /*
       * HACK: ensure that an editor activated activity is included for
       * all queued JupiterActivities and EditorActivities. Otherwise we
       * will get lost updates because the changes are not saved. See the
       * editor package and its classes for additional details. As we can
       * start queuing at any point we might miss the editor activated
       * activity or we joined the session after those activities were
       * fired on the remote sides.
       */

      final Map<IFile, List<User>> editorActivities = new HashMap<>();

      for (final IResourceActivity<? extends IResource> resourceActivity :
          referencePointQueue.buffer) {

        // resource cannot be null, see for-loop below
        final IResource resource = resourceActivity.getResource();
        final User source = resourceActivity.getSource();

        if (resourceActivity instanceof EditorActivity) {
          IFile file = (IFile) resource;

          final EditorActivity ea = (EditorActivity) resourceActivity;

          if (!alreadyRememberedEditorActivity(editorActivities, file, source)
              && ea.getType() != Type.ACTIVATED) {

            activities.add(new EditorActivity(ea.getSource(), Type.ACTIVATED, file));
          }

          rememberEditorActivity(editorActivities, file, source);

        } else if (resourceActivity instanceof JupiterActivity) {
          IFile file = (IFile) resource;

          if (!alreadyRememberedEditorActivity(editorActivities, file, source)) {
            activities.add(new EditorActivity(resourceActivity.getSource(), Type.ACTIVATED, file));

            rememberEditorActivity(editorActivities, file, source);
          }
        }

        activities.add(resourceActivity);
      }

      referencePointQueuesToRemove.add(referencePointQueue);
    }

    for (final ReferencePointQueue referencePointQueue : referencePointQueuesToRemove)
      referencePointQueues.remove(referencePointQueue);
  }

  private ReferencePointQueue getReferencePointQueue(final IReferencePoint referencePoint) {

    for (final ReferencePointQueue referencePointQueue : referencePointQueues) {
      if (referencePointQueue.referencePoint.equals(referencePoint)) return referencePointQueue;
    }

    return null;
  }
}
