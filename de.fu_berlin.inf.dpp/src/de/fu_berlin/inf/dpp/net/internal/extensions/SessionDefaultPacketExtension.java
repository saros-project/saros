package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;

import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

/**
 * Abstract base class for all DefaultPacketExtension that need to include the
 * current SessionID.
 * 
 * CAUTION: This class does not return a filter that makes sure that we are in a
 * Session.
 */
public abstract class SessionDefaultPacketExtension extends
    SarosDefaultPacketExtension {

    protected SessionIDObservable sessionID;

    public SessionDefaultPacketExtension(SessionIDObservable sessionID,
        String element) {
        super(element);
        this.sessionID = sessionID;
    }

    @Override
    public DefaultPacketExtension create() {
        DefaultPacketExtension extension = super.create();

        extension.setValue(PacketExtensionUtils.SESSION_ID, sessionID
            .getValue());

        return extension;
    }
}