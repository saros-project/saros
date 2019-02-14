package de.fu_berlin.inf.dpp.server.preferences;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.preferences.IPreferenceStore;
import de.fu_berlin.inf.dpp.preferences.Preferences;

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
