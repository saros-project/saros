package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;

/**
 * Abstract base class for all DefaultPacketExtension that need to include the
 * current SessionID
 */
public abstract class SessionDefaultPacketExtension extends
    SarosDefaultPacketExtension {

    public SessionDefaultPacketExtension(String element) {
        super(element);
    }

    @Override
    public DefaultPacketExtension create() {
        DefaultPacketExtension extension = super.create();

        extension.setValue(PacketExtensionUtils.SESSION_ID, PacketExtensionUtils
            .getSessionID());

        return extension;
    }
}