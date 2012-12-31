package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.annotations.Component;

@Component(module = "net")
public abstract class InvitationExtension extends SarosPacketExtension {

    final protected String invitationID;

    public InvitationExtension(String invitationID) {
        this.invitationID = invitationID;
    }

    public String getInvitationID() {
        return invitationID;
    }

    public abstract static class Provider<T extends InvitationExtension>
        extends SarosPacketExtension.Provider<T> {

        public Provider(String elementName, Class<?>... classes) {
            super(elementName, classes);
        }

        public PacketFilter getPacketFilter(final String invitationID) {

            return new AndFilter(super.getPacketFilter(), new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    InvitationExtension extension = getPayload(packet);

                    if (extension == null)
                        return false;

                    return invitationID.equals(extension.getInvitationID());
                }
            });
        }
    }
}
