package de.fu_berlin.inf.dpp.net.internal;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.project.ISharedProject;
import de.fu_berlin.inf.dpp.project.SessionManager.ConnectionSessionListener;

public class JupiterReceiver implements ConnectionSessionListener {

    public static final Logger log = Logger.getLogger(JupiterReceiver.class
            .getName());

    PacketListener listener = new PacketListener() {

        public void processPacket(Packet packet) {
            Message message = (Message) packet;

            RequestPacketExtension packetExtension = PacketExtensions
                    .getJupiterRequestExtension(message);

            assert packetExtension != null;

            ISharedProject project = Saros.getDefault().getSessionManager()
                    .getSharedProject();
            log.debug("Received request : "
                    + packetExtension.getRequest().toString());
            project.getSequencer().receiveRequest(packetExtension.getRequest());
        }
    };

    XMPPConnection connection;

    public void dispose() {
        // Nothing to dispose about :-D
    }

    public void prepare(XMPPConnection connection) {
        this.connection = connection;
    }

    public void start() {
        if (this.connection != null) {
            // TODO filter for correct session
            connection.addPacketListener(listener, new AndFilter(
                    new MessageTypeFilter(Message.Type.chat),
                    RequestPacketExtension.getFilter()));
        }
    }

    public void stop() {
        // TODO Think about queueing or what happens if we don't listen for
        // packets while stopped
        if (this.connection != null) {
            connection.removePacketListener(listener);
        }
        this.connection = null;
    }

}
