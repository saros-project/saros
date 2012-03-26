package de.fu_berlin.inf.dpp.util.log;

import org.apache.log4j.Logger;

public class LoggingUtils {

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
