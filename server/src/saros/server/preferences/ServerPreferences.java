package saros.server.preferences;

import saros.annotations.Component;
import saros.preferences.IPreferenceStore;
import saros.preferences.Preferences;

/** Server implementation of the abstract {@link Preferences} class. */
@Component(module = "server")
public class ServerPreferences extends Preferences {

  /**
   * Initializes a ServerPrefrerences.
   *
   * @param store the preference store to use
   */
  public ServerPreferences(IPreferenceStore store) {
    super(store);
  }
}
