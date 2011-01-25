package de.fu_berlin.inf.dpp.util.log;

import java.util.List;

import org.apache.log4j.Logger;

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

    /**
     * If debug is true, it will log the message in debug level.
     * 
     * Else it checks whether trace is enabled and if so log it to Trace.
     */
    public static void log(Logger log, String msg, boolean debug) {
        if (debug) {
            log.debug(msg);
        } else {
            if (log.isTraceEnabled()) {
                log.trace(msg);
            }
        }
    }
}
