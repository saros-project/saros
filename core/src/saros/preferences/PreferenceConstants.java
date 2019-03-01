package saros.preferences;

/**
 * This class provide constants for the preferences. This ensure the correct usage, when they are
 * loaded and stored.
 */
public class PreferenceConstants {

  private PreferenceConstants() {
    // hide constructor
  }

  public static final String AUTO_CONNECT = "autoconnect";

  public static final String AUTO_PORTMAPPING_DEVICEID = "autoportmappingdeviceid";

  public static final String AUTO_PORTMAPPING_LASTMAPPEDPORT = "autoportmappinglastmappedport";

  public static final String SKYPE_USERNAME = "skypename";

  public static final String CONCURRENT_UNDO = "concurrent_undo";

  public static final String SMACK_DEBUG_MODE = "smack_debug_mode";

  public static final String FILE_TRANSFER_PORT = "port";

  public static final String USE_NEXT_PORTS_FOR_FILE_TRANSFER = "use_next_ports_for_file_transfer";

  public static final String FORCE_IBB_CONNECTIONS = "chatfiletransfer";

  public static final String LOCAL_SOCKS5_PROXY_DISABLED = " local_socks5_proxy_disabled";

  public static final String LOCAL_SOCKS5_PROXY_USE_UPNP_EXTERNAL_ADDRESS =
      "local_socks5_proxy_use_upnp_external_address";

  public static final String LOCAL_SOCKS5_PROXY_CANDIDATES = "local_socks5_proxy_candidates";

  public static final String STUN = "stun_server";

  public static final String STUN_PORT = "stun_server_port";

  /** color ID that should be used in a session if it is not already occupied */
  public static final String FAVORITE_SESSION_COLOR_ID = "favorite.session.color.id";

  public static final String SESSION_NICKNAME = "session.nickname";

  public static final String INSTANT_SESSION_START_PREFERRED = "instant_session_start_preferred";
}
