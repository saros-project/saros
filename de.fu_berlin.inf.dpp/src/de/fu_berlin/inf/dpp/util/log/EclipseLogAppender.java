package de.fu_berlin.inf.dpp.util.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.Saros;

/**
 * Appender which appends to the Eclipse log of our plug-in
 */
public class EclipseLogAppender extends AppenderSkeleton {

    public static Throwable getThrowable(LoggingEvent event) {
        ThrowableInformation information = event.getThrowableInformation();
        if (information != null) {
            return information.getThrowable();
        } else {
            return null;
        }
    }

    public void log(int level, String message, @Nullable Throwable t) {

        Saros saros = Saros.getDefault();

        if (saros != null) {
            IStatus logMessage = new Status(level, Saros.SAROS, IStatus.OK,
                message, t);
            saros.getLog().log(logMessage);
        }
    }

    @Override
    protected void append(LoggingEvent event) {

        if (event.getLevel().isGreaterOrEqual(Level.WARN)) {

            String message = this.layout.format(event);

            if (event.getLevel().isGreaterOrEqual(Level.ERROR)) {
                log(IStatus.ERROR, message, getThrowable(event));
            } else if (event.getLevel().equals(Level.WARN)) {
                log(IStatus.WARNING, message, getThrowable(event));
            }
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

}
