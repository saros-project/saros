package de.fu_berlin.inf.dpp.session.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IReferencePoint;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This class enables the queuing of {@linkplain IActivity activities} for given
 * reference points.
 */
public class ActivityQueuer {

    private static class ReferencePointQueue {
        private final IReferencePoint referencePoint;
        private final List<IResourceActivity> buffer;
        private int readyToFlush;

        private ReferencePointQueue(IReferencePoint referencePoint) {
            this.referencePoint = referencePoint;
            buffer = new ArrayList<IResourceActivity>();
            readyToFlush = 1;
        }
    }

    private final List<ReferencePointQueue> referencePointQueues;

    public ActivityQueuer() {
        referencePointQueues = new ArrayList<ReferencePointQueue>();
    }

    /**
     * Processes the incoming {@linkplain IActivity activities} and decides
     * which activities should be queued. All {@linkplain IResourceActivity
     * resource related activities} which relate to a reference point that is
     * configured for queuing using {@link #enableQueuing} will be queued. The
     * method returns all other activities which should not be queued.
     * <p>
     * If a flushing of the queue was previously requested by calling
     * {@link #disableQueuing} than the method will return a list of all queued
     * activities.
     * 
     * @param activities
     * @return the activities that are not queued
     */
    public synchronized List<IActivity> process(final List<IActivity> activities) {

        if (referencePointQueues.isEmpty())
            return activities;

        final List<IActivity> activitiesToExecute = new ArrayList<IActivity>();

        flushQueues(activitiesToExecute);
        queueActivities(activitiesToExecute, activities);

        return activitiesToExecute;
    }

    /**
     * Enables the queuing of {@link IActivity activities} related to the given
     * project.
     * <p>
     * {@link #enableQueuing} and {@link #disableQueuing} can be called
     * multiples time for a given reference point, increasing or decreasing the
     * internal counter. Activities can be flushed when the counter reaches
     * zero.
     * 
     * 
     * @param referencePoint
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
     * Disables the queuing for all reference point. Currently queued activities
     * will be flushed after the next invocation of {@link #process} if the
     * reference point is marked as flush-able.
     * <p>
     * {@link #enableQueuing} and {@link #disableQueuing} can be called
     * multiples time for a given reference point, increasing or decreasing the
     * internal counter. Activities can be flushed when the counter reaches
     * zero.
     * <p>
     * <b>Note: </b> This method <b>MUST</b> be called at the end of an
     * invitation process because it stops the queuing for the given reference
     * point which at least releases the queued activities to prevent memory
     * leaks.
     * 
     * @param referencePoint
     */
    public synchronized void disableQueuing(final IReferencePoint referencePoint) {
        for (final ReferencePointQueue referencePointQueue : referencePointQueues) {

            if (referencePointQueue.referencePoint.equals(referencePoint)) {

                if (referencePointQueue.readyToFlush > 0)
                    referencePointQueue.readyToFlush--;

                return;
            }
        }
    }

    private boolean alreadyRememberedEditorActivity(
        final Map<SPath, List<User>> editorActivities, final SPath spath,
        final User user) {

        final List<User> users = editorActivities.get(spath);
        return users != null && users.contains(user);
    }

    private void rememberEditorActivity(
        final Map<SPath, List<User>> editorActivities, final SPath spath,
        final User user) {

        List<User> users = editorActivities.get(spath);

        if (users == null) {
            users = new ArrayList<User>();
            editorActivities.put(spath, users);
        }

        if (!users.contains(user))
            users.add(user);
    }

    private void queueActivities(final List<IActivity> activitiesToExecute,
        final List<IActivity> activities) {

        ReferencePointQueue referencePointQueue = null;

        for (final IActivity activity : activities) {
            if (activity instanceof IResourceActivity) {

                IResourceActivity resourceActivity = (IResourceActivity) activity;

                SPath path = resourceActivity.getPath();

                // can't queue activities without path
                if (path != null) {

                    // try to reuse the queue as lookup is O(n)
                    if (referencePointQueue == null
                        || !referencePointQueue.referencePoint.equals(path
                            .getProject().getReferencePoint())) {
                        referencePointQueue = getReferencePointQueue(path
                            .getProject().getReferencePoint());
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
        final List<ReferencePointQueue> referencePointQueuesToRemove = new ArrayList<ReferencePointQueue>();

        for (final ReferencePointQueue referencePointQueue : referencePointQueues) {

            if (referencePointQueue.readyToFlush > 0)
                continue;

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

            for (final IResourceActivity resourceActivity : referencePointQueue.buffer) {

                // path cannot be null, see for-loop below
                final SPath path = resourceActivity.getPath();
                final User source = resourceActivity.getSource();

                if (resourceActivity instanceof EditorActivity) {

                    final EditorActivity ea = (EditorActivity) resourceActivity;

                    if (!alreadyRememberedEditorActivity(editorActivities,
                        path, source) && ea.getType() != Type.ACTIVATED) {
                        activities.add(new EditorActivity(ea.getSource(),
                            Type.ACTIVATED, path));
                    }

                    rememberEditorActivity(editorActivities, path, source);
                } else if (resourceActivity instanceof JupiterActivity
                    && !alreadyRememberedEditorActivity(editorActivities, path,
                        source)) {

                    activities.add(new EditorActivity(resourceActivity
                        .getSource(), Type.ACTIVATED, path));

                    rememberEditorActivity(editorActivities, path, source);
                }

                activities.add(resourceActivity);
            }

            referencePointQueuesToRemove.add(referencePointQueue);
        }

        for (final ReferencePointQueue referencePointQueue : referencePointQueuesToRemove)
            referencePointQueues.remove(referencePointQueue);
    }

    private ReferencePointQueue getReferencePointQueue(
        final IReferencePoint referencePoint) {

        for (final ReferencePointQueue projectQueue : referencePointQueues) {
            if (projectQueue.referencePoint.equals(referencePoint))
                return projectQueue;
        }

        return null;
    }
}
