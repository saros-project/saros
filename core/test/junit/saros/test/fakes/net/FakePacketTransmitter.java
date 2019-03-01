package saros.test.fakes.net;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import saros.net.IReceiver;
import saros.net.ITransmitter;
import saros.net.xmpp.JID;

class FakePacketTransmitter implements ITransmitter {

  private Map<String, IReceiver> receivers;

  private JID localJID;

  private boolean strict;

  public FakePacketTransmitter(JID local, Map<JID, IReceiver> receivers, boolean strict) {
    this.localJID = local;
    this.strict = strict;

    this.receivers = new HashMap<String, IReceiver>();

    for (Map.Entry<JID, IReceiver> entry : receivers.entrySet())
      this.receivers.put(
          strict ? entry.getKey().getRAW() : entry.getKey().getBase(), entry.getValue());
  }

  @Override
  public void sendPacket(Packet packet) throws IOException {
    packet.setFrom(localJID.toString());

    JID to = new JID(packet.getTo());

    IReceiver receiver = receivers.get(strict ? to.getRAW() : to.getBase());

    if (receiver == null) throw new IOException("not connected to " + to);

    receiver.processPacket(packet);
  }

  @Override
  public void send(String connectionID, JID recipient, PacketExtension extension)
      throws IOException {
    sendPacketExtension(recipient, extension);
  }

  @Override
  public void send(JID recipient, PacketExtension extension) throws IOException {
    sendPacketExtension(recipient, extension);
  }

  @Override
  public void sendPacketExtension(JID jid, PacketExtension extension) {
    Message message = new Message();
    message.addExtension(extension);
    message.setTo(jid.toString());
    try {
      sendPacket(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
