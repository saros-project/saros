package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.context.IContextKeyBindings.PlatformVersion;
import de.fu_berlin.inf.dpp.context.IContextKeyBindings.SarosVersion;
import de.fu_berlin.inf.dpp.session.ISarosSession;
import java.util.Date;

/**
 * Collects some general session data (session time, session ID, session count), platform
 * information and settings.
 *
 * @author Lisa Dohrmann
 */
@Component(module = "feedback")
public class SessionDataCollector extends AbstractStatisticCollector {

  private static final String SAROS_VERSION = "saros.version";
  private static final String JAVA_VERSION = "java.version";
  private static final String OS_NAME = "os.name";
  private static final String PLATFORM_VERSION = "platform.version";

  private static final String KEY_FEEDBACK_DISABLED = "feedback.disabled";
  private static final String KEY_FEEDBACK_INTERVAL = "feedback.survey.interval";

  private static final String KEY_USER_ID = "random.user.id";
  private static final String KEY_USER_IS_HOST = "user.is.host";

  private static final String KEY_AUTO_FOLLOW_MODE_ENABLED = "auto.followmode.enabled";

  private static final String KEY_SESSION_COUNT = "session.count";

  /** The duration of the session for the local user in minutes with two digits precision. */
  private static final String KEY_SESSION_LOCAL_DURATION = "session.time";

  /**
   * ISO DateTime (UTC) of the time the session started for the local user.
   *
   * <p>This might not be equal to the start of the whole session, because the local user might not
   * be host.
   *
   * @since 9.9.11
   */
  private static final String KEY_SESSION_LOCAL_START = "session.local.start";

  /**
   * ISO DateTime (UTC) of the time the session ended for the local user.
   *
   * <p>This might not be equal to the end of the whole session, because the local user might not be
   * host.
   *
   * @since 9.9.11
   */
  private static final String KEY_SESSION_LOCAL_END = "session.local.end";

  /**
   * A pseudonym set by the user in the preferences to identify himself. This can be used to track
   * the randomly generated {@link StatisticManager##getUserID()} to a "real" person if the user
   * chooses to do so.
   *
   * @since 9.9.11
   */
  private static final String KEY_PSEUDONYM = "user.pseudonym";

  private final String sarosVersion;
  private final String platformVersion;

  private String currentSessionID;
  private Date localSessionStart;
  private Date localSessionEnd;
  private boolean isHost;

  public SessionDataCollector(
      StatisticManager statisticManager,
      ISarosSession session,
      @SarosVersion String sarosVersion,
      @PlatformVersion String platformVersion) {
    super(statisticManager, session);
    this.sarosVersion = sarosVersion;
    this.platformVersion = platformVersion;
  }

  @Override
  protected void processGatheredData() {
    data.setSessionID(currentSessionID);
    data.put(KEY_SESSION_LOCAL_START, localSessionStart);
    data.put(KEY_SESSION_LOCAL_END, localSessionEnd);

    data.put(
        KEY_SESSION_LOCAL_DURATION,
        StatisticManager.getTimeInMinutes(
            Math.max(0, localSessionEnd.getTime() - localSessionStart.getTime())));

    data.put(KEY_SESSION_COUNT, statisticManager.getSessionCount());
    data.put(KEY_USER_IS_HOST, isHost);

    if (statisticManager.isPseudonymSubmissionAllowed()) {
      String pseudonym = statisticManager.getStatisticsPseudonymID().trim();
      if (pseudonym.length() > 0) {
        data.put(KEY_PSEUDONYM, pseudonym);
      }
    }
    storeGeneralInfos();
  }

  @Override
  protected void doOnSessionEnd(ISarosSession sarosSession) {
    localSessionEnd = new Date();
    isHost = sarosSession.getLocalUser().isHost();
  }

  @Override
  protected void doOnSessionStart(ISarosSession sarosSession) {
    currentSessionID = sarosSession.getID();
    localSessionStart = new Date();
  }

  /**
   * Stores some general platform information and settings, e.g. Saros version, Java version,
   * feedback settings, states of various settings (auto follow mode)
   */
  protected void storeGeneralInfos() {
    data.put(SAROS_VERSION, sarosVersion);
    data.put(JAVA_VERSION, System.getProperty("java.version", "Unknown Java Version"));
    data.put(OS_NAME, System.getProperty("os.name", "Unknown OS"));
    data.put(PLATFORM_VERSION, platformVersion);
    data.put(KEY_FEEDBACK_DISABLED, FeedbackManager.isFeedbackDisabled());
    data.put(KEY_FEEDBACK_INTERVAL, FeedbackManager.getSurveyInterval());
    data.put(KEY_USER_ID, statisticManager.getUserID());
    data.put(KEY_AUTO_FOLLOW_MODE_ENABLED, /* no longer used */ false);
  }
}
