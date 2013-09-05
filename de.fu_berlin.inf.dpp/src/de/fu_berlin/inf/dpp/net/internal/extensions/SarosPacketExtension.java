package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

public abstract class SarosPacketExtension {

    public static final String VERSION = "de.fu_berlin.inf.dpp/extension/V1";

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
