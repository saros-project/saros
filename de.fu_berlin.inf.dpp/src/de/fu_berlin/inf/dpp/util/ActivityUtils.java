package de.fu_berlin.inf.dpp.util;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.IActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.JupiterActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextEditActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.TextSelectionActivityDataObject;
import de.fu_berlin.inf.dpp.activities.serializable.ViewportActivityDataObject;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;

/**
 * Class contains static helper methods on activities
 */
public class ActivityUtils {

    /**
     * Checks if all {@link TimedActivityDataObject}s in the given Collection
     * are of instance {@link ChecksumActivityDataObject}
     * 
     * @param timedActivities
     *            Collection of {@link ChecksumActivityDataObject}s
     * @return true, if only {@link ChecksumActivityDataObject}s are in the
     *         given collection, false otherwise
     */
    public static boolean containsChecksumsOnly(
        List<TimedActivityDataObject> timedActivities) {

        for (TimedActivityDataObject a : timedActivities)
            if (!(a.getActivity() instanceof ChecksumActivityDataObject))
                return false;
        return true;
    }

    /**
     * Checks if all {@link IActivityDataObject}s in the given Collection are of
     * instance {@link ViewportActivityDataObject},
     * {@link JupiterActivityDataObject},
     * {@link TextSelectionActivityDataObject}, or
     * {@link TextEditActivityDataObject} - activities that can be created by
     * the inviter during an project synchronization and are uncritically to
     * delay (during an IBB transfer).
     * 
     * @param activities
     *            Collection of {@link IActivityDataObject}s
     * @return true, if all {@link IActivityDataObject}s are instances of
     *         mentioned activities, false otherwise
     */
    public static boolean containsQueueableActivitiesOnly(
        List<IActivityDataObject> activities) {

        for (IActivityDataObject a : activities)
            if (a instanceof ViewportActivityDataObject
                || a instanceof JupiterActivityDataObject
                || a instanceof TextSelectionActivityDataObject
                || a instanceof TextEditActivityDataObject)
                continue;
            else
                return false;

        return true;
    }

}
