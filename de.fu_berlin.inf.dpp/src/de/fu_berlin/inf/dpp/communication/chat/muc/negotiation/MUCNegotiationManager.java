package de.fu_berlin.inf.dpp.communication.chat.muc.negotiation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.invitation.hooks.ISessionNegotiationHook;
import de.fu_berlin.inf.dpp.invitation.hooks.SessionNegotiationHookManager;
import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * The MUCNegotiationManager is responsible for transmitting the Communication
 * config of the host to all other participants of the shared project during the
 * Invitation process
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

    private static final Logger log = Logger
        .getLogger(MUCNegotiationManager.class);

    protected IPreferenceStore preferences;

    protected SessionIDObservable sessionID;

    protected String password;

    protected MUCSessionPreferences sessionPreferences;

    protected SarosNet sarosNet;

    private Random random = new Random();

    private final ISessionNegotiationHook negotiationHook = new ISessionNegotiationHook() {
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
        public Map<String, String> considerClientPreferences(
            Map<String, String> input) {
            // We don't think about the client's preferences. We are the host,
            // so our settings are settled.
            MUCSessionPreferences ownPreferences = getOwnPreferences();

            Map<String, String> map = new HashMap<String, String>();
            map.put(KEY_PASSWORD, ownPreferences.getPassword());
            map.put(KEY_ROOMNAME, ownPreferences.getRoomName());
            map.put(KEY_SERVICE, ownPreferences.getService());

            return map;
        }

        @Override
        public void applyActualParameters(Map<String, String> settings) {
            setSessionPreferences(new MUCSessionPreferences(
                settings.get(KEY_SERVICE), settings.get(KEY_ROOMNAME),
                settings.get(KEY_PASSWORD)));
        }

        @Override
        public String getIdentifier() {
            return HOOK_IDENTIFIER;
        }
    };

    public MUCNegotiationManager(SessionIDObservable sessionID,
        @Nullable SarosNet sarosNet, IPreferenceStore preferences,
        SessionNegotiationHookManager hooks) {
        this.sessionID = sessionID;
        this.sarosNet = sarosNet;
        this.preferences = preferences;
        this.password = String.valueOf(random.nextInt());

        hooks.addHook(negotiationHook);
    }

    /**
     * Load communication settings from PreferenceStore and generate chat room
     * and chat room password.
     */
    public MUCSessionPreferences getOwnPreferences() {
        return new MUCSessionPreferences(getMUCService(), "SAROS"
            + sessionID.getValue(), password);
    }

    /**
     * @return temporarily session preferences
     */
    public MUCSessionPreferences getSessionPreferences() {
        return sessionPreferences;
    }

    /**
     * Set temporarily communication shared project settings
     * 
     * @param remotePreferences
     *            received communication settings
     */
    public void setSessionPreferences(MUCSessionPreferences remotePreferences) {
        log.debug("Got hosts Communication Config: server "
            + remotePreferences.getService() + " room "
            + remotePreferences.getRoomName() + " pw "
            + remotePreferences.getPassword());

        sessionPreferences = remotePreferences;
    }

    private String getMUCService() {
        String service = null;

        boolean useCustomMUCService = preferences
            .getBoolean(PreferenceConstants.FORCE_CUSTOM_MUC_SERVICE);

        String customMUCService = preferences
            .getString(PreferenceConstants.CUSTOM_MUC_SERVICE);

        if (useCustomMUCService && customMUCService != null
            && !customMUCService.isEmpty())
            return customMUCService;

        if (sarosNet != null) {
            Connection connection = sarosNet.getConnection();

            if (connection != null)
                service = RosterUtils.getMultiUserChatService(connection,
                    connection.getServiceName());
        }

        if (service == null)
            service = customMUCService;

        if (service != null && service.isEmpty())
            service = null;

        return service;
    }

}
