package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.ui.preferencePages.FeedbackPreferencePage;

/**
 * The ErrorLogManager is supposed to upload an error log file to our server at
 * the end of a session, if the user gave his permission to this. The error log
 * contains all errors and warnings per default. If the user furthermore allowed
 * the submission of the full log (i.e. with debug and info messages) via the
 * {@link FeedbackPreferencePage}, then the full log is submitted.
 * 
 * @author Lisa Dohrmann
 * 
 * @TODO provide a better way to access the preference constants. coezbek wants
 *       to use his preference framework for this
 * 
 */
@Component(module = "feedback")
public class ErrorLogManager extends AbstractFeedbackManager implements
    Startable {

    protected static final Logger log = Logger.getLogger(ErrorLogManager.class
        .getName());

    protected final String sessionID;
    private static String mostRecentSessionID;

    private static synchronized void rememberSession(String sessionId) {
        mostRecentSessionID = sessionId;
    }

    @Override
    public void start() {
        // save the id of the just started session
        rememberSession(sessionID);
    }

    @Override
    public void stop() {
        if (isErrorLogSubmissionAllowed())
            submitErrorLog(saros);
    }

    public ErrorLogManager(Saros saros, ISarosSession session) {
        super(saros);
        sessionID = session.getID();

        logErrorLogSettings();
    }

    /**
     * Returns if the submission of the error log is allowed as a boolean.
     * 
     * @return true if it is allowed
     */
    public boolean isErrorLogSubmissionAllowed() {
        return isErrorLogSubmissionAllowed(saros);
    }

    /**
     * Returns if the submission of the error log is allowed as a boolean.
     * 
     * @return true if it is allowed
     */
    public static boolean isErrorLogSubmissionAllowed(Saros saros) {
        return getErrorLogSubmissionStatus(saros) == ALLOW;
    }

    /**
     * Returns whether the submission of the error log is allowed, forbidden or
     * unknown. The global preferences have priority but if the value wasn't
     * found there the value from the PreferenceStore (with fall back to the
     * default) is used.
     * 
     * @return 0 = unknown, 1 = allowed, 2 = forbidden
     */
    public static int getErrorLogSubmissionStatus(Saros saros) {
        int status = saros.getConfigPrefs().getInt(
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION, UNDEFINED);

        if (status == UNDEFINED)
            status = saros.getPreferenceStore().getInt(
                PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION);
        return status;
    }

    /**
     * Saves in the workspace and globally if the user wants to submit the error
     * log.<br>
     * <br>
     * Note: It must be set globally first, so a PropertyChangeListener for the
     * local setting is working with latest global data.
     */
    public void setErrorLogSubmissionAllowed(boolean allowed) {
        setErrorLogSubmissionAllowed(saros, allowed);
    }

    /**
     * Saves in the workspace and globally if the user wants to submit the error
     * log.<br>
     * <br>
     * Note: It must be set globally first, so a PropertyChangeListener for the
     * local setting is working with latest global data.
     */
    public static void setErrorLogSubmissionAllowed(Saros saros, boolean allowed) {
        int submission = allowed ? ALLOW : FORBID;
        // store in configuration and preference scope
        saros.getConfigPrefs().putInt(
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION, submission);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION, submission);
    }

    /**
     * Returns if the user gave an answer to the error log submission.
     * 
     * @return true if the user either allowed or forbade the submission,
     *         otherwise false
     */
    public static boolean hasErrorLogAgreement(Saros saros) {
        return getErrorLogSubmissionStatus(saros) != UNKNOWN;
    }

    /**
     * Returns if the submission of the full error log is allowed as a boolean.
     * 
     * @return true if it is allowed
     */
    public boolean isFullErrorLogSubmissionAllowed() {
        return isFullErrorLogSubmissionAllowed(saros);
    }

    /**
     * Returns if the submission of the full error log is allowed as a boolean.
     * 
     * @return true if it is allowed
     */
    public static boolean isFullErrorLogSubmissionAllowed(Saros saros) {
        return getFullErrorLogSubmissionStatus(saros) == ALLOW;
    }

    /**
     * Returns whether the submission of the *full* error log, i.e. the log that
     * contains errors and warnings as well as debug and info messages, is
     * allowed or forbidden
     * 
     * @return ALLOW or FORBID
     */
    public static int getFullErrorLogSubmissionStatus(Saros saros) {
        int status = saros.getConfigPrefs().getInt(
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL, UNDEFINED);

        if (status == UNDEFINED)
            status = saros.getPreferenceStore().getInt(
                PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL);
        return status;
    }

    /**
     * Saves in the workspace and globally if the user wants to submit the
     * *full* error log.<br>
     * <br>
     * Note: It must be set globally first, so a PropertyChangeListener for the
     * local setting is working with latest global data.
     */
    public void setFullErrorLogSubmissionAllowed(boolean allowed) {
        setFullErrorLogSubmissionAllowed(saros, allowed);
    }

    /**
     * Saves in the workspace and globally if the user wants to submit the
     * *full* error log.<br>
     * <br>
     * Note: It must be set globally first, so a PropertyChangeListener for the
     * local setting is working with latest global data.
     */
    public static void setFullErrorLogSubmissionAllowed(Saros saros,
        boolean allowed) {
        int submission = allowed ? ALLOW : FORBID;
        // store in configuration and preference scope
        saros.getConfigPrefs().putInt(
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL, submission);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL, submission);
    }

    /**
     * Returns the location of the error log for the given appender. The
     * location is determined by iterating through all log appenders until a
     * FileAppender with the given appenderName is found. Its current value of
     * the File option is than returned as the path of the error log.
     * 
     * @return the location of the error log file or null if none could be
     *         determined
     */
    public static String getErrorLogLocation(String appenderName) {
        Enumeration<?> appenders = Logger.getRootLogger().getAllAppenders();

        while (appenders.hasMoreElements()) {
            Appender app = (Appender) appenders.nextElement();
            if (app instanceof FileAppender) {
                // find a FileAppender with the given Name
                if (app.getName().equals(appenderName))
                    return ((FileAppender) app).getFile();
            }
        }
        return null;
    }

    /**
     * Returns the error log file produced by the ErrosOnlyAppender or null, if
     * none could be found.
     * 
     * @return an existing error log file or null
     */
    public static File getErrorsOnlyLogFile() {
        String errorLogLocation = getErrorLogLocation("ErrorsOnlyAppender");
        File file = new File(errorLogLocation);

        if (!file.exists()) {
            log.debug("There was no error log at " + errorLogLocation);
            return null;
        }
        return file;
    }

    /**
     * Returns the error log file produced by the SessionLogAppender or null, if
     * none could be found.
     * 
     * @return an existing error log file or null
     */
    public static File getFullErrorLogFile() {
        String errorLogLocation = getErrorLogLocation("SessionLogAppender");
        File file = new File(errorLogLocation);

        if (!file.exists()) {
            log.debug("There was no error log at " + errorLogLocation);
            return null;
        }
        return file;
    }

    /**
     * Submits an error log (errors-only or full, depending on the user's
     * choice) to our server by wrapping the submission in an
     * IRunnableWithProgress to report feedback of the execution in the active
     * workbench windows status bar.
     * 
     * @see FileSubmitter#uploadErrorLog(String, String, File, SubMonitor)
     * 
     * @nonblocking
     * @cancelable
     */
    public static void submitErrorLog(final Saros saros) {
        // determine which log should be uploaded
        final File errorLog = isFullErrorLogSubmissionAllowed(saros) ? getFullErrorLogFile()
            : getErrorsOnlyLogFile();

        if (errorLog == null)
            return; // If there is no error-log we cannot sent one

        /*
         * cut off a possible file extension and extend the log name to make it
         * unique, e.g. with the userID and sessionID
         */
        final String logNameExtended = FilenameUtils.getBaseName(errorLog
            .getName())
            + "_"
            + StatisticManagerConfiguration.getUserID(saros)
            + "_" + mostRecentSessionID;

        new Job("Uploading Error Log...") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    FileSubmitter.uploadErrorLog(saros.getStateLocation()
                        .toOSString(), logNameExtended, errorLog, monitor);
                } catch (IOException e) {
                    String msg = String.format("Couldn't upload file: %s. %s",
                        e.getMessage(), e.getCause() != null ? e.getCause()
                            .getMessage() : "");
                    log.error(msg);
                    return new Status(IStatus.ERROR, Saros.SAROS, msg, e);
                } catch (OperationCanceledException e) {
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }

        }.schedule();
    }

    @Override
    protected void ensureConsistentPreferences() {
        makePrefConsistent(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION);
        makePrefConsistent(PreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL);
    }

    protected void logErrorLogSettings() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current error log settings:\n");
        sb.append("  Error log submission allowed: "
            + isErrorLogSubmissionAllowed() + "\n");
        sb.append("  Full Error log submission allowed: "
            + isFullErrorLogSubmissionAllowed());
        log.info(sb.toString());
    }
}
