package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class SarosSessionPacketExtension extends SarosPacketExtension {

    @XStreamAlias("sid")
    @XStreamAsAttribute
    protected final String sessionID;

    protected SarosSessionPacketExtension(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public abstract static class Provider<T extends SarosSessionPacketExtension>
        extends SarosPacketExtension.Provider<T> {

        public Provider(String elementName, Class<?>... classes) {
            super(elementName, classes);
        }

        public PacketFilter getPacketFilter(final String sessionID) {

            return new AndFilter(super.getPacketFilter(), new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    SarosSessionPacketExtension extension = getPayload(packet);

                    if (extension == null)
                        return false;

                    return sessionID.equals(extension.getSessionID());
                }
            });
        }
    }
}
