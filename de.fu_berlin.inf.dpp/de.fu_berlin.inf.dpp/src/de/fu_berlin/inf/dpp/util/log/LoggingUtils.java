package de.fu_berlin.inf.dpp.util.log;

import java.util.List;

import de.fu_berlin.inf.dpp.activities.serializable.ChecksumActivityDataObject;
import de.fu_berlin.inf.dpp.net.TimedActivityDataObject;

public class LoggingUtils {

    public static boolean containsChecksumsOnly(
        List<TimedActivityDataObject> timedActivities) {
        for (TimedActivityDataObject a : timedActivities)
            if (!(a.getActivity() instanceof ChecksumActivityDataObject))
                return false;
        return true;
    }

}
