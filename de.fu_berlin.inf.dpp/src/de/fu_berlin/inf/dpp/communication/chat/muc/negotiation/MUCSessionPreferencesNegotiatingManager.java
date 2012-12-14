package de.fu_berlin.inf.dpp.communication.chat.muc.negotiation;

import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.Connection;
import org.picocontainer.annotations.Nullable;

import de.fu_berlin.inf.dpp.net.SarosNet;
import de.fu_berlin.inf.dpp.net.util.RosterUtils;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/*
 * FIXME this class seems not to transmit anything anymore. This class is only
 * queried for the current configuration. It should be renamed.
 */

/**
 * The CommunicationNegotiatingManager is responsible for transmitting the
 * Communication config of the host to all other participants of the shared
 * project during the Invitation process
 * 
 * @author ologa
 * @author bkahlert
 */
public class MUCSessionPreferencesNegotiatingManager {

    private static final Logger log = Logger
        .getLogger(MUCSessionPreferencesNegotiatingManager.class);

    protected IPreferenceStore preferences;

    protected SessionIDObservable sessionID;

    protected String password;

    protected MUCSessionPreferences sessionPreferences;

    protected SarosNet sarosNet;

    private Random random = new Random();

    public MUCSessionPreferencesNegotiatingManager(
        SessionIDObservable sessionID, @Nullable SarosNet sarosNet,
        IPreferenceStore preferences) {
        this.sessionID = sessionID;
        this.sarosNet = sarosNet;
        this.preferences = preferences;
        this.password = String.valueOf(random.nextInt());
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
