package de.fu_berlin.inf.dpp.project;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.Saros;
import de.fu_berlin.inf.dpp.net.internal.PacketExtensions;
import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;

public class JupiterReceiver {

    public static final Logger log = Logger.getLogger(JupiterReceiver.class
            .getName());

    PacketListener listener = new PacketListener() {

        public void processPacket(Packet packet) {
            Message message = (Message) packet;

            RequestPacketExtension packetExtension = PacketExtensions
                    .getJupiterRequestExtension(message);

            assert packetExtension != null;

			// TODO This is needed by ChecksumExtension (which does not work at the moment, anyway)
            // lastReceivedActivityTime = System.currentTimeMillis();
            ISharedProject project = Saros.getDefault().getSessionManager()
                    .getSharedProject();
            log.info("Received request : "
                    + packetExtension.getRequest().toString());
            project.getSequencer().receiveRequest(packetExtension.getRequest());
        }
    };

    XMPPConnection connection;
    
    public JupiterReceiver(XMPPConnection connection) {

        this.connection = connection;
        
        connection.addPacketListener(listener, new AndFilter(
                new MessageTypeFilter(Message.Type.chat),
                new PacketExtensionFilter(RequestPacketExtension.ELEMENT,
                        PacketExtensions.NAMESPACE)));

    }

    public void stop() {
        connection.removePacketListener(listener);
    }

}
