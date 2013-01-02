package de.fu_berlin.inf.dpp.test.fakes.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;

import de.fu_berlin.inf.dpp.User;
import de.fu_berlin.inf.dpp.exceptions.LocalCancellationException;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.ITransmitter;
import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.net.SarosPacketCollector;
import de.fu_berlin.inf.dpp.net.internal.extensions.SarosPacketExtension;
import de.fu_berlin.inf.dpp.project.ISarosSession;

class FakePacketTransmitter implements ITransmitter {

    private Map<String, IReceiver> receivers;

    private JID localJID;

    private boolean strict;

    public FakePacketTransmitter(JID local, Map<JID, IReceiver> receivers,
        boolean strict) {
        this.localJID = local;
        this.strict = strict;

        this.receivers = new HashMap<String, IReceiver>();

        for (Map.Entry<JID, IReceiver> entry : receivers.entrySet())
            this.receivers.put(strict ? entry.getKey().getRAW() : entry
                .getKey().getBase(), entry.getValue());
    }

    @Override
    public void sendPacket(Packet packet, boolean forceSarosCompatibility)
        throws IOException {
        packet.setFrom(localJID.toString());

        if (forceSarosCompatibility)
            packet.setPacketID(SarosPacketExtension.VERSION);

        JID to = new JID(packet.getTo());

        IReceiver receiver = receivers.get(strict ? to.getRAW() : to.getBase());

        if (receiver == null)
            throw new IOException("not connected to " + to);

        receiver.processPacket(packet);
    }

    @Override
    public void sendToSessionUser(JID recipient, PacketExtension extension)
        throws IOException {
        sendMessageToUser(recipient, extension);
    }

    @Override
    public void sendMessageToUser(JID jid, PacketExtension extension) {
        Message message = new Message();
        message.addExtension(extension);
        message.setTo(jid.toString());
        try {
            sendPacket(message, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean receiveUserListConfirmation(SarosPacketCollector collector,
        List<User> fromUsers, IProgressMonitor monitor)
        throws LocalCancellationException {
        // NOP
        return false;
    }

    @Override
    public void sendLeaveMessage(ISarosSession sarosSession) {
        // NOP
    }

    @Override
    public SarosPacketCollector getUserListConfirmationCollector() {
        return null;
    }

    @Override
    public void sendUserListRequest(JID peer) {
        // NOP
    }
}
