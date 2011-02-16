package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.project.AbstractSarosSessionListener;
import de.fu_berlin.inf.dpp.project.ISarosSession;
import de.fu_berlin.inf.dpp.project.ISarosSessionListener;
import de.fu_berlin.inf.dpp.project.SarosSessionManager;
import de.fu_berlin.inf.dpp.ui.preferencePages.FeedbackPreferencePage;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The StatisticManager is supposed to gather statistic data and submit it at
 * the end of a session (if the user has opted in).
 * 
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class StatisticManager extends AbstractFeedbackManager {

    protected static final Logger log = Logger.getLogger(StatisticManager.class
        .getName());

    protected static final Random random = new Random();

    public static final String INFO_URL = "https://www.saros-project.org/Feedback"; //$NON-NLS-1$

    public static final String STATISTIC_FILE_NAME = "session-data";
    public static final String STATISTIC_FILE_EXTENSION = ".txt";

    protected FeedbackManager feedbackManager;
    protected SessionStatistic statistic;

    protected Set<AbstractStatisticCollector> allCollectors;
    protected Set<AbstractStatisticCollector> activeCollectors;

    protected ISarosSessionListener sessionListener = new AbstractSarosSessionListener() {

        @Override
        public void sessionStarted(ISarosSession newSarosSession) {
            statistic = new SessionStatistic();
            activeCollectors = Collections
                .synchronizedSet(new HashSet<AbstractStatisticCollector>(
                    allCollectors));

            // count all started sessions
            countSessions();
        }

    };

    public StatisticManager(Saros saros, SarosSessionManager sessionManager,
        FeedbackManager feedbackManager) {
        super(saros);
        this.feedbackManager = feedbackManager;
        this.allCollectors = new HashSet<AbstractStatisticCollector>();

        sessionManager.addSarosSessionListener(sessionListener);
        logFeedbackSettings();
    }

    /**
     * Converts the given time in milliseconds to minutes. <br>
     * <code>80000 ms = 1 min 20 s = 1.33 min</code>
     * 
     * @param millisecs
     * @return the milliseconds in minutes as a double rounded to two decimal
     *         places
     */
    public static double getTimeInMinutes(long millisecs) {
        return Math.round(millisecs / 600.0) / 100.0;
    }

    /**
     * Generates a random user ID. The ID consists of the current date and time
     * plus a random positive Integer. Thus identical IDs for different users
     * should be very unlikely.
     * 
     * @return a random user ID e.g. 2009-06-11_14-53-59_1043704453
     */
    public static String generateUserID() {
        int randInt = random.nextInt(Integer.MAX_VALUE);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd_HH-mm-ss");
        String userID = dateFormat.format(new Date()) + "_" + randInt;

        return userID;
    }

    @Override
    protected void ensureConsistentPreferences() {
        makePrefConsistent(PreferenceConstants.STATISTIC_ALLOW_SUBMISSION);
    }

    /**
     * Registers a collector with this StatisticManager. The manager needs this
     * information to determine when all gathered information has arrived.<br>
     * <br>
     * NOTE: It is allowed to register the same collector multiple times. It has
     * 
     * @param collector
     */
    public void registerCollector(AbstractStatisticCollector collector) {
        allCollectors.add(collector);
    }

    /**
     * Returns if the submission of statistic is allowed as a boolean.
     * 
     * @return true if it is allowed
     */
    public boolean isStatisticSubmissionAllowed() {
        return getStatisticSubmissionStatus() == ALLOW;
    }

    public boolean isPseudonymSubmissionAllowed() {

        String result = saros.getPreferenceStore().getString(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM);

        if (result != null && result.trim().length() > 0) {
            return saros.getPreferenceStore().getBoolean(
                PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM);
        }

        return saros.getConfigPrefs().getBoolean(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM, false);
    }

    public void setPseudonymSubmissionAllowed(boolean isPseudonymAllowed) {
        // store in configuration and preference scope
        saros.getConfigPrefs().putBoolean(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM, isPseudonymAllowed);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTIC_ALLOW_PSEUDONYM, isPseudonymAllowed);

    }

    /**
     * Returns whether the submission of statistic is allowed, forbidden or
     * unknown. The global preferences have priority but if the value wasn't
     * found there the value from the PreferenceStore (with fall back to the
     * default) is used.
     * 
     * @return 0 = unknown, 1 = allowed, 2 = forbidden
     */
    public int getStatisticSubmissionStatus() {
        int status = saros.getConfigPrefs().getInt(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION, UNDEFINED);

        if (status == UNDEFINED)
            status = saros.getPreferenceStore().getInt(
                PreferenceConstants.STATISTIC_ALLOW_SUBMISSION);
        return status;
    }

    /**
     * Saves in the workspace and globally if the user wants to submit statistic
     * data.
     */
    public void setStatisticSubmissionAllowed(boolean allow) {
        int submission = allow ? ALLOW : FORBID;
        setStatisticSubmission(submission);
    }

    /**
     * Saves in the workspace and globally if the user wants to submit statistic
     * data.<br>
     * <br>
     * Note: It must be set globally first, so the PropertyChangeListener for
     * the local setting is working with latest global data.
     * 
     * @param submission
     *            (see constants of {@link AbstractFeedbackManager})
     */
    protected void setStatisticSubmission(int submission) {
        // store in configuration and preference scope
        saros.getConfigPrefs().putInt(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION, submission);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTIC_ALLOW_SUBMISSION, submission);
    }

    public void setStatisticsPseudonymID(String userID) {
        // store in configuration and preference scope
        saros.getConfigPrefs().put(PreferenceConstants.STATISTICS_PSEUDONYM_ID,
            userID);
        saros.saveConfigPrefs();
        saros.getPreferenceStore().setValue(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID, userID);
    }

    /**
     * The Statistics Pseudonym ID is a self-selected String the user set in the
     * preferences which will be voluntarily transmitted as part of the
     * statistics submission. Its intention is to let the Saros team identify a
     * user uniquely.
     * 
     * The Statistics Pseudonym ID is completely orthogonal to the
     * {@link #getUserID()} which is a randomly assigned ID and cannot not be
     * modified by the user.
     * 
     * @return "" is returned if the user has not set a UserID on the
     *         {@link FeedbackPreferencePage}
     */
    public String getStatisticsPseudonymID() {

        // First look in the Preferences
        String userID = saros.getPreferenceStore().getString(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID);

        if (userID != null && userID.trim().length() > 0)
            return userID;

        // Fall back to the Configuration
        return saros.getConfigPrefs().get(
            PreferenceConstants.STATISTICS_PSEUDONYM_ID, "");
    }

    /**
     * Returns if the user gave an answer to the statistic submission.
     * 
     * @return true if the user either allowed or forbade the submission,
     *         otherwise false
     */
    public boolean hasStatisticAgreement() {
        return getStatisticSubmissionStatus() != UNKNOWN;
    }

    /**
     * Returns the total number of sessions the user has used Saros for.
     * 
     * @return the number of session the user started or participated in
     *         (concerning all workspaces)
     */
    public long getSessionCount() {
        return saros.getConfigPrefs().getLong(
            PreferenceConstants.SESSION_COUNT, 0);
    }

    /**
     * Saves the session count in the global preferences.
     * 
     * @param count
     *            the number of sessions to save
     */
    public void putSessionCount(long count) {
        saros.getConfigPrefs()
            .putLong(PreferenceConstants.SESSION_COUNT, count);
        saros.saveConfigPrefs();
    }

    /**
     * Increases the global count for started sessions.
     */
    protected void countSessions() {
        putSessionCount(getSessionCount() + 1);
        log.info("Session count: " + getSessionCount());
    }

    /**
     * Logs the current feedback settings (enabled/disabled, interval, statistic
     * submission).
     */
    public void logFeedbackSettings() {
        StringBuilder sb = new StringBuilder();
        sb.append("Current feedback settings:\n");
        sb.append("  Feedback disabled: "
            + feedbackManager.isFeedbackDisabled() + "\n");
        sb.append("  Feedback interval: " + feedbackManager.getSurveyInterval()
            + "\n");
        sb.append("  Statistic submission allowed: "
            + isStatisticSubmissionAllowed() + "\n");
        sb.append("  Pseudonym submission allowed: "
            + isPseudonymSubmissionAllowed());
        if (isPseudonymSubmissionAllowed())
            sb.append("\n  Pseudonym is: " + getStatisticsPseudonymID());
        log.info(sb.toString());
    }

    /**
     * Returns the random user ID from the global preferences, if one was
     * created and saved yet. If none was found, a newly generated ID is stored
     * and returned.
     * 
     * @see StatisticManager#generateUserID()
     * 
     * @return the random user ID for this eclipse installation
     */
    public synchronized String getUserID() {
        String userID = saros.getConfigPrefs().get(
            PreferenceConstants.RANDOM_USER_ID, null);
        if (userID == null) {
            userID = generateUserID();
            // save ID in the global preferences
            saros.getConfigPrefs().put(PreferenceConstants.RANDOM_USER_ID,
                userID);
            saros.saveConfigPrefs();
        }
        // HACK if we are a developer, add this info to our user ID
        if (saros.getVersion().endsWith("DEVEL"))
            userID = "sarosTeam-" + userID;
        if (isPseudonymSubmissionAllowed())
            userID += "-" + getStatisticsPseudonymID();
        return userID;
    }

    /*------------------------------------------*
     * Methods concerning information gathering *
     *------------------------------------------*/

    /**
     * Returns the latest {@link SessionStatistic} or null if no statistic was
     * gathered since plugin start.
     * 
     * @return the latest statistic
     */
    public SessionStatistic getCurrentStatistic() {
        return statistic;
    }

    /**
     * Creates a name for the statistic file, based on a static prefix, the
     * session ID, the user ID, the current time and the static file extension. <br>
     * <br>
     * NOTE: Should only be called on session end, after information gathering
     * is complete. Otherwise the session ID doesn't yet exist and thus a new
     * random number will be created.
     * 
     * @return a name for the statistic file
     */
    protected String createFileName() {
        String sessionID = statistic.getSessionID();
        // create a random number if the session ID wasn't found
        if (sessionID == null) {
            sessionID = String.valueOf(random.nextInt(Integer.MAX_VALUE));
        }
        return STATISTIC_FILE_NAME + "_" + sessionID + "_" + getUserID() + "_"
            + System.currentTimeMillis() + STATISTIC_FILE_EXTENSION;
    }

    /**
     * Saves the statistic as a file in the plugin's state area and submits it
     * to our server if the user has permitted statistic submission.
     * 
     * @nonblocking Because the upload might take some time, it is executed
     *              asynchronously in a new thread.
     */
    protected void saveAndSubmitStatistic() {
        // save to disk
        File file = statistic
            .toFile(saros.getStateLocation(), createFileName());

        // only submit, if user permitted submission
        if (isStatisticSubmissionAllowed()) {
            submitStatisticFile(file);
        } else {
            log.info(String.format("Statistic was "
                + "gathered and saved to %s, but the "
                + "submission is forbidden by the user.",
                file.getAbsolutePath()));
        }
    }

    /**
     * Submits the given statistic file to our Tomcat server by wrapping the
     * submission in an IRunnableWithProgress to report feedback of the
     * execution in the active workbench windows status bar.
     * 
     * @see FileSubmitter#uploadStatisticFile(File, SubMonitor)
     * @nonblocking
     * @cancelable
     */
    protected void submitStatisticFile(final File file) {

        new Job("Uploading Statistics File...") {
            @Override
            public IStatus run(IProgressMonitor monitor) {
                try {
                    FileSubmitter.uploadStatisticFile(file,
                        SubMonitor.convert(monitor));
                } catch (IOException e) {
                    String msg = String.format("Couldn't upload file: %s. %s",
                        e.getMessage(), e.getCause() != null ? e.getCause()
                            .getMessage() : "");
                    log.error(msg);
                    return new Status(IStatus.ERROR, Saros.SAROS, msg, e);
                } catch (CancellationException e) {
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    /**
     * This method gets called from all collectors with their gathered data,
     * ready for processing. The given collector is afterwards removed from the
     * list of active collectors.<br>
     * If all data has arrived i.e. the list of active collectors is empty, the
     * SessionStatistic is stored to disk.
     */
    public void addData(AbstractStatisticCollector collector,
        SessionStatistic data) {
        if (activeCollectors == null || activeCollectors.isEmpty()) {
            log.warn("There are no active SessionCollectors left,"
                + " but we were called from one anyhow.", new StackTrace());
        }

        // fetch the data from the collector and remove him from the active list
        statistic.addAll(data);
        if (activeCollectors != null) {
            if (!activeCollectors.remove(collector)) {
                log.warn("We were called from a collector that wasn't in"
                    + " the list of active collectors");
            }
        }

        /*
         * Write statistic to file, if all data has arrived; send it to our
         * server, if the user permitted submission. Because the upload(s) might
         * take some time, it is executed asynchronously
         */
        if (activeCollectors == null || activeCollectors.isEmpty()) {
            log.debug(statistic.toString());
            saveAndSubmitStatistic();
        }
    }
}
