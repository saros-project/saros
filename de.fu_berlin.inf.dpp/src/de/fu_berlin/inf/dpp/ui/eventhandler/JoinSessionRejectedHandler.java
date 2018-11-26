package de.fu_berlin.inf.dpp.ui.eventhandler;

import de.fu_berlin.inf.dpp.communication.extensions.JoinSessionRejectedExtension;
import de.fu_berlin.inf.dpp.net.IReceiver;
import de.fu_berlin.inf.dpp.net.util.XMPPUtils;
import de.fu_berlin.inf.dpp.net.xmpp.JID;
import de.fu_berlin.inf.dpp.ui.util.DialogUtils;
import de.fu_berlin.inf.dpp.ui.util.SWTUtils;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Packet;

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
