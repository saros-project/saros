package de.fu_berlin.inf.dpp.project.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.fu_berlin.inf.dpp.activities.SPathDataObject;
import de.fu_berlin.inf.dpp.activities.business.EditorActivity.Type;
import de.fu_berlin.inf.dpp.activities.serializable.AbstractProjectActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.EditorActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This class enables the queuing of {@linkplain IActivityDataObject serialized
 * activities} for given projects.
 */
public class ActivityQueuer {

    private final List<AbstractProjectActivityDataObject> activityQueue;

    private final Set<String> projectsThatShouldBeQueued;

    private boolean stopQueuing;

    public ActivityQueuer() {
        activityQueue = new ArrayList<AbstractProjectActivityDataObject>();
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

            Map<SPathDataObject, List<JID>> editorADOs = new HashMap<SPathDataObject, List<JID>>();

            for (AbstractProjectActivityDataObject pado : activityQueue) {

                // path cannot be null, see handleProjectActivities
                SPathDataObject path = pado.getPath();
                JID source = pado.getSource();

                if (pado instanceof EditorActivityDataObject) {

                    EditorActivityDataObject eado = (EditorActivityDataObject) pado;

                    if (!alreadyRememberedEditorADO(editorADOs, path, source)
                        && eado.getType() != Type.ACTIVATED) {
                        activitiesThatWillBeExecuted
                            .add(new EditorActivityDataObject(eado.getSource(),
                                Type.ACTIVATED, path));
                    }

                    rememberEditorADO(editorADOs, path, source);
                } else if (pado instanceof JupiterActivityDataObject
                    && !alreadyRememberedEditorADO(editorADOs, path, source)) {

                    activitiesThatWillBeExecuted
                        .add(new EditorActivityDataObject(pado.getSource(),
                            Type.ACTIVATED, path));

                    rememberEditorADO(editorADOs, path, source);
                }
                activitiesThatWillBeExecuted.add(pado);
            }

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

    private boolean alreadyRememberedEditorADO(
        Map<SPathDataObject, List<JID>> editorADOs, SPathDataObject spdo,
        JID jid) {

        List<JID> jids = editorADOs.get(spdo);
        return jids != null && jids.contains(jid);
    }

    private void rememberEditorADO(Map<SPathDataObject, List<JID>> editorADOs,
        SPathDataObject spdo, JID jid) {
        List<JID> jids = editorADOs.get(spdo);

        if (jids == null) {
            jids = new ArrayList<JID>();
            editorADOs.put(spdo, jids);
        }

        if (!jids.contains(jid))
            jids.add(jid);
    }
}
