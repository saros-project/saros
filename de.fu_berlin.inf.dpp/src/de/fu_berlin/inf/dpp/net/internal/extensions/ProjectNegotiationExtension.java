package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public abstract class ProjectNegotiationExtension extends
    SarosSessionPacketExtension {

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

        public PacketFilter getPacketFilter(final String sessionID,
            final String negotiationID) {

            return new AndFilter(super.getPacketFilter(sessionID),
                new PacketFilter() {
                    @Override
                    public boolean accept(Packet packet) {
                        ProjectNegotiationExtension extension = getPayload(packet);

                        if (extension == null)
                            return false;

                        return negotiationID.equals(extension
                            .getNegotiationID());
                    }
                });
        }
    }
}
