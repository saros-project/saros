package de.fu_berlin.inf.dpp.communication.muc.negotiation;

import java.util.Random;

import org.apache.log4j.Logger;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * The CommunicationNegotiatingManager is responsible to transmit the
 * Communication config of the host to all other participants of the shared
 * project during the Invitation process
 * 
 * @author ologa
 * @author bkahlert
 * 
 */
public class MUCSessionPreferencesNegotiatingManager {

    private static final Logger log = Logger
        .getLogger(MUCSessionPreferencesNegotiatingManager.class);

    @Inject
    protected Saros saros;

    protected SessionIDObservable sessionID;

    protected MUCSessionPreferences localPreferences;
    protected MUCSessionPreferences sessionPreferences;

    protected String password;

    XStreamExtensionProvider<MUCSessionPreferences> communicationProvider = new XStreamExtensionProvider<MUCSessionPreferences>(
        "sarosComPrefs", MUCSessionPreferences.class);

    public MUCSessionPreferencesNegotiatingManager(SessionIDObservable sessionID) {
        this.sessionID = sessionID;
        this.password = Integer.toString(new Random().nextInt());
    }

    /**
     * Load communication settings from PreferenceStore and generate chat room
     * and chat room password.
     */
    public MUCSessionPreferences getOwnPreferences() {
        String service = saros.getPreferenceStore().getString(
            PreferenceConstants.CHATSERVER);
        String roomName = "SAROS" + sessionID.getValue();
        return new MUCSessionPreferences(service, roomName, this.password);
    }

    /**
     * 
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

}
