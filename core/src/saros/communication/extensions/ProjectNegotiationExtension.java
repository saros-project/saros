package saros.communication.extensions;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public abstract class ProjectNegotiationExtension extends SarosSessionPacketExtension {

  @XStreamAlias("nid")
  @XStreamAsAttribute
  protected final String negotiationID;

  protected ProjectNegotiationExtension(String sessionID, String negotiationID) {
    super(sessionID);
    this.negotiationID = negotiationID;
  }

  public String getNegotiationID() {
    return negotiationID;
  }

  public abstract static class Provider<T extends ProjectNegotiationExtension>
      extends SarosSessionPacketExtension.Provider<T> {

    public Provider(String elementName, Class<?>... classes) {
      super(elementName, classes);
    }

    /**
     * @JTourBusStop 5, Creating custom network messages, Extending the packet filter:
     *
     * <p>It might be necessary to extends the packet filter so here is the basic example how to
     * extend it properly.
     */
    public PacketFilter getPacketFilter(final String sessionID, final String negotiationID) {

      return new AndFilter(
          super.getPacketFilter(sessionID),
          new PacketFilter() {
            @Override
            public boolean accept(Packet packet) {
              ProjectNegotiationExtension extension = getPayload(packet);

              if (extension == null) return false;

              return negotiationID.equals(extension.getNegotiationID());
            }
          });
    }
  }
}
