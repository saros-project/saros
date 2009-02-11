package de.fu_berlin.inf.dpp.net.business;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.picocontainer.annotations.Inject;

import de.fu_berlin.inf.dpp.net.internal.RequestPacketExtension;
import de.fu_berlin.inf.dpp.net.internal.XMPPChatReceiver;
import de.fu_berlin.inf.dpp.net.internal.extensions.PacketExtensions;
import de.fu_berlin.inf.dpp.project.CurrentProjectProxy;
import de.fu_berlin.inf.dpp.project.ISharedProject;

/**
 * @Component The single instance of this class per application is managed by
 *            PicoContainer
 */
public class JupiterReceiver {

    public static final Logger log = Logger.getLogger(JupiterReceiver.class
        .getName());

    @Inject
    CurrentProjectProxy currentProject;

    public JupiterReceiver(XMPPChatReceiver receiver) {
        receiver.addPacketListener(listener, new AndFilter(
            new MessageTypeFilter(Message.Type.chat), RequestPacketExtension
                .getFilter()));
    }

    PacketListener listener = new PacketListener() {

        public void processPacket(Packet packet) {
            Message message = (Message) packet;

            RequestPacketExtension packetExtension = PacketExtensions
                .getJupiterRequestExtension(message);

            assert packetExtension != null;

            log.debug("Received request : "
                + packetExtension.getRequest().toString());

            ISharedProject project = currentProject.getVariable();

            assert project != null;

            project.getSequencer().receiveRequest(packetExtension.getRequest());
        }
    };
}
