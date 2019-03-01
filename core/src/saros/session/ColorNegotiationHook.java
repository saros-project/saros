package saros.session;

import java.util.HashMap;
import java.util.Map;
import saros.negotiation.hooks.ISessionNegotiationHook;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.xmpp.JID;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;

/** NegotiationHook to exchange initial and favorite color values and preferences */
public class ColorNegotiationHook implements ISessionNegotiationHook {
  private static final String HOOK_IDENTIFIER = "colorManagement";
  private static final String KEY_CLIENT_COLOR = "clientColor";
  private static final String KEY_CLIENT_FAV_COLOR = "clientFavoriteColor";
  private static final String KEY_HOST_COLOR = "hostColor";
  private static final String KEY_HOST_FAV_COLOR = "hostFavoriteColor";

  public static final String KEY_INITIAL_COLOR = "color";
  public static final String KEY_FAV_COLOR = "favoriteColor";

  private Preferences preferences;
  private ISarosSessionManager sessionManager;

  public ColorNegotiationHook(
      Preferences preferences,
      SessionNegotiationHookManager hooks,
      ISarosSessionManager sessionManager) {
    this.preferences = preferences;
    this.sessionManager = sessionManager;
    hooks.addHook(this);
  }

  @Override
  public void setInitialHostPreferences(IPreferenceStore hostPreferences) {
    hostPreferences.setValue(KEY_INITIAL_COLOR, preferences.getFavoriteColorID());
    hostPreferences.setValue(KEY_FAV_COLOR, preferences.getFavoriteColorID());
  }

  @Override
  public Map<String, String> tellClientPreferences() {
    String favoriteColor = Integer.toString(preferences.getFavoriteColorID());

    Map<String, String> colorSettings = new HashMap<String, String>();
    colorSettings.put(KEY_CLIENT_COLOR, favoriteColor);
    colorSettings.put(KEY_CLIENT_FAV_COLOR, favoriteColor);

    return colorSettings;
  }

  @Override
  public Map<String, String> considerClientPreferences(JID client, Map<String, String> input) {

    if (input == null) return null;

    String hostColor = Integer.toString(sessionManager.getSession().getLocalUser().getColorID());

    String hostFavoriteColor = Integer.toString(preferences.getFavoriteColorID());

    Map<String, String> defined = new HashMap<String, String>();
    defined.put(KEY_CLIENT_COLOR, input.get(KEY_CLIENT_COLOR));
    defined.put(KEY_CLIENT_FAV_COLOR, input.get(KEY_CLIENT_FAV_COLOR));
    defined.put(KEY_HOST_COLOR, hostColor);
    defined.put(KEY_HOST_FAV_COLOR, hostFavoriteColor);

    return defined;
  }

  @Override
  public void applyActualParameters(
      Map<String, String> input,
      IPreferenceStore hostPreferences,
      IPreferenceStore clientPreferences) {

    hostPreferences.setValue(KEY_INITIAL_COLOR, Integer.parseInt(input.get(KEY_HOST_COLOR)));
    hostPreferences.setValue(KEY_FAV_COLOR, Integer.parseInt(input.get(KEY_HOST_FAV_COLOR)));

    clientPreferences.setValue(KEY_INITIAL_COLOR, Integer.parseInt(input.get(KEY_CLIENT_COLOR)));
    clientPreferences.setValue(KEY_FAV_COLOR, Integer.parseInt(input.get(KEY_CLIENT_FAV_COLOR)));
  }

  @Override
  public String getIdentifier() {
    return HOOK_IDENTIFIER;
  }
}
