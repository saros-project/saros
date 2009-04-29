/**
 *
 */
package de.fu_berlin.inf.dpp.net.internal.extensions;

import org.jivesoftware.smack.packet.DefaultPacketExtension;
import org.jivesoftware.smack.packet.Message;

import de.fu_berlin.inf.dpp.net.JID;
import de.fu_berlin.inf.dpp.observables.SessionIDObservable;

public class RequestForFileListExtension extends SessionDefaultPacketExtension {

    public RequestForFileListExtension(SessionIDObservable sessionIDObservable) {
        super(sessionIDObservable, "requestList");
    }

    @Override
    public DefaultPacketExtension create() {
        return super.create();
    }

    @Override
    public void processMessage(JID sender, Message message) {
        requestForFileListReceived(sender);
    }

    public void requestForFileListReceived(JID sender) {
        throw new UnsupportedOperationException(
            "This implementation should only be used to construct Extensions to be sent.");
    }

}