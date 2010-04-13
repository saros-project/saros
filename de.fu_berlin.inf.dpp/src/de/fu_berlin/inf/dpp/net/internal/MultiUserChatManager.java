package de.fu_berlin.inf.dpp.net.internal;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;
import de.fu_berlin.inf.dpp.project.SessionManager;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager;
import de.fu_berlin.inf.dpp.util.CommunicationNegotiatingManager.CommunicationPreferences;

@Component(module = "net")
public class MultiUserChatManager {

    private static Logger log = Logger.getLogger(MultiUserChatManager.class);

    protected CommunicationPreferences comPrefs;

    /* current muc connection. */
    protected MultiUserChat muc;

    @Inject
    protected Saros saros;

    @Inject
    protected SessionIDObservable sessionID;

    @Inject
    protected SessionManager sessionManager;

    @Inject
    protected CommunicationNegotiatingManager comNegotiatingManager;

    /**
     * Initialize the MultiUserChat
     * 
     * @param connection
     * @param user
     *            my username
     * @throws XMPPException
     */
    public void initMUC(Connection connection, String user)
        throws XMPPException {

        if (sessionManager.getSharedProject().isHost()) {
            comPrefs = comNegotiatingManager.getOwnPrefs();
        } else {
            comPrefs = comNegotiatingManager.getSessionPrefs();
        }

        /* create room domain of current connection. */
        // JID(connection.getUser()).getDomain();
        String host = this.comPrefs.chatroom + "@" + this.comPrefs.chatserver;

        // Create a MultiUserChat using an XMPPConnection for a room
        MultiUserChat muc = new MultiUserChat(connection, host);

        try {
            // Create the room
            muc.create(user);
        } catch (XMPPException e) {
            log.debug(e);
        }

        // try to join to room
        muc.join(user, this.comPrefs.password);

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
                this.comPrefs.password);
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            submitForm.setAnswer("muc#roomconfig_persistentroom", false);

            // Send the completed form (with default values) to the
            // server to configure the room
            muc.sendConfigurationForm(submitForm);
        } catch (XMPPException e) {
            log.debug(e);
        }

        log.debug("MUC joined. Server: " + this.comPrefs.chatserver + " Room: "
            + this.comPrefs.chatroom + " Password " + this.comPrefs.password);
        this.muc = muc;
    }

    /**
     * this method returns current muc or null no muc exists.
     * 
     * @return
     */
    public MultiUserChat getMUC() {
        return this.muc;
    }

    public String getRoomName() {
        return this.comPrefs.chatroom;
    }

    public String getRoomPassword() {
        return this.comPrefs.password;
    }

    public boolean isConnected() {
        if ((this.muc != null) && this.muc.isJoined()) {
            return true;
        }
        return false;
    }
}
