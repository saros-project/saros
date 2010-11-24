package de.fu_berlin.inf.dpp.communication.muc.session;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.ChatState;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.fu_berlin.inf.dpp.communication.muc.negotiation.MUCSessionPreferences;
import de.fu_berlin.inf.dpp.communication.muc.session.events.IMUCSessionListener;
import de.fu_berlin.inf.dpp.communication.muc.session.history.MUCSessionHistory;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryJoinElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryLeaveElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryMessageReceptionElement;
import de.fu_berlin.inf.dpp.communication.muc.session.history.elements.MUCSessionHistoryStateChangeElement;
import de.fu_berlin.inf.dpp.communication.muc.session.states.IMUCStateListener;
import de.fu_berlin.inf.dpp.communication.muc.session.states.MUCStateManager;
import de.fu_berlin.inf.dpp.net.JID;

/**
 * This class encapsulates Smacks {@link MultiUserChat} and offers
 * {@link ChatState}s that are normally only provided by Smack for {@link Chat}
 * s.
 * 
 * @author bkahlert
 */
public class MUCSession {
    private final Logger log = Logger.getLogger(MUCSession.class);

    /**
     * {@link Connection} this {@link MUCSession} uses
     */
    protected Connection connection;

    /**
     * {@link MUCSessionPreferences} this {@link MUCSession} uses
     */
    protected MUCSessionPreferences preferences;

    /**
     * Saves whether this {@link MUCSession} instance created the
     * {@link MultiUserChat}. This information is used for the decision whether
     * to destroy or simply leave the {@link MultiUserChat} on disconnection.
     */
    protected boolean createdRoom;

    /**
     * The encapsulated {@link MultiUserChat}
     */
    protected MultiUserChat muc;

    /**
     * Keeps track of all events that occur to this {@link MUCSession}
     */
    protected MUCSessionHistory history = new MUCSessionHistory();

    /**
     * {@link JID}s taking part at the running {@link MUCSession} with their
     * current {@link ChatState}
     */
    protected HashMap<JID, ChatState> participants = new HashMap<JID, ChatState>();

    /**
     * {@link IMUCSessionListener} for events on the {@link MUCSession} level
     */
    protected List<IMUCSessionListener> mucSessionListeners = new ArrayList<IMUCSessionListener>();

    /**
     * {@link MUCStateManager} used for {@link ChatState} events
     */
    protected MUCStateManager mucStateManager;

    /**
     * {@link IMUCStateListener} used for {@link ChatState} propagation
     */
    protected IMUCStateListener mucStateListener = new IMUCStateListener() {
        public void stateChanged(JID jid, ChatState state) {
            log.debug("stateChanged fired with state: " + state.toString());

            if (!participants.containsKey(jid) && state == ChatState.active) {
                /*
                 * joined notification
                 */
                participants.put(jid, state);
                history.addEntry(new MUCSessionHistoryJoinElement(jid,
                    new Date()));
                MUCSession.this.notifyJIDJoined(jid);
                return;
            } else if (participants.containsKey(jid) && state == ChatState.gone) {
                /*
                 * left notification
                 */
                participants.remove(jid);
                history.addEntry(new MUCSessionHistoryLeaveElement(jid,
                    new Date()));
                MUCSession.this.notifyJIDLeft(jid);
                return;
            } else {
                /*
                 * state changed notification
                 */
                participants.put(jid, state);
                history.addEntry(new MUCSessionHistoryStateChangeElement(jid,
                    new Date(), state));
                MUCSession.this.notifyJIDStateChanged(jid, state);
            }
        }
    };

    /**
     * {@link PacketListener} for processing incoming messages
     */
    protected PacketListener packetListener = new PacketListener() {
        public void processPacket(Packet packet) {
            log.debug("processPacket called");

            if (packet instanceof Message) {
                Message message = (Message) packet;
                if (message.getBody() == null || message.getBody().equals("")) {
                    return;
                }

                JID sender = JID
                    .createFromServicePerspective(message.getFrom());
                history.addEntry(new MUCSessionHistoryMessageReceptionElement(
                    sender, new Date(), message.getBody()));
                MUCSession.this.notifyJIDMessageReceived(sender,
                    message.getBody());
            }
        }
    };

    /**
     * Creates a new {@link MUCSession}. You need to call
     * {@link MUCSession#connect()} in order to effectively create and join the
     * {@link MUCSession}.
     * 
     * @param connection
     * @param communicationPreferences
     */
    public MUCSession(Connection connection,
        MUCSessionPreferences communicationPreferences) {
        this.connection = connection;
        this.preferences = communicationPreferences;
    }

    /**
     * Connects to a {@link MultiUserChat} on the base of the passed
     * {@link MUCSessionPreferences}.
     * 
     * @return true if the room has been created and joined; false if it only
     *         has been joined
     * @throws XMPPException
     *             TODO connect should be split into create and join; bkahlert
     *             2010/11/23
     */
    public boolean connect() throws XMPPException {
        if (preferences == null)
            throw new IllegalStateException("No comPrefs found!");
        this.createdRoom = createAndJoinMUC();
        this.mucStateManager = MUCStateManager
            .getInstance(this.connection, muc);
        this.mucStateManager.addMUCStateListener(mucStateListener);
        this.muc.addMessageListener(packetListener);
        return this.createdRoom;
    }

    /**
     * Disconnects from a {@link MultiUserChat}
     * 
     * @return TODO disconnect should be split into leave and destroy; bkahlert
     *         2010/11/23
     */
    public boolean disconnect() {
        if (this.muc == null)
            return this.createdRoom;
        this.muc.removeMessageListener(packetListener);
        this.mucStateManager.removeMUCStateListener(mucStateListener);

        /*
         * Because no ChatState changes can be received anymore we need to
         * manually propagate them locally.
         */
        MUCSession.this.notifyJIDLeft(this.getJID());
        this.setState(ChatState.gone);
        this.mucStateManager = null;
        this.history.clear();

        if (this.createdRoom) {
            try {
                muc.destroy(null, null);
            } catch (XMPPException e) {
                log.debug("Could not destroy room: " + preferences.getRoom(), e);
            }
        } else {
            muc.leave();
        }
        this.muc = null;
        return this.createdRoom;
    }

    /**
     * Creates and joins the {@link MultiUserChat} on the base of the passed
     * {@link MUCSessionPreferences}.
     * 
     * @throws XMPPException
     *             TODO connect should be split into create and join; bkahlert
     *             2010/11/23
     */
    protected boolean createAndJoinMUC() throws XMPPException {
        /*
         * Connect to a room
         */
        MultiUserChat muc = new MultiUserChat(connection, preferences.getRoom());

        /*
         * Join the room
         */
        /*
         * TODO: Notice: Some chat server implementations implicitly create a
         * room on the first join. Therefore it would be better to force the
         * user to explicitly call create
         */
        boolean joined = false;
        try {
            muc.join(connection.getUser(), preferences.getPassword());
            joined = true;
        } catch (XMPPException e) {
            log.debug(e);
        }

        /*
         * If join was not possible, try to create and then join the room TODO:
         * Check whether the case happens that the room was not joined, that is:
         * No room creation is ever necessary.
         */
        this.createdRoom = false;
        if (!joined) {
            try {
                muc.create(connection.getUser());
                this.createdRoom = true;
                muc.join(connection.getUser(), preferences.getPassword());
            } catch (XMPPException e) {
                log.debug(e);
            }
        }

        if (this.createdRoom) {
            configureRoom();
        }

        log.debug("MUC " + ((createdRoom) ? "created and " : "")
            + "joined. Server: " + preferences.getService() + " Room: "
            + preferences.getRoomName() + " Password "
            + preferences.getPassword());

        this.muc = muc;

        return createdRoom;
    }

    protected void configureRoom() {
        try {
            // Get the the room's configuration form
            Form form = muc.getConfigurationForm();

            // Create a new form to submit based on the original form
            Form submitForm = form.createAnswerForm();

            // Add default answers to the form to submit
            for (Iterator<FormField> fields = form.getFields(); fields
                .hasNext();) {
                FormField field = fields.next();
                if (!FormField.TYPE_HIDDEN.equals(field.getType())
                    && (field.getVariable() != null)) {
                    // Sets the default value as the answer
                    submitForm.setDefaultAnswer(field.getVariable());
                }
            }

            // set configuration, see XMPP Specs
            submitForm.setAnswer("muc#roomconfig_moderatedroom", false);
            submitForm.setAnswer("muc#roomconfig_publicroom", false);
            submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
            submitForm.setAnswer("muc#roomconfig_roomsecret",
                preferences.getPassword());
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            submitForm.setAnswer("muc#roomconfig_persistentroom", false);

            // Send the completed form (with default values) to the
            // server to configure the room
            muc.sendConfigurationForm(submitForm);
        } catch (XMPPException e) {
            log.debug(e);
        }
    }

    /**
     * Sends a message to all participants
     * 
     * @param message
     */
    public void sendMessage(String message) {
        if (muc == null) {
            log.error("MUC does not exist");
            return;
        }

        if (message == null)
            return;

        try {
            Message msg = muc.createMessage();
            msg.setBody(message);
            muc.sendMessage(msg);
        } catch (XMPPException e) {
            log.error("Error sending message to: " + getPreferences().getRoom());
        }
    }

    /**
     * Sets the own {@link ChatState} and notifies all participants
     * 
     * @param state
     */
    public void setState(ChatState state) {
        if (muc == null) {
            log.error("MUC does not exist");
            return;
        }

        if (state == null)
            return;

        try {
            mucStateManager.setState(state);
        } catch (XMPPException e) {
            log.error("Error sending state to: " + getPreferences().getRoom());
        }
    }

    /**
     * Returns the {@link MUCSessionPreferences} used for this
     * {@link MUCSession}
     * 
     * @return
     */
    public MUCSessionPreferences getPreferences() {
        return this.preferences;
    }

    /**
     * Returns the {@link JID} used for connection
     * 
     * @return
     */
    public JID getJID() {
        if (this.connection == null)
            return null;
        return new JID(this.connection.getUser());
    }

    /**
     * Returns true if the {@link JID} has joined the {@link MUCSession}
     * 
     * @param jid
     * @return
     */
    public boolean isJoined(JID jid) {
        return this.participants.get(jid) != null;
    }

    /**
     * Returns true if the {@link JID} used for connection has joined the
     * {@link MUCSession}
     * 
     * @return
     */
    public boolean isJoined() {
        return muc != null && muc.isJoined();
    }

    /**
     * Return the {@link ChatState} of a participant
     * 
     * @param jid
     *            the participant
     * @return null if {@link JID} is no participant of this {@link MUCSession}
     */
    public ChatState getState(JID jid) {
        return this.participants.get(jid);
    }

    /**
     * Returns the {@link ChatState} of all participants but oneself.
     * 
     * @return
     */
    public List<ChatState> getForeignStates() {
        JID self = this.getJID();
        List<ChatState> foreignStates = new ArrayList<ChatState>();
        for (JID jid : this.participants.keySet()) {
            if (jid.equals(self))
                continue;
            foreignStates.add(this.participants.get(jid));
        }
        return foreignStates;
    }

    /**
     * Returns the number of foreign {@link ChatState} that are equal to the
     * provided {@link ChatState}.
     * 
     * @param filterState
     * @return
     * 
     * @see #getForeignStates()
     */
    public int getForeignStatesCount(ChatState filterState) {
        int count = 0;
        for (ChatState chatState : getForeignStates()) {
            if (chatState == filterState) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns all {@link MUCSessionHistoryElement} that make up the
     * {@link MUCSessionHistory}
     * 
     * @return
     */
    public MUCSessionHistoryElement[] getHistory() {
        return this.history.getEntries();
    }

    /**
     * Clears the {@link MUCSessionHistory}
     */
    public void clearHistory() {
        this.history.clear();
    }

    /**
     * Adds a {@link IMUCSessionListener}
     * 
     * @param mucSessionListener
     */
    public void addMUCSessionListener(IMUCSessionListener mucSessionListener) {
        this.mucSessionListeners.add(mucSessionListener);
    }

    /**
     * Removes a {@link IMUCSessionListener}
     * 
     * @param mucSessionListener
     */
    public void removeMUCSessionListener(IMUCSessionListener mucSessionListener) {
        this.mucSessionListeners.remove(mucSessionListener);
    }

    /**
     * Notify all {@link IMUCSessionListener}s about a joined {@link JID}
     */
    public void notifyJIDJoined(JID jid) {
        for (IMUCSessionListener mucSessionListener : this.mucSessionListeners) {
            mucSessionListener.joined(jid);
        }
    }

    /**
     * Notify all {@link IMUCSessionListener}s about a left {@link JID}
     */
    public void notifyJIDLeft(JID jid) {
        for (IMUCSessionListener mucSessionListener : this.mucSessionListeners) {
            mucSessionListener.left(jid);
        }
    }

    /**
     * Notify all {@link IMUCSessionListener}s about a received message
     */
    public void notifyJIDMessageReceived(JID sender, String message) {
        for (IMUCSessionListener mucSessionListener : this.mucSessionListeners) {
            mucSessionListener.messageReceived(sender, message);
        }
    }

    /**
     * Notify all {@link IMUCSessionListener}s about a changed {@link ChatState}
     */
    public void notifyJIDStateChanged(JID jid, ChatState state) {
        for (IMUCSessionListener mucSessionListener : this.mucSessionListeners) {
            mucSessionListener.stateChanged(jid, state);
        }
    }

}