package de.fu_berlin.inf.dpp.util;

import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.IPreferenceStore;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.observables.SarosSessionObservable;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.preferences.PreferenceConstants;

/**
 * The CommunicationNegotiatingManager is responsible to transmit the
 * Communication config of the host to all other participants of the shared
 * project during the Invitation process
 * 
 * @author ologa
 * 
 */
public class CommunicationNegotiatingManager {

    private static final Logger log = Logger
        .getLogger(CommunicationNegotiatingManager.class);

    public static class CommunicationPreferences {

        public String chatserver;

        public String chatroom;

        public String password;

    }

    protected Saros saros;

    protected XMPPTransmitter transmitter;

    protected IPreferenceStore prefs;

    protected SessionIDObservable sessionID;

    protected CommunicationPreferences sessionPrefs;

    protected CommunicationPreferences comPrefs;

    protected SarosSessionObservable sarosSessionObservable;

    protected String randomPassword;

    XStreamExtensionProvider<CommunicationPreferences> communicationProvider = new XStreamExtensionProvider<CommunicationPreferences>(
        "sarosComPrefs", CommunicationPreferences.class);

    public CommunicationNegotiatingManager(final Saros saros,
        XMPPTransmitter transmitter, SessionIDObservable sessionID,
        final SarosSessionObservable sarosSessionObservable) {

        this.saros = saros;
        this.transmitter = transmitter;
        this.sessionID = sessionID;
        this.sarosSessionObservable = sarosSessionObservable;

        randomPassword = new Random().nextInt() + "";
        log.debug("Generated Chat room password: " + randomPassword);
    }

    /**
     * Set temporarily communication shared project settings
     * 
     * @param remoteComPrefs
     *            received communication settings
     */
    public void setSessionPrefs(CommunicationPreferences remoteComPrefs) {
        log.debug("Got hosts Communication Config: server "
            + remoteComPrefs.chatserver + " room " + remoteComPrefs.chatroom
            + " pw " + remoteComPrefs.password);

        sessionPrefs = remoteComPrefs;
    }

    /**
     * Load communication settings from PreferenceStore or generate chatroom and
     * chatroom password and set it to the actual CommunicationPreferences
     * object.
     * 
     */
    protected void loadComPrefs() {
        prefs = saros.getPreferenceStore();
        this.comPrefs = new CommunicationPreferences();
        comPrefs.chatserver = prefs.getString(PreferenceConstants.CHATSERVER);

        if (prefs.getBoolean(PreferenceConstants.USER_DEFINED_CHATROOM) == true)
            comPrefs.chatroom = prefs.getString(PreferenceConstants.CHATROOM);
        else
            comPrefs.chatroom = "SAROS" + sessionID.getValue();

        if (prefs
            .getBoolean(PreferenceConstants.USER_DEFINED_CHATROOM_PASSWORD) == true) {
            comPrefs.password = prefs
                .getString(PreferenceConstants.CHATROOM_PASSWORD);
        } else {
            comPrefs.password = getRandomPassword();
        }

    }

    /**
     * 
     * @return Own communication preferences (from PreferenceStore or auto
     *         generated)
     */
    public CommunicationPreferences getOwnPrefs() {
        if (comPrefs == null)
            loadComPrefs();
        return comPrefs;
    }

    /**
     * 
     * @return temporarily session preferences
     */
    public CommunicationPreferences getSessionPrefs() {
        return sessionPrefs;
    }

    /**
     * 
     * @return Chatroom password
     */
    public String getRandomPassword() {
        return randomPassword;
    }

}
