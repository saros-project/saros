package de.fu_berlin.inf.dpp.net.internal;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.TimedActivity;
import de.fu_berlin.inf.dpp.net.internal.extensions.ActivitiesPacketExtension;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.util.PacketProtokollLogger;

public class MultiUserChatManager {

    private static Logger log = Logger.getLogger(MultiUserChatManager.class
        .getName());

    // TODO Room name should be configured by settings.
    /* name of multi user chat room */
    private String room = "saros";

    /* host name of jabber-server on which the muc room is created */
    private final String server = "conference.jabber.org";

    // TODO really needed as field?
    private static String JID_PROPERTY = "jid";

    /* current muc connection. */
    private MultiUserChat muc;

    public MultiUserChatManager(String roomName) {
        this.room = roomName;
    }

    public void initMUC(XMPPConnection connection, String user, String room)
        throws XMPPException {
        this.room = room;
        initMUC(connection, user);
    }

    public void initMUC(XMPPConnection connection, String user)
        throws XMPPException {

        /* create room domain of current connection. */
        // JID(connection.getUser()).getDomain();
        String host = this.room + "@" + this.server;

        // Create a MultiUserChat using an XMPPConnection for a room
        MultiUserChat muc = new MultiUserChat(connection, host);

        // try to join to room
        try {
            muc.join(user);
        } catch (XMPPException e) {
            MultiUserChatManager.log.debug(e);
            if (e.getMessage().contains("404")) {
                // room doesn't exist

                try {

                    // Create the room
                    muc.create("testbot");

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
                    submitForm.setAnswer("muc#roomconfig_moderatedroom", true);
                    submitForm.setAnswer("muc#roomconfig_allowinvites", true);
                    submitForm
                        .setAnswer("muc#roomconfig_persistentroom", false);

                    // Send the completed form (with default values) to the
                    // server to configure the room
                    muc.sendConfigurationForm(submitForm);

                } catch (XMPPException ee) {
                    MultiUserChatManager.log.debug(e.getLocalizedMessage(), ee);
                    throw ee;
                }
            } else {
                MultiUserChatManager.log.debug(e.getLocalizedMessage(), e);
                throw e;
            }
        }
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

    public void sendActivities(ISharedProject sharedProject,
        List<TimedActivity> activities) {

        // log.info("Sent muc activities: " + activities);
        try {
            /* create new message for multi chat. */
            Message newMessage = this.muc.createMessage();
            /* add packet extension. */
            newMessage.addExtension(new ActivitiesPacketExtension(Saros
                .getDefault().getSessionManager().getSessionID(), activities));
            /* add jid property */
            newMessage.setProperty(MultiUserChatManager.JID_PROPERTY, Saros
                .getDefault().getMyJID().toString());

            // newMessage.setBody("test");
            this.muc.sendMessage(newMessage);
            PacketProtokollLogger.getInstance().sendPacket(newMessage);

        } catch (XMPPException e) {

            Saros.getDefault().getLog().log(
                new Status(IStatus.ERROR, Saros.SAROS, IStatus.ERROR,
                    "Could not send message, message queued", e));
        }

    }

    public String getRoomName() {
        return this.room;
    }

    public boolean isConnected() {
        if ((this.muc != null) && this.muc.isJoined()) {
            return true;
        }
        return false;
    }
}
