package saros.server.net;

import saros.annotations.Component;
import saros.preferences.PreferenceConstants;
import saros.versioning.VersionManager;

/** Instructs the network layer to advertise this Saros instance as a server. */
@Component(module = "server")
public class ServerFeatureAdvertiser {

  /**
   * Initializes the ServerFeatureAdvertiser.
   *
   * @param versionManager to add info
   */
  public ServerFeatureAdvertiser(VersionManager versionManager) {
    versionManager.setLocalInfo(PreferenceConstants.SERVER_SUPPORT, Boolean.TRUE.toString());
  }
}
