package saros.communication.chat.muc.negotiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Nullable;
import saros.communication.chat.muc.MultiUserChatPreferences;
import saros.negotiation.hooks.ISessionNegotiationHook;
import saros.negotiation.hooks.SessionNegotiationHookManager;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.net.xmpp.XMPPConnectionService;
import saros.preferences.EclipsePreferenceConstants;
import saros.session.ISarosSession;
import saros.session.ISarosSessionManager;

/**
 * The MUCNegotiationManager is responsible for transmitting the Communication config of the host to
 * all other participants of the shared project during the Invitation process
 *
 * @author ologa
 * @author bkahlert
 */
/*
 * FIXME This class used to transmit something, but that's not the case anymore.
 * In the meantime it was queried for the current configuration. Finally, it
 * provides an invitation hook. The purpose (and the name) of this class need to
 * be clarified.
 */
public class MUCNegotiationManager {

  private static final Logger LOG = Logger.getLogger(MUCNegotiationManager.class);

  private static final String NOT_IN_SESSION = "NOT_IN_SESSION";

  private final IPreferenceStore preferences;

  private final String password;

  private final XMPPConnectionService connectionService;

  private final ISarosSessionManager sessionManager;

  private final Random random = new Random();

  private MultiUserChatPreferences sessionPreferences;

  private final ISessionNegotiationHook negotiationHook =
      new ISessionNegotiationHook() {
        private static final String HOOK_IDENTIFIER = "multiUserChat";
        private static final String KEY_SERVICE = "service";
        private static final String KEY_ROOMNAME = "roomName";
        private static final String KEY_PASSWORD = "password";

        @Override
        public Map<String, String> tellClientPreferences() {
          // Nothing to do
          return null;
        }

        @Override
        public void setInitialHostPreferences(saros.preferences.IPreferenceStore hostPreferences) {
          // NOP
        }

        @Override
        public Map<String, String> considerClientPreferences(
            JID client, Map<String, String> input) {
          // We don't think about the client's preferences. We are the host,
          // so our settings are settled.
          MultiUserChatPreferences ownPreferences = getOwnPreferences();

          Map<String, String> map = new HashMap<String, String>();
          map.put(KEY_PASSWORD, ownPreferences.getPassword());
          map.put(KEY_ROOMNAME, ownPreferences.getRoomName());
          map.put(KEY_SERVICE, ownPreferences.getService());

          return map;
        }

        @Override
        public void applyActualParameters(
            Map<String, String> input,
            saros.preferences.IPreferenceStore hostPreferences,
            saros.preferences.IPreferenceStore clientPreferences) {

          if (input == null) return;

          final String service = input.get(KEY_SERVICE);
          final String roomname = input.get(KEY_ROOMNAME);
          final String actualPassword = input.get(KEY_PASSWORD);

          if (service == null || roomname == null || actualPassword == null) return;

          setSessionPreferences(new MultiUserChatPreferences(service, roomname, actualPassword));
        }

        @Override
        public String getIdentifier() {
          return HOOK_IDENTIFIER;
        }
      };

  public MUCNegotiationManager(
      ISarosSessionManager sessionManager,
      @Nullable XMPPConnectionService connectionService,
      IPreferenceStore preferences,
      SessionNegotiationHookManager hooks) {
    this.sessionManager = sessionManager;
    this.connectionService = connectionService;
    this.preferences = preferences;
    this.password = String.valueOf(random.nextInt());

    hooks.addHook(negotiationHook);
  }

  /**
   * Load communication settings from PreferenceStore and generate chat room and chat room password.
   */
  public MultiUserChatPreferences getOwnPreferences() {

    // TODO if no session is present return null
    final ISarosSession session = sessionManager.getSession();

    return new MultiUserChatPreferences(
        getMUCService(), "SAROS_" + (session != null ? session.getID() : NOT_IN_SESSION), password);
  }

  /** @return temporarily session preferences */
  public MultiUserChatPreferences getSessionPreferences() {
    return sessionPreferences;
  }

  /**
   * Set temporarily communication shared project settings
   *
   * @param remotePreferences received communication settings
   */
  public void setSessionPreferences(MultiUserChatPreferences remotePreferences) {
    LOG.debug(
        "Got hosts Communication Config: server "
            + remotePreferences.getService()
            + " room "
            + remotePreferences.getRoomName()
            + " pw "
            + remotePreferences.getPassword());

    sessionPreferences = remotePreferences;
  }

  private String getMUCService() {
    String service = null;

    boolean useCustomMUCService =
        preferences.getBoolean(EclipsePreferenceConstants.FORCE_CUSTOM_MUC_SERVICE);

    String customMUCService = preferences.getString(EclipsePreferenceConstants.CUSTOM_MUC_SERVICE);

    if (useCustomMUCService && customMUCService != null && !customMUCService.isEmpty())
      return customMUCService;

    if (connectionService != null) {
      Connection connection = connectionService.getConnection();

      if (connection != null)
        service = XMPPUtils.getMultiUserChatService(connection, connection.getServiceName());
    }

    if (service == null) service = customMUCService;

    if (service != null && service.isEmpty()) service = null;

    return service;
  }
}
