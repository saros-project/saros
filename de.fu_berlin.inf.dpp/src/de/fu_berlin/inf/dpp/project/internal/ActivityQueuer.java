package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.AbstractProjectActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;

/**
 * This class enables the queuing of {@linkplain IActivityDataObject serialized
 * activities} for given projects.
 */
public class ActivityQueuer {

    private final List<IActivityDataObject> activityQueue;

    private final Set<String> projectsThatShouldBeQueued;

    private boolean stopQueuing;

    public ActivityQueuer() {
        activityQueue = new ArrayList<IActivityDataObject>();
        projectsThatShouldBeQueued = new HashSet<String>();
        stopQueuing = false;
    }

    /**
     * Processes the incoming {@linkplain IActivityDataObject serialized
     * activities} and decides which activities should be queued. All resource
     * related {@linkplain AbstractProjectActivityDataObject project activities}
     * which relate to a project that is configured for queuing using
     * {@link #enableQueuing(String)} will be queued. The method returns all
     * other activities which should not be queued.
     * 
     * If a flushing of the queue was previously requested by calling
     * {@link #disableQueuing()} than the method will return a list of all
     * queued activities.
     * 
     * @param activities
     * @return the activities that are not queued
     */
    public synchronized List<IActivityDataObject> process(
        List<IActivityDataObject> activities) {
        List<IActivityDataObject> activitiesThatWillBeExecuted = new ArrayList<IActivityDataObject>();

        if (stopQueuing) {
            activitiesThatWillBeExecuted.addAll(activityQueue);
            activitiesThatWillBeExecuted.addAll(activities);
            projectsThatShouldBeQueued.clear();
            activityQueue.clear();
            return activitiesThatWillBeExecuted;
        }

        for (IActivityDataObject dataObject : activities) {

            if (dataObject instanceof AbstractProjectActivityDataObject) {
                AbstractProjectActivityDataObject projectDataObject = (AbstractProjectActivityDataObject) dataObject;
                handleProjectActivities(projectDataObject,
                    activitiesThatWillBeExecuted);
            } else {
                activitiesThatWillBeExecuted.add(dataObject);
            }
        }
        return activitiesThatWillBeExecuted;
    }

    /**
     * Enables the queuing of {@link IActivityDataObject serialized activities}
     * related to the project with the given id.
     * 
     * @param projectId
     */
    public synchronized void enableQueuing(String projectId) {
        projectsThatShouldBeQueued.add(projectId);
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

    private void handleProjectActivities(
        AbstractProjectActivityDataObject projectDataObject,
        List<IActivityDataObject> activitiesThatWillBeExecuted) {
        SPathDataObject path = projectDataObject.getPath();
        if (path == null) {
            // can't queue without path
            activitiesThatWillBeExecuted.add(projectDataObject);
        } else if (projectsThatShouldBeQueued.contains(path.getProjectID())) {
            activityQueue.add(projectDataObject);
        } else {
            activitiesThatWillBeExecuted.add(projectDataObject);
        }
    }
}
