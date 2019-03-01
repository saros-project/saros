package saros.ui.eventhandler;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;
import saros.communication.extensions.JoinSessionRejectedExtension;
import saros.net.IReceiver;
import saros.net.util.XMPPUtils;
import saros.net.xmpp.JID;
import saros.ui.util.DialogUtils;
import saros.ui.util.SWTUtils;

public final class JoinSessionRejectedHandler {

  private static final Logger LOG = Logger.getLogger(JoinSessionRejectedHandler.class);

  private final IReceiver receiver;

  private final PacketListener joinSessionRejectedListener =
      new PacketListener() {

        @Override
        public void processPacket(final Packet packet) {
          SWTUtils.runSafeSWTAsync(
              LOG,
              new Runnable() {

                @Override
                public void run() {
                  handleRejection(
                      new JID(packet.getFrom()),
                      JoinSessionRejectedExtension.PROVIDER.getPayload(packet));
                }
              });
        }
      };

  public JoinSessionRejectedHandler(IReceiver receiver) {
    this.receiver = receiver;
    this.receiver.addPacketListener(
        joinSessionRejectedListener, JoinSessionRejectedExtension.PROVIDER.getPacketFilter());
  }

  private void handleRejection(JID from, JoinSessionRejectedExtension extension) {

    String name = XMPPUtils.getNickname(null, from, from.getBase());

    DialogUtils.openInformationMessageDialog(
        SWTUtils.getShell(),
        "Join Session Request Rejected",
        "Your request to join the session of " + name + " was rejected.");
  }
}
