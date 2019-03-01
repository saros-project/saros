package de.fu_berlin.inf.dpp.preferences;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.feedback.AbstractFeedbackManager;
import de.fu_berlin.inf.dpp.feedback.FeedbackInterval;
import de.fu_berlin.inf.dpp.feedback.FeedbackManager;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.Preferences;

/**
 * Class used to initialize default preference values.
 *
 * @author rdjemili
 */
public class EclipsePreferenceInitializer extends AbstractPreferenceInitializer {

  /*
   * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer
   */
  private static final Logger LOG = Logger.getLogger(EclipsePreferenceInitializer.class.getName());

  @Override
  public void initializeDefaultPreferences() {
    LOG.info("initializing preference default values");
    setPreferences(new DefaultScope().getNode(Saros.PLUGIN_ID));
  }

  public static void setPreferences(Preferences preferences) {
    setPreferences(new OSGiPreferencesAccessAdapter(preferences));
  }

  public static void setPreferences(IPreferenceStore preferenceStore) {
    setPreferences(new IPreferenceStoreAccessAdapter(preferenceStore));
  }

  private static void setPreferences(final PreferenceAccessAdapter adapter) {

    PreferenceInitializer.initialize(new CorePreferenceStoreAccessAdapter(adapter));

    adapter.setDefault(EclipsePreferenceConstants.GATEWAYCHECKPERFORMED, false);

    // Advanced Preferences

    adapter.setDefault(
        EclipsePreferenceConstants.AUTO_STOP_EMPTY_SESSION, MessageDialogWithToggle.PROMPT);

    adapter.setDefault(EclipsePreferenceConstants.ENABLE_BALLOON_NOTIFICATION, true);

    // TODO Dead preference?
    adapter.setDefault(
        EclipsePreferenceConstants.CONTACT_SELECTION_FILTER_NON_SAROS_CONTACTS, true);

    // Initialize Feedback Preferences
    adapter.setDefault(
        EclipsePreferenceConstants.FEEDBACK_SURVEY_DISABLED, FeedbackManager.FEEDBACK_ENABLED);
    adapter.setDefault(
        EclipsePreferenceConstants.FEEDBACK_SURVEY_INTERVAL,
        FeedbackInterval.DEFAULT.getInterval());
    adapter.setDefault(
        EclipsePreferenceConstants.STATISTIC_ALLOW_SUBMISSION, AbstractFeedbackManager.UNKNOWN);
    adapter.setDefault(
        EclipsePreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION, AbstractFeedbackManager.UNKNOWN);
    adapter.setDefault(
        EclipsePreferenceConstants.ERROR_LOG_ALLOW_SUBMISSION_FULL, AbstractFeedbackManager.FORBID);

    // Communication default settings
    adapter.setDefault(EclipsePreferenceConstants.USE_IRC_STYLE_CHAT_LAYOUT, false);
    adapter.setDefault(EclipsePreferenceConstants.CUSTOM_MUC_SERVICE, "");
    adapter.setDefault(EclipsePreferenceConstants.FORCE_CUSTOM_MUC_SERVICE, true);
    adapter.setDefault(EclipsePreferenceConstants.SOUND_ENABLED, true);

    adapter.setDefault(EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_SENT, true);
    adapter.setDefault(EclipsePreferenceConstants.SOUND_PLAY_EVENT_MESSAGE_RECEIVED, true);
    adapter.setDefault(EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_ONLINE, true);
    adapter.setDefault(EclipsePreferenceConstants.SOUND_PLAY_EVENT_CONTACT_OFFLINE, true);

    /*
     * Initially 50/50 distribution Roster/Chatpart in saros view
     */
    adapter.setDefault(EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_LEFT, 1);
    adapter.setDefault(EclipsePreferenceConstants.SAROSVIEW_SASH_WEIGHT_RIGHT, 1);

    // Hack for MARCH 2013 release

    adapter.setDefault("FAVORITE_COLOR_ID_HACK_CREATE_RANDOM_COLOR", true);

    /*
     * Editor stuff
     */

    adapter.setDefault(EclipsePreferenceConstants.SHOW_CONTRIBUTION_ANNOTATIONS, "true");
    adapter.setDefault(
        EclipsePreferenceConstants.SHOW_SELECTIONFILLUP_ANNOTATIONS,
        "false"); // set to "false" because of poor performance
  }

  private static interface PreferenceAccessAdapter {
    public void setDefault(String key, int value);

    public void setDefault(String key, long value);

    public void setDefault(String key, boolean value);

    public void setDefault(String key, String value);
  }

  private static class OSGiPreferencesAccessAdapter implements PreferenceAccessAdapter {
    private final Preferences preferences;

    private OSGiPreferencesAccessAdapter(final Preferences preferences) {
      this.preferences = preferences;
    }

    @Override
    public void setDefault(String key, int value) {
      preferences.putInt(key, value);
    }

    @Override
    public void setDefault(String key, long value) {
      preferences.putLong(key, value);
    }

    @Override
    public void setDefault(String key, boolean value) {
      preferences.putBoolean(key, value);
    }

    @Override
    public void setDefault(String key, String value) {
      preferences.put(key, value);
    }
  }

  private static class IPreferenceStoreAccessAdapter implements PreferenceAccessAdapter {
    private final IPreferenceStore preferenceStore;

    private IPreferenceStoreAccessAdapter(final IPreferenceStore preferenceStore) {
      this.preferenceStore = preferenceStore;
    }

    @Override
    public void setDefault(String key, int value) {
      preferenceStore.setValue(key, value);
    }

    @Override
    public void setDefault(String key, long value) {
      preferenceStore.setValue(key, value);
    }

    @Override
    public void setDefault(String key, boolean value) {
      preferenceStore.setValue(key, value);
    }

    @Override
    public void setDefault(String key, String value) {
      preferenceStore.setValue(key, value);
    }
  }

  private static class CorePreferenceStoreAccessAdapter
      implements de.fu_berlin.inf.dpp.preferences.IPreferenceStore {

    private final PreferenceAccessAdapter adapter;

    private CorePreferenceStoreAccessAdapter(final PreferenceAccessAdapter adapter) {
      this.adapter = adapter;
    }

    @Override
    public void addPreferenceChangeListener(IPreferenceChangeListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void removePreferenceChangeListener(IPreferenceChangeListener listener) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getBoolean(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean getDefaultBoolean(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultInt(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getDefaultLong(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getString(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getDefaultString(String name) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setValue(String name, int value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setDefault(String name, int value) {
      adapter.setDefault(name, value);
    }

    @Override
    public void setValue(String name, long value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setDefault(String name, long value) {
      adapter.setDefault(name, value);
    }

    @Override
    public void setValue(String name, String value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setDefault(String name, String value) {
      adapter.setDefault(name, value);
    }

    @Override
    public void setValue(String name, boolean value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setDefault(String name, boolean value) {
      adapter.setDefault(name, value);
    }
  }
}
