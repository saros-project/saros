package de.fu_berlin.inf.dpp.util.log;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.picocontainer.annotations.Inject;
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

    public static class LogEvent {

        public LogEvent(int level, String message, Throwable t) {
            this.level = level;
            this.message = message;
            this.t = t;
        }

        protected int level;

        protected String message;

        protected Throwable t;

        public int getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getT() {
            return t;
        }
    }

    @Inject
    protected Saros saros;

    protected List<LogEvent> queuedMessages = new LinkedList<LogEvent>();

    protected void log(int level, String message, @Nullable Throwable t) {

        if (saros == null) {
            try {
                Saros.reinject(this);
            } catch (RuntimeException e) {
                // ignore
            }
        }

        queuedMessages.add(new LogEvent(level, message, t));
        if (saros == null) {
            return;
        }

        while (queuedMessages.size() > 0) {
            LogEvent event = queuedMessages.remove(0);
            IStatus logMessage = new Status(event.getLevel(), Saros.SAROS,
                IStatus.OK, event.getMessage(), event.getT());
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
