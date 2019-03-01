package de.fu_berlin.inf.dpp.whiteboard.net;

import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEMessage;
import de.fu_berlin.inf.dpp.whiteboard.sxe.net.SXEMessageWriter;
import org.jivesoftware.smack.packet.PacketExtension;

/**
 * Very simple extension embedding an SXEMessage using the SXEMessageFactory to convert it to a
 * String.
 *
 * @author jurke
 */
public class SXEExtension implements PacketExtension {

  protected SXEMessageWriter msgFactory = new SXEMessageWriter();

  protected SXEMessage message;

  @Override
  public String getElementName() {
    return SXEMessage.SXE_TAG;
  }

  @Override
  public String getNamespace() {
    return SXEMessage.SXE_XMLNS;
  }

  @Override
  public String toXML() {
    return msgFactory.getSXEMessageAsString(message);
  }

  public SXEMessage getMessage() {
    return message;
  }

  public void setMessage(SXEMessage message) {
    this.message = message;
  }
}
