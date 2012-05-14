package de.fu_berlin.inf.dpp.feedback;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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
import org.picocontainer.Startable;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;
import de.fu_berlin.inf.dpp.ui.preferencePages.FeedbackPreferencePage;
import de.fu_berlin.inf.dpp.util.StackTrace;

/**
 * The StatisticManager is supposed to gather statistic data and submit it at
 * the end of a session (if the user has opted in).
 * 
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class StatisticManager extends AbstractFeedbackManager implements
    Startable {

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

    public void start() {
        statistic = new SessionStatistic();
        activeCollectors = Collections
            .synchronizedSet(new HashSet<AbstractStatisticCollector>(
                allCollectors));

        // count all started sessions
        countSessions();
    }

    public void stop() {
        // Everything should have been submitted by now.
        assert activeCollectors.isEmpty();
    }

    public StatisticManager(Saros saros, FeedbackManager feedbackManager) {
        super(saros);
        this.feedbackManager = feedbackManager;
        this.allCollectors = new HashSet<AbstractStatisticCollector>();

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

    @Override
    protected void ensureConsistentPreferences() {
        makePrefConsistent(PreferenceConstants.STATISTIC_ALLOW_SUBMISSION);
    }

    /**
     * Registers a collector with this StatisticManager. The manager needs this
     * information to determine when all gathered information has arrived.<br>
     * <br>
     * NOTE: It is allowed to register the same collector multiple times.
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
        return StatisticManagerConfiguration
            .isStatisticSubmissionAllowed(saros);
    }

    public boolean isPseudonymSubmissionAllowed() {
        return StatisticManagerConfiguration
            .isPseudonymSubmissionAllowed(saros);
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
        return StatisticManagerConfiguration.getStatisticsPseudonymID(saros);
    }

    /**
     * Returns if the user gave an answer to the statistic submission.
     * 
     * @return true if the user either allowed or forbade the submission,
     *         otherwise false
     */
    public boolean hasStatisticAgreement() {
        return StatisticManagerConfiguration.hasStatisticAgreement(saros);
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
            sb.append("\n  Pseudonym is: "
                + StatisticManagerConfiguration.getStatisticsPseudonymID(saros));
        log.info(sb.toString());
    }

    /**
     * Returns the random user ID from the global preferences, if one was
     * created and saved yet. If none was found, a newly generated ID is stored
     * and returned.
     * 
     * @see StatisticManagerConfiguration#generateUserID()
     * 
     * @return the random user ID for this eclipse installation
     */
    public synchronized String getUserID() {
        return StatisticManagerConfiguration.getUserID(saros);
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
        File file = createStatisticFile(statistic, saros, createFileName());

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
     * Helper method to create and populate the statistic file. It is static so
     * it can be mocked for unit tests.
     * 
     * @param statistic
     * @param saros
     * @param fileName
     * @return
     */
    public static File createStatisticFile(SessionStatistic statistic,
        Saros saros, String fileName) {
        return statistic.toFile(saros.getStateLocation(), fileName);
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

    // Support for JUnit tests
    public int getAvailableCollectorCount() {
        return allCollectors.size();
    }

    public int getActiveCollectorCount() {
        return activeCollectors == null ? 0 : activeCollectors.size();
    }
}
