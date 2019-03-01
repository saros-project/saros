package saros.whiteboard.net;

import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import saros.whiteboard.sxe.constants.SXEMessageType;
import saros.whiteboard.sxe.net.SXEMessage;
import saros.whiteboard.sxe.net.SXESession;

/**
 * A customizable filter for SXE that allows to filter for a certain peer within a certain session
 * belonging to a selection of message types.
 *
 * @author jurke
 */
public class SXEPacketFilter implements PacketFilter {

  private static final Logger log = Logger.getLogger(SXEPacketFilter.class);

  private final List<SXEMessageType> messageTypes;
  private String peer;
  private SXESession session;

  public SXEPacketFilter(SXESession session, String peer, SXEMessageType... messageTypes) {
    this.peer = peer;
    this.messageTypes = Arrays.asList(messageTypes);
    this.session = session;
  }

  public SXEPacketFilter(SXESession session, SXEMessageType... messageTypes) {
    this.messageTypes = Arrays.asList(messageTypes);
    this.session = session;
  }

  public SXEPacketFilter(String peer, SXEMessageType... messageTypes) {
    this.peer = peer;
    this.messageTypes = Arrays.asList(messageTypes);
  }

  public SXEPacketFilter(SXEMessageType... messageTypes) {
    this.messageTypes = Arrays.asList(messageTypes);
  }

  @Override
  public boolean accept(Packet packet) {
    try {

      SXEExtension extension =
          (SXEExtension) packet.getExtension(SXEMessage.SXE_TAG, SXEMessage.SXE_XMLNS);

      if (extension != null) {

        if (peer != null)
          if (!packet.getFrom().equals(peer)) {
            log.debug(
                "Received SXE packet "
                    + extension.getMessage().getMessageType()
                    + " from wrong peer: "
                    + packet.getFrom());
            return false;
          }

        if (session != null)
          if (!extension.getMessage().getSession().equals(session)) {
            log.debug(
                "Received SXE packet "
                    + extension.getMessage().getMessageType()
                    + " from non session member: "
                    + packet.getFrom());
            return false;
          }

        if (messageTypes.isEmpty()) return true;

        return messageTypes.contains(extension.getMessage().getMessageType());
      }

    } catch (Exception e) {
      log.debug("Received malformed packet: ", e);
    }
    return false;
  }

  @Override
  public String toString() {
    return "SXEPacketFilter: filer"
        + (peer != null ? " peer=" + peer : "")
        + (session != null ? " session=" + session.getSessionId() : "")
        + (!messageTypes.isEmpty() ? " types=" + messageTypes.toString() : "");
  }
}
