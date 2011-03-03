package de.fu_berlin.inf.dpp.util.log;

import java.util.LinkedList;
import java.util.List;

import de.fu_berlin.inf.dpp.SarosPluginContext;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;

/**
 * Appender which appends to the Eclipse log of our plug-in
 */
@Component(module = "logging")
public class EclipseLogAppender extends AppenderSkeleton {

    /**
     * While there is no Saros plug-in known the LoggingEvents are cached in
     * this list
     */
    protected List<LoggingEvent> cache = new LinkedList<LoggingEvent>();

    /*
     * Dependencies
     */
    @Inject
    protected Saros saros;

    protected void appendInternal(LoggingEvent event) {

        Level level = event.getLevel();

        if (!level.isGreaterOrEqual(Level.WARN)) {
            return;
        }

        String message = this.layout.format(event);

        int status;

        if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
            status = IStatus.ERROR;
        } else {
            status = IStatus.WARNING;
        }

        IStatus logMessage = new Status(status, Saros.SAROS, IStatus.OK,
            message, getThrowable(event));
        saros.getLog().log(logMessage);
    }

    @Override
    protected void append(LoggingEvent event) {

        if (saros == null && Saros.isInitialized()) {

            // Initialize
            SarosPluginContext.initComponent(this);

            // Flush Cache
            for (LoggingEvent cached : cache) {
                appendInternal(cached);
            }
        }

        // Cache events until Saros is initialized
        if (saros != null) {
            appendInternal(event);
        } else {
            cache.add(event);
        }
    }

    @Override
    public Priority getThreshold() {
        return Level.WARN;
    }

    public void close() {
        // Do nothing
    }

    public boolean requiresLayout() {
        return true;
    }

    public static Throwable getThrowable(LoggingEvent event) {
        ThrowableInformation information = event.getThrowableInformation();
        if (information != null) {
            return information.getThrowable();
        } else {
            return null;
        }
    }

}
