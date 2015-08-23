package de.fu_berlin.inf.dpp.session.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.activities.EditorActivity;
import de.fu_berlin.inf.dpp.activities.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.IActivity;
import de.fu_berlin.inf.dpp.activities.IResourceActivity;
import de.fu_berlin.inf.dpp.activities.JupiterActivity;
import de.fu_berlin.inf.dpp.activities.SPath;
import de.fu_berlin.inf.dpp.filesystem.IProject;
import de.fu_berlin.inf.dpp.session.User;

/**
 * This class enables the queuing of {@linkplain IActivity activities} for given
 * projects.
 */
public class ActivityQueuer {

    private final List<IResourceActivity> activityQueue;

    private final Set<IProject> projectsThatShouldBeQueued;

    private boolean stopQueuing;

    public ActivityQueuer() {
        activityQueue = new ArrayList<IResourceActivity>();
        projectsThatShouldBeQueued = new HashSet<IProject>();
        stopQueuing = false;
    }

    /**
     * Processes the incoming {@linkplain IActivity activities} and decides
     * which activities should be queued. All {@linkplain IResourceActivity
     * resource related activities} which relate to a project that is configured
     * for queuing using {@link #enableQueuing(IProject)} will be queued. The
     * method returns all other activities which should not be queued.
     * 
     * If a flushing of the queue was previously requested by calling
     * {@link #disableQueuing()} than the method will return a list of all
     * queued activities.
     * 
     * @param activities
     * @return the activities that are not queued
     */
    public synchronized List<IActivity> process(List<IActivity> activities) {
        List<IActivity> activitiesThatWillBeExecuted = new ArrayList<IActivity>();

        if (stopQueuing) {
            if (activityQueue.isEmpty())
                return activities;

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

            for (IResourceActivity resourceActivity : activityQueue) {

                // path cannot be null, see for-loop below
                SPath path = resourceActivity.getPath();
                User source = resourceActivity.getSource();

                if (resourceActivity instanceof EditorActivity) {

                    EditorActivity ea = (EditorActivity) resourceActivity;

                    if (!alreadyRememberedEditorActivity(editorActivities,
                        path, source) && ea.getType() != Type.ACTIVATED) {
                        activitiesThatWillBeExecuted.add(new EditorActivity(ea
                            .getSource(), Type.ACTIVATED, path));
                    }

                    rememberEditorActivity(editorActivities, path, source);
                } else if (resourceActivity instanceof JupiterActivity
                    && !alreadyRememberedEditorActivity(editorActivities, path,
                        source)) {

                    activitiesThatWillBeExecuted.add(new EditorActivity(
                        resourceActivity.getSource(), Type.ACTIVATED, path));

                    rememberEditorActivity(editorActivities, path, source);
                }
                activitiesThatWillBeExecuted.add(resourceActivity);
            }

            activitiesThatWillBeExecuted.addAll(activities);
            projectsThatShouldBeQueued.clear();
            activityQueue.clear();
            return activitiesThatWillBeExecuted;
        }

        for (IActivity activity : activities) {
            if (activity instanceof IResourceActivity) {
                IResourceActivity resourceActivity = (IResourceActivity) activity;

                SPath path = resourceActivity.getPath();

                // can't queue activities without path
                if (path != null
                    && projectsThatShouldBeQueued.contains(path.getProject())) {
                    activityQueue.add(resourceActivity);
                    continue;
                }
            }

            activitiesThatWillBeExecuted.add(activity);
        }

        return activitiesThatWillBeExecuted;
    }

    /**
     * Enables the queuing of {@link IActivity serialized activities} related to
     * the given project.
     * 
     * @param project
     */
    public synchronized void enableQueuing(IProject project) {
        projectsThatShouldBeQueued.add(project);
        stopQueuing = false;
    }

    /**
     * Disables the queuing for all projects. Currently queued activities will
     * be flushed after the next invocation of {@link #process(List)}.
     * 
     * @Note This method <b>MUST</b> be called at the end of an invitation
     *       process because it stops the queuing for all projects which at
     *       least releases the queued activities to prevent memory leaks. At
     *       the moment stopping the queuing for each project separately is not
     *       needed, since the projects are added after the invitation process
     *       at the same time. When multiple invitations at the same moment will
     *       be possible, this implementation needs to be changed.
     */
    public synchronized void disableQueuing() {
        stopQueuing = true;
    }

    private boolean alreadyRememberedEditorActivity(
        Map<SPath, List<User>> editorActivities, SPath spath, User user) {

        List<User> users = editorActivities.get(spath);
        return users != null && users.contains(user);
    }

    private void rememberEditorActivity(
        Map<SPath, List<User>> editorActivities, SPath spath, User user) {
        List<User> users = editorActivities.get(spath);

        if (users == null) {
            users = new ArrayList<User>();
            editorActivities.put(spath, users);
        }

        if (!users.contains(user))
            users.add(user);
    }
}
