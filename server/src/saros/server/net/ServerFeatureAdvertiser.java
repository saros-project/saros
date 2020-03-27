package saros.server.net;

import saros.annotations.Component;
import saros.communication.InfoManager;
import saros.preferences.PreferenceConstants;

/** Instructs the network layer to advertise this Saros instance as a server. */
@Component(module = "server")
public class ServerFeatureAdvertiser {

  /**
   * Initializes the ServerFeatureAdvertiser.
   *
   * @param infoManager info manager to add info
   */
  public ServerFeatureAdvertiser(InfoManager infoManager) {
    infoManager.setLocalInfo(PreferenceConstants.SERVER_SUPPORT, Boolean.TRUE.toString());
  }
}
