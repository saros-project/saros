package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public abstract class SarosPacketExtension {

    // keep this short as it is included in every packet extension !
    public static final String VERSION = "SPXV1";

    @XStreamAlias("v")
    @XStreamAsAttribute
    private final String version = VERSION;

    public abstract static class Provider<T extends SarosPacketExtension>
        extends XStreamExtensionProvider<T> {

        public Provider(String elementName, Class<?>... classes) {
            super(elementName, classes);
        }

        @Override
        public PacketFilter getPacketFilter() {

            return new AndFilter(super.getPacketFilter(), new PacketFilter() {
                @Override
                public boolean accept(Packet packet) {
                    SarosPacketExtension extension = getPayload(packet);

                    return extension != null
                        && VERSION.equals(extension.version);
                }
            });
        }
    }
}
