package saros.preferences;

import saros.editor.colorstorage.UserColorID;

/**
 * The <code>PreferenceInitializer</code> is responsible for initializing preference values needed
 * for standard operation of the DPP core.
 */
public class PreferenceInitializer {

  /**
   * Initializes the given <code>IPreferenceStore</code> with default preferences values used by the
   * DPP Core.
   *
   * @param store the store to initialize
   */
  public static void initialize(IPreferenceStore store) {

    store.setDefault(PreferenceConstants.AUTO_CONNECT, true);
    store.setDefault(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID, "");
    store.setDefault(PreferenceConstants.SKYPE_USERNAME, "");
    store.setDefault(PreferenceConstants.SMACK_DEBUG_MODE, false);
    store.setDefault(PreferenceConstants.FILE_TRANSFER_PORT, 7777);
    store.setDefault(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER, true);
    store.setDefault(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED, false);

    store.setDefault(PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS, true);

    store.setDefault(PreferenceConstants.LOCAL_SOCKS5_PROXY_CANDIDATES, "");
    store.setDefault(PreferenceConstants.FORCE_IBB_CONNECTIONS, false);

    store.setDefault(PreferenceConstants.STUN, "stunserver.org");
    store.setDefault(PreferenceConstants.STUN_PORT, 0);
    store.setDefault(PreferenceConstants.CONCURRENT_UNDO, false);

    store.setDefault(PreferenceConstants.FAVORITE_SESSION_COLOR_ID, UserColorID.UNKNOWN);

    store.setDefault(PreferenceConstants.SESSION_NICKNAME, "");
    store.setDefault(PreferenceConstants.INSTANT_SESSION_START_PREFERRED, false);
  }
}
