package de.fu_berlin.inf.dpp.communication.muc.negotiation;

import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Preferences for a {@link MultiUserChat}
 */
public class MUCSessionPreferences {

    /**
     * The room's hostname or IP at which the multi-user chat service is
     * running.
     */
    protected String service;

    /**
     * The room's name part.
     */
    protected String roomName;

    /**
     * The room's password.
     */
    protected String password;

    /**
     * the name of the room in the form "roomName@service", where "service" is
     * the hostname at which the multi-user chat service is running. Make sure
     * to provide a valid JID.
     * 
     * @param service
     * @param roomName
     * @param password
     */
    public MUCSessionPreferences(String service, String roomName, String password) {
        this.service = service;
        this.roomName = roomName;
        this.password = password;
    }

    /**
     * Returns room's hostname or IP at which the multi-user chat service is
     * running.
     * 
     * @return
     */
    public String getService() {
        return service;
    }

    /**
     * Returns room's name part.
     * 
     * @return
     */
    public String getRoomName() {
        return roomName;
    }

    /**
     * Returns the room's password.
     * 
     * @return
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the name of the room in the form "roomName@service", where
     * "service" is the hostname at which the multi-user chat service is
     * running.
     * 
     * @return
     * @see MultiUserChat#MultiUserChat(org.jivesoftware.smack.Connection,
     *      String)
     */
    public String getRoom() {
        return this.roomName + "@" + this.service;
    }

}
