package de.fu_berlin.inf.dpp.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public abstract class InvitationExtension extends SarosPacketExtension {

  @XStreamAlias("nid")
  @XStreamAsAttribute
  protected final String negotiationID;

  public InvitationExtension(String negotiationID) {
    this.negotiationID = negotiationID;
  }

  public String getNegotiationID() {
    return negotiationID;
  }

  public abstract static class Provider<T extends InvitationExtension>
      extends SarosPacketExtension.Provider<T> {

    public Provider(String elementName, Class<?>... classes) {
      super(elementName, classes);
    }

    public PacketFilter getPacketFilter(final String invitationID) {

      return new AndFilter(
          super.getPacketFilter(),
          new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
              InvitationExtension extension = getPayload(packet);

              if (extension == null) return false;

              return invitationID.equals(extension.getNegotiationID());
            }
          });
    }
  }
}
