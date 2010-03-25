package de.fu_berlin.inf.dpp.util;

import java.util.Random;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.internal.XMPPReceiver;
import de.fu_berlin.inf.dpp.net.internal.XMPPTransmitter;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider;
import de.fu_berlin.inf.dpp.net.internal.XStreamExtensionProvider.XStreamIQPacket;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.observables.SharedProjectObservable;
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

    protected XMPPReceiver receiver;

    protected IPreferenceStore prefs;

    protected SessionIDObservable sessionID;

    protected CommunicationPreferences sessionPrefs;

    protected CommunicationPreferences comPrefs;

    protected SharedProjectObservable sharedProjectObservable;

    protected String randomPassword;

    XStreamExtensionProvider<CommunicationPreferences> communicationProvider = new XStreamExtensionProvider<CommunicationPreferences>(
        "sarosComPrefs", CommunicationPreferences.class);

    public CommunicationNegotiatingManager(final Saros saros,
        final XMPPReceiver receiver, XMPPTransmitter transmitter,
        SessionIDObservable sessionID,
        final SharedProjectObservable sharedProjectObservable) {

        this.saros = saros;
        this.transmitter = transmitter;
        this.receiver = receiver;
        this.sessionID = sessionID;
        this.sharedProjectObservable = sharedProjectObservable;

        randomPassword = new Random().nextInt() + "";
        log.debug("Generated Chat room password: " + randomPassword);

        receiver.addPacketListener(new PacketListener() {

            public void processPacket(Packet packet) {

                log.debug("CommunciationInfo Packet arrived");

                @SuppressWarnings("unchecked")
                XStreamIQPacket<CommunicationPreferences> iq = (XStreamIQPacket<CommunicationPreferences>) packet;

                if (iq.getType() != IQ.Type.GET)
                    return;

                log.debug("Got ComPrefs from " + iq.getFrom());
                log.debug("session host: "
                    + sharedProjectObservable.getValue().getHost().getJID()
                        .toString());
                if (sharedProjectObservable.getValue().getHost().getJID()
                    .toString().equals(iq.getFrom())) {
                    CommunicationPreferences remotePrefs = iq.getPayload();
                    setSessionPrefs(remotePrefs);
                }

            }
        }, communicationProvider.getIQFilter());
    }

    /**
     * Send Chat settings to new invited user
     * 
     * @param targetJID
     *            the JID of the new invited user
     */
    public void sendComPrefs(JID targetJID, SubMonitor monitor) {
        monitor.beginTask("Collecting and sending Chat information...", 100);
        log.debug("CommunicationInfo will be send");
        loadComPrefs();
        monitor.worked(25);
        transmitter.sendQuery(targetJID, communicationProvider, getOwnPrefs(),
            3000);
        monitor.worked(75);
        monitor.done();
        log.debug("CommunicationInfo sent");
    }

    /**
     * Set temporarily communication shared project settings
     * 
     * @param remoteComPrefs
     *            received communication settings
     */
    protected void setSessionPrefs(CommunicationPreferences remoteComPrefs) {
        log.debug("Got hosts Communication Config.");
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
            comPrefs.password = prefs.getString(PreferenceConstants.PASSWORD);
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
