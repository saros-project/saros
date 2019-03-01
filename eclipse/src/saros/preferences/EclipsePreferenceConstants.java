package saros.preferences;

/** Constant definitions for plug-in preferences */
public class EclipsePreferenceConstants {

  private EclipsePreferenceConstants() {
    // hide constructor
  }

  public static final String ACTIVE_ACCOUNT = "active_account";

  public static final String ACCOUNT_DATA = "account_data";

  public static final String GATEWAYCHECKPERFORMED = "gatewaycheckperformed";

  public static final String ENABLE_BALLOON_NOTIFICATION = "enable_balloon_notification";

  public static final String SERVER_ACTIVATED = "server_activated";

  /*
   * Preferences of the feedback preferences page
   *
   * These preferences are kept both in the workspace scope and globally (in
   * the configuration).
   */

  /** Can be set and read by the FeedbackManager */
  public static final String FEEDBACK_SURVEY_DISABLED = "feedback.survey.disabled";

  /** Can be set and read by the FeedbackManager */
  public static final String FEEDBACK_SURVEY_INTERVAL = "feedback.survey.interval";

  /** Can be set and read by the StatisticManager */
  public static final String STATISTIC_ALLOW_SUBMISSION = "statistic.allow.submission";

  /** Can be set and read by the ErrorLogManager */
  public static final String ERROR_LOG_ALLOW_SUBMISSION = "error.log.allow.submission";

  /** Can be set and read by the ErrorLogManager */
  public static final String ERROR_LOG_ALLOW_SUBMISSION_FULL = "error.log.allow.submission.full";

  /*
   * Global preferences, not initialized i.e. no default values
   */

  /** Can be set and read by the StatisticManager */
  public static final String SESSION_COUNT = "session.count";

  /** Can be set and read by the FeedbackManager */
  public static final String SESSIONS_UNTIL_NEXT = "sessions.until.next.survey";

  /** Can be read by the StatisticManager */
  public static final String RANDOM_USER_ID = "user.id";

  /**
   * Preference used for a way to let the user identify himself.
   *
   * <p>For instance, this might get a value such as "coezbek" or "rdjemili".
   */
  public static final String STATISTICS_PSEUDONYM_ID = "STATISTICS_PSEUDONYM_ID";

  /**
   * Preference used to let the user declare whether he wants a self defined pseudonym be
   * transmitted during statistics or error log transmission
   */
  public static final String STATISTIC_ALLOW_PSEUDONYM = "STATISTIC_ALLOW_PSEUDONYM";

  /*
   * Preferences for Communication /Chat
   */

  public static final String USE_IRC_STYLE_CHAT_LAYOUT = "chat.irc.layout";

  public static final String CUSTOM_MUC_SERVICE = "custom_muc_service";

  public static final String FORCE_CUSTOM_MUC_SERVICE = "force_custom_muc_service";

  /* Sound Events */

  public static final String SOUND_ENABLED = "sound.enabled";

  public static final String SOUND_PLAY_EVENT_MESSAGE_SENT = "sound.play.event.message.sent";

  public static final String SOUND_PLAY_EVENT_MESSAGE_RECEIVED =
      "sound.play.event.message.received";

  public static final String SOUND_PLAY_EVENT_CONTACT_ONLINE = "sound.play.event.contact.online";

  public static final String SOUND_PLAY_EVENT_CONTACT_OFFLINE = "sound.play.event.contact.offline";

  /*
   * Wizard options that need to be permanently saved
   */
  public static final String PROJECTSELECTION_FILTERCLOSEDPROJECTS =
      "projectselection.filterClosedProjects";
  public static final String CONTACT_SELECTION_FILTER_NON_SAROS_CONTACTS =
      "projectselection.filterNonSarosContacts";

  /*
   * Preferences to remember the saros view settings
   */
  public static final String SAROSVIEW_SASH_WEIGHT_LEFT = "ui.sarosview.sashweight.left";
  public static final String SAROSVIEW_SASH_WEIGHT_RIGHT = "ui.sarosview.sashweight.right";

  /*
   * Stop sessions when alone as host?
   */
  public static final String AUTO_STOP_EMPTY_SESSION = "auto.stop.empty.session";

  /*
   * Annotation stuff for the editor
   */

  public static final String SHOW_CONTRIBUTION_ANNOTATIONS =
      "editor.annotation.contribution.enabled";

  public static final String SHOW_SELECTIONFILLUP_ANNOTATIONS =
      "editor.annotation.selectionfillup.enabled";
}
