package de.fu_berlin.inf.dpp.session;

import de.fu_berlin.inf.dpp.negotiation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.negotiation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import java.util.HashMap;
import java.util.Map;

/** NegotiationHook to exchange server-status information */
public abstract class ServerStatusHook implements ISessionNegotiationHook {
  private static final String HOOK_IDENTIFIER = "serverSession";

  private static final String KEY_HOST_IS_SERVER = "hostIsServer";
  private static final String KEY_CLIENT_IS_SERVER = "clientIsServer";

  public static final String KEY_IS_SERVER = "isServer";

  public ServerStatusHook(SessionNegotiationHookManager hooks) {
    hooks.addHook(this);
  }

  /**
   * Returns true if this saros instance is server-based
   *
   * @return if this saros instance is server-based
   */
  public abstract boolean isServer();

  @Override
  public void setInitialHostPreferences(IPreferenceStore hostPreferences) {
    hostPreferences.setValue(KEY_IS_SERVER, isServer());
  }

  @Override
  public Map<String, String> tellClientPreferences() {
    Map<String, String> clientPreferences = new HashMap<String, String>();
    clientPreferences.put(KEY_CLIENT_IS_SERVER, String.valueOf(isServer()));
    return clientPreferences;
  }

  @Override
  public Map<String, String> considerClientPreferences(JID client, Map<String, String> input) {

    Map<String, String> defined = new HashMap<String, String>();
    defined.put(KEY_CLIENT_IS_SERVER, input.get(KEY_CLIENT_IS_SERVER));
    defined.put(KEY_HOST_IS_SERVER, String.valueOf(isServer()));
    return defined;
  }

  @Override
  public void applyActualParameters(
      Map<String, String> input,
      IPreferenceStore hostPreferences,
      IPreferenceStore clientPreferences) {

    clientPreferences.setValue(
        KEY_IS_SERVER, Boolean.parseBoolean(input.get(KEY_CLIENT_IS_SERVER)));
    hostPreferences.setValue(KEY_IS_SERVER, input.get(KEY_HOST_IS_SERVER));
  }

  @Override
  public String getIdentifier() {
    return HOOK_IDENTIFIER;
  }
}
