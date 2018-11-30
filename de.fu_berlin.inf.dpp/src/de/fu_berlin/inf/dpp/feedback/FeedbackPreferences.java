package de.fu_berlin.inf.dpp.feedback;

import de.fu_berlin.inf.dpp.preferences.EclipsePreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;

public class FeedbackPreferences {

  private static Preferences preferences;

  public static synchronized void setPreferences(Preferences preferences) {
    if (preferences == null) throw new NullPointerException("preferences is null");

    FeedbackPreferences.preferences = preferences;
  }

  /**
   * Returns the {@link Preferences preferences} that are currently used by the Feedback component.
   *
   * @throws IllegalStateException if no preferences instance is available
   */
  public static synchronized Preferences getPreferences() {
    if (FeedbackPreferences.preferences == null)
      throw new IllegalStateException("preferences are not initialized");

    return FeedbackPreferences.preferences;
  }

  public static void applyDefaults(IPreferenceStore defaultPreferences) {
    if (FeedbackPreferences.preferences == null)
      throw new IllegalStateException("preferences are not initialized");

    final String[] keys = {
      EclipsePreferenceConstants.FEEDBACK_SURVEY_DISABLED,
      EclipsePreferenceConstants.FEEDBACK_SURVEY_INTERVAL,
      EclipsePreferenceConstants.STATISTIC_ALLOW_SUBMISSION,
      EclipsePreferenceConstants.STATISTIC_ALLOW_PSEUDONYM,
      EclipsePreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION,
      EclipsePreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL
    };

    for (final String key : keys)
      if (preferences.get(key, null) == null)
        preferences.put(key, defaultPreferences.getDefaultString(key));
  }
}
