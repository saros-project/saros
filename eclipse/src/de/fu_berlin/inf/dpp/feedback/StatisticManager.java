package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import de.fu_berlin.inf.dpp.ui.preferencePages.FeedbackPreferencePage;
import de.fu_berlin.inf.dpp.util.StackTrace;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.picocontainer.Startable;

/**
 * The StatisticManager is supposed to gather statistic data and submit it at the end of a session
 * (if the user has opted in).
 *
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class StatisticManager extends AbstractFeedbackManager implements Startable {

  private static final Logger log = Logger.getLogger(StatisticManager.class.getName());

  private static final String STATISTIC_UPLOAD_URL =
      System.getProperty("de.fu_berlin.inf.dpp.feedback.STATISTIC_UPLOAD_URL");

  private static final Random RANDOM = new Random();

  public static final String INFO_URL = "http://www.saros-project.org/Feedback"; // $NON-NLS-1$

  public static final String STATISTIC_FILE_NAME = "session-data";
  public static final String STATISTIC_FILE_EXTENSION = ".txt";

  private SessionStatistic statistic;

  private Set<AbstractStatisticCollector> allCollectors;
  private Set<AbstractStatisticCollector> activeCollectors;

  @Override
  public void start() {
    statistic = new SessionStatistic();
    activeCollectors =
        Collections.synchronizedSet(new HashSet<AbstractStatisticCollector>(allCollectors));

    // count all started sessions
    countSessions();
  }

  @Override
  public void stop() {
    // Everything should have been submitted by now.
    assert activeCollectors.isEmpty();
  }

  public StatisticManager() {
    this.allCollectors = new HashSet<AbstractStatisticCollector>();

    logFeedbackSettings();
  }

  /**
   * Converts the given time in milliseconds to minutes. <br>
   * <code>80000 ms = 1 min 20 s = 1.33 min</code>
   *
   * @param millisecs
   * @return the milliseconds in minutes as a double rounded to two decimal places
   */
  public static double getTimeInMinutes(long millisecs) {
    return Math.round(millisecs / 600.0) / 100.0;
  }

  /**
   * Registers a collector with this StatisticManager. The manager needs this information to
   * determine when all gathered information has arrived.<br>
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
    return StatisticManagerConfiguration.isStatisticSubmissionAllowed();
  }

  public boolean isPseudonymSubmissionAllowed() {
    return StatisticManagerConfiguration.isPseudonymSubmissionAllowed();
  }

  /**
   * The Statistics Pseudonym ID is a self-selected String the user set in the preferences which
   * will be voluntarily transmitted as part of the statistics submission. Its intention is to let
   * the Saros team identify a user uniquely.
   *
   * <p>The Statistics Pseudonym ID is completely orthogonal to the {@link #getUserID()} which is a
   * randomly assigned ID and cannot not be modified by the user.
   *
   * @return "" is returned if the user has not set a UserID on the {@link FeedbackPreferencePage}
   */
  public String getStatisticsPseudonymID() {
    return StatisticManagerConfiguration.getStatisticsPseudonymID();
  }

  /**
   * Returns if the user gave an answer to the statistic submission.
   *
   * @return true if the user either allowed or forbade the submission, otherwise false
   */
  public boolean hasStatisticAgreement() {
    return StatisticManagerConfiguration.hasStatisticAgreement();
  }

  /**
   * Returns the total number of sessions the user has used Saros for.
   *
   * @return the number of session the user started or participated in (concerning all workspaces)
   */
  public long getSessionCount() {
    return FeedbackPreferences.getPreferences()
        .getLong(EclipsePreferenceConstants.SESSION_COUNT, 0);
  }

  /**
   * Saves the session count in the global preferences.
   *
   * @param count the number of sessions to save
   */
  public void putSessionCount(long count) {
    FeedbackPreferences.getPreferences().putLong(EclipsePreferenceConstants.SESSION_COUNT, count);
  }

  /** Increases the global count for started sessions. */
  protected void countSessions() {
    putSessionCount(getSessionCount() + 1);
    log.info("Session count: " + getSessionCount());
  }

  /** Logs the current feedback settings (enabled/disabled, interval, statistic submission). */
  public void logFeedbackSettings() {
    StringBuilder sb = new StringBuilder();
    sb.append("Current feedback settings:\n");
    sb.append("  Feedback disabled: " + FeedbackManager.isFeedbackDisabled() + "\n");
    sb.append("  Feedback interval: " + FeedbackManager.getSurveyInterval() + "\n");
    sb.append("  Statistic submission allowed: " + isStatisticSubmissionAllowed() + "\n");
    sb.append("  Pseudonym submission allowed: " + isPseudonymSubmissionAllowed());
    if (isPseudonymSubmissionAllowed())
      sb.append("\n  Pseudonym is: " + StatisticManagerConfiguration.getStatisticsPseudonymID());
    log.info(sb.toString());
  }

  /**
   * Returns the random user ID from the global preferences, if one was created and saved yet. If
   * none was found, a newly generated ID is stored and returned.
   *
   * @see StatisticManagerConfiguration#generateUserID()
   * @return the random user ID for this eclipse installation
   */
  public synchronized String getUserID() {
    return StatisticManagerConfiguration.getUserID();
  }

  /*------------------------------------------*
   * Methods concerning information gathering *
   *------------------------------------------*/

  /**
   * Returns the latest {@link SessionStatistic} or null if no statistic was gathered since plugin
   * start.
   *
   * @return the latest statistic
   */
  public SessionStatistic getCurrentStatistic() {
    return statistic;
  }

  /**
   * Creates a name for the statistic file, based on a static prefix, the session ID, the user ID,
   * the current time and the static file extension. <br>
   * <br>
   * NOTE: Should only be called on session end, after information gathering is complete. Otherwise
   * the session ID doesn't yet exist and thus a new random number will be created.
   *
   * @return a name for the statistic file
   */
  protected String generateFileName() {
    String sessionID = statistic.getSessionID();

    if (sessionID == null) sessionID = String.valueOf(RANDOM.nextInt(Integer.MAX_VALUE));

    return STATISTIC_FILE_NAME + "_" + sessionID + "_" + getUserID();
  }

  /**
   * Saves the statistic as a file in the plugin's state area and submits it to our server if the
   * user has permitted statistic submission.
   *
   * @nonblocking Because the upload might take some time, it is executed asynchronously in a new
   *     thread.
   */
  protected void saveAndSubmitStatistic() {

    if (!isStatisticSubmissionAllowed()) {
      log.info("User does not allow to submit session statistics");
      return;
    }

    File file = null;

    try {

      file = File.createTempFile(generateFileName(), STATISTIC_FILE_EXTENSION);

      file.deleteOnExit();
      statistic.toFile(file);

    } catch (IOException e) {
      log.error(
          "failed to save session statistic to file: "
              + (file == null ? "could not create temp file" : file.getAbsolutePath()),
          e);
      return;
    }

    submitStatisticFile(file);
  }

  /**
   * Submits the given statistic file to our Tomcat server by wrapping the submission in an
   * IRunnableWithProgress to report feedback of the execution in the active workbench windows
   * status bar.
   *
   * @see StatisticManager#uploadStatisticFile(File, IProgressMonitor)
   * @nonblocking
   * @cancelable
   */
  protected void submitStatisticFile(final File file) {

    new Job("Uploading Statistics File...") {
      @Override
      public IStatus run(IProgressMonitor monitor) {
        try {
          StatisticManager.uploadStatisticFile(file, monitor);
        } catch (IOException e) {
          String msg =
              String.format(
                  "Couldn't upload file: %s. %s",
                  e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
          log.error(msg);
          return new Status(IStatus.ERROR, Saros.PLUGIN_ID, msg, e);
        } catch (OperationCanceledException e) {
          return Status.CANCEL_STATUS;
        } finally {
          monitor.done();
        }
        return Status.OK_STATUS;
      }
    }.schedule();
  }

  /**
   * This method gets called from all collectors with their gathered data, ready for processing. The
   * given collector is afterwards removed from the list of active collectors.<br>
   * If all data has arrived i.e. the list of active collectors is empty, the SessionStatistic is
   * stored to disk.
   */
  public void addData(AbstractStatisticCollector collector, SessionStatistic data) {
    if (activeCollectors == null || activeCollectors.isEmpty()) {
      log.warn(
          "There are no active SessionCollectors left," + " but we were called from one anyhow.",
          new StackTrace());
    }

    // fetch the data from the collector and remove him from the active list
    statistic.addAll(data);
    if (activeCollectors != null) {
      if (!activeCollectors.remove(collector)) {
        log.warn(
            "We were called from a collector that wasn't in" + " the list of active collectors");
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

  /**
   * Convenience wrapper method to upload a statistic file to the server.
   *
   * @param file the file to be uploaded
   * @throws IOException is thrown if the upload failed; the exception wraps the target exception
   *     that contains the main cause for the failure
   * @blocking
   */
  private static void uploadStatisticFile(File file, IProgressMonitor monitor) throws IOException {

    if (STATISTIC_UPLOAD_URL == null) {
      log.warn("statistic upload url is not configured, cannot upload statistic file");
      return;
    }

    FileSubmitter.uploadFile(file, STATISTIC_UPLOAD_URL, monitor);
  }
}
