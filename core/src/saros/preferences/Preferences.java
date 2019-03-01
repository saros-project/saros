package de.fu_berlin.inf.dpp.preferences;

import de.fu_berlin.inf.dpp.editor.colorstorage.UserColorID;
import java.util.ArrayList;
import java.util.List;

/**
 * Preferences provide methods to get and set preferences on an IDE-specific level (i.e. they are
 * shared for all Saros instances of one IDE).
 *
 * <p>These Preferences are stored using an {@link IPreferenceStore}.
 */
public abstract class Preferences {

  protected final IPreferenceStore store;

  public Preferences(IPreferenceStore store) {
    this.store = store;
  }

  /**
   * Returns whether debug mode for SMACK is enabled or not.
   *
   * @return true if debug is enabled.
   */
  public boolean isSmackDebugModeEnabled() {
    return store.getBoolean(PreferenceConstants.SMACK_DEBUG_MODE);
  }

  /** @return Saros's XMPP server DNS address. */
  public String getSarosXMPPServer() {
    return "saros-con.imp.fu-berlin.de";
  }

  /**
   * @return the default server.<br>
   *     Is never empty or null.
   */
  public String getDefaultServer() {
    return getSarosXMPPServer();
  }

  /**
   * Returns whether auto-connect is enabled or not.
   *
   * @return true if auto-connect is enabled.
   */
  public boolean isAutoConnecting() {
    return store.getBoolean(PreferenceConstants.AUTO_CONNECT);
  }

  /**
   * Returns whether port mapping is enabled or not by evaluating the stored deviceID to be empty or
   * not.
   *
   * @return true if port mapping is enabled, false otherwise
   */
  public boolean isAutoPortmappingEnabled() {
    return !store.getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID).isEmpty();
  }

  /** @return the Socks5 candidates for the Socks5 proxy */
  public List<String> getSocks5Candidates() {
    String addresses = store.getString(PreferenceConstants.LOCAL_SOCKS5_PROXY_CANDIDATES);

    List<String> result = new ArrayList<String>();

    for (String address : addresses.split(",")) {
      address = address.trim();

      if (address.isEmpty()) continue;

      result.add(address);
    }

    return result;
  }

  /**
   * Returns whether the external address of the gateway should be used as a Socks5 candidate or
   * not.
   *
   * @return true if external address of the gateway should be used as a Socks5 candidate, false
   *     otherwise
   */
  public boolean useExternalGatewayAddress() {
    return store.getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS);
  }

  /**
   * Returns the device ID of the gateway to perform port mapping on.
   *
   * @return Device ID of the gateway or empty String if disabled.
   */
  public String getAutoPortmappingGatewayID() {
    return store.getString(PreferenceConstants.AUTO_PORTMAPPING_DEVICEID);
  }

  /** @return the last port of the auto port mapping. */
  public int getAutoPortmappingLastPort() {
    return store.getInt(PreferenceConstants.AUTO_PORTMAPPING_LASTMAPPEDPORT);
  }

  /**
   * Returns the Skype user name or an empty string if none was specified.
   *
   * @return the user name.for Skype or an empty string
   */
  public String getSkypeUserName() {
    return store.getString(PreferenceConstants.SKYPE_USERNAME);
  }

  /**
   * Returns the port for SOCKS5 file transfer. If {@link
   * PreferenceConstants#USE_NEXT_PORTS_FOR_FILE_TRANSFER} is set, a negative number is returned
   * (smacks will try next free ports above this number)
   *
   * @return port for smacks configuration (negative if to try out ports above)
   */
  public int getFileTransferPort() {
    int port = store.getInt(PreferenceConstants.FILE_TRANSFER_PORT);

    if (store.getBoolean(PreferenceConstants.USE_NEXT_PORTS_FOR_FILE_TRANSFER)) return -port;
    else return port;
  }

  /**
   * Returns whether force In-Band Bytestreams (IBB) transport is enabled or not.
   *
   * @return true if force In-Band Bytestreams (IBB) transport is enabled.
   */
  public boolean forceIBBTransport() {
    return store.getBoolean(PreferenceConstants.FORCE_IBB_CONNECTIONS);
  }

  /**
   * Returns whether concurrent undo is enabled or not.
   *
   * @return true if concurrent undo is enabled.
   */
  public boolean isConcurrentUndoActivated() {
    return store.getBoolean(PreferenceConstants.CONCURRENT_UNDO);
  }

  /**
   * Returns whether local SOCKS5 proxy is enabled or not.
   *
   * @return true if local SOCKS5 proxy is enabled.
   */
  public boolean isLocalSOCKS5ProxyEnabled() {
    return !store.getBoolean(PreferenceConstants.LOCAL_SOCKS5_PROXY_DISABLED);
  }

  /** @return Stun IP address. */
  public String getStunIP() {
    return store.getString(PreferenceConstants.STUN);
  }

  /** @return Stun Port. */
  public int getStunPort() {
    return store.getInt(PreferenceConstants.STUN_PORT);
  }

  /**
   * Returns the favorite color ID that should be used during a session.
   *
   * @return the favorite color ID or {@value UserColorID#UNKNOWN} if no favorite color ID is
   *     available
   */
  public int getFavoriteColorID() {
    return store.getInt(PreferenceConstants.FAVORITE_SESSION_COLOR_ID);
  }

  /**
   * Returns the preference for instant session start feature.
   *
   * @return true if instant session start is preferred
   */
  public boolean isInstantSessionStartPreferred() {
    return store.getBoolean(PreferenceConstants.INSTANT_SESSION_START_PREFERRED);
  }
}
